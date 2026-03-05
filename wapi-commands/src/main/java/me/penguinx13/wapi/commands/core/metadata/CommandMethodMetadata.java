package me.penguinx13.wapi.commands.core.metadata;

import java.lang.reflect.Method;
import java.util.List;

public record CommandMethodMetadata(
        Class<?> declaringType,
        Method method,
        String root,
        List<String> path,
        String permission,
        boolean playerOnly,
        String description,
        List<ArgumentMetadata> arguments
) {}
