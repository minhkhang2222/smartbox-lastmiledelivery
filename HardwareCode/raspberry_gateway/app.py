from __future__ import annotations

import asyncio
import logging
import time
from contextlib import asynccontextmanager
from pathlib import Path
from typing import AsyncIterator
from uuid import uuid4

import httpx
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import FileResponse, Response, StreamingResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

from .camera import CameraWorker
from .config import Settings
from .face_auth import FaceAuthenticationError, FaceAuthenticator

BASE_DIR = Path(__file__).resolve().parent
STATIC_DIR = BASE_DIR / "static"
LOGGER = logging.getLogger("uvicorn.error")


class CreateStationOrderRequest(BaseModel):
    locker_ids: list[str] = Field(alias="lockerIds", min_length=1)
    recipient_phone_number: str = Field(alias="recipientPhoneNumber", min_length=1)


class PickupOtpRequest(BaseModel):
    otp_code: str = Field(alias="otpCode", min_length=4, max_length=12)


def _response_json(response: httpx.Response, fallback: str) -> dict:
    try:
        data = response.json()
    except ValueError as exc:
        raise HTTPException(502, fallback) from exc
    if not isinstance(data, dict):
        raise HTTPException(502, fallback)
    return data


def _error_detail(data: dict, fallback: str) -> str:
    detail = data.get("detail") or data.get("message") or data.get("error")
    return detail if isinstance(detail, str) and detail.strip() else fallback


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    settings = Settings.from_env()
    LOGGER.info(
        "Gateway starting: station_id=%s device_id=%s backend_url=%s "
        "camera_backend=%s camera_index=%d camera_size=%dx%d camera_fps=%.1f "
        "face_model=%s face_model_root=%s",
        settings.station_id or "not-configured",
        settings.device_id or "not-configured",
        settings.backend_url,
        settings.camera_backend,
        settings.camera_index,
        settings.camera_width,
        settings.camera_height,
        settings.camera_fps,
        settings.face_model_name,
        settings.face_model_root,
    )
    camera = CameraWorker(settings)
    client = httpx.AsyncClient(timeout=settings.request_timeout)
    app.state.settings = settings
    app.state.camera = camera
    app.state.backend_client = client
    app.state.face_authenticator = FaceAuthenticator(
        model_root=settings.face_model_root,
        model_name=settings.face_model_name,
    )
    camera.start()
    try:
        yield
    finally:
        LOGGER.info("Gateway shutting down")
        camera.stop()
        await client.aclose()


app = FastAPI(title="Smart Locker Station Gateway", version="0.1.0", lifespan=lifespan)
app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")


@app.get("/", include_in_schema=False)
async def index() -> FileResponse:
    return FileResponse(STATIC_DIR / "index.html")


@app.get("/api/station")
async def station_info(request: Request) -> dict[str, str]:
    settings: Settings = request.app.state.settings
    return {
        "stationId": settings.station_id,
        "deviceId": settings.device_id,
    }


@app.get("/api/lockers")
async def station_lockers(request: Request) -> list[dict]:
    settings: Settings = request.app.state.settings
    if not settings.station_id:
        raise HTTPException(503, "SMARTLOCKER_STATION_ID is not configured")

    client: httpx.AsyncClient = request.app.state.backend_client
    url = f"{settings.backend_url}/api/lockers/station/{settings.station_id}"
    try:
        response = await client.get(url, headers=settings.backend_headers)
        response.raise_for_status()
        data = response.json()
    except httpx.TimeoutException as exc:
        raise HTTPException(504, "The central server timed out") from exc
    except httpx.HTTPStatusError as exc:
        raise HTTPException(
            502, f"The central server returned HTTP {exc.response.status_code}"
        ) from exc
    except (httpx.RequestError, ValueError) as exc:
        raise HTTPException(502, "Unable to read locker data from the central server") from exc

    if not isinstance(data, list):
        raise HTTPException(502, "The central server returned invalid locker data")
    return data


@app.get("/api/camera/status")
async def camera_status(request: Request) -> dict:
    status = request.app.state.camera.status()
    return {
        "available": status.available,
        "backend": status.backend,
        "message": status.message,
        "lastFrameAt": status.last_frame_at,
    }


@app.post("/api/camera/capture")
async def camera_capture(request: Request) -> Response:
    frame = request.app.state.camera.latest_jpeg()
    if frame is None:
        raise HTTPException(503, "The camera has not provided a frame yet")
    return Response(frame, media_type="image/jpeg", headers={"Cache-Control": "no-store"})


@app.post("/api/face-auth")
async def face_authenticate(request: Request) -> dict:
    trace_id = uuid4().hex[:12]
    request_started = time.perf_counter()
    settings: Settings = request.app.state.settings
    client_host = request.client.host if request.client else "unknown"
    LOGGER.info(
        "[face-auth:%s] Request received: client=%s device_id=%s",
        trace_id,
        client_host,
        settings.device_id or "not-configured",
    )
    if not settings.device_id:
        LOGGER.error(
            "[face-auth:%s] Request rejected: SMARTLOCKER_DEVICE_ID is not configured",
            trace_id,
        )
        raise HTTPException(503, "SMARTLOCKER_DEVICE_ID is not configured")

    camera: CameraWorker = request.app.state.camera
    camera_status = camera.status()
    frame = camera.latest_frame()
    if frame is None:
        LOGGER.error(
            "[face-auth:%s] No camera frame: available=%s backend=%s message=%s",
            trace_id,
            camera_status.available,
            camera_status.backend,
            camera_status.message,
        )
        raise HTTPException(503, "The camera has not provided a frame yet")
    frame_age_ms = (
        (time.time() - camera_status.last_frame_at) * 1000
        if camera_status.last_frame_at is not None
        else -1.0
    )
    LOGGER.info(
        "[face-auth:%s] Camera frame copied: available=%s backend=%s "
        "age=%.1f ms shape=%s dtype=%s",
        trace_id,
        camera_status.available,
        camera_status.backend,
        frame_age_ms,
        tuple(frame.shape),
        frame.dtype,
    )
    if not camera_status.available:
        LOGGER.warning(
            "[face-auth:%s] Processing a cached frame while camera is unavailable: %s",
            trace_id,
            camera_status.message,
        )
    elif frame_age_ms > 2000:
        LOGGER.warning(
            "[face-auth:%s] Camera frame is stale: age=%.1f ms",
            trace_id,
            frame_age_ms,
        )

    authenticator: FaceAuthenticator = request.app.state.face_authenticator
    try:
        embedding = await asyncio.to_thread(
            authenticator.extract_embedding, frame, trace_id
        )
    except FaceAuthenticationError as exc:
        LOGGER.warning(
            "[face-auth:%s] Local face processing rejected request after %.1f ms: %s",
            trace_id,
            (time.perf_counter() - request_started) * 1000,
            exc,
        )
        raise HTTPException(422, str(exc)) from exc

    client: httpx.AsyncClient = request.app.state.backend_client
    url = f"{settings.backend_url}/api/face-auth/unlock"
    backend_started = time.perf_counter()
    LOGGER.info(
        "[face-auth:%s] Sending normalized embedding to backend: url=%s "
        "dimensions=%d device_type=RASPBERRY",
        trace_id,
        url,
        len(embedding),
    )
    try:
        response = await client.post(
            url,
            json={
                "deviceId": settings.device_id,
                "deviceType": "RASPBERRY",
                "embedding": embedding,
            },
            headers=settings.backend_headers,
        )
        data = response.json()
    except httpx.TimeoutException as exc:
        LOGGER.warning(
            "[face-auth:%s] Backend timed out after %.1f ms",
            trace_id,
            (time.perf_counter() - backend_started) * 1000,
        )
        raise HTTPException(504, "The verification server timed out") from exc
    except (httpx.RequestError, ValueError) as exc:
        LOGGER.exception(
            "[face-auth:%s] Unable to read backend response after %.1f ms",
            trace_id,
            (time.perf_counter() - backend_started) * 1000,
        )
        raise HTTPException(502, "Unable to read the verification response") from exc

    backend_ms = (time.perf_counter() - backend_started) * 1000
    LOGGER.info(
        "[face-auth:%s] Backend responded: status=%d elapsed=%.1f ms "
        "content_type=%s",
        trace_id,
        response.status_code,
        backend_ms,
        response.headers.get("content-type", "unknown"),
    )
    if not response.is_success:
        detail = None
        if isinstance(data, dict):
            detail = data.get("message") or data.get("detail")
        LOGGER.warning(
            "[face-auth:%s] Backend rejected face: status=%d error=%s detail=%s "
            "total=%.1f ms",
            trace_id,
            response.status_code,
            data.get("error", "unknown") if isinstance(data, dict) else "unknown",
            detail or "Face verification failed",
            (time.perf_counter() - request_started) * 1000,
        )
        raise HTTPException(response.status_code, detail or "Face verification failed")
    if not isinstance(data, dict):
        LOGGER.error(
            "[face-auth:%s] Backend returned non-object JSON: type=%s",
            trace_id,
            type(data).__name__,
        )
        raise HTTPException(502, "The server returned invalid verification data")
    LOGGER.info(
        "[face-auth:%s] Authentication finished: success=%s unlocked_lockers=%d "
        "total=%.1f ms",
        trace_id,
        data.get("success"),
        len(data.get("unlockedLockers") or []),
        (time.perf_counter() - request_started) * 1000,
    )
    return data


@app.post("/api/orders")
async def create_station_order(payload: CreateStationOrderRequest, request: Request) -> dict:
    settings: Settings = request.app.state.settings
    if not settings.station_id:
        raise HTTPException(503, "The station is not configured")

    phone = payload.recipient_phone_number.strip()
    locker_ids = list(dict.fromkeys(payload.locker_ids))
    if not phone:
        raise HTTPException(422, "Enter the recipient phone number")
    if len(locker_ids) != len(payload.locker_ids):
        raise HTTPException(422, "The locker list contains duplicates")

    client: httpx.AsyncClient = request.app.state.backend_client
    try:
        order_response = await client.post(
            f"{settings.backend_url}/api/orders",
            json={
                "stationId": settings.station_id,
                "lockerIds": locker_ids,
                "recipientPhoneNumber": phone,
            },
            headers=settings.backend_headers,
        )
        order_data = _response_json(
            order_response, "The server returned invalid order data"
        )
        if not order_response.is_success:
            raise HTTPException(
                order_response.status_code,
                _error_detail(order_data, "Unable to create the order"),
            )
    except httpx.TimeoutException as exc:
        raise HTTPException(504, "The central server timed out") from exc
    except httpx.RequestError as exc:
        raise HTTPException(502, "Unable to connect to the central server") from exc

    return {
        "success": True,
        "message": "Order created successfully.",
        "orderId": order_data.get("orderId"),
        "lockerIds": locker_ids,
    }


@app.post("/api/pickup/otp")
async def pickup_with_otp(payload: PickupOtpRequest, request: Request) -> dict:
    settings: Settings = request.app.state.settings
    if not settings.device_id:
        raise HTTPException(503, "SMARTLOCKER_DEVICE_ID is not configured")

    client: httpx.AsyncClient = request.app.state.backend_client
    try:
        response = await client.post(
            f"{settings.backend_url}/api/pickup/otp",
            json={
                "deviceId": settings.device_id,
                "deviceType": "RASPBERRY",
                "otpCode": payload.otp_code.strip(),
            },
            headers=settings.backend_headers,
        )
        data = _response_json(response, "The server returned invalid pickup data")
    except httpx.TimeoutException as exc:
        raise HTTPException(504, "The pickup server timed out") from exc
    except httpx.RequestError as exc:
        raise HTTPException(502, "Unable to connect to the pickup server") from exc

    if not response.is_success:
        raise HTTPException(response.status_code, data.get("message") or "OTP pickup failed")
    return data


@app.get("/api/camera/stream")
async def camera_stream(request: Request) -> StreamingResponse:
    camera: CameraWorker = request.app.state.camera

    async def frames() -> AsyncIterator[bytes]:
        previous: bytes | None = None
        while True:
            if await request.is_disconnected():
                break
            frame = await asyncio.to_thread(camera.wait_for_jpeg, previous, 2.0)
            if frame is None or frame is previous:
                continue
            previous = frame
            yield (
                b"--frame\r\n"
                b"Content-Type: image/jpeg\r\n"
                b"Cache-Control: no-cache\r\n\r\n" + frame + b"\r\n"
            )

    return StreamingResponse(
        frames(),
        media_type="multipart/x-mixed-replace; boundary=frame",
        headers={"Cache-Control": "no-store", "X-Accel-Buffering": "no"},
    )
