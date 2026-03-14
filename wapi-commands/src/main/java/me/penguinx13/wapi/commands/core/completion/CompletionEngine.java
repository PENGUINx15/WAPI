package me.penguinx13.wapi.commands.core.completion;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;
import me.penguinx13.wapi.commands.core.resolver.ArgumentResolver;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;
import me.penguinx13.wapi.commands.core.tree.CommandNode;
import me.penguinx13.wapi.commands.core.tree.NodeType;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class CompletionEngine {
    public CompletionStage<List<String>> complete(CommandContext context, ExecutionState state) {
        String cacheKey = String.join(" ", context.commandPath()) + "|" + context.rawInput();
        List<String> cached = state.suggestionCache().get(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        if (state.cancelled()) {
            return CompletableFuture.completedFuture(List.of());
        }

        CommandRuntime runtime = context.service(CommandRuntime.class);
        var bound = context.command().orElse(null);
        if (bound == null) {
            return CompletableFuture.completedFuture(runtime.tree().suggestNextLiterals(context.tokens()));
        }

        int argumentIndex = resolveArgumentIndex(context, bound.metadata().arguments().size());
        if (argumentIndex < 0) {
            return CompletableFuture.completedFuture(runtime.tree().suggestNextLiterals(context.tokens()));
        }

        ArgumentMetadata nextArgument = bound.metadata().arguments().get(argumentIndex);
        ArgumentResolver<?> resolver = runtime.resolverRegistry().resolve(nextArgument);
        String currentToken = context.tokens().isEmpty() ? "" : context.tokens().get(context.tokens().size() - 1);

        return resolver.suggest(currentToken, nextArgument, context).thenApply(suggestions -> {
            List<String> resolvedSuggestions = suggestions;
            if (resolvedSuggestions.isEmpty()) {
                resolvedSuggestions = placeholderSuggestions(runtime, context, nextArgument);
            }
            state.suggestionCache().put(cacheKey, resolvedSuggestions);
            return resolvedSuggestions;
        });
    }

    private int resolveArgumentIndex(CommandContext context, int totalArguments) {
        if (totalArguments == 0) {
            return -1;
        }

        int consumedArguments = context.capturedPathArguments().size();
        String currentToken = context.tokens().isEmpty() ? "" : context.tokens().get(context.tokens().size() - 1);
        boolean atNewToken = currentToken.isBlank();
        if (atNewToken) {
            return Math.min(consumedArguments, totalArguments - 1);
        }

        if (consumedArguments == 0) {
            return 0;
        }

        return Math.min(consumedArguments - 1, totalArguments - 1);
    }

    private List<String> placeholderSuggestions(CommandRuntime runtime, CommandContext context, ArgumentMetadata argument) {
        String rawPlaceholder = argument.placeholder();
        String fallbackPlaceholder = (rawPlaceholder == null || rawPlaceholder.isBlank()) ? argument.name() : rawPlaceholder;

        return runtime.tree().resolveNode(context.commandPath())
                .flatMap(node -> argumentNode(node, argument.name()))
                .map(node -> formatPlaceholder(node.argumentOptional(), node.argumentPlaceholder(), fallbackPlaceholder))
                .orElseGet(() -> formatPlaceholder(argument.optional(), argument.placeholder(), fallbackPlaceholder));
    }

    private java.util.Optional<CommandNode> argumentNode(CommandNode node, String argumentName) {
        if (node.type() == NodeType.ARGUMENT && argumentName.equals(node.argumentName())) {
            return java.util.Optional.of(node);
        }
        if (node.argumentChildren().isEmpty()) {
            return java.util.Optional.empty();
        }
        return argumentNode(node.argumentChildren().get(0), argumentName);
    }

    private List<String> formatPlaceholder(boolean optional, String fromNode, String fallback) {
        String base = (fromNode == null || fromNode.isBlank()) ? fallback : fromNode;
        if (optional) {
            return List.of("[" + stripBrackets(base) + "]");
        }
        return List.of("<" + stripBrackets(base) + ">");
    }

    private String stripBrackets(String placeholder) {
        String trimmed = placeholder.trim();
        if ((trimmed.startsWith("<") && trimmed.endsWith(">")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}
