package smartlocker.smartlocker.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.stereotype.Service;

import smartlocker.smartlocker.dto.FaceAuthRequest;
import smartlocker.smartlocker.dto.FaceAuthResult;
import smartlocker.smartlocker.dto.FaceIdentifyResult;
import smartlocker.smartlocker.dto.LockerCommandPayload;
import smartlocker.smartlocker.dto.MqttCommandEnum;
import smartlocker.smartlocker.model.Device;
import smartlocker.smartlocker.model.Locker;
import smartlocker.smartlocker.model.Order;
import smartlocker.smartlocker.model.OrderLocker;
import smartlocker.smartlocker.model.OrderLockerStatus;
import smartlocker.smartlocker.model.User;
import smartlocker.smartlocker.repository.DeviceRepository;
import smartlocker.smartlocker.repository.OrderRepository;

@Service
public class FaceAuthenticationService {
    private final FaceUnlockService faceUnlockService;
    private final DeviceRepository deviceRepository;
    private final OrderRepository orderRepository;
    private final MqttCommandPublisher mqttCommandPublisher;

    public FaceAuthenticationService(
            FaceUnlockService faceUnlockService,
            DeviceRepository deviceRepository,
            OrderRepository orderRepository,
            MqttCommandPublisher mqttCommandPublisher) {
        this.faceUnlockService = faceUnlockService;
        this.deviceRepository = deviceRepository;
        this.orderRepository = orderRepository;
        this.mqttCommandPublisher = mqttCommandPublisher;
    }

    public FaceAuthResult authenticateAndUnlock(FaceAuthRequest request) {
        validate(request);

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new FaceAuthException(FaceAuthException.Reason.DEVICE_NOT_FOUND,
                        "Thiết bị không tồn tại trên hệ thống."));

        if (device.getStation() == null) {
            throw new FaceAuthException(FaceAuthException.Reason.INVALID_DEVICE,
                    "Thiết bị chưa được gán vào trạm tủ.");
        }
        // Face-auth chỉ nhận request từ Raspberry Pi, không nhận UUID của ESP32.
        if (!"RASPBERRY".equalsIgnoreCase(device.getDeviceType())) {
            throw new FaceAuthException(FaceAuthException.Reason.INVALID_DEVICE,
                    "Thiết bị gửi face-auth phải có device_type là RASPBERRY.");
        }

        User user = faceUnlockService.findUserWithVector(request.getDeviceId(), request.getEmbedding())
                .orElseThrow(() -> new FaceAuthException(FaceAuthException.Reason.FACE_NOT_MATCHED,
                        "Không thể xác thực khuôn mặt."));

        List<Order> orders = orderRepository.findByRecipientUserIdAndStationId(user.getId(), device.getStation().getId());
        Map<String, Locker> lockersByCode = new LinkedHashMap<>();

        for (Order order : Optional.ofNullable(orders).orElseGet(ArrayList::new)) {
            if (order.getOrderLockers() == null) {
                continue;
            }
            for (OrderLocker orderLocker : order.getOrderLockers()) {
                if (!isActive(orderLocker.getStatus())) {
                    continue;
                }
                Locker locker = orderLocker.getLocker();
                if (locker != null && locker.getLockerCode() != null && locker.getDevice() != null) {
                    lockersByCode.putIfAbsent(locker.getLockerCode(), locker);
                }
            }
        }

        if (lockersByCode.isEmpty()) {
            return new FaceAuthResult(false,
                    "Xác thực thành công nhưng không có ngăn tủ đang hoạt động tại trạm này.",
                    user.getId(), List.of());
        }

        for (Locker locker : lockersByCode.values()) {
            LockerCommandPayload payload = new LockerCommandPayload(
                    MqttCommandEnum.UNLOCK, locker.getLockerCode(), 1000L);
            mqttCommandPublisher.publishUnlockCommand(
                    locker.getDevice().getId(), device.getStation().getId(), payload);
        }

        return new FaceAuthResult(true, "Xác thực khuôn mặt thành công.", user.getId(),
                List.copyOf(lockersByCode.keySet()));
    }

    public FaceIdentifyResult identify(FaceAuthRequest request) {
        validate(request);

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new FaceAuthException(FaceAuthException.Reason.DEVICE_NOT_FOUND,
                        "Thiết bị không tồn tại trên hệ thống."));
        if (device.getStation() == null) {
            throw new FaceAuthException(FaceAuthException.Reason.INVALID_DEVICE,
                    "Thiết bị chưa được gán vào trạm tủ.");
        }
        if (!"RASPBERRY".equalsIgnoreCase(device.getDeviceType())) {
            throw new FaceAuthException(FaceAuthException.Reason.INVALID_DEVICE,
                    "Thiết bị gửi face-auth phải có device_type là RASPBERRY.");
        }

        User user = faceUnlockService.findUserWithVector(request.getDeviceId(), request.getEmbedding())
                .orElseThrow(() -> new FaceAuthException(FaceAuthException.Reason.FACE_NOT_MATCHED,
                        "Không thể xác thực khuôn mặt."));

        return new FaceIdentifyResult(
                true,
                "Xác thực khuôn mặt thành công.",
                user.getId(),
                user.getFullName());
    }

    private void validate(FaceAuthRequest request) {
        if (request == null || request.getDeviceId() == null) {
            throw new FaceAuthException(FaceAuthException.Reason.INVALID_REQUEST, "deviceId là bắt buộc.");
        }
        if (!"RASPBERRY".equalsIgnoreCase(request.getDeviceType())) {
            throw new FaceAuthException(FaceAuthException.Reason.INVALID_REQUEST,
                    "deviceType phải là RASPBERRY.");
        }
        if (request.getEmbedding() == null || request.getEmbedding().length != 512) {
            throw new FaceAuthException(FaceAuthException.Reason.INVALID_REQUEST,
                    "embedding phải có đúng 512 phần tử.");
        }
        for (float value : request.getEmbedding()) {
            if (!Float.isFinite(value)) {
                throw new FaceAuthException(FaceAuthException.Reason.INVALID_REQUEST,
                        "embedding chỉ được chứa số hữu hạn.");
            }
        }
    }

    private boolean isActive(OrderLockerStatus status) {
        return status == OrderLockerStatus.WAIT_FOR_DEPOSIT
                || status == OrderLockerStatus.WAIT_FOR_COLLECTION;
    }
}
