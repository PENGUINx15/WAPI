package me.penguinx13.wapi.examplepaper;

import me.penguinx13.wapi.commands.core.pipeline.ArgumentParsingStage;
import me.penguinx13.wapi.commands.core.pipeline.AuthorizationStage;
import me.penguinx13.wapi.commands.core.pipeline.CommandPipeline;
import me.penguinx13.wapi.commands.core.pipeline.InvocationStage;
import me.penguinx13.wapi.commands.core.pipeline.PostProcessingStage;
import me.penguinx13.wapi.commands.core.pipeline.RoutingStage;
import me.penguinx13.wapi.commands.core.pipeline.ValidationStage;
import me.penguinx13.wapi.commands.core.registry.CommandRegistrationService;
import me.penguinx13.wapi.commands.core.resolver.DefaultResolvers;
import me.penguinx13.wapi.commands.core.resolver.ResolverRegistry;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;
import me.penguinx13.wapi.commands.core.runtime.NoopMetricsSink;
import me.penguinx13.wapi.commands.core.validation.ValidationService;
import me.penguinx13.wapi.commands.paper.error.DefaultErrorPresenter;
import me.penguinx13.wapi.commands.paper.platform.PaperCommandBinder;
import me.penguinx13.wapi.commands.paper.platform.PaperLogger;
import me.penguinx13.wapi.commands.paper.platform.PaperPlatformBridge;
import me.penguinx13.wapi.commands.paper.platform.PaperPlayerResolver;
import me.penguinx13.wapi.commands.paper.platform.PaperScheduler;
import me.penguinx13.wapi.examplepaper.commands.ExampleStatsCommand;
import me.penguinx13.wapi.examplepaper.model.PlayerStats;
import me.penguinx13.wapi.orm.Repository;
import me.penguinx13.wapi.orm.SQLiteManager;
import me.penguinx13.wapi.orm.SimpleORM;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public final class WapiExamplePaperPlugin extends JavaPlugin {

    private SQLiteManager sqliteManager;

    @Override
    public void onEnable() {
        sqliteManager = new SQLiteManager(getDataFolder(), "example.db", Bukkit::isPrimaryThread);

        SimpleORM orm = new SimpleORM(sqliteManager);
        orm.registerEntity(PlayerStats.class);
        Repository<PlayerStats, UUID> repository = orm.getRepository(PlayerStats.class);

        CommandRegistrationService registrationService = new CommandRegistrationService();
        registrationService.register(new ExampleStatsCommand(this, repository));

        ResolverRegistry resolverRegistry = new ResolverRegistry();
        DefaultResolvers.registerDefaults(resolverRegistry);
        resolverRegistry.register(new PaperPlayerResolver());

        ValidationService validationService = new ValidationService();
        PaperPlatformBridge bridge = new PaperPlatformBridge(new PaperScheduler(this));

        CommandRuntime runtime = new CommandRuntime(
                registrationService.buildTree(),
                new CommandPipeline(List.of(
                        new RoutingStage(),
                        new ArgumentParsingStage(),
                        new ValidationStage(),
                        new AuthorizationStage(),
                        new InvocationStage(),
                        new PostProcessingStage()
                )),
                resolverRegistry,
                validationService,
                new DefaultErrorPresenter(new PaperLogger(getLogger())),
                List.of(),
                bridge,
                new NoopMetricsSink()
        );

        new PaperCommandBinder(this, bridge).bind(runtime);
        getLogger().info("WAPI example plugin enabled.");
    }

    @Override
    public void onDisable() {
        if (sqliteManager != null) {
            sqliteManager.shutdown();
        }
    }
}
