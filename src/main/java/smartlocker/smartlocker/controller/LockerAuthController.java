package smartlocker.smartlocker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartlocker.smartlocker.dto.FaceAuthRequest;
import smartlocker.smartlocker.dto.LockerCommandPayload;
import smartlocker.smartlocker.dto.MqttCommandEnum;
import smartlocker.smartlocker.model.*;
import smartlocker.smartlocker.repository.DeviceRepository;
import smartlocker.smartlocker.repository.OrderRepository;
import smartlocker.smartlocker.service.FaceUnlockService;
import smartlocker.smartlocker.service.MqttCommandPublisher;

import java.util.*;

@RestController
@RequestMapping("/api/face-auth")
@CrossOrigin(origins = "*")
public class LockerAuthController {

    private final FaceUnlockService faceUnlockService;
    private final DeviceRepository deviceRepository;
    private final OrderRepository orderRepository;
    private final MqttCommandPublisher mqttCommandPublisher;

    @Autowired
    public LockerAuthController(FaceUnlockService faceUnlockService,
            DeviceRepository deviceRepository,
            OrderRepository orderRepository,
            MqttCommandPublisher mqttCommandPublisher) {
        this.faceUnlockService = faceUnlockService;
        this.deviceRepository = deviceRepository;
        this.orderRepository = orderRepository;
        this.mqttCommandPublisher = mqttCommandPublisher;
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> unlockByFace(@RequestBody FaceAuthRequest request) {
        if (request.getDeviceId() == null || request.getEmbedding() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Thông tin thiết bị hoặc dữ liệu khuôn mặt không hợp lệ!"));
        }

        // 1. Nhận diện người dùng qua vector khuôn mặt
        Optional<User> userOpt = faceUnlockService.findUserWithVector(request.getDeviceId(), request.getEmbedding());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Không thể nhận diện khuôn mặt!"));
        }

        User user = userOpt.get();

        // 2. Tìm thông tin thiết bị và trạm tủ
        Optional<Device> deviceOpt = deviceRepository.findById(request.getDeviceId());
        if (deviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Thiết bị không tồn tại trên hệ thống!"));
        }

        Device device = deviceOpt.get();
        LockerStation station = device.getStation();
        if (station == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Thiết bị chưa được gán với trạm tủ nào!"));
        }

        // 3. Tìm các đơn hàng của người dùng tại trạm tủ này
        List<Order> orders = orderRepository.findByUserIdAndStationId(user.getId(), station.getId());
        List<String> unlockedLockers = new ArrayList<>();

        if (orders != null) {
            for (Order order : orders) {
                if (order.getOrderLockers() != null) {
                    for (OrderLocker orderLocker : order.getOrderLockers()) {
                        Locker locker = orderLocker.getLocker();
                        if (locker != null && locker.getLockerCode() != null) {
                            unlockedLockers.add(locker.getLockerCode());

                            // Gửi lệnh mở tủ qua MQTT đến thiết bị
                            if (mqttCommandPublisher != null) {
                                LockerCommandPayload payload = new LockerCommandPayload(MqttCommandEnum.UNLOCK,
                                        locker.getLockerCode(), 1000L);
                                try {
                                    mqttCommandPublisher.publishUnlockCommand(device.getId(), station.getId(), payload);
                                } catch (Exception e) {
                                    System.err.println("Failed to publish MQTT unlock command for locker "
                                            + locker.getLockerCode() + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }

        if (unlockedLockers.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Nhận diện thành công nhưng không tìm thấy đơn hàng/ngăn tủ nào của bạn tại trạm này."));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message",
                "Xác thực khuôn mặt thành công. Đã gửi lệnh mở các ngăn tủ: " + String.join(", ", unlockedLockers),
                "unlockedLockers", unlockedLockers));
    }
}
