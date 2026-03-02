package me.penguinx13.wapi.commands.core.error;

import java.util.Map;

public abstract class CommandException extends RuntimeException {
    private final Map<String, Object> metadata;

    protected CommandException(String message) {
        this(message, null, Map.of());
    }

    protected CommandException(String message, Throwable cause) {
        this(message, cause, Map.of());
    }

    protected CommandException(String message, Throwable cause, Map<String, Object> metadata) {
        super(message, cause);
        this.metadata = Map.copyOf(metadata);
    }

    public Map<String, Object> metadata() {
        return metadata;
    }
}
