package smartlocker.smartlocker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import org.springframework.stereotype.Repository;
import smartlocker.smartlocker.model.Locker;

import java.util.UUID;

@Repository
public interface LockerRepository extends JpaRepository<Locker, UUID> {
    java.util.List<Locker> findByStationId(UUID stationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Locker l WHERE l.id IN :ids")
    java.util.List<Locker> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);
}
