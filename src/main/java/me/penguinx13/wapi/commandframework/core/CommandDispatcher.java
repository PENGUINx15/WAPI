package me.penguinx13.wapi.commandframework.core;

import me.penguinx13.wapi.commandframework.annotations.Range;
import me.penguinx13.wapi.commandframework.exception.ArgumentParseException;
import me.penguinx13.wapi.commandframework.exception.CommandException;
import me.penguinx13.wapi.commandframework.exception.PermissionDeniedException;
import me.penguinx13.wapi.commandframework.exception.PlayerOnlyException;
import me.penguinx13.wapi.commandframework.exception.UsageException;
import me.penguinx13.wapi.commandframework.model.ArgumentMeta;
import me.penguinx13.wapi.commandframework.model.CommandMethodMeta;
import me.penguinx13.wapi.commandframework.model.CommandTreeNode;
import me.penguinx13.wapi.commandframework.resolver.ArgumentResolver;
import me.penguinx13.wapi.commandframework.resolver.ResolverRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDispatcher {
    private final CommandTree commandTree;
    private final ResolverRegistry resolverRegistry;

    public CommandDispatcher(CommandTree commandTree, ResolverRegistry resolverRegistry) {
        this.commandTree = commandTree;
        this.resolverRegistry = resolverRegistry;
    }

    public void dispatch(CommandSender sender, String rootLiteral, String[] args) {
        CommandMethodMeta meta = resolve(rootLiteral, args);
        enforceAccess(sender, meta);

        int argumentStart = meta.getPath().size();
        List<String> rawArguments = args.length > argumentStart
                ? List.of(args).subList(argumentStart, args.length)
                : Collections.emptyList();

        Object[] invocationArguments = buildInvocationArguments(sender, meta, rawArguments);

        try {
            meta.getMethod().invoke(meta.getInstance(), invocationArguments);
        } catch (IllegalAccessException e) {
            throw new CommandException("Could not access command method.", e);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof CommandException commandException) {
                throw commandException;
            }
            throw new CommandException("An internal command error occurred.", target);
        }
    }

    public List<String> tabComplete(CommandSender sender, String rootLiteral, String[] args) {
        CommandTreeNode rootNode = commandTree.getRoot(rootLiteral);
        if (rootNode == null) {
            return Collections.emptyList();
        }

        TraversalResult result = walkToBestMatch(rootNode, args);
        if (result.missingLiteral) {
            return result.suggestions;
        }

        CommandMethodMeta meta = result.command;
        if (meta == null) {
            return result.suggestions;
        }

        if (!meta.getPermission().isBlank() && !sender.hasPermission(meta.getPermission())) {
            return Collections.emptyList();
        }

        int argumentIndex = result.argumentIndex;
        if (argumentIndex < 0 || argumentIndex >= meta.getArguments().size()) {
            return Collections.emptyList();
        }

        ArgumentMeta argumentMeta = meta.getArguments().get(argumentIndex);
        ArgumentResolver<?> resolver = resolverRegistry.find(argumentMeta.getType());
        if (resolver == null) {
            return Collections.emptyList();
        }

        String typed = args.length == 0 ? "" : args[args.length - 1];
        return resolver.suggest(typed, argumentMeta.getType(), sender);
    }

    private CommandMethodMeta resolve(String rootLiteral, String[] args) {
        CommandTreeNode rootNode = commandTree.getRoot(rootLiteral);
        if (rootNode == null) {
            throw new CommandException("Unknown root command '/" + rootLiteral + "'.");
        }

        CommandTreeNode current = rootNode;
        int consumedPath = 0;

        while (consumedPath < args.length) {
            CommandTreeNode child = current.getChild(args[consumedPath].toLowerCase());
            if (child == null) {
                break;
            }
            current = child;
            consumedPath++;
        }

        CommandMethodMeta meta = current.getCommand();
        if (meta == null) {
            throw new UsageException('/' + rootLiteral + " <subcommand>");
        }

        int providedArgumentCount = args.length - consumedPath;
        long requiredArgumentCount = meta.getArguments().stream().filter(argument -> !argument.isOptional()).count();
        if (providedArgumentCount < requiredArgumentCount || providedArgumentCount > meta.getArguments().size()) {
            throw new UsageException(UsageGenerator.usageOf(meta));
        }

        return meta;
    }

    private void enforceAccess(CommandSender sender, CommandMethodMeta meta) {
        if (!meta.getPermission().isBlank() && !sender.hasPermission(meta.getPermission())) {
            throw new PermissionDeniedException(meta.getPermission());
        }
        if (meta.isPlayerOnly() && !(sender instanceof Player)) {
            throw new PlayerOnlyException();
        }
    }

    private Object[] buildInvocationArguments(CommandSender sender, CommandMethodMeta meta, List<String> rawArguments) {
        List<Object> parsedArguments = new ArrayList<>();
        int consumed = 0;

        for (ArgumentMeta argumentMeta : meta.getArguments()) {
            boolean hasValue = consumed < rawArguments.size();
            if (!hasValue) {
                if (!argumentMeta.isOptional()) {
                    throw new UsageException(UsageGenerator.usageOf(meta));
                }
                if (!argumentMeta.getDefaultValue().isBlank()) {
                    parsedArguments.add(parseToken(sender, argumentMeta, argumentMeta.getDefaultValue()));
                } else {
                    parsedArguments.add(defaultValueForMissing(argumentMeta));
                }
                continue;
            }

            String raw = rawArguments.get(consumed++);
            parsedArguments.add(parseToken(sender, argumentMeta, raw));
        }

        List<Object> invocation = new ArrayList<>();
        int argCursor = 0;
        for (Parameter parameter : meta.getMethodParameters()) {
            if (Player.class == parameter.getType()) {
                if (!(sender instanceof Player player)) {
                    throw new PlayerOnlyException();
                }
                invocation.add(player);
                continue;
            }
            if (CommandSender.class.isAssignableFrom(parameter.getType())) {
                invocation.add(sender);
                continue;
            }
            invocation.add(parsedArguments.get(argCursor++));
        }

        return invocation.toArray();
    }

    private Object parseToken(CommandSender sender, ArgumentMeta meta, String token) {
        ArgumentResolver<?> resolver = resolverRegistry.find(meta.getType());
        if (resolver == null) {
            throw new CommandException("No argument resolver registered for " + meta.getType().getSimpleName() + '.');
        }

        Object value = resolver.resolve(token, meta.getType(), sender);
        validateRange(meta, value);
        return value;
    }

    private Object defaultValueForMissing(ArgumentMeta meta) {
        Class<?> type = meta.getType();
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == double.class) {
            return 0D;
        }
        throw new CommandException("Optional primitive type without default is not supported: " + type.getSimpleName());
    }

    private void validateRange(ArgumentMeta meta, Object value) {
        Range range = meta.getRange();
        if (range == null || value == null || !(value instanceof Number number)) {
            return;
        }
        double numeric = number.doubleValue();
        if (numeric < range.min() || numeric > range.max()) {
            throw new ArgumentParseException(
                    "Argument '" + meta.getName() + "' must be between " + range.min() + " and " + range.max() + '.');
        }
    }

    private TraversalResult walkToBestMatch(CommandTreeNode rootNode, String[] args) {
        CommandTreeNode current = rootNode;
        int index = 0;

        while (index < args.length) {
            String token = args[index].toLowerCase();
            CommandTreeNode child = current.getChild(token);
            if (child == null) {
                List<String> literalSuggestions = current.getChildren().stream()
                        .map(CommandTreeNode::getLiteral)
                        .filter(literal -> literal.startsWith(token))
                        .toList();

                if (!literalSuggestions.isEmpty()) {
                    return TraversalResult.forLiteralSuggestions(literalSuggestions);
                }
                break;
            }
            current = child;
            index++;
        }

        CommandMethodMeta meta = current.getCommand();
        if (meta == null) {
            List<String> suggestions = current.getChildren().stream().map(CommandTreeNode::getLiteral).toList();
            return TraversalResult.forLiteralSuggestions(suggestions);
        }

        int argumentStart = meta.getPath().size();
        int argumentIndex = Math.max(0, args.length - argumentStart - 1);
        return TraversalResult.forCommand(meta, argumentIndex);
    }

    private static class TraversalResult {
        private final CommandMethodMeta command;
        private final int argumentIndex;
        private final List<String> suggestions;
        private final boolean missingLiteral;

        private TraversalResult(CommandMethodMeta command, int argumentIndex, List<String> suggestions, boolean missingLiteral) {
            this.command = command;
            this.argumentIndex = argumentIndex;
            this.suggestions = suggestions;
            this.missingLiteral = missingLiteral;
        }

        static TraversalResult forCommand(CommandMethodMeta command, int argumentIndex) {
            return new TraversalResult(command, argumentIndex, Collections.emptyList(), false);
        }

        static TraversalResult forLiteralSuggestions(List<String> suggestions) {
            return new TraversalResult(null, -1, suggestions, true);
        }
    }
}
