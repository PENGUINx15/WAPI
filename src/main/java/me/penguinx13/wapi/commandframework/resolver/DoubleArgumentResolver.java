package me.penguinx13.wapi.commandframework.resolver;

import me.penguinx13.wapi.commandframework.exception.ArgumentParseException;
import org.bukkit.command.CommandSender;

public class DoubleArgumentResolver implements ArgumentResolver<Double> {
    @Override
    public boolean supports(Class<?> type) {
        return type == double.class || type == Double.class;
    }

    @Override
    public Double resolve(String input, Class<?> type, CommandSender sender) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException ex) {
            throw new ArgumentParseException("Expected number but got '" + input + "'.", ex);
        }
    }
}
