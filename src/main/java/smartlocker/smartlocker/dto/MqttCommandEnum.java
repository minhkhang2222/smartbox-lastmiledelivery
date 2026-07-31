package smartlocker.smartlocker.dto;

public enum MqttCommandEnum {
    UNLOCK("UNLOCK"),
    LOCK("LOCK"),
    WAIT_FOR_DEPOSIT("WAIT_FOR_DEPOSIT");

    private final String command;

    MqttCommandEnum(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
