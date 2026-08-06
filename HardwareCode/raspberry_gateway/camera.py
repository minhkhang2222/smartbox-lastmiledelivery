from __future__ import annotations

import logging
import threading
import time
from dataclasses import dataclass
from typing import Any

import cv2
import numpy as np

from .config import Settings

LOGGER = logging.getLogger("uvicorn.error")


@dataclass(frozen=True, slots=True)
class CameraStatus:
    available: bool
    backend: str
    message: str
    last_frame_at: float | None


class CameraWorker:
    """The only component allowed to open and read the station camera."""

    def __init__(self, settings: Settings) -> None:
        self.settings = settings
        self._condition = threading.Condition()
        self._latest_jpeg: bytes | None = None
        self._latest_frame: np.ndarray | None = None
        self._last_frame_at: float | None = None
        self._backend_name = "none"
        self._message = "Camera is starting"
        self._available = False
        self._frame_count = 0
        self._last_health_log_at = 0.0
        self._stopping = threading.Event()
        self._thread: threading.Thread | None = None

    def start(self) -> None:
        if self._thread and self._thread.is_alive():
            LOGGER.info("Camera worker start ignored because it is already running")
            return
        LOGGER.info(
            "Starting camera worker: requested_backend=%s index=%d size=%dx%d fps=%.1f",
            self.settings.camera_backend,
            self.settings.camera_index,
            self.settings.camera_width,
            self.settings.camera_height,
            self.settings.camera_fps,
        )
        self._stopping.clear()
        self._thread = threading.Thread(target=self._run, name="camera-worker", daemon=True)
        self._thread.start()

    def stop(self) -> None:
        LOGGER.info("Stopping camera worker")
        self._stopping.set()
        with self._condition:
            self._condition.notify_all()
        if self._thread:
            self._thread.join(timeout=3)
            if self._thread.is_alive():
                LOGGER.warning("Camera worker did not stop within 3 seconds")
            else:
                LOGGER.info("Camera worker stopped")

    def status(self) -> CameraStatus:
        with self._condition:
            return CameraStatus(
                available=self._available,
                backend=self._backend_name,
                message=self._message,
                last_frame_at=self._last_frame_at,
            )

    def latest_jpeg(self) -> bytes | None:
        with self._condition:
            return self._latest_jpeg

    def latest_frame(self) -> np.ndarray | None:
        """Return an isolated BGR frame without giving up camera ownership."""
        with self._condition:
            return None if self._latest_frame is None else self._latest_frame.copy()

    def wait_for_jpeg(self, previous: bytes | None, timeout: float = 2.0) -> bytes | None:
        with self._condition:
            self._condition.wait_for(
                lambda: self._stopping.is_set() or self._latest_jpeg is not previous,
                timeout=timeout,
            )
            return self._latest_jpeg

    def _publish(self, frame: np.ndarray, backend: str) -> None:
        ok, encoded = cv2.imencode(".jpg", frame, [cv2.IMWRITE_JPEG_QUALITY, 82])
        if not ok:
            LOGGER.warning("Camera frame JPEG encoding failed: backend=%s", backend)
            return
        now = time.time()
        with self._condition:
            self._latest_frame = frame.copy()
            self._latest_jpeg = encoded.tobytes()
            self._last_frame_at = now
            self._backend_name = backend
            self._message = "Camera is ready"
            self._available = True
            self._frame_count += 1
            should_log_health = (
                self._frame_count == 1 or now - self._last_health_log_at >= 30
            )
            if should_log_health:
                self._last_health_log_at = now
            self._condition.notify_all()
        if should_log_health:
            LOGGER.info(
                "Camera health: backend=%s frames=%d shape=%s dtype=%s jpeg_bytes=%d",
                backend,
                self._frame_count,
                tuple(frame.shape),
                frame.dtype,
                len(encoded),
            )

    def _set_error(self, message: str, backend: str = "none") -> None:
        LOGGER.warning(message)
        with self._condition:
            self._available = False
            self._backend_name = backend
            self._message = message
            self._condition.notify_all()

    def _run(self) -> None:
        if self.settings.camera_backend == "disabled":
            self._set_error("Camera is disabled by configuration")
            return

        while not self._stopping.is_set():
            try:
                backend = self._select_backend()
                LOGGER.info("Camera backend selected: %s", backend)
                if backend == "picamera2":
                    self._run_picamera2()
                else:
                    self._run_opencv()
            except Exception as exc:  # Camera drivers raise platform-specific exceptions.
                self._set_error(f"Camera unavailable: {exc}", self._backend_name)
                LOGGER.info("Camera worker will retry in 3 seconds")
                self._stopping.wait(3)

    def _select_backend(self) -> str:
        requested = self.settings.camera_backend
        if requested != "auto":
            return requested
        try:
            import picamera2  # noqa: F401

            return "picamera2"
        except ImportError:
            return "opencv"

    def _run_picamera2(self) -> None:
        from picamera2 import Picamera2

        LOGGER.info("Opening Picamera2 camera")
        camera = Picamera2()
        self._backend_name = "picamera2"
        config = camera.create_video_configuration(
            main={
                "size": (self.settings.camera_width, self.settings.camera_height),
                "format": "RGB888",
            }
        )
        camera.configure(config)
        camera.start()
        LOGGER.info("Picamera2 camera started")
        delay = 1 / max(self.settings.camera_fps, 1)
        try:
            while not self._stopping.is_set():
                rgb_frame = camera.capture_array("main")
                self._publish(cv2.cvtColor(rgb_frame, cv2.COLOR_RGB2BGR), "picamera2")
                self._stopping.wait(delay)
        finally:
            LOGGER.info("Closing Picamera2 camera")
            camera.stop()
            camera.close()

    def _run_opencv(self) -> None:
        LOGGER.info("Opening OpenCV camera: index=%d", self.settings.camera_index)
        camera: Any = cv2.VideoCapture(self.settings.camera_index)
        self._backend_name = "opencv"
        camera.set(cv2.CAP_PROP_FRAME_WIDTH, self.settings.camera_width)
        camera.set(cv2.CAP_PROP_FRAME_HEIGHT, self.settings.camera_height)
        camera.set(cv2.CAP_PROP_FPS, self.settings.camera_fps)
        if not camera.isOpened():
            camera.release()
            raise RuntimeError(f"cannot open camera index {self.settings.camera_index}")
        LOGGER.info(
            "OpenCV camera opened: actual_size=%dx%d actual_fps=%.1f",
            int(camera.get(cv2.CAP_PROP_FRAME_WIDTH)),
            int(camera.get(cv2.CAP_PROP_FRAME_HEIGHT)),
            camera.get(cv2.CAP_PROP_FPS),
        )
        try:
            while not self._stopping.is_set():
                ok, frame = camera.read()
                if not ok:
                    raise RuntimeError("cannot read a frame")
                self._publish(frame, "opencv")
        finally:
            LOGGER.info("Releasing OpenCV camera")
            camera.release()
