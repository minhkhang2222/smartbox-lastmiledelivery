from __future__ import annotations

import os
from dataclasses import dataclass


def _float_env(name: str, default: float) -> float:
    try:
        return float(os.getenv(name, str(default)))
    except ValueError as exc:
        raise RuntimeError(f"{name} must be a number") from exc


@dataclass(frozen=True, slots=True)
class Settings:
    backend_url: str
    station_id: str
    device_id: str
    device_token: str
    camera_backend: str
    camera_index: int
    camera_width: int
    camera_height: int
    camera_fps: float
    request_timeout: float
    face_model_root: str
    face_model_name: str

    @classmethod
    def from_env(cls) -> "Settings":
        camera_backend = os.getenv("SMARTLOCKER_CAMERA_BACKEND", "auto").lower()
        if camera_backend not in {"auto", "picamera2", "opencv", "disabled"}:
            raise RuntimeError(
                "SMARTLOCKER_CAMERA_BACKEND must be auto, picamera2, opencv, or disabled"
            )

        return cls(
            backend_url=os.getenv("SMARTLOCKER_BACKEND_URL", "http://localhost:8080").rstrip("/"),
            station_id=os.getenv("SMARTLOCKER_STATION_ID", "").strip(),
            device_id=os.getenv("SMARTLOCKER_DEVICE_ID", "").strip(),
            device_token=os.getenv("SMARTLOCKER_DEVICE_TOKEN", "").strip(),
            camera_backend=camera_backend,
            camera_index=int(os.getenv("SMARTLOCKER_CAMERA_INDEX", "0")),
            camera_width=int(os.getenv("SMARTLOCKER_CAMERA_WIDTH", "1280")),
            camera_height=int(os.getenv("SMARTLOCKER_CAMERA_HEIGHT", "720")),
            camera_fps=_float_env("SMARTLOCKER_CAMERA_FPS", 15.0),
            request_timeout=_float_env("SMARTLOCKER_REQUEST_TIMEOUT", 10.0),
            face_model_root=os.getenv(
                "SMARTLOCKER_FACE_MODEL_ROOT", "/home/smartlocker/MODEL3/"
            ).strip(),
            face_model_name=os.getenv("SMARTLOCKER_FACE_MODEL_NAME", "buffalo_l").strip(),
        )

    @property
    def backend_headers(self) -> dict[str, str]:
        headers = {"Accept": "application/json"}
        if self.device_id:
            headers["X-Device-Id"] = self.device_id
        if self.device_token:
            headers["Authorization"] = f"Bearer {self.device_token}"
        return headers
