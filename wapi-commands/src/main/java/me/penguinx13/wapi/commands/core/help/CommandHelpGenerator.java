package me.penguinx13.wapi.commands.core.help;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;
import me.penguinx13.wapi.commands.core.metadata.BoundCommandMethod;
import me.penguinx13.wapi.commands.core.platform.PermissionEvaluator;
import me.penguinx13.wapi.commands.core.tree.CommandNode;
import me.penguinx13.wapi.commands.core.tree.CommandTree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CommandHelpGenerator {
    public String routingFailureMessage(CommandContext context, CommandTree tree) {
        Resolution resolution = resolveClosestNode(context.tokens(), tree);
        List<String> usages = commandUsages(context, resolution.node());
        if (usages.isEmpty()) {
            return "Unknown command.";
        }

        boolean unknownSubcommand = resolution.consumedCount() < context.tokens().size();
        if (unknownSubcommand) {
            return "Unknown command.\n\nDid you mean:\n\n" + String.join("\n", usages);
        }

        return "Available commands:\n\n" + String.join("\n", usages);
    }

    public String missingArgumentMessage(CommandContext context, BoundCommandMethod method) {
        String usage = formatUsage(method);
        return "Missing required argument.\n\nUsage:\n\n" + usage;
    }

    private List<String> commandUsages(CommandContext context, CommandNode node) {
        Map<String, String> byUsage = new LinkedHashMap<>();
        collectHandlers(node).stream()
                .filter(method -> hasPermission(context, method))
                .sorted(Comparator.comparing(method -> String.join(" ", method.metadata().path())))
                .forEach(method -> {
                    String usage = formatUsage(method);
                    byUsage.putIfAbsent(usage, formatWithDescription(usage, method.metadata().description()));
                });
        return List.copyOf(byUsage.values());
    }

    private boolean hasPermission(CommandContext context, BoundCommandMethod method) {
        String permission = method.metadata().permission();
        if (permission == null || permission.isBlank()) {
            return true;
        }
        PermissionEvaluator evaluator = context.service(PermissionEvaluator.class);
        return evaluator == null || evaluator.hasPermission(context.sender(), permission);
    }

    private String formatWithDescription(String usage, String description) {
        if (description == null || description.isBlank()) {
            return usage;
        }
        return usage + " - " + description;
    }

    private String formatUsage(BoundCommandMethod method) {
        StringBuilder builder = new StringBuilder("/")
                .append(method.metadata().root());
        for (String segment : method.metadata().path()) {
            builder.append(' ').append(segment);
        }
        for (ArgumentMetadata argument : method.metadata().arguments()) {
            builder.append(' ').append(formatArgument(argument));
        }
        return builder.toString();
    }

    private String formatArgument(ArgumentMetadata argument) {
        String label = Optional.ofNullable(argument.placeholder())
                .filter(value -> !value.isBlank())
                .map(value -> sanitizePlaceholder(value, argument.optional()))
                .orElse(argument.name());
        if (argument.optional()) {
            return "[" + label + "]";
        }
        return "<" + label + ">";
    }

    private String sanitizePlaceholder(String placeholder, boolean optional) {
        String value = placeholder.trim();
        if (value.startsWith("<") && value.endsWith(">")) {
            return value.substring(1, value.length() - 1).trim();
        }
        if (value.startsWith("[") && value.endsWith("]")) {
            return value.substring(1, value.length() - 1).trim();
        }
        if (optional && value.endsWith("?")) {
            return value.substring(0, value.length() - 1).trim();
        }
        return value;
    }

    private List<BoundCommandMethod> collectHandlers(CommandNode node) {
        List<BoundCommandMethod> handlers = new ArrayList<>();
        collect(node, handlers);
        return handlers;
    }

    private void collect(CommandNode node, List<BoundCommandMethod> handlers) {
        node.handler().ifPresent(handlers::add);
        node.literalChildren().values().forEach(child -> collect(child, handlers));
        node.argumentChildren().forEach(child -> collect(child, handlers));
    }

    private Resolution resolveClosestNode(List<String> tokens, CommandTree tree) {
        for (int size = tokens.size(); size >= 0; size--) {
            List<String> prefix = tokens.subList(0, size);
            Optional<CommandNode> node = tree.resolveNode(prefix);
            if (node.isPresent()) {
                return new Resolution(node.get(), size);
            }
        }
        return new Resolution(tree.resolveNode(List.of()).orElseThrow(), 0);
    }

    private record Resolution(CommandNode node, int consumedCount) {}
}
