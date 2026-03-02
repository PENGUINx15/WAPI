package me.penguinx13.wapi.commands.core.scan;

import me.penguinx13.wapi.commands.annotations.*;
import me.penguinx13.wapi.commands.core.metadata.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public final class AnnotationCommandScanner implements CommandMetadataCache.MetadataScanner {
    @Override
    public List<CommandMethodMetadata> scan(Class<?> type) {
        RootCommand root = type.getAnnotation(RootCommand.class);
        if (root == null) return List.of();
        List<CommandMethodMetadata> methods = new ArrayList<>();
        for (Method method : type.getDeclaredMethods()) {
            SubCommand sub = method.getAnnotation(SubCommand.class);
            if (sub == null) continue;
            method.setAccessible(true);
            methods.add(new CommandMethodMetadata(type, method, root.value(), List.of(sub.value().split("\\s+")), sub.permission(), sub.playerOnly(), sub.description(), scanArguments(method)));
        }
        return List.copyOf(methods);
    }

    private List<ArgumentMetadata> scanArguments(Method method) {
        List<ArgumentMetadata> args = new ArrayList<>();
        Parameter[] params = method.getParameters();
        for (int i = 0; i < params.length; i++) {
            Parameter p = params[i];
            Arg arg = p.getAnnotation(Arg.class);
            if (arg == null) continue;
            List<Annotation> validations = Arrays.stream(p.getAnnotations())
                    .filter(a -> a.annotationType() == Range.class || a.annotationType() == Min.class || a.annotationType() == Max.class || a.annotationType() == Regex.class)
                    .toList();
            args.add(new ArgumentMetadata(arg.value(), p.getType(), arg.optional(), arg.defaultValue(), i, validations, p));
        }
        return List.copyOf(args);
    }
}
