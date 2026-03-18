package me.penguinx13.wapi.examplepaper;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import me.penguinx13.wapi.enchants.api.EnchantRegistry;
import me.penguinx13.wapi.enchants.listener.EnchantingTableProtectionListener;
import me.penguinx13.wapi.enchants.manager.EnchantManager;
import me.penguinx13.wapi.enchants.storage.EnchantStorage;
import me.penguinx13.wapi.enchants.util.CustomEnchantItemUtil;
import me.penguinx13.wapi.examplepaper.commands.ExampleEnchantCommand;
import me.penguinx13.wapi.examplepaper.commands.ExampleStatsCommand;
import me.penguinx13.wapi.examplepaper.enchants.ExampleEnchantListener;
import me.penguinx13.wapi.examplepaper.enchants.LifestealEnchant;
import me.penguinx13.wapi.examplepaper.model.PlayerStats;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.managers.CooldownManager;
import me.penguinx13.wapi.managers.MessageManager;
import me.penguinx13.wapi.orm.Repository;
import me.penguinx13.wapi.orm.SQLiteManager;
import me.penguinx13.wapi.orm.SimpleORM;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class WapiExamplePaperPlugin extends JavaPlugin {

    private SQLiteManager sqliteManager;
    private ConfigManager configManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.registerConfig("config.yml");

        FileConfiguration config = configManager.getConfig("config.yml");
        String startupMessage = config.getString("messages.startup",
                "<green>WAPI example plugin enabled.</green>");
        MessageManager.sendLog(this, "info",
                MessageManager.applyTemplate(startupMessage,
                        Map.of("plugin", getName())));

        cooldownManager = new CooldownManager();
        UUID consoleKey = new UUID(0L, 0L);
        cooldownManager.markCooldown(consoleKey, "startup", Duration.ofSeconds(5));

        sqliteManager = new SQLiteManager(getDataFolder(), "example.db",
                Bukkit::isPrimaryThread);

        SimpleORM orm = new SimpleORM(sqliteManager);
        orm.registerEntity(PlayerStats.class);
        Repository<PlayerStats, UUID> repository = orm.getRepository(PlayerStats.class);

        EnchantRegistry enchantRegistry = new EnchantRegistry();
        LifestealEnchant lifestealEnchant = new LifestealEnchant();

        enchantRegistry.register(lifestealEnchant);
        EnchantStorage enchantStorage = new EnchantStorage(this, enchantRegistry);
        CustomEnchantItemUtil customEnchantItemUtil = new CustomEnchantItemUtil(this);
        EnchantManager enchantManager = new EnchantManager(enchantStorage);

        CommandRegistrationService registrationService = new CommandRegistrationService();
        registrationService.register(new ExampleStatsCommand(this, repository));
        registrationService.register(
                new ExampleEnchantCommand(enchantStorage, lifestealEnchant,
                        customEnchantItemUtil));

        ResolverRegistry resolverRegistry = new ResolverRegistry();
        DefaultResolvers.registerDefaults(resolverRegistry);
        resolverRegistry.register(new PaperPlayerResolver());

        ValidationService validationService = new ValidationService();
        PaperPlatformBridge bridge = new PaperPlatformBridge(
                new PaperScheduler(this));

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
        getServer().getPluginManager().registerEvents(
                new ExampleEnchantListener(enchantManager), this
        );
        getServer().getPluginManager().registerEvents(
                new EnchantingTableProtectionListener(customEnchantItemUtil), this
        );
        if (cooldownManager.isOnCooldown(consoleKey, "startup")) {
            MessageManager.sendLog(this, "info",
                    "Startup cooldown manager example initialized.");
        }
    }

    @Override
    public void onDisable() {
        if (sqliteManager != null) {
            sqliteManager.shutdown();
        }
    }
}
