package smartlocker.smartlocker.dto;

import java.util.List;
import java.util.UUID;

public class CreateOrderRequest {
    private UUID userId;
    private UUID stationId;
    private List<UUID> lockerIds;

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
}
