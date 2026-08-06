package smartlocker.smartlocker.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import smartlocker.smartlocker.dto.LockerCommandPayload;
import smartlocker.smartlocker.dto.MqttCommandEnum;
import smartlocker.smartlocker.model.OrderLocker;
import smartlocker.smartlocker.model.OrderLockerStatus;
import smartlocker.smartlocker.model.Order;
import smartlocker.smartlocker.repository.OrderLockerRepository;
import tools.jackson.databind.ObjectMapper;

class MqttEventListenerTest {

    private OrderLockerRepository orderLockerRepository;
    private OrderOtpService orderOtpService;
    private MqttEventListener listener;

    @BeforeEach
    void setUp() {
        orderLockerRepository = mock(OrderLockerRepository.class);
        orderOtpService = mock(OrderOtpService.class);
        listener = new MqttEventListener(
                mock(MqttService.class),
                mock(ObjectMapper.class),
                orderLockerRepository,
                orderOtpService);
    }

    @Test
    void lockMovesMatchingLockerToWaitForCollection() {
        UUID stationId = UUID.randomUUID();
        OrderLocker orderLocker = new OrderLocker();
        orderLocker.setStatus(OrderLockerStatus.WAIT_FOR_DEPOSIT);
        when(orderLockerRepository.findWaitingForDepositByLockerCodeAndStation("A01", stationId))
                .thenReturn(Optional.of(orderLocker));

        listener.handleLock(stationId, new LockerCommandPayload(MqttCommandEnum.LOCK, "A01", 1000L));

        verify(orderLockerRepository).save(orderLocker);
        assertEquals(OrderLockerStatus.WAIT_FOR_COLLECTION, orderLocker.getStatus());
    }

    @Test
    void orphanLockIsAcceptedButDoesNotUpdateAnyOrder() {
        UUID stationId = UUID.randomUUID();
        when(orderLockerRepository.findWaitingForDepositByLockerCodeAndStation("A01", stationId))
                .thenReturn(Optional.empty());

        listener.handleLock(stationId, new LockerCommandPayload(MqttCommandEnum.LOCK, "A01", 1000L));

        verify(orderLockerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void finalDepositedLockerCreatesOrderOtp() {
        UUID stationId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        OrderLocker orderLocker = new OrderLocker();
        orderLocker.setOrder(order);
        orderLocker.setStatus(OrderLockerStatus.WAIT_FOR_DEPOSIT);
        when(orderLockerRepository.findWaitingForDepositByLockerCodeAndStation("A01", stationId))
                .thenReturn(Optional.of(orderLocker));
        when(orderLockerRepository.countWaitingForDeposit(orderId)).thenReturn(0L);

        listener.handleLock(stationId, new LockerCommandPayload(MqttCommandEnum.LOCK, "A01", 1000L));

        verify(orderOtpService).createOtp(orderId);
    }

    @Test
    void lockPayloadFromEventTopicIsParsedAndRouted() {
        UUID stationId = UUID.randomUUID();
        OrderLocker orderLocker = new OrderLocker();
        orderLocker.setStatus(OrderLockerStatus.WAIT_FOR_DEPOSIT);
        when(orderLockerRepository.findWaitingForDepositByLockerCodeAndStation("A01", stationId))
                .thenReturn(Optional.of(orderLocker));
        listener = new MqttEventListener(
                mock(MqttService.class),
                new ObjectMapper(),
                orderLockerRepository,
                mock(OrderOtpService.class));

        listener.handleEvent(
                "smartlocker/" + stationId + "/" + UUID.randomUUID() + "/event",
                "{\"commandType\":\"LOCK\",\"command\":\"LOCK\",\"type\":\"LOCK\","
                        + "\"lockerId\":\"A01\",\"durationMs\":1000}");

        verify(orderLockerRepository).save(orderLocker);
        assertEquals(OrderLockerStatus.WAIT_FOR_COLLECTION, orderLocker.getStatus());
    }
}
