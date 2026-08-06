"""Capture a face on Raspberry Pi and request SmartBox face authentication."""

import os
import sys
import time
from pathlib import Path

import cv2
import insightface
import numpy as np
import requests

API_URL = os.getenv(
    "SMARTBOX_FACE_AUTH_URL",
    "https://api.smartboxeiu.site/api/face-auth/unlock",
)
DEVICE_ID = "b3e80147-a2f4-4d77-8b90-18e6d522d202"
CAMERA_INDEX = int(os.getenv("SMARTBOX_CAMERA_INDEX", "0"))
REQUEST_TIMEOUT_SECONDS = float(os.getenv("SMARTBOX_REQUEST_TIMEOUT", "15"))
DEVICE_TYPE = "RASPBERRY"
FACE_MODEL_ROOT = Path(
    os.getenv("SMARTLOCKER_FACE_MODEL_ROOT", "/opt/smartlocker/insightface")
).expanduser().resolve()
FACE_MODEL_NAME = os.getenv("SMARTLOCKER_FACE_MODEL_NAME", "buffalo_l").strip()


def normalize(vector: np.ndarray) -> np.ndarray:
    norm = np.linalg.norm(vector)
    if norm < 1e-10:
        raise ValueError("Embedding có norm bằng 0")
    return vector / norm


def capture_frame() -> np.ndarray:
    camera = cv2.VideoCapture(CAMERA_INDEX)
    try:
        if not camera.isOpened():
            raise RuntimeError(f"Không mở được camera index {CAMERA_INDEX}")

        # Bỏ qua vài frame đầu để camera tự cân bằng sáng.
        frame = None
        for _ in range(10):
            ok, frame = camera.read()
            if not ok:
                raise RuntimeError("Không đọc được hình ảnh từ camera")
            time.sleep(0.05)
        return frame
    finally:
        camera.release()


def extract_embedding(analyzer: insightface.app.FaceAnalysis, frame: np.ndarray) -> list[float]:
    faces = analyzer.get(frame)
    if not faces:
        raise RuntimeError("Không phát hiện khuôn mặt")

    face = max(faces, key=lambda item: (item.bbox[2] - item.bbox[0]) * (item.bbox[3] - item.bbox[1]))
    embedding = normalize(face.embedding.astype(np.float32))
    if embedding.size != 512:
        raise RuntimeError(f"Model trả về vector {embedding.size} chiều, API yêu cầu 512 chiều")
    return embedding.tolist()


def authenticate(embedding: list[float]) -> dict:
    response = requests.post(
        API_URL,
        json={
            "deviceId": DEVICE_ID,
            "deviceType": DEVICE_TYPE,
            "embedding": embedding,
        },
        timeout=REQUEST_TIMEOUT_SECONDS,
    )
    try:
        body = response.json()
    except ValueError:
        body = {"message": response.text}

    if not response.ok:
        raise RuntimeError(f"API trả HTTP {response.status_code}: {body}")
    return body


def main() -> int:
    try:
        model_dir = FACE_MODEL_ROOT / "models" / FACE_MODEL_NAME
        if not model_dir.is_dir() or not any(model_dir.glob("*.onnx")):
            raise RuntimeError(
                "Khong tim thay model khuon mat cuc bo. "
                f"Hay chep cac file .onnx vao {model_dir}"
            )

        analyzer = insightface.app.FaceAnalysis(
            name=FACE_MODEL_NAME,
            root=str(FACE_MODEL_ROOT),
            providers=["CPUExecutionProvider"],
        )
        analyzer.prepare(ctx_id=-1, det_size=(640, 640))
        result = authenticate(extract_embedding(analyzer, capture_frame()))
        print(result)
        return 0 if result.get("success") else 2
    except (RuntimeError, ValueError, requests.RequestException) as error:
        print(f"Face authentication failed: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
