package me.penguinx13.wapi.commands.core.context;

import me.penguinx13.wapi.commands.core.metadata.BoundCommandMethod;
import me.penguinx13.wapi.commands.core.platform.CommandSenderAdapter;

import java.util.*;

public record CommandContext(
        CommandSenderAdapter sender,
        List<String> rawArguments,
        Map<String, Object> parsedArguments,
        List<String> commandPath,
        BoundCommandMethod command,
        Map<Class<?>, Object> services,
        Set<String> flags,
        Map<String, List<String>> suggestionCache
) {
    public CommandContext withCommand(BoundCommandMethod bound, List<String> path) {
        return new CommandContext(sender, rawArguments, parsedArguments, path, bound, services, flags, suggestionCache);
    }

    public CommandContext withParsedArgument(String key, Object value) {
        Map<String, Object> copy = new HashMap<>(parsedArguments);
        copy.put(key, value);
        return new CommandContext(sender, rawArguments, Map.copyOf(copy), commandPath, command, services, flags, suggestionCache);
    }

    @SuppressWarnings("unchecked")
    public <T> T service(Class<T> type) {
        return (T) services.get(type);
    }

    public static CommandContext initial(CommandSenderAdapter sender, List<String> rawArguments, Map<Class<?>, Object> services) {
        return new CommandContext(sender, List.copyOf(rawArguments), Map.of(), List.of(), null, Map.copyOf(services), Set.of(), new HashMap<>());
    }
}
