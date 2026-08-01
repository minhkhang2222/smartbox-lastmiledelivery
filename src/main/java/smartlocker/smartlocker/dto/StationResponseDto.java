package smartlocker.smartlocker.dto;

import java.util.UUID;
import smartlocker.smartlocker.model.LockerStation;

public record StationResponseDto(UUID id, String name, String address, String status) {
    public static StationResponseDto fromEntity(LockerStation station) {
        return new StationResponseDto(
                station.getId(),
                station.getName(),
                station.getAddress(),
                station.getStatus());
    }
}
