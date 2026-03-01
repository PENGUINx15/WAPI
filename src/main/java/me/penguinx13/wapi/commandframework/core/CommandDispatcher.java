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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommandDispatcher {
    private static final String HELP_LITERAL = "help";

    private final CommandTree commandTree;
    private final ResolverRegistry resolverRegistry;

    public CommandDispatcher(CommandTree commandTree, ResolverRegistry resolverRegistry) {
        this.commandTree = commandTree;
        this.resolverRegistry = resolverRegistry;
    }

    public void dispatch(CommandSender sender, String rootLiteral, String[] args) {
        CommandTreeNode rootNode = requireRoot(rootLiteral);

        HelpTarget helpTarget = resolveHelpTarget(rootNode, args);
        if (helpTarget != null) {
            sendHelp(sender, rootLiteral, helpTarget.node, helpTarget.depth, helpTarget.pathPrefix);
            return;
        }

        Resolution resolution = resolveForExecution(rootLiteral, rootNode, args);
        enforceAccess(sender, resolution.command);

        Object[] invocationArguments = buildInvocationArguments(sender, resolution.command, resolution.rawArguments);

        try {
            resolution.command.getMethod().invoke(resolution.command.getInstance(), invocationArguments);
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

        String currentToken = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        CommandTreeNode current = rootNode;
        int index = 0;

        while (index < Math.max(0, args.length - 1)) {
            String token = args[index].toLowerCase(Locale.ROOT);
            if (HELP_LITERAL.equals(token)) {
                return Collections.emptyList();
            }
            CommandTreeNode child = current.getChild(token);
            if (child == null) {
                return Collections.emptyList();
            }
            current = child;
            index++;
        }

        List<String> literalSuggestions = current.getChildren().stream()
                .filter(node -> canAccess(sender, node.getCommand()))
                .map(CommandTreeNode::getLiteral)
                .filter(literal -> literal.startsWith(currentToken))
                .sorted()
                .toList();

        if (!literalSuggestions.isEmpty()) {
            return literalSuggestions;
        }

        if (HELP_LITERAL.startsWith(currentToken)) {
            return List.of(HELP_LITERAL);
        }

        CommandMethodMeta meta = current.getCommand();
        if (meta == null || !canAccess(sender, meta)) {
            return Collections.emptyList();
        }

        int argumentPosition = args.length - meta.getPath().size() - 1;
        if (argumentPosition < 0 || argumentPosition >= meta.getArguments().size()) {
            return Collections.emptyList();
        }

        ArgumentMeta argumentMeta = meta.getArguments().get(argumentPosition);
        ArgumentResolver<?> resolver = resolverRegistry.find(argumentMeta.getType());
        if (resolver == null) {
            return Collections.emptyList();
        }

        Set<String> suggestions = new LinkedHashSet<>(resolver.suggest(currentToken, argumentMeta.getType(), sender));
        if (argumentMeta.isOptional() && currentToken.isEmpty()) {
            suggestions.add("<skip>");
        }
        return suggestions.stream().filter(suggestion -> suggestion.startsWith(currentToken)).toList();
    }

    private Resolution resolveForExecution(String rootLiteral, CommandTreeNode rootNode, String[] args) {
        CommandTreeNode current = rootNode;
        int consumedPath = 0;

        while (consumedPath < args.length) {
            CommandTreeNode child = current.getChild(args[consumedPath].toLowerCase(Locale.ROOT));
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
            throw new UsageException(UsageGenerator.usageOf(rootLiteral, meta));
        }

        List<String> rawArguments = providedArgumentCount == 0
                ? Collections.emptyList()
                : List.of(args).subList(consumedPath, args.length);
        return new Resolution(meta, rawArguments);
    }

    private HelpTarget resolveHelpTarget(CommandTreeNode rootNode, String[] args) {
        if (args.length == 0) {
            return new HelpTarget(rootNode, 0, List.of());
        }

        if (args.length == 1 && HELP_LITERAL.equalsIgnoreCase(args[0])) {
            return new HelpTarget(rootNode, 0, List.of());
        }

        CommandTreeNode current = rootNode;
        List<String> pathPrefix = new ArrayList<>();

        for (String arg : args) {
            String token = arg.toLowerCase(Locale.ROOT);
            if (HELP_LITERAL.equals(token)) {
                return new HelpTarget(current, pathPrefix.size(), List.copyOf(pathPrefix));
            }
            CommandTreeNode child = current.getChild(token);
            if (child == null) {
                return null;
            }
            current = child;
            pathPrefix.add(token);
        }

        return null;
    }

    private CommandTreeNode requireRoot(String rootLiteral) {
        CommandTreeNode rootNode = commandTree.getRoot(rootLiteral);
        if (rootNode == null) {
            throw new CommandException("Unknown root command '/" + rootLiteral + "'.");
        }
        return rootNode;
    }

    private void sendHelp(CommandSender sender, String rootLiteral, CommandTreeNode fromNode, int depthOffset, List<String> pathPrefix) {
        String header = pathPrefix.isEmpty()
                ? "/" + rootLiteral
                : "/" + rootLiteral + " " + String.join(" ", pathPrefix);
        sender.sendMessage(ChatColor.GOLD + "Command help for " + header + ':');

        List<String> lines = buildHelpLines(sender, rootLiteral, fromNode, depthOffset);
        if (lines.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No available subcommands.");
            return;
        }
        lines.forEach(sender::sendMessage);
    }

    private List<String> buildHelpLines(CommandSender sender, String rootLiteral, CommandTreeNode node, int depth) {
        List<String> lines = new ArrayList<>();

        CommandMethodMeta command = node.getCommand();
        if (command != null && canAccess(sender, command)) {
            String usage = UsageGenerator.usageOf(rootLiteral, command);
            StringBuilder line = new StringBuilder();
            line.append(ChatColor.YELLOW).append("  ".repeat(Math.max(0, depth))).append("- ")
                    .append(ChatColor.AQUA).append(usage);
            if (!command.getPermission().isBlank()) {
                line.append(ChatColor.DARK_GRAY).append(" [perm: ").append(command.getPermission()).append(']');
            }
            if (!command.getDescription().isBlank()) {
                line.append(ChatColor.GRAY).append(" - ").append(command.getDescription());
            }
            lines.add(line.toString());
        }

        List<CommandTreeNode> sortedChildren = node.getChildren().stream()
                .sorted(Comparator.comparing(CommandTreeNode::getLiteral))
                .toList();

        for (CommandTreeNode child : sortedChildren) {
            lines.addAll(buildHelpLines(sender, rootLiteral, child, depth + 1));
        }
        return lines;
    }

    private void enforceAccess(CommandSender sender, CommandMethodMeta meta) {
        if (!meta.getPermission().isBlank() && !sender.hasPermission(meta.getPermission())) {
            throw new PermissionDeniedException(meta.getPermission());
        }
        if (meta.isPlayerOnly() && !(sender instanceof Player)) {
            throw new PlayerOnlyException();
        }
    }

    private boolean canAccess(CommandSender sender, CommandMethodMeta meta) {
        if (meta == null) {
            return true;
        }
        if (!meta.getPermission().isBlank() && !sender.hasPermission(meta.getPermission())) {
            return false;
        }
        return !meta.isPlayerOnly() || sender instanceof Player;
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

    private static class Resolution {
        private final CommandMethodMeta command;
        private final List<String> rawArguments;

        private Resolution(CommandMethodMeta command, List<String> rawArguments) {
            this.command = command;
            this.rawArguments = rawArguments;
        }
    }

    private static class HelpTarget {
        private final CommandTreeNode node;
        private final int depth;
        private final List<String> pathPrefix;

        private HelpTarget(CommandTreeNode node, int depth, List<String> pathPrefix) {
            this.node = node;
            this.depth = depth;
            this.pathPrefix = pathPrefix;
        }
    }
}
