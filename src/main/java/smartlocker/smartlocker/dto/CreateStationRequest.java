package smartlocker.smartlocker.dto;

public record CreateStationRequest(
        String name,
        String address,
        String status
) {}
