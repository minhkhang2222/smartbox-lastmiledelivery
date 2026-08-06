package smartlocker.smartlocker.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smartlocker.smartlocker.dto.FaceAuthResult;
import smartlocker.smartlocker.dto.PickupOtpRequest;
import smartlocker.smartlocker.service.PickupOtpService;

@RestController
@RequestMapping("/api/pickup")
@CrossOrigin(origins = "*")
public class PickupController {
    private final PickupOtpService pickupOtpService;

    public PickupController(PickupOtpService pickupOtpService) {
        this.pickupOtpService = pickupOtpService;
    }

    @PostMapping("/otp")
    public ResponseEntity<?> pickupWithOtp(@RequestBody PickupOtpRequest request) {
        try {
            FaceAuthResult result = pickupOtpService.verifyAndUnlock(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
