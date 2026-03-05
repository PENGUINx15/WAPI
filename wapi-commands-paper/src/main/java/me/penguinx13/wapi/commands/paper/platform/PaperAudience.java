package me.penguinx13.wapi.commands.paper.platform;

import me.penguinx13.wapi.commands.core.platform.Audience;
import me.penguinx13.wapi.commands.core.platform.CommandSenderAdapter;

public final class PaperAudience implements Audience {
    @Override
    public void message(CommandSenderAdapter sender, String text) { sender.sendMessage(text); }
}
