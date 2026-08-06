package smartlocker.smartlocker.dto;

import java.util.List;
import java.util.UUID;

public class CreateOrderRequest {
    private UUID stationId;
    private List<UUID> lockerIds;
    private String recipientPhoneNumber;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(UUID stationId, List<UUID> lockerIds, String recipientPhoneNumber) {
        this.stationId = stationId;
        this.lockerIds = lockerIds;
        this.recipientPhoneNumber = recipientPhoneNumber;
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
