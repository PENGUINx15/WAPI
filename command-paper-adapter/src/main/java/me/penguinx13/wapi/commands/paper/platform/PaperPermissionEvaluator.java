package me.penguinx13.wapi.commands.paper.platform;

import me.penguinx13.wapi.commands.core.platform.CommandSenderAdapter;
import me.penguinx13.wapi.commands.core.platform.PermissionEvaluator;

public final class PaperPermissionEvaluator implements PermissionEvaluator {
    @Override
    public boolean hasPermission(CommandSenderAdapter sender, String permission) {
        return permission == null || permission.isBlank() || ((PaperCommandSenderAdapter) sender).delegate().hasPermission(permission);
    }
}
