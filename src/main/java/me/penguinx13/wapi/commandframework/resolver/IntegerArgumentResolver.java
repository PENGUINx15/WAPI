package me.penguinx13.wapi.commandframework.resolver;

import me.penguinx13.wapi.commandframework.exception.ArgumentParseException;
import org.bukkit.command.CommandSender;

public class IntegerArgumentResolver implements ArgumentResolver<Integer> {
    @Override
    public boolean supports(Class<?> type) {
        return type == int.class || type == Integer.class;
    }

    @Override
    public Integer resolve(String input, Class<?> type, CommandSender sender) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            throw new ArgumentParseException("Expected integer but got '" + input + "'.", ex);
        }
    }
}
