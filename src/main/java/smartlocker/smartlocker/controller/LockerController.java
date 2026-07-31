package smartlocker.smartlocker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartlocker.smartlocker.dto.LockerResponseDto;
import smartlocker.smartlocker.model.Locker;
import smartlocker.smartlocker.service.LockerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lockers")
@CrossOrigin(origins = "*")
public class LockerController {

    private final LockerService lockerService;

    public LockerController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    @GetMapping
    public List<Locker> getAllLockers() {
        return lockerService.getAllLockers();
    }

    @GetMapping("/station/{stationId}")
    public List<LockerResponseDto> getLockersByStationId(@PathVariable UUID stationId) {
        return lockerService.getLockerDtosByStationId(stationId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Locker> getLockerById(@PathVariable UUID id) {
        return lockerService.getLockerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Locker createLocker(@RequestBody Locker locker) {
        return lockerService.createLocker(locker);
    }
}
