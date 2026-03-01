package me.penguinx13.wapi.commandframework.core;

import me.penguinx13.wapi.commandframework.annotations.Arg;
import me.penguinx13.wapi.commandframework.annotations.Range;
import me.penguinx13.wapi.commandframework.annotations.RootCommand;
import me.penguinx13.wapi.commandframework.annotations.SubCommand;
import me.penguinx13.wapi.commandframework.exception.CommandException;
import me.penguinx13.wapi.commandframework.model.ArgumentMeta;
import me.penguinx13.wapi.commandframework.model.CommandMethodMeta;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandMetadataCache {
    private final Map<Class<?>, List<CommandMethodMeta>> cache = new ConcurrentHashMap<>();

    public List<CommandMethodMeta> scan(Object commandObject) {
        return cache.computeIfAbsent(commandObject.getClass(), ignored -> scanInternal(commandObject));
    }

    private List<CommandMethodMeta> scanInternal(Object commandObject) {
        Class<?> type = commandObject.getClass();
        RootCommand rootCommand = type.getAnnotation(RootCommand.class);
        if (rootCommand == null) {
            throw new CommandException("Class " + type.getName() + " is missing @RootCommand annotation.");
        }

        List<CommandMethodMeta> metas = new ArrayList<>();
        for (Method method : type.getDeclaredMethods()) {
            SubCommand subCommand = method.getAnnotation(SubCommand.class);
            if (subCommand == null) {
                continue;
            }
            method.setAccessible(true);
            metas.add(buildMeta(commandObject, method, rootCommand, subCommand));
        }

        return Collections.unmodifiableList(metas);
    }

    private CommandMethodMeta buildMeta(Object instance, Method method, RootCommand root, SubCommand sub) {
        List<String> path = sub.value().isBlank()
                ? Collections.emptyList()
                : Arrays.stream(sub.value().trim().split("\\s+"))
                .map(String::toLowerCase)
                .toList();

        List<ArgumentMeta> arguments = new ArrayList<>();
        List<Parameter> methodParameters = List.of(method.getParameters());
        for (Parameter parameter : methodParameters) {
            if (isSenderParameter(parameter)) {
                continue;
            }

            Arg arg = parameter.getAnnotation(Arg.class);
            if (arg == null) {
                throw new CommandException("Parameter '" + parameter.getName() + "' in " + method.getName() +
                        " must either be CommandSender/Player or annotated with @Arg.");
            }

            Range range = parameter.getAnnotation(Range.class);
            arguments.add(new ArgumentMeta(arg.value(), parameter.getType(), arg.optional(), arg.defaultValue(), range));
        }

        String permission = pickPermission(sub.permission(), root.permission());
        boolean playerOnly = sub.playerOnly() || root.playerOnly() || takesPlayerSender(methodParameters);

        return new CommandMethodMeta(
                instance,
                method,
                root.value().toLowerCase(),
                path,
                permission,
                sub.description(),
                playerOnly,
                arguments,
                methodParameters
        );
    }

    private boolean isSenderParameter(Parameter parameter) {
        Class<?> type = parameter.getType();
        return CommandSender.class.isAssignableFrom(type) || type == Player.class;
    }

    private boolean takesPlayerSender(List<Parameter> parameters) {
        return parameters.stream().anyMatch(p -> p.getType() == Player.class);
    }

    private String pickPermission(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "";
    }
}
