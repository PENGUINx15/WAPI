package me.penguinx13.wapi.commands.paper.platform;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;
import me.penguinx13.wapi.commands.core.spi.PlatformCommandBinder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PaperCommandBinder implements PlatformCommandBinder {
    private final JavaPlugin plugin;
    private final PaperPlatformBridge bridge;

    public PaperCommandBinder(JavaPlugin plugin, PaperPlatformBridge bridge) {
        this.plugin = plugin;
        this.bridge = bridge;
    }

    @Override
    public void bind(CommandRuntime runtime) {
        String root = plugin.getDescription().getCommands().keySet().stream().findFirst().orElse("main");
        PluginCommand pluginCommand = plugin.getCommand(root);
        if (pluginCommand == null) {
            return;
        }

        pluginCommand.setExecutor((sender, command, label, args) -> {
            List<String> tokens = new java.util.ArrayList<>();
            tokens.add(label);
            tokens.addAll(Arrays.asList(args));
            Map<Class<?>, Object> services = new HashMap<>();
            services.put(CommandRuntime.class, runtime);
            services.put(me.penguinx13.wapi.commands.core.platform.PermissionEvaluator.class, new PaperPermissionEvaluator());
            CommandContext context = CommandContext.initial(bridge.adaptSender(sender), String.join(" ", tokens), tokens, services);
            runtime.executeAndRespond(context, new ExecutionState());
            return true;
        });
    }
}
