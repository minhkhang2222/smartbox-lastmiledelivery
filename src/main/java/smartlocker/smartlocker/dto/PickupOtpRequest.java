package smartlocker.smartlocker.dto;

import java.util.UUID;

public record PickupOtpRequest(
        UUID deviceId,
        String deviceType,
        String otpCode) {
}
