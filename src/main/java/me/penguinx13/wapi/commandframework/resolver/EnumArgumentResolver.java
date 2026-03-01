package me.penguinx13.wapi.commandframework.resolver;

import me.penguinx13.wapi.commandframework.exception.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EnumArgumentResolver implements ArgumentResolver<Enum<?>> {
    @Override
    public boolean supports(Class<?> type) {
        return type.isEnum();
    }

    @Override
    public Enum<?> resolve(String input, Class<?> type, CommandSender sender) {
        Object[] constants = type.getEnumConstants();
        for (Object constant : constants) {
            Enum<?> enumValue = (Enum<?>) constant;
            if (enumValue.name().equalsIgnoreCase(input)) {
                return enumValue;
            }
        }
        String available = Arrays.stream(constants)
                .map(value -> ((Enum<?>) value).name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(", "));
        throw new ArgumentParseException("Invalid value '" + input + "'. Allowed: " + available + ".");
    }

    @Override
    public List<String> suggest(String input, Class<?> type, CommandSender sender) {
        String prefix = input == null ? "" : input.toLowerCase(Locale.ROOT);
        return Arrays.stream(type.getEnumConstants())
                .map(value -> ((Enum<?>) value).name().toLowerCase(Locale.ROOT))
                .filter(name -> name.startsWith(prefix))
                .collect(Collectors.toList());
    }
}
