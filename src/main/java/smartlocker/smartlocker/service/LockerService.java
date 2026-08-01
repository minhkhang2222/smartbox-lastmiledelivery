package smartlocker.smartlocker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smartlocker.smartlocker.dto.LockerResponseDto;
import smartlocker.smartlocker.model.Locker;
import smartlocker.smartlocker.model.OrderLocker;
import smartlocker.smartlocker.model.OrderLockerStatus;
import smartlocker.smartlocker.repository.LockerRepository;
import smartlocker.smartlocker.repository.OrderLockerRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LockerService {

    private final LockerRepository lockerRepository;
    private final OrderLockerRepository orderLockerRepository;

    @Autowired
    public LockerService(LockerRepository lockerRepository, OrderLockerRepository orderLockerRepository) {
        this.lockerRepository = lockerRepository;
        this.orderLockerRepository = orderLockerRepository;
    }

    public List<Locker> getAllLockers() {
        return lockerRepository.findAll();
    }

    public List<Locker> getLockersByStationId(UUID stationId) {
        return lockerRepository.findByStationId(stationId);
    }

    public List<LockerResponseDto> getLockerDtosByStationId(UUID stationId) {
        List<Locker> lockers = lockerRepository.findByStationId(stationId);
        List<OrderLockerStatus> activeStatuses = Arrays.asList(
                OrderLockerStatus.WAIT_FOR_DEPOSIT,
                OrderLockerStatus.WAIT_FOR_COLLECTION);

        return lockers.stream().map(locker -> {
            Optional<OrderLocker> activeOrderOpt = orderLockerRepository
                    .findFirstByLockerIdAndStatusInOrderByCreatedAtDesc(locker.getId(), activeStatuses);
            
            String computedStatus = "FREE";
            if (activeOrderOpt.isPresent()) {
                OrderLocker ol = activeOrderOpt.get();
                if (ol.getOrder() != null && ol.getOrder().getStatus() != null) {
                    computedStatus = ol.getOrder().getStatus().name();
                } else {
                    computedStatus = ol.getStatus().name();
                }
            }
            return LockerResponseDto.fromEntity(locker, computedStatus);
        }).collect(Collectors.toList());
    }

    public Optional<Locker> getLockerById(UUID id) {
        return lockerRepository.findById(id);
    }

    public Locker createLocker(Locker locker) {
        return lockerRepository.save(locker);
    }

    public void deleteLocker(UUID id) {
        lockerRepository.deleteById(id);
    }
}
