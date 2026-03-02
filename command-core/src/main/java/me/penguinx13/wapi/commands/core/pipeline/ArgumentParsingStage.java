package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.error.UserInputException;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;
import me.penguinx13.wapi.commands.core.resolver.ArgumentResolver;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class ArgumentParsingStage implements CommandStage {
    @Override
    public CompletionStage<StageResult> execute(CommandContext context, ExecutionState state) {
        CommandRuntime runtime = context.service(CommandRuntime.class);
        var method = context.command().orElseThrow(() -> new UserInputException("No command mapped for invocation."));
        Map<String, Object> parsed = new HashMap<>();
        CompletionStage<Void> chain = CompletableFuture.completedFuture(null);

        int optionalOffset = 0;
        for (ArgumentMetadata argument : method.metadata().arguments()) {
            String token = context.capturedPathArguments().get(argument.name());
            if (token == null && argument.optional()) {
                int optionalTokenIndex = context.commandPath().size() + optionalOffset;
                if (optionalTokenIndex < context.tokens().size()) {
                    token = context.tokens().get(optionalTokenIndex);
                } else {
                    token = argument.defaultValue();
                }
                optionalOffset++;
            }
            if (token == null) {
                return CompletableFuture.failedStage(new UserInputException("Missing required argument: " + argument.name()));
            }
            ArgumentResolver<?> resolver = runtime.resolverRegistry().resolve(argument, context);
            String finalToken = token;
            chain = chain.thenCompose(v -> resolver.parse(finalToken, argument, context)
                    .thenAccept(value -> parsed.put(argument.name(), value)));
        }

        return chain.thenApply(v -> StageResult.next(context.withParsedArguments(parsed)));
    }
}
