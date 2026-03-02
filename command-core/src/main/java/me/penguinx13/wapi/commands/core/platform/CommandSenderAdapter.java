package me.penguinx13.wapi.commands.core.platform;

import java.util.UUID;

public interface CommandSenderAdapter {
    String name();
    UUID uniqueId();
    boolean isPlayer();
    void sendMessage(String message);
    Object unwrap();
    Class<?> platformSenderType();
}
