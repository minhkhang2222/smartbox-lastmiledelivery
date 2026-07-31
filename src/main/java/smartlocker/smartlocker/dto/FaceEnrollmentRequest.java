package smartlocker.smartlocker.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public class FaceEnrollmentRequest {
    private UUID userId;
    private MultipartFile midFace;
    private MultipartFile leftFace;
    private MultipartFile rightFace;
    private MultipartFile upFace;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public MultipartFile getMidFace() {
        return midFace;
    }

    public void setMidFace(MultipartFile midFace) {
        this.midFace = midFace;
    }

    public MultipartFile getLeftFace() {
        return leftFace;
    }

    public void setLeftFace(MultipartFile leftFace) {
        this.leftFace = leftFace;
    }

    public MultipartFile getRightFace() {
        return rightFace;
    }

    public void setRightFace(MultipartFile rightFace) {
        this.rightFace = rightFace;
    }

    public MultipartFile getUpFace() {
        return upFace;
    }

    public void setUpFace(MultipartFile upFace) {
        this.upFace = upFace;
    }
}
