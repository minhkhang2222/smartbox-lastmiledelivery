package smartlocker.smartlocker.dto;

import java.util.List;
import java.util.UUID;

public record FaceAuthResult(
        boolean success,
        String message,
        UUID userId,
        List<String> unlockedLockers) {
}
