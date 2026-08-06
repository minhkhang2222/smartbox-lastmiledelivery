package smartlocker.smartlocker.dto;

import java.util.UUID;

public class FaceAuthRequest {
    private UUID deviceId;
    private String deviceType;
    private float[] embedding;

    public FaceAuthRequest() {
    }

    public FaceAuthRequest(UUID deviceId, String deviceType, float[] embedding) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.embedding = embedding;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}
