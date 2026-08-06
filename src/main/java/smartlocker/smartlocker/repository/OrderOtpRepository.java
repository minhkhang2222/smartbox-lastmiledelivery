package smartlocker.smartlocker.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smartlocker.smartlocker.model.OrderOtp;

@Repository
public interface OrderOtpRepository extends JpaRepository<OrderOtp, UUID> {
    Optional<OrderOtp> findTopByOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(String otpCode);

    boolean existsByOtpCodeAndIsUsedFalse(String otpCode);

    Optional<OrderOtp> findTopByOrderIdAndIsUsedFalseAndExpiredAtAfterOrderByCreatedAtDesc(
            UUID orderId, LocalDateTime now);
}
