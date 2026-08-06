package smartlocker.smartlocker.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smartlocker.smartlocker.dto.FaceAuthResult;
import smartlocker.smartlocker.dto.LockerCommandPayload;
import smartlocker.smartlocker.dto.MqttCommandEnum;
import smartlocker.smartlocker.dto.PickupOtpRequest;
import smartlocker.smartlocker.model.Device;
import smartlocker.smartlocker.model.Locker;
import smartlocker.smartlocker.model.Order;
import smartlocker.smartlocker.model.OrderLocker;
import smartlocker.smartlocker.model.OrderLockerStatus;
import smartlocker.smartlocker.model.OrderOtp;
import smartlocker.smartlocker.repository.DeviceRepository;
import smartlocker.smartlocker.repository.OrderOtpRepository;

@Service
public class PickupOtpService {
    private final DeviceRepository deviceRepository;
    private final OrderOtpRepository orderOtpRepository;
    private final MqttCommandPublisher mqttCommandPublisher;

    public PickupOtpService(
            DeviceRepository deviceRepository,
            OrderOtpRepository orderOtpRepository,
            MqttCommandPublisher mqttCommandPublisher) {
        this.deviceRepository = deviceRepository;
        this.orderOtpRepository = orderOtpRepository;
        this.mqttCommandPublisher = mqttCommandPublisher;
    }

    @Transactional
    public FaceAuthResult verifyAndUnlock(PickupOtpRequest request) {
        if (request == null || request.deviceId() == null || request.otpCode() == null
                || request.otpCode().isBlank()) {
            throw new IllegalArgumentException("Device ID and OTP are required.");
        }
        if (!"RASPBERRY".equalsIgnoreCase(request.deviceType())) {
            throw new IllegalArgumentException("Pickup OTP requests must come from a Raspberry device.");
        }

        Device device = deviceRepository.findById(request.deviceId())
                .orElseThrow(() -> new IllegalArgumentException("The station device was not found."));
        if (device.getStation() == null || !"RASPBERRY".equalsIgnoreCase(device.getDeviceType())) {
            throw new IllegalArgumentException("The Raspberry device is not assigned to a station.");
        }

        OrderOtp otp = orderOtpRepository
                .findTopByOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(request.otpCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("The OTP is invalid or has already been used."));
        if (otp.getExpiredAt() == null || otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("The OTP has expired.");
        }
        if (otp.getAttemptsCount() != null && otp.getMaxAttempts() != null
                && otp.getAttemptsCount() >= otp.getMaxAttempts()) {
            throw new IllegalArgumentException("The OTP has reached its maximum number of attempts.");
        }

        Order order = otp.getOrder();
        if (order == null || order.getStation() == null
                || !order.getStation().getId().equals(device.getStation().getId())) {
            throw new IllegalArgumentException("This OTP does not belong to the current station.");
        }

        Map<String, Locker> lockersByCode = new LinkedHashMap<>();
        for (OrderLocker orderLocker : order.getOrderLockers()) {
            Locker locker = orderLocker.getLocker();
            if (orderLocker.getStatus() == OrderLockerStatus.WAIT_FOR_COLLECTION
                    && locker != null && locker.getDevice() != null && locker.getLockerCode() != null) {
                lockersByCode.putIfAbsent(locker.getLockerCode(), locker);
            }
        }
        if (lockersByCode.isEmpty()) {
            throw new IllegalArgumentException("No locker is ready for pickup with this OTP.");
        }

        for (Locker locker : lockersByCode.values()) {
            mqttCommandPublisher.publishUnlockCommand(
                    locker.getDevice().getId(),
                    device.getStation().getId(),
                    new LockerCommandPayload(MqttCommandEnum.UNLOCK, locker.getLockerCode(), 1000L));
        }
        otp.setIsUsed(true);
        orderOtpRepository.save(otp);

        return new FaceAuthResult(
                true,
                "OTP verified. The pickup locker has been unlocked.",
                order.getRecipientUser() == null ? null : order.getRecipientUser().getId(),
                List.copyOf(lockersByCode.keySet()));
    }
}
