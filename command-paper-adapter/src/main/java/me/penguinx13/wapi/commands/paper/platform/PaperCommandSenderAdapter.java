package me.penguinx13.wapi.commands.paper.platform;

import me.penguinx13.wapi.commands.core.platform.CommandSenderAdapter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public record PaperCommandSenderAdapter(CommandSender delegate) implements CommandSenderAdapter {
    public String name() { return delegate.getName(); }
    public UUID uniqueId() { return delegate instanceof Player p ? p.getUniqueId() : UUID.nameUUIDFromBytes(delegate.getName().getBytes()); }
    public boolean isPlayer() { return delegate instanceof Player; }
    public void sendMessage(String message) { delegate.sendMessage(message); }
}
