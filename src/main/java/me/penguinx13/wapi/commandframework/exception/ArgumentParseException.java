package me.penguinx13.wapi.commandframework.exception;

public class ArgumentParseException extends CommandException {
    public ArgumentParseException(String message) {
        super(message);
    }

    public ArgumentParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
