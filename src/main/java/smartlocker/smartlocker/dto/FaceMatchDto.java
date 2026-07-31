package smartlocker.smartlocker.dto;

import java.util.UUID;

public class FaceMatchDto {
    UUID embeddingId;

    UUID userId;

    Double distance;

    public UUID getEmbeddingId() {
        return embeddingId;
    }

    public void setEmbeddingId(UUID embeddingId) {
        this.embeddingId = embeddingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

}
