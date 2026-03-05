package me.penguinx13.wapi.commands.paper.platform;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.error.UserInputException;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;
import me.penguinx13.wapi.commands.core.resolver.ArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class PaperPlayerResolver implements ArgumentResolver<Player> {
    @Override
    public Class<Player> supports() {
        return Player.class;
    }

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public boolean canResolve(ArgumentMetadata argumentMetadata) {
        return Player.class.isAssignableFrom(argumentMetadata.type());
    }

    @Override
    public CompletionStage<Player> parse(String input, ArgumentMetadata metadata, CommandContext context) {
        Player player = Bukkit.getPlayerExact(input);
        if (player == null) {
            return CompletableFuture.failedStage(new UserInputException("Player not found: " + input));
        }
        return CompletableFuture.completedFuture(player);
    }

    @Override
    public CompletionStage<List<String>> suggest(String input, ArgumentMetadata metadata, CommandContext context) {
        String needle = input.toLowerCase(Locale.ROOT);
        return CompletableFuture.completedFuture(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(needle))
                .toList());
    }
}
