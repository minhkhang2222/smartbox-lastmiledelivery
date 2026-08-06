package smartlocker.smartlocker.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private String role = "USER";

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFaceEmbedding> faceEmbeddings = new ArrayList<>();

    @OneToMany(mappedBy = "recipientUser", cascade = CascadeType.ALL)
    private List<Order> receivedOrders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStationRegistration> registrations = new ArrayList<>();

    public User() {
    }

    public User(UUID id, String fullName, String email, String phoneNumber, String passwordHash) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<UserFaceEmbedding> getFaceEmbeddings() {
        return faceEmbeddings;
    }

    public void setFaceEmbeddings(List<UserFaceEmbedding> faceEmbeddings) {
        this.faceEmbeddings = faceEmbeddings;
    }

    public List<Order> getReceivedOrders() {
        return receivedOrders;
    }

    public void setReceivedOrders(List<Order> receivedOrders) {
        this.receivedOrders = receivedOrders;
    }

    public List<UserStationRegistration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<UserStationRegistration> registrations) {
        this.registrations = registrations;
    }
}
