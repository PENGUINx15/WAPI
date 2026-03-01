package me.penguinx13.wapi.commandframework.resolver;

import me.penguinx13.wapi.commandframework.exception.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public interface ArgumentResolver<T> {
    boolean supports(Class<?> type);

    T resolve(String input, Class<?> type, CommandSender sender) throws ArgumentParseException;

    default List<String> suggest(String input, Class<?> type, CommandSender sender) {
        return Collections.emptyList();
    }
}
