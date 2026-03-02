package me.penguinx13.wapi.commands.core.resolver;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface ArgumentResolver<T> {
    Class<T> supports();
    int priority();
    default boolean supportsAsync() { return true; }
    boolean canResolve(ArgumentMetadata argumentMetadata);
    CompletionStage<T> parse(String input, ArgumentMetadata metadata, CommandContext context);
    CompletionStage<List<String>> suggest(String input, ArgumentMetadata metadata, CommandContext context);
}
