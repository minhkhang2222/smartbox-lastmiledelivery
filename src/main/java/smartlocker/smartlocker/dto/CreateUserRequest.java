package smartlocker.smartlocker.dto;

import java.util.UUID;

public record CreateUserRequest(
        String fullName,
        String email,
        String phoneNumber,
        String password,
        String role
) {}
