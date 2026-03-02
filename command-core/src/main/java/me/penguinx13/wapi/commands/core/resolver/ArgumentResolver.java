package me.penguinx13.wapi.commands.core.resolver;

import me.penguinx13.wapi.commands.core.context.CommandContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ArgumentResolver<T> {
    Class<T> supports();
    int priority();
    T parse(String input, CommandContext context);
    CompletableFuture<List<String>> suggest(String input, CommandContext context);
}
