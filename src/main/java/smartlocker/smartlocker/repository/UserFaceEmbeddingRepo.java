package smartlocker.smartlocker.repository;

import org.springframework.stereotype.Repository;

import smartlocker.smartlocker.dto.FaceMatchDto;
import smartlocker.smartlocker.model.UserFaceEmbedding;

import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFaceEmbeddingRepo extends JpaRepository<UserFaceEmbedding, UUID> {

        Optional<UserFaceEmbedding> findByUser(smartlocker.smartlocker.model.User user);

        Optional<UserFaceEmbedding> findByUserId(UUID userId);

        @Query(value = "SELECT * FROM user_face_embeddings ORDER BY embedding <=> CAST(:embedding AS vector) LIMIT :limit", nativeQuery = true)
        List<UserFaceEmbedding> findNearestEmbeddings(@Param("embedding") float[] embedding, @Param("limit") int limit);

        @Query(value = """
                        SELECT
                            e.id AS "embeddingId",
                            e.user_id AS "userId",
                            e.embedding <=> CAST(:embedding AS vector) AS distance
                        FROM user_face_embeddings e
                        JOIN user_station_registrations r
                            ON r.user_id = e.user_id
                        JOIN devices d
                            ON d.station_id = r.station_id
                        WHERE d.device_code = :deviceCode
                          AND r.status = 'ACTIVE'
                        ORDER BY distance ASC
                        LIMIT 1
                        """, nativeQuery = true)
        Optional<FaceMatchDto> findBestMatchDistance(
                        @Param("deviceCode") UUID deviceCode,
                        @Param("embedding") float[] embedding);

}
