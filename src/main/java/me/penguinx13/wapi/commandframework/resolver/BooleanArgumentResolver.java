package me.penguinx13.wapi.commandframework.resolver;

import me.penguinx13.wapi.commandframework.exception.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BooleanArgumentResolver implements ArgumentResolver<Boolean> {
    @Override
    public boolean supports(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    @Override
    public Boolean resolve(String input, Class<?> type, CommandSender sender) {
        if ("true".equalsIgnoreCase(input)) {
            return true;
        }
        if ("false".equalsIgnoreCase(input)) {
            return false;
        }
        throw new ArgumentParseException("Expected boolean (true/false) but got '" + input + "'.");
    }

    @Override
    public List<String> suggest(String input, Class<?> type, CommandSender sender) {
        return List.of("true", "false");
    }
}
