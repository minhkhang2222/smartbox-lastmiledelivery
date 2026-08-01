package smartlocker.smartlocker.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import smartlocker.smartlocker.model.Order;
import smartlocker.smartlocker.model.OrderLocker;
import smartlocker.smartlocker.model.OrderLockerStatus;
import smartlocker.smartlocker.model.OrderStatus;
import smartlocker.smartlocker.repository.OrderLockerRepository;
import smartlocker.smartlocker.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler 1: Timeout các OrderLocker còn WAIT_FOR_DEPOSIT quá 30 giây → INACTIVE
 *
 * Chạy mỗi 30 giây. Nếu createdAt của OrderLocker < (now - 30s) và status vẫn là
 * WAIT_FOR_DEPOSIT → người dùng không bỏ đồ vào, chuyển thành INACTIVE.
 */
@Component
public class OrderLockerTimeoutScheduler {

    private static final int TIMEOUT_SECONDS = 30;

    private final OrderLockerRepository orderLockerRepository;

    public OrderLockerTimeoutScheduler(OrderLockerRepository orderLockerRepository) {
        this.orderLockerRepository = orderLockerRepository;
    }

    @Scheduled(fixedDelay = 30_000) // chạy 30s sau khi lần trước hoàn thành
    @Transactional
    public void timeoutWaitingOrderLockers() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(TIMEOUT_SECONDS);

        List<OrderLocker> timedOut = orderLockerRepository.findTimedOutWaitingForDeposit(cutoffTime);

        if (timedOut.isEmpty()) {
            return;
        }

        System.out.println("[Scheduler1] Found " + timedOut.size() + " OrderLocker(s) timed out, setting to INACTIVE.");

        for (OrderLocker ol : timedOut) {
            ol.setStatus(OrderLockerStatus.INACTIVE);
            System.out.println("[Scheduler1] OrderLocker " + ol.getId()
                    + " (locker=" + ol.getLocker().getLockerCode() + ") → INACTIVE (timeout)");
        }

        orderLockerRepository.saveAll(timedOut);
    }
}
