package me.penguinx13.wapi.commandframework.exception;

public class PlayerOnlyException extends CommandException {
    public PlayerOnlyException() {
        super("This command can only be used by a player.");
    }
}
