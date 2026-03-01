package me.penguinx13.wapi.commandframework.exception;

public class PermissionDeniedException extends CommandException {
    public PermissionDeniedException(String permission) {
        super("You do not have permission: " + permission);
    }
}
