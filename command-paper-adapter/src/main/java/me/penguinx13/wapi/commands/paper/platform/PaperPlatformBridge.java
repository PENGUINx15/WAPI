package me.penguinx13.wapi.commands.paper.platform;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.platform.CommandSenderAdapter;
import me.penguinx13.wapi.commands.core.result.CommandResult;
import me.penguinx13.wapi.commands.core.spi.PlatformBridge;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class PaperPlatformBridge implements PlatformBridge {
    private final PaperScheduler scheduler;

    public PaperPlatformBridge(PaperScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public CommandSenderAdapter adaptSender(Object platformSender) {
        return new PaperCommandSenderAdapter((org.bukkit.command.CommandSender) platformSender);
    }

    @Override
    public CompletionStage<Void> deliverResult(CommandContext context, CommandResult result) {
        scheduler.runSync(() -> {
            if (result.message() != null && !result.message().isBlank()) {
                context.sender().sendMessage(result.message());
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> deliverSuggestions(CommandContext context, List<String> suggestions) {
        return CompletableFuture.completedFuture(null);
    }
}
