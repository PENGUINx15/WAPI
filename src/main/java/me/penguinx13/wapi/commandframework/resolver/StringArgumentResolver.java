package me.penguinx13.wapi.commandframework.resolver;

import org.bukkit.command.CommandSender;

public class StringArgumentResolver implements ArgumentResolver<String> {
    @Override
    public boolean supports(Class<?> type) {
        return type == String.class;
    }

    @Override
    public String resolve(String input, Class<?> type, CommandSender sender) {
        return input;
    }
}
