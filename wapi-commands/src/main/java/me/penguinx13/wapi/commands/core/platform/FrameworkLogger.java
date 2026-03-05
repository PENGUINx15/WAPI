package me.penguinx13.wapi.commands.core.platform;

public interface FrameworkLogger {
    void info(String message);
    void warn(String message);
    void error(String message, Throwable throwable);
}
