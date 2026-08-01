package smartlocker.smartlocker.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import smartlocker.smartlocker.model.Order;
import smartlocker.smartlocker.model.OrderLockerStatus;
import smartlocker.smartlocker.model.OrderStatus;
import smartlocker.smartlocker.repository.OrderLockerRepository;
import smartlocker.smartlocker.repository.OrderRepository;

import java.util.List;

/**
 * Scheduler 2: Kiểm tra các Order đang WAITING_FOR_DEPOSIT.
 *
 * Chạy mỗi 15 giây. Với mỗi order đang WAITING_FOR_DEPOSIT:
 *  - Nếu không còn OrderLocker WAIT_FOR_DEPOSIT và có ít nhất một WAIT_FOR_COLLECTION
 *    → chuyển Order sang PENDING
 *  - Nếu còn WAIT_FOR_DEPOSIT → bỏ qua, chờ lần sau
 *
 * Nếu tất cả OrderLocker đều INACTIVE do timeout thì Order chuyển sang FAILED.
 */
@Component
public class OrderDepositCompletionScheduler {

    private final OrderRepository orderRepository;
    private final OrderLockerRepository orderLockerRepository;

    public OrderDepositCompletionScheduler(OrderRepository orderRepository,
                                            OrderLockerRepository orderLockerRepository) {
        this.orderRepository = orderRepository;
        this.orderLockerRepository = orderLockerRepository;
    }

    @Scheduled(fixedDelay = 15_000) // chạy 15s sau khi lần trước hoàn thành
    @Transactional
    public void checkDepositCompletion() {
        List<Order> waitingOrders = orderRepository.findByStatus(OrderStatus.WAITING_FOR_DEPOSIT);

        if (waitingOrders.isEmpty()) {
            return;
        }

        System.out.println("[Scheduler2] Checking " + waitingOrders.size()
                + " order(s) in WAITING_FOR_DEPOSIT...");

        for (Order order : waitingOrders) {
            long waitingForDepositCount = orderLockerRepository.countWaitingForDeposit(order.getId());

            if (waitingForDepositCount == 0) {
                // Không còn ngăn chờ gửi. Nếu có ít nhất một ngăn chờ nhận thì order
                // đã nhận được đồ; nếu tất cả INACTIVE thì toàn bộ đã timeout.
                boolean hasAnyWaitingForCollection = order.getOrderLockers().stream()
                        .anyMatch(ol -> ol.getStatus() == OrderLockerStatus.WAIT_FOR_COLLECTION);

                if (hasAnyWaitingForCollection) {
                    order.setStatus(OrderStatus.PENDING);
                    orderRepository.save(order);
                    System.out.println("[Scheduler2] Order " + order.getId()
                            + " → PENDING (all lockers deposited or failed)");
                } else {
                    // Tất cả FAILED → order thất bại hoàn toàn
                    order.setStatus(OrderStatus.FAILED);
                    orderRepository.save(order);
                    System.out.println("[Scheduler2] Order " + order.getId()
                            + " → FAILED (all lockers timed out, no deposit)");
                }
            }
        }
    }
}
