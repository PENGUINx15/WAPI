package me.penguinx13.wapi.commands.core.completion;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;
import me.penguinx13.wapi.commands.core.resolver.ArgumentResolver;
import me.penguinx13.wapi.commands.core.resolver.ResolverRegistry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CompletionEngine {
    private final ResolverRegistry resolvers;
    public CompletionEngine(ResolverRegistry resolvers) { this.resolvers = resolvers; }

    @SuppressWarnings("unchecked")
    public CompletableFuture<List<String>> suggest(ArgumentMetadata argument, String input, CommandContext context) {
        String cacheKey = argument.name() + ":" + input;
        if (context.suggestionCache().containsKey(cacheKey)) {
            return CompletableFuture.completedFuture(context.suggestionCache().get(cacheKey));
        }
        ArgumentResolver<Object> resolver = (ArgumentResolver<Object>) resolvers.best(argument.type());
        return resolver.suggest(input, context).thenApply(list -> {
            context.suggestionCache().put(cacheKey, list);
            return list;
        });
    }
}
