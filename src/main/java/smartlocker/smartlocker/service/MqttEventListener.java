package smartlocker.smartlocker.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import smartlocker.smartlocker.ENUM.LockerEventType;
import smartlocker.smartlocker.dto.LockerEventPayload;
import smartlocker.smartlocker.model.Order;
import smartlocker.smartlocker.model.OrderLocker;
import smartlocker.smartlocker.model.OrderLockerStatus;
import smartlocker.smartlocker.model.OrderStatus;
import smartlocker.smartlocker.repository.OrderLockerRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

@Component
public class MqttEventListener {

    private final MqttService mqttService;
    private final ObjectMapper objectMapper;
    private final OrderLockerRepository orderLockerRepository;

    public MqttEventListener(MqttService mqttService,
            ObjectMapper objectMapper,
            OrderLockerRepository orderLockerRepository) {
        this.mqttService = mqttService;
        this.objectMapper = objectMapper;
        this.orderLockerRepository = orderLockerRepository;
    }

    @PostConstruct
    public void registerEventListener() {
        // Topic pattern: smartlocker/{stationId}/{deviceId}/event
        mqttService.subscribe("smartlocker/+/+/event", (topic, message) -> {
            System.out.println("[MQTT Event] topic=" + topic + " payload=" + message);
            handleEvent(topic, message);
        });
    }

    private void handleEvent(String topic, String message) {
        try {
            // Parse stationId từ topic: smartlocker/{stationId}/{deviceId}/event
            String[] parts = topic.split("/");
            if (parts.length < 4) {
                System.err.println("[MqttEventListener] Invalid topic format: " + topic);
                return;
            }
            UUID stationId = UUID.fromString(parts[1]);

            LockerEventPayload payload = objectMapper.readValue(message, LockerEventPayload.class);

            if (payload.getEventType() == null) {
                System.err.println("[MqttEventListener] eventType is null in payload: " + message);
                return;
            }

            switch (payload.getEventType()) {
                case DOOR_CLOSED -> handleDoorClosed(stationId, payload);
                case DOOR_OPENED -> System.out.println("[MqttEventListener] DOOR_OPENED for locker: "
                        + payload.getLockerCode() + " (no action needed)");
                default -> System.out.println("[MqttEventListener] Unknown event type: " + payload.getEventType());
            }

        } catch (Exception e) {
            System.err.println("[MqttEventListener] Failed to process event: " + e.getMessage());
        }
    }

    /**
     * Xử lý event cửa tủ đóng lại đủ 3 giây (debounce tại ESP).
     * Tìm OrderLocker đang WAIT_FOR_DEPOSIT, chuyển thành PENDING.
     * Race condition safe: chỉ update nếu vẫn đang WAIT_FOR_DEPOSIT.
     */
    @Transactional
    void handleDoorClosed(UUID stationId, LockerEventPayload payload) {
        String lockerCode = payload.getLockerCode();
        if (lockerCode == null || lockerCode.isBlank()) {
            System.err.println("[MqttEventListener] DOOR_CLOSED missing lockerCode");
            return;
        }

        Optional<OrderLocker> optOrderLocker = orderLockerRepository
                .findWaitingForDepositByLockerCodeAndStation(lockerCode, stationId);

        if (optOrderLocker.isEmpty()) {
            System.out.println("[MqttEventListener] No WAIT_FOR_DEPOSIT OrderLocker found for locker "
                    + lockerCode + " at station " + stationId + " (possibly already FAILED or no active order)");
            return;
        }

        OrderLocker orderLocker = optOrderLocker.get();

        // Race condition guard: chỉ chuyển nếu vẫn đang WAIT_FOR_DEPOSIT
        if (orderLocker.getStatus() != OrderLockerStatus.WAIT_FOR_DEPOSIT) {
            System.out.println("[MqttEventListener] OrderLocker " + orderLocker.getId()
                    + " is no longer WAIT_FOR_DEPOSIT (current: " + orderLocker.getStatus() + "), skipping.");
            return;
        }

        orderLocker.setStatus(OrderLockerStatus.PENDING);
        orderLockerRepository.save(orderLocker);

        System.out.println("[MqttEventListener] OrderLocker " + orderLocker.getId()
                + " (locker=" + lockerCode + ") → PENDING. Door closed confirmed.");
    }
}
