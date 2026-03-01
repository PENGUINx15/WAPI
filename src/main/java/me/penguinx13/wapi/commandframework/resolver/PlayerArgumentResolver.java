package me.penguinx13.wapi.commandframework.resolver;

import me.penguinx13.wapi.commandframework.exception.ArgumentParseException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerArgumentResolver implements ArgumentResolver<Player> {
    @Override
    public boolean supports(Class<?> type) {
        return type == Player.class;
    }

    @Override
    public Player resolve(String input, Class<?> type, CommandSender sender) {
        Player player = Bukkit.getPlayerExact(input);
        if (player == null) {
            throw new ArgumentParseException("Player '" + input + "' is not online.");
        }
        return player;
    }

    @Override
    public List<String> suggest(String input, Class<?> type, CommandSender sender) {
        String prefix = input == null ? "" : input.toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .collect(Collectors.toList());
    }
}
