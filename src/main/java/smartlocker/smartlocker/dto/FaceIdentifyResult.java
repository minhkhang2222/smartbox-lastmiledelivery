package smartlocker.smartlocker.dto;

import java.util.UUID;

public record FaceIdentifyResult(
        boolean success,
        String message,
        UUID userId,
        String fullName) {
}
