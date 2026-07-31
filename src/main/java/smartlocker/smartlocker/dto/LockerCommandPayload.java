package smartlocker.smartlocker.dto;

public class LockerCommandPayload {

    private MqttCommandEnum commandType; // UNLOCK, LOCK, WAIT_FOR_DEPOSIT
    private String lockerId; // e.g., LOCKER_05
    private Long durationMs;

    public LockerCommandPayload() {
    }

    public LockerCommandPayload(MqttCommandEnum commandType, String lockerId) {
        this.commandType = commandType;
        this.lockerId = lockerId;
        this.durationMs = 1000L;
    }

    public LockerCommandPayload(MqttCommandEnum commandType, String lockerId, Long durationMs) {
        this.commandType = commandType;
        this.lockerId = lockerId;
        this.durationMs = durationMs;
    }

    public LockerCommandPayload(String commandType, String lockerId, Long durationMs) {
        this.commandType = MqttCommandEnum.valueOf(commandType);
        this.lockerId = lockerId;
        this.durationMs = durationMs;
    }

    public MqttCommandEnum getCommandType() {
        return commandType;
    }

    public void setCommandType(MqttCommandEnum commandType) {
        this.commandType = commandType;
    }

    public String getCommand() {
        return commandType != null ? commandType.getCommand() : null;
    }

    public String getType() {
        return commandType != null ? commandType.name() : null;
    }

    public void setType(String type) {
        this.commandType = type != null ? MqttCommandEnum.valueOf(type) : null;
    }

    public String getLockerId() {
        return lockerId;
    }

    public void setLockerId(String lockerId) {
        this.lockerId = lockerId;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
