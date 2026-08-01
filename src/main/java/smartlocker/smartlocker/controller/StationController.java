package smartlocker.smartlocker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartlocker.smartlocker.dto.StationResponseDto;
import smartlocker.smartlocker.repository.LockerStationRepository;
import smartlocker.smartlocker.repository.UserStationRegistrationRepository;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*")
public class StationController {

    private final LockerStationRepository stationRepository;
    private final UserStationRegistrationRepository registrationRepository;

    public StationController(LockerStationRepository stationRepository,
            UserStationRegistrationRepository registrationRepository) {
        this.stationRepository = stationRepository;
        this.registrationRepository = registrationRepository;
    }

    @GetMapping
    public ResponseEntity<List<StationResponseDto>> getAllStations() {
        List<StationResponseDto> stations = stationRepository.findAll().stream()
                .map(StationResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/registered/{userId}")
    public ResponseEntity<List<StationResponseDto>> getRegisteredStations(@PathVariable java.util.UUID userId) {
        List<StationResponseDto> stations = registrationRepository
                .findAllByUserIdAndStatusOrderByRegisteredAtDesc(userId, "ACTIVE")
                .stream()
                .map(registration -> StationResponseDto.fromEntity(registration.getStation()))
                .toList();
        return ResponseEntity.ok(stations);
    }
}
