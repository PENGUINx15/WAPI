package me.penguinx13.wapi;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager implements Listener {

    private static  Plugin plugin;

    public static void sendMessage(Player player, String message) {
        if (player != null && message != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
            List<String> result = splitText(message);
            for (String line : result) {
                if (line.contains("{action}")) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(line.replace("&", "§").replace("{action}", "")));
                } else if (line.contains("{title}")) {
                    player.sendTitle(line.replace("&", "§").replace("{title}", ""), "", 10, 70, 20);
                } else if (line.contains("{subtitle}")) {
                    player.sendTitle("", line.replace("&", "§").replace("{subtitle}", ""), 10, 70, 20);
                } else if (line.contains("{message}")) {
                    player.sendMessage(line.replace("&", "§").replace("{message}", ""));
                } else if (line.contains("{json}")) {
                    try {
                        BaseComponent[] components = ComponentSerializer.parse(line.replace("{json}", "").replace("&", "§"));
                        player.spigot().sendMessage(components);

                        sendLog(plugin, "info", "Sending json successful");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendLog(plugin, "info", "Invalid JSON format: " + e.getMessage());
                    }
                }

            }
        } else {
            sendLog(plugin ,"info",(player == null ? "Player" : "Message") + " is null");
        }
    }

    public static void sendLog(Plugin plugin, String type, String message) {
        if (message != null) {
            switch (type.toLowerCase()) {
                case "warn":
                    plugin.getLogger().warning(message);
                    break;
                case "error":
                    plugin.getLogger().severe(message);
                    break;
                default:
                    plugin.getLogger().info(message);
                    break;
            }
        } else {
            plugin.getLogger().warning("Message is null");
        }
    }
    private static List<String> splitText(String text) {
        String regex = "\\{(title|action|message|subtitle)}(.*?)(?=\\{(?:title|action|message|subtitle)}|$)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        List<String> nonEmptyParts = new ArrayList<>();
        while (matcher.find()) {
            nonEmptyParts.add(matcher.group(0).trim());
        }

        return nonEmptyParts;
    }

}
