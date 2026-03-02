package me.penguinx13.wapi.commands.integration;

import me.penguinx13.wapi.commands.core.pipeline.*;
import me.penguinx13.wapi.commands.core.registry.CommandRegistrationService;
import me.penguinx13.wapi.commands.paper.error.DefaultErrorPresenter;

import java.util.List;

public final class CommandFrameworkBootstrap {
    private final CommandRegistrationService registrations = new CommandRegistrationService();
    private final CommandPipeline pipeline = new CommandPipeline(
            List.of(
                    new RoutingStage(),
                    new ArgumentParsingStage(),
                    new ValidationStage(),
                    new AuthorizationStage(),
                    new CooldownStage(),
                    new InvocationStage(),
                    new PostProcessingStage(),
                    new ErrorHandlingStage()
            ),
            List.of(),
            new DefaultErrorPresenter()
    );

    public void register(Object command) {
        registrations.register(command);
        registrations.buildTree();
    }

    public CommandPipeline pipeline() {
        return pipeline;
    }
}
