package smartlocker.smartlocker.dto;

public record UpdateUserProfileRequest(
        String fullName,
        String email,
        String phoneNumber) {
}
