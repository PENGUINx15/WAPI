package me.penguinx13.wapi.commandframework.core;

import me.penguinx13.wapi.commandframework.error.CommandErrorHandler;
import me.penguinx13.wapi.commandframework.error.DefaultCommandErrorHandler;
import me.penguinx13.wapi.commandframework.exception.CommandException;
import me.penguinx13.wapi.commandframework.model.CommandMethodMeta;
import me.penguinx13.wapi.commandframework.resolver.DefaultArgumentResolvers;
import me.penguinx13.wapi.commandframework.resolver.ResolverRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CommandRegistry {
    private final JavaPlugin plugin;
    private final CommandMetadataCache metadataCache = new CommandMetadataCache();
    private final CommandTree commandTree = new CommandTree();
    private final ResolverRegistry resolverRegistry = new ResolverRegistry();
    private final CommandDispatcher dispatcher;
    private volatile CommandErrorHandler errorHandler;

    public CommandRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        DefaultArgumentResolvers.registerDefaults(resolverRegistry);
        this.dispatcher = new CommandDispatcher(commandTree, resolverRegistry);
        this.errorHandler = new DefaultCommandErrorHandler();
    }

    public void registerCommand(Object commandObject) {
        List<CommandMethodMeta> commandMethods = metadataCache.scan(commandObject);
        for (CommandMethodMeta meta : commandMethods) {
            commandTree.add(meta);
        }
        String root = commandMethods.stream().findFirst()
                .map(CommandMethodMeta::getRoot)
                .orElseThrow(() -> new CommandException("No @SubCommand methods were found."));

        CommandExecutorAdapter adapter = new CommandExecutorAdapter(this, root);
        if (plugin.getCommand(root) == null) {
            throw new CommandException("Root command '/" + root + "' is missing from plugin.yml.");
        }
        plugin.getCommand(root).setExecutor(adapter);
        plugin.getCommand(root).setTabCompleter(adapter);
    }

    public void setErrorHandler(CommandErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public ResolverRegistry getResolverRegistry() {
        return resolverRegistry;
    }

    public void execute(CommandSender sender, String root, String[] args) {
        try {
            dispatcher.dispatch(sender, root, args);
        } catch (CommandException e) {
            errorHandler.handle(sender, e);
        }
    }

    public List<String> tabComplete(CommandSender sender, String root, String[] args) {
        try {
            return dispatcher.tabComplete(sender, root, args);
        } catch (Exception ignored) {
            return List.of();
        }
    }
}
