package me.penguinx13.wapi.commands.core.error;

public final class ExecutionException extends CommandFrameworkException {
    public ExecutionException(String message, Throwable cause) { super(message, cause); }
}
