package smartlocker.smartlocker.dto;

import java.util.List;
import java.util.UUID;

public class CreateOrderRequest {
    private UUID userId;
    private UUID stationId;
    private List<UUID> lockerIds;
    private String recipientPhoneNumber;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(UUID userId, UUID stationId, List<UUID> lockerIds) {
        this.userId = userId;
        this.stationId = stationId;
        this.lockerIds = lockerIds;
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

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public void setRecipientPhoneNumber(String recipientPhoneNumber) {
        this.recipientPhoneNumber = recipientPhoneNumber;
    }
}
