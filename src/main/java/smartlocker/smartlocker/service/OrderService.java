package smartlocker.smartlocker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartlocker.smartlocker.dto.CreateOrderRequest;
import smartlocker.smartlocker.dto.CreateOrderResponse;
import smartlocker.smartlocker.dto.LockerCommandPayload;
import smartlocker.smartlocker.dto.MqttCommandEnum;
import smartlocker.smartlocker.exception.LockersNotAvailableException;
import smartlocker.smartlocker.model.*;
import smartlocker.smartlocker.repository.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderLockerRepository orderLockerRepository;

    @Autowired
    private LockerStationRepository stationRepository;

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStationRegistrationRepository registrationRepository;

    @Autowired(required = false)
    private MqttCommandPublisher mqttCommandPublisher;

    @Value("${mqtt.publish-enabled:true}")
    private boolean mqttPublishEnabled;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request payload không được null.");
        }

        // Bước 1: userId tồn tại
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId không được để trống.");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy User với ID: " + request.getUserId()));

        // Bước 2: stationId tồn tại và station đang ACTIVE
        if (request.getStationId() == null) {
            throw new IllegalArgumentException("stationId không được để trống.");
        }
        LockerStation station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy LockerStation với ID: " + request.getStationId()));

        if (station.getStatus() == null || !"ACTIVE".equalsIgnoreCase(station.getStatus())) {
            throw new IllegalArgumentException(
                    "Trạm tủ ID " + request.getStationId() + " đang không ở trạng thái ACTIVE.");
        }

        // Bước 3: User có đăng ký ACTIVE tại station
        boolean isUserRegisteredActive = registrationRepository.existsByUserIdAndStationIdAndStatus(
                request.getUserId(), request.getStationId(), "ACTIVE");
        if (!isUserRegisteredActive) {
            throw new IllegalArgumentException("User chưa đăng ký trạng thái ACTIVE tại trạm tủ này.");
        }

        if (request.getRecipientPhoneNumber() == null || request.getRecipientPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("recipientPhoneNumber không được để trống.");
        }
        String recipientPhoneNumber = request.getRecipientPhoneNumber().trim();
        User recipientUser = userRepository.findByPhoneNumber(recipientPhoneNumber).orElse(null);

        // Bước 4: lockerIds không rỗng
        List<UUID> lockerIds = request.getLockerIds();
        if (lockerIds == null || lockerIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách tủ (lockerIds) không được để trống.");
        }

        // Bước 5: Không có ID locker bị trùng trong request
        Set<UUID> uniqueLockerIds = new HashSet<>(lockerIds);
        if (uniqueLockerIds.size() != lockerIds.size()) {
            throw new IllegalArgumentException("Danh sách lockerIds có chứa ID bị trùng lặp.");
        }

        // Bước 6: Tất cả locker đều tồn tại trong DB
        // Lock các row locker đến khi transaction kết thúc để hai request đồng thời
        // không thể cùng giữ một locker.
        List<Locker> lockers = lockerRepository.findAllByIdForUpdate(lockerIds);
        if (lockers.size() != uniqueLockerIds.size()) {
            throw new LockersNotAvailableException("Một hoặc nhiều tủ trong danh sách không tồn tại trong hệ thống.");
        }

        // Bước 7 & 8 & 9: Kiểm tra từng locker
        for (Locker locker : lockers) {
            // Bước 7: Tất cả locker đều thuộc stationId
            if (locker.getStation() == null || !locker.getStation().getId().equals(request.getStationId())) {
                throw new IllegalArgumentException(
                        "Tủ " + locker.getLockerCode() + " không thuộc về trạm tủ ID " + request.getStationId());
            }

            // Tủ được xem là đang có đơn nếu còn chờ gửi hoặc chờ nhận.
            // Không dùng Locker.status vì đó là trạng thái vật lý của tủ.
            boolean hasActiveOrderLocker = orderLockerRepository.existsByLockerIdAndStatusIn(
                    locker.getId(),
                    Arrays.asList(OrderLockerStatus.WAIT_FOR_DEPOSIT, OrderLockerStatus.WAIT_FOR_COLLECTION));
            if (hasActiveOrderLocker) {
                throw new LockersNotAvailableException("LOCKERS_NOT_AVAILABLE: Tủ " + locker.getLockerCode()
                        + " đã có đơn hàng (OrderLocker) đang hoạt động.");
            }

            // Bước 9: Device quản lý locker không ở trạng thái lỗi hoặc offline
            Device device = locker.getDevice();
            if (device != null && device.getStatus() != null) {
                String devStatus = device.getStatus().toUpperCase();
                if ("OFFLINE".equals(devStatus) || "ERROR".equals(devStatus)) {
                    throw new LockersNotAvailableException(
                            "Thiết bị điều khiển tủ " + locker.getLockerCode() + " đang bị " + devStatus + ".");
                }
            }
        }

        // TRANSACTION TẠO ORDER & GIỮ TỦ
        // 1. Tạo Order (status = WAITING_FOR_DEPOSIT)
        Order order = new Order();
        order.setUser(user);
        order.setStation(station);
        order.setSenderPhoneNumber(user.getPhoneNumber());
        order.setRecipientPhoneNumber(recipientPhoneNumber);
        order.setRecipientUser(recipientUser);
        order.setStatus(OrderStatus.WAITING_FOR_DEPOSIT);
        order.setCreatedAt(LocalDateTime.now());
        order.setExpiredAt(LocalDateTime.now().plusHours(24));

        Order savedOrder = orderRepository.save(order);

        // 2. Tạo order_lockers (status = ACTIVE) & Đổi lockers.status (FREE ->
        // WAITING_FOR_DEPOSIT)
        List<OrderLocker> orderLockers = new ArrayList<>();
        for (Locker locker : lockers) {

            // Tạo order_locker
            OrderLocker orderLocker = new OrderLocker();
            orderLocker.setOrder(savedOrder);
            orderLocker.setLocker(locker);
            orderLocker.setStatus(OrderLockerStatus.WAIT_FOR_DEPOSIT);
            orderLockers.add(orderLocker);

            // Gửi lệnh MQTT WAIT_FOR_DEPOSIT xuống station device cho tủ này
            if (mqttPublishEnabled && mqttCommandPublisher != null && locker.getDevice() != null) {
                LockerCommandPayload payload = new LockerCommandPayload(MqttCommandEnum.WAIT_FOR_DEPOSIT,
                        locker.getLockerCode(), 1000L);
                try {
                    mqttCommandPublisher.publishWaitForDepositCommand(locker.getDevice().getId(), station.getId(),
                            payload);
                } catch (Exception e) {
                    System.err.println("Failed to publish MQTT WAIT_FOR_DEPOSIT command for locker "
                            + locker.getLockerCode() + ": " + e.getMessage());
                }
            }
        }
        orderLockerRepository.saveAll(orderLockers);
        savedOrder.setOrderLockers(orderLockers);

        return new CreateOrderResponse(
                savedOrder.getId(),
                user.getId(),
                station.getId(),
                lockerIds,
                savedOrder.getStatus(),
                savedOrder.getCreatedAt(),
                savedOrder.getExpiredAt());
    }
}
