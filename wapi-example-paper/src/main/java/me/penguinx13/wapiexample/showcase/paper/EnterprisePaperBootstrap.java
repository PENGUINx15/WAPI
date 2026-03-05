package me.penguinx13.wapiexample.showcase.paper;

import me.penguinx13.wapi.commands.core.pipeline.*;
import me.penguinx13.wapi.commands.core.registry.CommandRegistrationService;
import me.penguinx13.wapi.commands.core.resolver.DefaultResolvers;
import me.penguinx13.wapi.commands.core.resolver.ResolverRegistry;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;
import me.penguinx13.wapi.commands.core.validation.ValidationService;
import me.penguinx13.wapi.commands.paper.error.DefaultErrorPresenter;
import me.penguinx13.wapi.commands.paper.platform.*;
import me.penguinx13.wapiexample.showcase.commands.EnterpriseCommand;
import me.penguinx13.wapiexample.showcase.core.db.EnterpriseUserRepository;
import me.penguinx13.wapiexample.showcase.core.metrics.EnterpriseMetricsSink;
import me.penguinx13.wapiexample.showcase.core.middleware.*;
import me.penguinx13.wapiexample.showcase.core.resolver.EnterpriseUserResolver;
import me.penguinx13.wapiexample.showcase.core.service.EnterpriseCooldownService;
import me.penguinx13.wapiexample.showcase.core.validation.NotReservedNameValidator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.ExecutorService;

public final class EnterprisePaperBootstrap {
    public CommandRuntime build(JavaPlugin plugin,
                                EnterpriseCommand command,
                                EnterpriseUserRepository repository,
                                EnterpriseMetricsSink metricsSink,
                                ExecutorService heavyExecutor) {
        CommandRegistrationService registrations = new CommandRegistrationService();
        ResolverRegistry resolverRegistry = new ResolverRegistry();
        ValidationService validationService = new ValidationService();
        EnterpriseCooldownService cooldownService = new EnterpriseCooldownService();

        DefaultResolvers.registerDefaults(resolverRegistry);
        resolverRegistry.register(new PaperPlayerResolver());
        resolverRegistry.register(new EnterpriseUserResolver(repository));
        validationService.register(new NotReservedNameValidator());

        registrations.register(command);

        PaperScheduler scheduler = new PaperScheduler(plugin);
        PaperPlatformBridge bridge = new PaperPlatformBridge(scheduler);

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
                List.of(
                        new ErrorMappingMiddleware(),
                        new LoggingMiddleware(plugin),
                        new AuditMiddleware(plugin),
                        new DeleteUserCooldownMiddleware(cooldownService)
                ),
                bridge,
                metricsSink
        );

        new PaperCommandBinder(plugin, bridge).bind(runtime);
        return runtime;
    }
}
