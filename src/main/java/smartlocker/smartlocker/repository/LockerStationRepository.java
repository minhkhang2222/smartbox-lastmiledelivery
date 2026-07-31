package smartlocker.smartlocker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartlocker.smartlocker.model.LockerStation;

import java.util.UUID;

@Repository
public interface LockerStationRepository extends JpaRepository<LockerStation, UUID> {
}
