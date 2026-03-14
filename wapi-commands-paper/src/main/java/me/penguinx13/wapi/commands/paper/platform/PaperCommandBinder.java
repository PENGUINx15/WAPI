package me.penguinx13.wapi.commands.paper.platform;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;
import me.penguinx13.wapi.commands.core.spi.PlatformCommandBinder;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
            List<String> tokens = new ArrayList<>();
            tokens.add(label);
            tokens.addAll(Arrays.asList(args));
            Map<Class<?>, Object> services = baseServices(runtime);
            CommandContext context = CommandContext.initial(bridge.adaptSender(sender), String.join(" ", tokens), tokens, services);
            runtime.executeAndRespond(context, new ExecutionState());
            return true;
        });

        pluginCommand.setTabCompleter((sender, command, alias, args) -> {
            List<String> tokens = new ArrayList<>();
            tokens.add(alias);
            tokens.addAll(Arrays.asList(args));
            if (args.length == 0) {
                tokens.add("");
            }

            Map<Class<?>, Object> services = baseServices(runtime);
            CommandContext context = CommandContext.initial(bridge.adaptSender(sender), String.join(" ", tokens), tokens, services);
            CompletableFuture<List<String>> completionFuture = runtime.complete(context, new ExecutionState()).toCompletableFuture();
            return completionFuture.join();
        });
    }

    private Map<Class<?>, Object> baseServices(CommandRuntime runtime) {
        Map<Class<?>, Object> services = new HashMap<>();
        services.put(CommandRuntime.class, runtime);
        services.put(me.penguinx13.wapi.commands.core.platform.PermissionEvaluator.class, new PaperPermissionEvaluator());
        return services;
    }
}
