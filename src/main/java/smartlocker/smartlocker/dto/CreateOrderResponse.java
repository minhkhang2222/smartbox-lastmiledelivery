package smartlocker.smartlocker.dto;

import smartlocker.smartlocker.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CreateOrderResponse {
    private UUID orderId;
    private UUID userId;
    private UUID stationId;
    private List<UUID> lockerIds;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    public CreateOrderResponse() {
    }

    public CreateOrderResponse(UUID orderId, UUID userId, UUID stationId, List<UUID> lockerIds, OrderStatus status, LocalDateTime createdAt, LocalDateTime expiredAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.stationId = stationId;
        this.lockerIds = lockerIds;
        this.status = status;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getStationId() {
        return stationId;
    }

    public void setStationId(UUID stationId) {
        this.stationId = stationId;
    }

    public List<UUID> getLockerIds() {
        return lockerIds;
    }

    public void setLockerIds(List<UUID> lockerIds) {
        this.lockerIds = lockerIds;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }
}
