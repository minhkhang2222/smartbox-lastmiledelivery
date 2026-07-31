package smartlocker.smartlocker.dto;

import smartlocker.smartlocker.model.Locker;

import java.util.UUID;

public class LockerResponseDto {

    private UUID id;
    private String lockerCode;
    private String status;
    private UUID stationId;
    private UUID deviceId;

    public LockerResponseDto() {
    }

    public LockerResponseDto(UUID id, String lockerCode, String status, UUID stationId, UUID deviceId) {
        this.id = id;
        this.lockerCode = lockerCode;
        this.status = status;
        this.stationId = stationId;
        this.deviceId = deviceId;
    }

    public static LockerResponseDto fromEntity(Locker locker, String status) {
        if (locker == null) return null;
        UUID stationId = (locker.getStation() != null) ? locker.getStation().getId() : null;
        UUID deviceId = (locker.getDevice() != null) ? locker.getDevice().getId() : null;
        return new LockerResponseDto(
                locker.getId(),
                locker.getLockerCode(),
                status != null ? status : "FREE",
                stationId,
                deviceId
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLockerCode() {
        return lockerCode;
    }

    public void setLockerCode(String lockerCode) {
        this.lockerCode = lockerCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getStationId() {
        return stationId;
    }

    public void setStationId(UUID stationId) {
        this.stationId = stationId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }
}
