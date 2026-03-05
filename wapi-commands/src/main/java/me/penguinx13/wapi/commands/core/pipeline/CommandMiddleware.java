package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.result.CommandResult;

import java.util.concurrent.CompletionStage;

public interface CommandMiddleware {
    CompletionStage<CommandResult> handle(CommandInvocation invocation, MiddlewareChain chain);
}
