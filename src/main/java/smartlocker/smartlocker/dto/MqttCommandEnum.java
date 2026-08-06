package smartlocker.smartlocker.dto;

public enum MqttCommandEnum {
    UNLOCK("UNLOCK"),
    LOCK("LOCK");

    private final String command;

    MqttCommandEnum(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
