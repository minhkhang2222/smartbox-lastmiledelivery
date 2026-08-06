package smartlocker.smartlocker.controller;

import org.springframework.web.bind.annotation.RestController;

import smartlocker.smartlocker.dto.FaceEnrollmentRequest;
import smartlocker.smartlocker.service.FaceAuthRegService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import org.springframework.http.HttpStatus;
import smartlocker.smartlocker.service.FaceRegistrationRateLimiter;

@RestController
@RequestMapping("/api/face")
public class FaceRegisterController {
    private final FaceAuthRegService faceAuthRegService;
    private final FaceRegistrationRateLimiter rateLimiter;

    public FaceRegisterController(FaceAuthRegService faceAuthRegService,
                                  FaceRegistrationRateLimiter rateLimiter) {
        this.faceAuthRegService = faceAuthRegService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerFace(
            @ModelAttribute FaceEnrollmentRequest request) {
        if (!rateLimiter.isAllowed(request.getUserId())) {
            long remaining = rateLimiter.getRemainingCooldownSeconds(request.getUserId());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Please wait " + remaining + " seconds before trying face registration again.");
        }

        try {
            faceAuthRegService.faceAuthRegister(request);
            return ResponseEntity.ok("Successfully registered face");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Fail to register face");
        }
    }
}

