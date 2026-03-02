package me.penguinx13.wapi.commands.core.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;

public record ArgumentMetadata(
        String name,
        Class<?> type,
        boolean optional,
        String defaultValue,
        int index,
        List<Annotation> validationAnnotations,
        Parameter parameter
) {}
