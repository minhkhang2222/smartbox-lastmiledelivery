package smartlocker.smartlocker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartlocker.smartlocker.model.LockerStation;
import smartlocker.smartlocker.repository.LockerStationRepository;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*")
public class StationController {

    private final LockerStationRepository stationRepository;

    public StationController(LockerStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public ResponseEntity<List<LockerStation>> getAllStations() {
        return ResponseEntity.ok(stationRepository.findAll());
    }
}
