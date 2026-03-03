package me.penguinx13.wapi.commands.core.context;

import me.penguinx13.wapi.commands.core.metadata.BoundCommandMethod;
import me.penguinx13.wapi.commands.core.platform.CommandSenderAdapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record CommandContext(
        CommandSenderAdapter sender,
        String rawInput,
        List<String> tokens,
        List<String> commandPath,
        Map<String, Object> parsedArguments,
        Map<String, String> capturedPathArguments,
        Map<Class<?>, Object> services,
        Optional<BoundCommandMethod> command
) {
    public CommandContext {
        tokens = List.copyOf(tokens);
        commandPath = List.copyOf(commandPath);
        parsedArguments = Map.copyOf(parsedArguments);
        capturedPathArguments = Map.copyOf(capturedPathArguments);
        services = Map.copyOf(services);
    }

    public static CommandContext initial(
            CommandSenderAdapter sender,
            String rawInput,
            List<String> tokens,
            Map<Class<?>, Object> services
    ) {
        return new CommandContext(
                sender,
                rawInput,
                tokens,
                List.of(),
                Map.of(),
                Map.of(),
                services,
                Optional.empty()
        );
    }

    public CommandContext withRoute(BoundCommandMethod method, List<String> path, Map<String, String> captures) {
        return new CommandContext(sender, rawInput, tokens, path, parsedArguments, captures, services, Optional.of(method));
    }

    public CommandContext withParsedArguments(Map<String, Object> args) {
        return new CommandContext(sender, rawInput, tokens, commandPath, args, capturedPathArguments, services, command);
    }

    @SuppressWarnings("unchecked")
    public <T> T service(Class<T> type) {
        return (T) services.get(type);
    }
}
