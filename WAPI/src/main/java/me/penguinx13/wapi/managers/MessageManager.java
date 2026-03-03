package me.penguinx13.wapi.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager implements Listener {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder().character('&').hexColors().build();

    public static void sendMessage(Player player, String message) {
        if (player != null && message != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
            List<String> result = splitText(message);
            for (String line : result) {
                if (line.contains("{action}")) {
                    String parsedLine = line.replace("{action}", "");
                    Component component = parseWithFallback(parsedLine);
                    player.sendActionBar(component);
                } else if (line.contains("{title}")) {
                    String parsedLine = line.replace("{title}", "");
                    Component component = parseWithFallback(parsedLine);
                    player.sendTitle(LEGACY_SERIALIZER.serialize(component), "", 10, 70, 20);
                } else if (line.contains("{subtitle}")) {
                    String parsedLine = line.replace("{subtitle}", "");
                    Component component = parseWithFallback(parsedLine);
                    player.sendTitle("", LEGACY_SERIALIZER.serialize(component), 10, 70, 20);
                } else if (line.contains("{message}")) {
                    String parsedLine = line.replace("{message}", "");
                    player.sendMessage(parseWithFallback(parsedLine));
                } else if (line.contains("{json}")) {
                    player.spigot().sendMessage(ComponentSerializer.parse(line.replace("{json}", "").replace("&", "§")));
                } else {
                    player.sendMessage(parseWithFallback(line));
                }
            }
        } else {
            System.out.println((player == null ? "Player" : "Message") + " is null");
        }
    }

    public static void sendMessage(Player player, String message, Map<String, ?> values) {
        sendMessage(player, applyTemplate(message, values));
    }


    public static void sendMessage(Player player, String message, Object... values) {
        sendMessage(player, applyTemplate(message, values));
    }

    public static String applyTemplate(String message, Map<String, ?> values) {
        return replaceValues(message, values);
    }

    public static String applyTemplate(String message, Object... values) {
        return replaceValues(message, values);
    }

    public static Component parseWithFallback(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        if (looksLikeMiniMessage(message)) {
            try {
                return MINI_MESSAGE.deserialize(message);
            } catch (Exception ignored) {
                // fallback to legacy parser
            }
        }

        return LEGACY_SERIALIZER.deserialize(message);
    }

    private static boolean looksLikeMiniMessage(String message) {
        return message.indexOf('<') >= 0 && message.indexOf('>') > message.indexOf('<');
    }

    public static String replaceValues(String message, Object... values) {
        if (message == null || values == null || values.length == 0) {
            return message;
        }

        String result = message;
        for (int i = 0; i + 1 < values.length; i += 2) {
            Object key = values[i];
            if (key == null) {
                continue;
            }

            String placeholder = "{" + key + "}";
            Object value = values[i + 1];
            String replacement = value == null ? "" : value.toString();
            result = result.replace(placeholder, replacement);
        }

        return result;
    }

    public static String replaceValues(String message, Map<String, ?> values) {
        if (message == null || values == null || values.isEmpty()) {
            return message;
        }

        String result = message;
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }

            String placeholder = "{" + entry.getKey() + "}";
            String replacement = entry.getValue() == null ? "" : entry.getValue().toString();
            result = result.replace(placeholder, replacement);
        }

        return result;
    }

    public static String replaceValue(String message, String key, Object value) {
        if (message == null || key == null) {
            return message;
        }

        return message.replace("{" + key + "}", value == null ? "" : value.toString());
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
        String regex = "\\{(title|action|message|subtitle|json)}(.*?)(?=\\{(?:title|action|message|subtitle|json)}|$)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        List<String> nonEmptyParts = new ArrayList<>();
        while (matcher.find()) {
            nonEmptyParts.add(matcher.group(0).trim());
        }

        if (nonEmptyParts.isEmpty() && !text.isBlank()) {
            nonEmptyParts.add("{message}" + text.trim());
        }

        return nonEmptyParts;
    }

}
