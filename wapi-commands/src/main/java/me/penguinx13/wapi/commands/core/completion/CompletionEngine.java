package me.penguinx13.wapi.commands.core.completion;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;
import me.penguinx13.wapi.commands.core.resolver.ArgumentResolver;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;

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

        if (bound.metadata().arguments().isEmpty()) {
            return CompletableFuture.completedFuture(runtime.tree().suggestNextLiterals(context.tokens()));
        }

        int argumentIndex = Math.min(
                context.parsedArguments().size(),
                bound.metadata().arguments().size() - 1
        );
        ArgumentMetadata nextArgument = bound.metadata().arguments().get(argumentIndex);
        ArgumentResolver<?> resolver = runtime.resolverRegistry().resolve(nextArgument);
        String currentToken = context.tokens().isEmpty()
                ? ""
                : context.tokens().get(context.tokens().size() - 1);

        return resolver.suggest(currentToken, nextArgument, context).thenApply(suggestions -> {
            state.suggestionCache().put(cacheKey, suggestions);
            return suggestions;
        });
    }
}
