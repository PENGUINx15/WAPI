package me.penguinx13.wapi.commandframework.model;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class CommandMethodMeta {
    private final Object instance;
    private final Method method;
    private final String root;
    private final List<String> path;
    private final String permission;
    private final String description;
    private final boolean playerOnly;
    private final List<ArgumentMeta> arguments;
    private final List<Parameter> methodParameters;

    public CommandMethodMeta(
            Object instance,
            Method method,
            String root,
            List<String> path,
            String permission,
            String description,
            boolean playerOnly,
            List<ArgumentMeta> arguments,
            List<Parameter> methodParameters
    ) {
        this.instance = instance;
        this.method = method;
        this.root = root;
        this.path = path;
        this.permission = permission;
        this.description = description;
        this.playerOnly = playerOnly;
        this.arguments = arguments;
        this.methodParameters = methodParameters;
    }

    public Object getInstance() {
        return instance;
    }

    public Method getMethod() {
        return method;
    }

    public String getRoot() {
        return root;
    }

    public List<String> getPath() {
        return path;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public List<ArgumentMeta> getArguments() {
        return arguments;
    }

    public List<Parameter> getMethodParameters() {
        return methodParameters;
    }
}
