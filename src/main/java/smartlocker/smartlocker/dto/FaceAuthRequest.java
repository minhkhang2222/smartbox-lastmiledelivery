package smartlocker.smartlocker.dto;

import java.util.UUID;

public class FaceAuthRequest {
    private UUID deviceId;
    private float[] embedding;

    public FaceAuthRequest() {
    }

    public FaceAuthRequest(UUID deviceId, float[] embedding) {
        this.deviceId = deviceId;
        this.embedding = embedding;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}
