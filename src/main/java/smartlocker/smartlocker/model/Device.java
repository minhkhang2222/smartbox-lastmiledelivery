package smartlocker.smartlocker.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_code", unique = true, nullable = false)
    private String deviceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private LockerStation station;

    @Column(name = "status", nullable = false)
    private String status; // e.g., ONLINE, OFFLINE, ERROR

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    private List<Locker> lockers = new ArrayList<>();

    public Device() {
    }

    public Device(UUID id, String deviceCode, LockerStation station, String status) {
        this.id = id;
        this.deviceCode = deviceCode;
        this.station = station;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public LockerStation getStation() {
        return station;
    }

    public void setStation(LockerStation station) {
        this.station = station;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Locker> getLockers() {
        return lockers;
    }

    public void setLockers(List<Locker> lockers) {
        this.lockers = lockers;
    }
}
