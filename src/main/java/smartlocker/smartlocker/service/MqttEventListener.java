package smartlocker.smartlocker.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import smartlocker.smartlocker.dto.LockerCommandPayload;
import smartlocker.smartlocker.dto.MqttCommandEnum;
import smartlocker.smartlocker.model.OrderLocker;
import smartlocker.smartlocker.model.OrderLockerStatus;
import smartlocker.smartlocker.repository.OrderLockerRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

@Component
public class MqttEventListener {

    private final MqttService mqttService;
    private final ObjectMapper objectMapper;
    private final OrderLockerRepository orderLockerRepository;
    private final OrderOtpService orderOtpService;

    public MqttEventListener(MqttService mqttService,
            ObjectMapper objectMapper,
            OrderLockerRepository orderLockerRepository,
            OrderOtpService orderOtpService) {
        this.mqttService = mqttService;
        this.objectMapper = objectMapper;
        this.orderLockerRepository = orderLockerRepository;
        this.orderOtpService = orderOtpService;
    }

    @PostConstruct
    public void registerEventListener() {
        // Topic pattern: smartlocker/{stationId}/{deviceId}/event
        mqttService.subscribe("smartlocker/+/+/event", (topic, message) -> {
            System.out.println("[MQTT Event] topic=" + topic + " payload=" + message);
            handleEvent(topic, message);
        });
    }

    void handleEvent(String topic, String message) {
        try {
            // Parse stationId từ topic: smartlocker/{stationId}/{deviceId}/event
            String[] parts = topic.split("/");
            if (parts.length < 4) {
                System.err.println("[MqttEventListener] Invalid topic format: " + topic);
                return;
            }
            UUID stationId = UUID.fromString(parts[1]);

            LockerCommandPayload payload = objectMapper.readValue(message, LockerCommandPayload.class);

            if (payload.getCommandType() == null) {
                System.err.println("[MqttEventListener] commandType is null in payload: " + message);
                return;
            }

            if (payload.getCommandType() == MqttCommandEnum.LOCK) {
                handleLock(stationId, payload);
            } else {
                System.out.println("[MqttEventListener] Ignoring device command: " + payload.getCommandType());
            }

        } catch (Exception e) {
            System.err.println("[MqttEventListener] Failed to process event: " + e.getMessage());
        }
    }

    /**
     * Xử lý LOCK sau khi cửa tủ đóng ổn định (debounce tại ESP).
     * Tìm OrderLocker đang WAIT_FOR_DEPOSIT, chuyển thành WAIT_FOR_COLLECTION.
     * LOCK không khớp order đang chờ được bỏ qua vì ESP không giữ trạng thái order.
     */
    @Transactional
    void handleLock(UUID stationId, LockerCommandPayload payload) {
        String lockerCode = payload.getLockerId();
        if (lockerCode == null || lockerCode.isBlank()) {
            System.err.println("[MqttEventListener] LOCK missing lockerId");
            return;
        }

        Optional<OrderLocker> optOrderLocker = orderLockerRepository
                .findWaitingForDepositByLockerCodeAndStation(lockerCode, stationId);

        if (optOrderLocker.isEmpty()) {
            System.out.println("[MqttEventListener] Ignoring LOCK for locker " + lockerCode
                    + " at station " + stationId + ": no WAIT_FOR_DEPOSIT order");
            return;
        }

        OrderLocker orderLocker = optOrderLocker.get();

        // Race condition guard: chỉ chuyển nếu vẫn đang WAIT_FOR_DEPOSIT
        if (orderLocker.getStatus() != OrderLockerStatus.WAIT_FOR_DEPOSIT) {
            System.out.println("[MqttEventListener] OrderLocker " + orderLocker.getId()
                    + " is no longer WAIT_FOR_DEPOSIT (current: " + orderLocker.getStatus() + "), skipping.");
            return;
        }

        orderLocker.setStatus(OrderLockerStatus.WAIT_FOR_COLLECTION);
        orderLockerRepository.save(orderLocker);

        if (orderLocker.getOrder() != null
                && orderLockerRepository.countWaitingForDeposit(orderLocker.getOrder().getId()) == 0) {
            orderOtpService.createOtp(orderLocker.getOrder().getId());
        }

        System.out.println("[MqttEventListener] OrderLocker " + orderLocker.getId()
                + " (locker=" + lockerCode + ") → WAIT_FOR_COLLECTION. LOCK confirmed.");
    }
}
