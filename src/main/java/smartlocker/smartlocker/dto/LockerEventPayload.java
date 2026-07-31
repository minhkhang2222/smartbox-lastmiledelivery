package smartlocker.smartlocker.dto;

import java.time.Instant;

import smartlocker.smartlocker.ENUM.LockerEventType;

public class LockerEventPayload {
    private String eventId;
    private String deviceId;
    private String stationId;
    private String lockerCode;  // e.g., "A01"
    private LockerEventType eventType;
    private Instant timestamp;

    public LockerEventPayload() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getLockerCode() {
        return lockerCode;
    }

    public void setLockerCode(String lockerCode) {
        this.lockerCode = lockerCode;
    }

    public LockerEventType getEventType() {
        return eventType;
    }

    public void setEventType(LockerEventType eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}