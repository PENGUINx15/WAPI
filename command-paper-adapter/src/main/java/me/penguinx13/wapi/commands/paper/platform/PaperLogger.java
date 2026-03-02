package me.penguinx13.wapi.commands.paper.platform;

import me.penguinx13.wapi.commands.core.platform.FrameworkLogger;

import java.util.logging.Logger;

public record PaperLogger(Logger logger) implements FrameworkLogger {
    public void info(String message) { logger.info(message); }
    public void warn(String message) { logger.warning(message); }
    public void error(String message, Throwable throwable) { logger.severe(message + ": " + throwable.getMessage()); }
}
