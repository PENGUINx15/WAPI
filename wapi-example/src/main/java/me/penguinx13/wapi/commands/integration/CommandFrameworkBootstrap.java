package me.penguinx13.wapi.commands.integration;

import me.penguinx13.wapi.commands.core.pipeline.*;
import me.penguinx13.wapi.commands.core.registry.CommandRegistrationService;
import me.penguinx13.wapi.commands.core.resolver.DefaultResolvers;
import me.penguinx13.wapi.commands.core.resolver.ResolverRegistry;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;
import me.penguinx13.wapi.commands.core.runtime.NoopMetricsSink;
import me.penguinx13.wapi.commands.core.validation.ValidationService;
import me.penguinx13.wapi.commands.paper.error.DefaultErrorPresenter;
import me.penguinx13.wapi.commands.paper.platform.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class CommandFrameworkBootstrap {
    private final CommandRegistrationService registrations = new CommandRegistrationService();
    private final ResolverRegistry resolverRegistry = new ResolverRegistry();
    private final ValidationService validationService = new ValidationService();

    private final JavaPlugin plugin;

    public CommandFrameworkBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
        DefaultResolvers.registerDefaults(resolverRegistry);
    }

    public void register(Object command) {
        registrations.register(command);
    }

    public CommandRuntime buildAndBind() {
        PaperScheduler scheduler = new PaperScheduler(plugin);
        PaperPlatformBridge bridge = new PaperPlatformBridge(scheduler);
        resolverRegistry.register(new PaperPlayerResolver());

        CommandPipeline pipeline = new CommandPipeline(List.of(
                new RoutingStage(),
                new ArgumentParsingStage(),
                new ValidationStage(),
                new AuthorizationStage(),
                new CooldownStage(),
                new InvocationStage(),
                new PostProcessingStage(),
                new ErrorHandlingStage()
        ));

        CommandRuntime runtime = new CommandRuntime(
                registrations.buildTree(),
                pipeline,
                resolverRegistry,
                validationService,
                new DefaultErrorPresenter(new PaperLogger(plugin.getLogger())),
                List.of(),
                bridge,
                new NoopMetricsSink()
        );

        new PaperCommandBinder(plugin, bridge).bind(runtime);
        return runtime;
    }
}
