package smartlocker.smartlocker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartlocker.smartlocker.model.Locker;

import java.util.UUID;

@Repository
public interface LockerRepository extends JpaRepository<Locker, UUID> {
    java.util.List<Locker> findByStationId(UUID stationId);
}
