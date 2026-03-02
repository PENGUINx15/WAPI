package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.error.AuthorizationException;
import me.penguinx13.wapi.commands.core.platform.PermissionEvaluator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class AuthorizationStage implements CommandStage {
    @Override
    public CompletionStage<StageResult> execute(CommandContext context, ExecutionState state) {
        PermissionEvaluator permissionEvaluator = context.service(PermissionEvaluator.class);
        var method = context.command().orElseThrow();

        if (method.metadata().playerOnly() && !context.sender().isPlayer()) {
            throw new AuthorizationException("This command can only be executed by players.");
        }

        String permission = method.metadata().permission();
        if (permission != null && !permission.isBlank() && permissionEvaluator != null && !permissionEvaluator.hasPermission(context.sender(), permission)) {
            throw new AuthorizationException("You do not have permission to execute this command.");
        }
        return CompletableFuture.completedFuture(StageResult.next(context));
    }
}
