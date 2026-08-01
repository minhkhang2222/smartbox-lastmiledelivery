package smartlocker.smartlocker.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;
import smartlocker.smartlocker.dto.LockerCommandPayload;

@Service
public class MqttCommandPublisher {

    private final MqttService mqttService;
    private final ObjectMapper mapper;

    public MqttCommandPublisher(MqttService mqttService, ObjectMapper mapper) {
        this.mqttService = mqttService;
        this.mapper = mapper;
    }

    public void publishCommand(UUID deviceId, UUID stationId, LockerCommandPayload payload) {
        try {
            mqttService.publish("smartlocker/" + stationId + "/" + deviceId + "/command",
                    mapper.writeValueAsString(payload));
        } catch (Exception e) {
            System.err.println("Error serializing MQTT payload: " + e.getMessage());
        }
    }

    public void publishLockCommand(UUID deviceId, UUID stationId, LockerCommandPayload payload) {
        publishCommand(deviceId, stationId, payload);
    }

    public void publishUnlockCommand(UUID deviceId, UUID stationId, LockerCommandPayload payload) {
        publishCommand(deviceId, stationId, payload);
    }

    public void publishWaitForDepositCommand(UUID deviceId, UUID stationId, LockerCommandPayload payload) {
        publishCommand(deviceId, stationId, payload);
    }
}
