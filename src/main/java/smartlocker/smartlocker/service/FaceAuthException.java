package smartlocker.smartlocker.service;

public class FaceAuthException extends RuntimeException {
    public enum Reason {
        INVALID_REQUEST,
        DEVICE_NOT_FOUND,
        INVALID_DEVICE,
        FACE_NOT_MATCHED
    }

    private final Reason reason;

    public FaceAuthException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
