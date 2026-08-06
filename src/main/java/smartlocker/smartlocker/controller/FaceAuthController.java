package smartlocker.smartlocker.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smartlocker.smartlocker.dto.FaceAuthRequest;
import smartlocker.smartlocker.dto.FaceAuthResult;
import smartlocker.smartlocker.dto.FaceIdentifyResult;
import smartlocker.smartlocker.service.FaceAuthException;
import smartlocker.smartlocker.service.FaceAuthenticationService;

@RestController
@RequestMapping("/api/face-auth")
@CrossOrigin(origins = "*")
public class FaceAuthController {
    private final FaceAuthenticationService faceAuthenticationService;

    public FaceAuthController(FaceAuthenticationService faceAuthenticationService) {
        this.faceAuthenticationService = faceAuthenticationService;
    }

    @PostMapping("/unlock")
    public ResponseEntity<FaceAuthResult> unlock(@RequestBody FaceAuthRequest request) {
        return ResponseEntity.ok(faceAuthenticationService.authenticateAndUnlock(request));
    }

    @PostMapping("/identify")
    public ResponseEntity<FaceIdentifyResult> identify(@RequestBody FaceAuthRequest request) {
        return ResponseEntity.ok(faceAuthenticationService.identify(request));
    }

    @ExceptionHandler(FaceAuthException.class)
    public ResponseEntity<Map<String, String>> handleFaceAuthException(FaceAuthException exception) {
        HttpStatus status = switch (exception.getReason()) {
            case INVALID_REQUEST, INVALID_DEVICE -> HttpStatus.BAD_REQUEST;
            case DEVICE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FACE_NOT_MATCHED -> HttpStatus.UNAUTHORIZED;
        };
        return ResponseEntity.status(status).body(Map.of(
                "error", exception.getReason().name(),
                "message", exception.getMessage()));
    }
}
