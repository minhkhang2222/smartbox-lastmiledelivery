from __future__ import annotations

import logging
import threading
import time
from pathlib import Path
from typing import Any

import numpy as np


LOGGER = logging.getLogger("uvicorn.error")


class FaceAuthenticationError(RuntimeError):
    """A user-facing face processing error."""


class FaceAuthenticator:
    """Load InsightFace only from local storage and serialize Pi inference."""

    def __init__(self, model_root: str, model_name: str = "buffalo_l") -> None:
        self._analyzer: Any | None = None
        self._lock = threading.Lock()
        self._model_root = Path(model_root).expanduser().resolve()
        self._model_name = model_name

    def extract_embedding(
        self, frame: np.ndarray, trace_id: str = "unknown"
    ) -> list[float]:
        # InsightFace/ONNX sessions are reused between requests and must not be
        # entered concurrently. Wait for the previous inference instead of
        # rejecting every request that happens to overlap it.
        queued_at = time.perf_counter()
        LOGGER.info(
            "[face-auth:%s] Inference queued: shape=%s dtype=%s",
            trace_id,
            tuple(frame.shape),
            frame.dtype,
        )
        with self._lock:
            wait_ms = (time.perf_counter() - queued_at) * 1000
            inference_started = time.perf_counter()
            LOGGER.info(
                "[face-auth:%s] Inference lock acquired after %.1f ms",
                trace_id,
                wait_ms,
            )
            analyzer = self._get_analyzer()
            try:
                faces = analyzer.get(frame)
            except Exception as exc:
                LOGGER.exception(
                    "[face-auth:%s] InsightFace inference raised an exception",
                    trace_id,
                )
                raise FaceAuthenticationError(
                    f"Không thể xử lý khung hình khuôn mặt: {exc}"
                ) from exc
            inference_ms = (time.perf_counter() - inference_started) * 1000
            LOGGER.info(
                "[face-auth:%s] InsightFace completed in %.1f ms; faces=%d",
                trace_id,
                inference_ms,
                len(faces),
            )
            if not faces:
                LOGGER.warning(
                    "[face-auth:%s] No face detected in the captured frame",
                    trace_id,
                )
                raise FaceAuthenticationError("Không phát hiện khuôn mặt trong khung hình.")

            face = max(
                faces,
                key=lambda item: (item.bbox[2] - item.bbox[0])
                * (item.bbox[3] - item.bbox[1]),
            )
            bbox = np.asarray(face.bbox, dtype=np.float32)
            det_score = getattr(face, "det_score", None)
            LOGGER.info(
                "[face-auth:%s] Selected largest face: bbox=[%.1f, %.1f, %.1f, %.1f] "
                "area=%.1f detection_score=%s",
                trace_id,
                float(bbox[0]),
                float(bbox[1]),
                float(bbox[2]),
                float(bbox[3]),
                float((bbox[2] - bbox[0]) * (bbox[3] - bbox[1])),
                "unknown" if det_score is None else f"{float(det_score):.4f}",
            )
            embedding = np.array(face.embedding, dtype=np.float32, copy=True)
            norm = float(np.linalg.norm(embedding))
            if norm < 1e-10:
                LOGGER.error(
                    "[face-auth:%s] Invalid zero-norm embedding: dimensions=%d",
                    trace_id,
                    embedding.size,
                )
                raise FaceAuthenticationError("Không thể tạo dữ liệu nhận diện khuôn mặt.")
            embedding /= norm
            if embedding.size != 512:
                LOGGER.error(
                    "[face-auth:%s] Unexpected embedding dimensions: expected=512 actual=%d",
                    trace_id,
                    embedding.size,
                )
                raise FaceAuthenticationError("Model khuôn mặt không trả về embedding 512 chiều.")
            LOGGER.info(
                "[face-auth:%s] Embedding ready: dimensions=%d raw_norm=%.6f "
                "normalized_norm=%.6f min=%.6f max=%.6f total=%.1f ms",
                trace_id,
                embedding.size,
                norm,
                float(np.linalg.norm(embedding)),
                float(embedding.min()),
                float(embedding.max()),
                (time.perf_counter() - queued_at) * 1000,
            )
            return embedding.tolist()

    def _get_analyzer(self) -> Any:
        if self._analyzer is not None:
            return self._analyzer

        # FaceAnalysis downloads a model automatically when its model directory is
        # absent. Check it first so authentication never starts a network download.
        model_dir = self._model_root / "models" / self._model_name
        model_files = list(model_dir.glob("*.onnx")) if model_dir.is_dir() else []
        if not model_files:
            LOGGER.error(
                "Face model is missing: name=%s directory=%s",
                self._model_name,
                model_dir,
            )
            raise FaceAuthenticationError(
                "Khong tim thay model khuon mat cuc bo. "
                f"Hay chep cac file .onnx vao {model_dir}"
            )

        LOGGER.info(
            "Loading InsightFace model: name=%s root=%s onnx_files=%d "
            "provider=CPUExecutionProvider det_size=640x640",
            self._model_name,
            self._model_root,
            len(model_files),
        )
        load_started = time.perf_counter()

        try:
            import insightface
        except ImportError as exc:
            LOGGER.exception("Unable to import InsightFace")
            raise FaceAuthenticationError(
                "InsightFace chưa được cài trên Raspberry Pi."
            ) from exc

        try:
            analyzer = insightface.app.FaceAnalysis(
                name=self._model_name,
                root=str(self._model_root),
                providers=["CPUExecutionProvider"],
            )
            analyzer.prepare(ctx_id=-1, det_size=(640, 640))
        except Exception as exc:
            LOGGER.exception("Unable to initialize the InsightFace model")
            raise FaceAuthenticationError(f"Không thể nạp model khuôn mặt: {exc}") from exc

        self._analyzer = analyzer
        LOGGER.info(
            "InsightFace model loaded successfully in %.1f ms",
            (time.perf_counter() - load_started) * 1000,
        )
        return analyzer
