package smartlocker.smartlocker.dto;

import java.util.UUID;

public interface FaceMatchDto {
    UUID getEmbeddingId();

    UUID getUserId();

    Double getDistance();
}
