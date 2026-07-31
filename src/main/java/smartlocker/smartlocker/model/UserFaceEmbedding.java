package smartlocker.smartlocker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_face_embeddings")
public class UserFaceEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 512)
    @Column(name = "embedding", nullable = false)
    private float[] embedding;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "face_angle", nullable = false)
    private String faceAngle;// top,bot,left,right,mid

    public UserFaceEmbedding() {
    }

    public UserFaceEmbedding(UUID id, User user, float[] embedding, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.embedding = embedding;
        this.createdAt = createdAt;
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

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFaceAngle() {
        return faceAngle;
    }

    public void setFaceAngle(String faceAngle) {
        this.faceAngle = faceAngle;
    }
}
