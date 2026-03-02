package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.error.ErrorPresenter;

import java.util.List;

public final class CommandPipeline {
    private final List<CommandStage> stages;
    private final List<CommandInterceptor> interceptors;
    private final ErrorPresenter errorPresenter;

    public CommandPipeline(List<CommandStage> stages, List<CommandInterceptor> interceptors, ErrorPresenter errorPresenter) {
        this.stages = stages;
        this.interceptors = interceptors;
        this.errorPresenter = errorPresenter;
    }

    public void execute(CommandContext initial) {
        CommandContext current = initial;
        try {
            for (CommandStage stage : stages) {
                String stageName = stage.getClass().getSimpleName();
                for (CommandInterceptor interceptor : interceptors) current = interceptor.beforeStage(stageName, current);
                current = stage.execute(current);
                for (CommandInterceptor interceptor : interceptors) current = interceptor.afterStage(stageName, current);
            }
        } catch (Throwable throwable) {
            errorPresenter.present(current, throwable);
        }
    }
}
