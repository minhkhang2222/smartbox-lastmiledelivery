package smartlocker.smartlocker.dto;

import java.util.UUID;

public record AdminUserDto(
        UUID id,
        String fullName,
        String email,
        String phoneNumber,
        String role
) {}
