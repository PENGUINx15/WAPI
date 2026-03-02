package me.penguinx13.wapi.commands.core.error;

public abstract class CommandFrameworkException extends RuntimeException {
    protected CommandFrameworkException(String message) { super(message); }
    protected CommandFrameworkException(String message, Throwable cause) { super(message, cause); }
}
