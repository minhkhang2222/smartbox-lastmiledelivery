package smartlocker.smartlocker.dto;

import smartlocker.smartlocker.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Summary of an Order shown to the user in their order list.
 * OTP code is only provided when it is still valid (not expired, not used).
 */
public record UserOrderSummaryDto(
        UUID orderId,
        String stationName,
        String stationAddress,
        OrderStatus status,
        String recipientPhoneNumber,
        LocalDateTime createdAt,
        LocalDateTime expiredAt,
        /** Active OTP code if present (frontend will mask it) */
        String activeOtpCode,
        LocalDateTime otpExpiredAt,
        List<String> lockerCodes
) {}
