package smartlocker.smartlocker.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "locker_stations")
public class LockerStation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "status", nullable = false)
    private String status; // e.g., ACTIVE, INACTIVE, UNDER_MAINTENANCE

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL)
    private List<Device> devices = new ArrayList<>();

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL)
    private List<Locker> lockers = new ArrayList<>();

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStationRegistration> registrations = new ArrayList<>();

    public LockerStation() {
    }

    public LockerStation(UUID id, String name, String address, String status) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public List<Locker> getLockers() {
        return lockers;
    }

    public void setLockers(List<Locker> lockers) {
        this.lockers = lockers;
    }

    public List<UserStationRegistration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<UserStationRegistration> registrations) {
        this.registrations = registrations;
    }
}
