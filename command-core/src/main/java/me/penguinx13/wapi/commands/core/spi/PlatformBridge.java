package me.penguinx13.wapi.commands.core.spi;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.platform.CommandSenderAdapter;
import me.penguinx13.wapi.commands.core.result.CommandResult;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface PlatformBridge {
    CommandSenderAdapter adaptSender(Object platformSender);
    CompletionStage<Void> deliverResult(CommandContext context, CommandResult result);
    CompletionStage<Void> deliverSuggestions(CommandContext context, List<String> suggestions);
}
