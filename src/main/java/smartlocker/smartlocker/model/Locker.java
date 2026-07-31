package smartlocker.smartlocker.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "lockers")
public class Locker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "locker_code", nullable = false)
    private String lockerCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private LockerStation station;

    public Locker() {
    }

    public Locker(UUID id, String lockerCode, Device device, LockerStation station) {
        this.id = id;
        this.lockerCode = lockerCode;
        this.device = device;
        this.station = station;
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

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public LockerStation getStation() {
        return station;
    }

    public void setStation(LockerStation station) {
        this.station = station;
    }
}
