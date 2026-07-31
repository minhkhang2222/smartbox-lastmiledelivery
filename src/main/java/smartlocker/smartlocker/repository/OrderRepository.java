package smartlocker.smartlocker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smartlocker.smartlocker.model.Order;
import smartlocker.smartlocker.model.OrderStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserIdAndStationId(UUID userId, UUID stationId);

    List<Order> findByUserId(UUID userId);

    /**
     * Tìm tất cả Order đang ở trạng thái WAITING_FOR_DEPOSIT.
     * Dùng cho Scheduler 2: kiểm tra xem có thể chuyển sang PENDING không.
     */
    List<Order> findByStatus(OrderStatus status);
}
