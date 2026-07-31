package smartlocker.smartlocker.controller;

import org.springframework.web.bind.annotation.RestController;

import smartlocker.smartlocker.dto.FaceEnrollmentRequest;
import smartlocker.smartlocker.service.FaceAuthRegService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/face")
public class FaceRegisterController {
    private final FaceAuthRegService faceAuthRegService;

    public FaceRegisterController(FaceAuthRegService faceAuthRegService) {
        this.faceAuthRegService = faceAuthRegService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerFace(
            @ModelAttribute FaceEnrollmentRequest request) {
        try {
            faceAuthRegService.faceAuthRegister(request);
            return ResponseEntity.ok("Successfully registered face");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Fail to register face");
        }
    }
}
