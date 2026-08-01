package smartlocker.smartlocker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import smartlocker.smartlocker.model.UserStationRegistration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserStationRegistrationRepository extends JpaRepository<UserStationRegistration, UUID> {
    Optional<UserStationRegistration> findByUserIdAndStationIdAndStatus(UUID userId, UUID stationId, String status);

    boolean existsByUserIdAndStationIdAndStatus(UUID userId, UUID stationId, String status);

    @EntityGraph(attributePaths = "station")
    List<UserStationRegistration> findAllByUserIdAndStatusOrderByRegisteredAtDesc(UUID userId, String status);
}
