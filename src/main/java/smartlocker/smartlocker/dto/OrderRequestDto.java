package smartlocker.smartlocker.dto;

import java.util.List;
import java.util.UUID;

public class OrderRequestDto {
    private UUID userId;
    private UUID stationId;
    private UUID[] lockersIds;
    private List<UUID> lockerIds;

    public OrderRequestDto() {
    }

    public OrderRequestDto(UUID userId, UUID stationId, UUID[] lockersIds) {
        this.userId = userId;
        this.stationId = stationId;
        this.lockersIds = lockersIds;
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

    public UUID[] getLockersIds() {
        return lockersIds;
    }

    public void setLockersIds(UUID[] lockersIds) {
        this.lockersIds = lockersIds;
    }

    public List<UUID> getLockerIds() {
        return lockerIds;
    }

    public void setLockerIds(List<UUID> lockerIds) {
        this.lockerIds = lockerIds;
    }
}
