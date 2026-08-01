package smartlocker.smartlocker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartlocker.smartlocker.model.OrderLocker;
import smartlocker.smartlocker.model.OrderLockerStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderLockerRepository extends JpaRepository<OrderLocker, UUID> {

    List<OrderLocker> findByOrderId(UUID orderId);

    Optional<OrderLocker> findFirstByLockerIdAndStatusInOrderByCreatedAtDesc(
            UUID lockerId,
            List<OrderLockerStatus> statuses
    );

    boolean existsByLockerIdAndStatusIn(UUID lockerId, List<OrderLockerStatus> statuses);

    /**
     * Tìm OrderLocker đang WAIT_FOR_DEPOSIT theo lockerCode và stationId.
     * Dùng khi nhận event door_closed từ MQTT.
     */
    @Query("""
        SELECT ol FROM OrderLocker ol
        JOIN ol.locker l
        JOIN l.station s
        WHERE l.lockerCode = :lockerCode
          AND s.id = :stationId
          AND ol.status = smartlocker.smartlocker.model.OrderLockerStatus.WAIT_FOR_DEPOSIT
        ORDER BY ol.createdAt DESC
        LIMIT 1
    """)
    Optional<OrderLocker> findWaitingForDepositByLockerCodeAndStation(
            @Param("lockerCode") String lockerCode,
            @Param("stationId") UUID stationId
    );

    /**
     * Tìm tất cả OrderLocker đang WAIT_FOR_DEPOSIT đã quá thời gian timeout.
     * Dùng cho Scheduler 1: timeout sau 30 giây.
     */
    @Query("""
        SELECT ol FROM OrderLocker ol
        WHERE ol.status = smartlocker.smartlocker.model.OrderLockerStatus.WAIT_FOR_DEPOSIT
          AND ol.createdAt < :cutoffTime
    """)
    List<OrderLocker> findTimedOutWaitingForDeposit(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Đếm các ngăn vẫn đang chờ người gửi bỏ đồ.
     */
    @Query("""
        SELECT COUNT(ol) FROM OrderLocker ol
        WHERE ol.order.id = :orderId
          AND ol.status = smartlocker.smartlocker.model.OrderLockerStatus.WAIT_FOR_DEPOSIT
    """)
    long countWaitingForDeposit(@Param("orderId") UUID orderId);
}
