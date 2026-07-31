package smartlocker.smartlocker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_station_registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "station_id"})
})
public class UserStationRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private LockerStation station;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // e.g., PENDING, ACTIVE, INACTIVE

    public UserStationRegistration() {
    }

    public UserStationRegistration(User user, LockerStation station) {
        this.user = user;
        this.station = station;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LockerStation getStation() {
        return station;
    }

    public void setStation(LockerStation station) {
        this.station = station;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
