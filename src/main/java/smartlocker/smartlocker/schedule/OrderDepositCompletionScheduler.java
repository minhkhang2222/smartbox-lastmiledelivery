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
 *  - Nếu TẤT CẢ OrderLocker đều là PENDING → chuyển Order sang PENDING (người dùng đã bỏ đồ hết)
 *  - Nếu có bất kỳ OrderLocker nào vẫn chưa là PENDING/FAILED/INACTIVE → bỏ qua, chờ lần sau
 *
 * Lưu ý: OrderLocker bị FAILED sẽ được Scheduler 1 xử lý. Scheduler này chỉ check
 * khi tất cả đã ở trạng thái final (PENDING, FAILED, INACTIVE).
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
            long nonFinalCount = orderLockerRepository.countNonFinalOrderLockers(order.getId());

            if (nonFinalCount == 0) {
                // Tất cả OrderLocker đã ở trạng thái cuối (PENDING hoặc FAILED)
                // Kiểm tra có ít nhất 1 PENDING không (tức có đồ được bỏ vào)
                boolean hasAnyPending = order.getOrderLockers().stream()
                        .anyMatch(ol -> ol.getStatus() == OrderLockerStatus.PENDING);

                if (hasAnyPending) {
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
