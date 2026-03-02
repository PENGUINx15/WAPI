package me.penguinx13.wapi.commands.core.result;

import java.util.Map;

public record CommandResult(boolean success, String message, Map<String, Object> metadata) {
    public static CommandResult success(String message) {
        return new CommandResult(true, message, Map.of());
    }

    public static CommandResult failure(String message) {
        return new CommandResult(false, message, Map.of());
    }

    public static CommandResult emptySuccess() {
        return new CommandResult(true, "", Map.of());
    }
}
