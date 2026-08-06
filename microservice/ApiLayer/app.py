import os
# Prevent OpenMP / ONNX Runtime thread affinity error inside Docker containers
os.environ["OMP_NUM_THREADS"] = "4"
os.environ["OPENBLAS_NUM_THREADS"] = "4"
os.environ["MKL_NUM_THREADS"] = "4"
os.environ["VECLIB_MAXIMUM_THREADS"] = "4"
os.environ["NUMEXPR_NUM_THREADS"] = "4"

import cv2
import numpy as np
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
import insightface

app = FastAPI(title="Core AI Embedding Extraction Microservice")


# Initialize InsightFace model with CPU execution provider as default
print("[CoreAI] Initializing InsightFace model analysis (CPU mode)...")
face_analyzer = insightface.app.FaceAnalysis(name='buffalo_l', providers=['CPUExecutionProvider'])
face_analyzer.prepare(ctx_id=-1, det_size=(640, 640))
print("[CoreAI] InsightFace model initialized successfully on CPU.")


def l2_normalize(v: np.ndarray) -> np.ndarray:
    """Return a unit vector (safe against zero-norm)."""
    norm = np.linalg.norm(v)
    if norm < 1e-10:
        return v
    return v / norm

@app.post("/vectorize")
async def vectorize(image: UploadFile = File(...)):
    """
    Accepts a raw face crop image or full image, extracts the face embedding vector.
    Returns: JSON list containing the 512-dimensional embedding vector.
    """
    try:
        # Read uploaded image bytes
        contents = await image.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if img is None:
            raise HTTPException(status_code=400, detail="Invalid image file.")
            
        # Detect faces and extract features
        faces = face_analyzer.get(img)
        if not faces:
            raise HTTPException(status_code=404, detail="No face detected in the image.")
            
        # Get the largest face based on bounding box area if multiple faces detected
        largest_face = max(faces, key=lambda f: (f.bbox[2] - f.bbox[0]) * (f.bbox[3] - f.bbox[1]))
        
        # Extract embedding and normalize
        raw_embedding = largest_face.embedding
        normalized_embedding = l2_normalize(raw_embedding)
        
        return JSONResponse(content={
            "status": "success",
            "embedding": normalized_embedding.tolist()
        })
        
    except HTTPException as he:
        raise he
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=9001)
