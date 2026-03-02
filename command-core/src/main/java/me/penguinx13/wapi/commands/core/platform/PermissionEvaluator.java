package me.penguinx13.wapi.commands.core.platform;

public interface PermissionEvaluator {
    boolean hasPermission(CommandSenderAdapter sender, String permission);
}
