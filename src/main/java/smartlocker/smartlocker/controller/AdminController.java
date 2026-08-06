package smartlocker.smartlocker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import smartlocker.smartlocker.dto.*;
import smartlocker.smartlocker.model.LockerStation;
import smartlocker.smartlocker.model.User;
import smartlocker.smartlocker.repository.LockerStationRepository;
import smartlocker.smartlocker.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin-only REST controller for managing users and locker stations.
 *
 * Authorization note: The frontend sends the JWT and checks role === 'ADMIN'.
 * For full server-side enforcement, wire Spring Security to restrict /api/admin/**
 * to tokens carrying role=ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final LockerStationRepository stationRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminController(UserRepository userRepository,
                           LockerStationRepository stationRepository) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
    }

    // ─── USERS ───────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> listUsers() {
        List<AdminUserDto> users = userRepository.findAll().stream()
                .map(u -> new AdminUserDto(u.getId(), u.getFullName(), u.getEmail(),
                        u.getPhoneNumber(), u.getRole()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
        if (req.fullName() == null || req.fullName().isBlank()
                || req.phoneNumber() == null || req.phoneNumber().isBlank()
                || req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "fullName, phoneNumber and password are required."));
        }
        if (userRepository.findByPhoneNumber(req.phoneNumber().trim()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Phone number already in use."));
        }
        if (req.email() != null && !req.email().isBlank()
                && userRepository.findByEmail(req.email().trim().toLowerCase()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already in use."));
        }

        User user = new User();
        user.setFullName(req.fullName().trim());
        user.setPhoneNumber(req.phoneNumber().trim());
        user.setEmail(req.email() != null && !req.email().isBlank()
                ? req.email().trim().toLowerCase() : null);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(req.role() != null && !req.role().isBlank() ? req.role().toUpperCase() : "USER");

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AdminUserDto(saved.getId(), saved.getFullName(), saved.getEmail(),
                        saved.getPhoneNumber(), saved.getRole()));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable UUID userId,
                                        @RequestBody CreateUserRequest req) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        if (req.fullName() != null && !req.fullName().isBlank()) user.setFullName(req.fullName().trim());
        if (req.phoneNumber() != null && !req.phoneNumber().isBlank()) {
            userRepository.findByPhoneNumber(req.phoneNumber().trim())
                    .filter(u -> !u.getId().equals(userId))
                    .ifPresent(u -> { throw new IllegalArgumentException("Phone number already in use."); });
            user.setPhoneNumber(req.phoneNumber().trim());
        }
        if (req.email() != null && !req.email().isBlank()) {
            userRepository.findByEmail(req.email().trim().toLowerCase())
                    .filter(u -> !u.getId().equals(userId))
                    .ifPresent(u -> { throw new IllegalArgumentException("Email already in use."); });
            user.setEmail(req.email().trim().toLowerCase());
        }
        if (req.password() != null && !req.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        if (req.role() != null && !req.role().isBlank()) {
            user.setRole(req.role().toUpperCase());
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(new AdminUserDto(saved.getId(), saved.getFullName(), saved.getEmail(),
                saved.getPhoneNumber(), saved.getRole()));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        if (userRepository.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        userRepository.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    // ─── STATIONS ────────────────────────────────────────────────────────────

    @GetMapping("/stations")
    public ResponseEntity<List<StationResponseDto>> listStations() {
        return ResponseEntity.ok(stationRepository.findAll().stream()
                .map(StationResponseDto::fromEntity)
                .collect(Collectors.toList()));
    }

    @PostMapping("/stations")
    public ResponseEntity<?> createStation(@RequestBody CreateStationRequest req) {
        if (req.name() == null || req.name().isBlank()
                || req.address() == null || req.address().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "name and address are required."));
        }
        LockerStation station = new LockerStation();
        station.setName(req.name().trim());
        station.setAddress(req.address().trim());
        station.setStatus(req.status() != null ? req.status().toUpperCase() : "ACTIVE");
        LockerStation saved = stationRepository.save(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(StationResponseDto.fromEntity(saved));
    }

    @PutMapping("/stations/{stationId}")
    public ResponseEntity<?> updateStation(@PathVariable UUID stationId,
                                           @RequestBody CreateStationRequest req) {
        LockerStation station = stationRepository.findById(stationId).orElse(null);
        if (station == null) return ResponseEntity.notFound().build();

        if (req.name() != null && !req.name().isBlank()) station.setName(req.name().trim());
        if (req.address() != null && !req.address().isBlank()) station.setAddress(req.address().trim());
        if (req.status() != null && !req.status().isBlank()) station.setStatus(req.status().toUpperCase());

        return ResponseEntity.ok(StationResponseDto.fromEntity(stationRepository.save(station)));
    }

    @DeleteMapping("/stations/{stationId}")
    public ResponseEntity<?> deleteStation(@PathVariable UUID stationId) {
        if (stationRepository.findById(stationId).isEmpty()) return ResponseEntity.notFound().build();
        stationRepository.deleteById(stationId);
        return ResponseEntity.noContent().build();
    }

    // ─── Exception handler for duplicate checks ───────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
