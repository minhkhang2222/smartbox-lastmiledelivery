package smartlocker.smartlocker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private LockerStation station;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.WAITING_FOR_DEPOSIT;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLocker> orderLockers = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderOtp> otps = new ArrayList<>();

    public Order() {
    }

    public Order(UUID id, User user, LockerStation station, OrderStatus status, LocalDateTime createdAt, LocalDateTime expiredAt) {
        this.id = id;
        this.user = user;
        this.station = station;
        this.status = status;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public List<OrderLocker> getOrderLockers() {
        return orderLockers;
    }

    public void setOrderLockers(List<OrderLocker> orderLockers) {
        this.orderLockers = orderLockers;
    }

    public List<OrderOtp> getOtps() {
        return otps;
    }

    public void setOtps(List<OrderOtp> otps) {
        this.otps = otps;
    }
}
