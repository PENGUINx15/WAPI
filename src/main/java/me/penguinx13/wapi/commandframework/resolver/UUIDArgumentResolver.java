package me.penguinx13.wapi.commandframework.resolver;

import me.penguinx13.wapi.commandframework.exception.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class UUIDArgumentResolver implements ArgumentResolver<UUID> {
    @Override
    public boolean supports(Class<?> type) {
        return type == UUID.class;
    }

    @Override
    public UUID resolve(String input, Class<?> type, CommandSender sender) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException ex) {
            throw new ArgumentParseException("Expected UUID but got '" + input + "'.", ex);
        }
    }
}
