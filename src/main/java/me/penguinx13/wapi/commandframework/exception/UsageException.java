package me.penguinx13.wapi.commandframework.exception;

public class UsageException extends CommandException {
    public UsageException(String usage) {
        super("Usage: " + usage);
    }
}
