package me.penguinx13.wapi.commands.core.resolver;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.error.UserInputException;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ResolverRegistry {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class
    );

    private final Map<Class<?>, List<ArgumentResolver<?>>> resolvers = new ConcurrentHashMap<>();

    public synchronized <T> void register(ArgumentResolver<T> resolver) {
        List<ArgumentResolver<?>> chain = new ArrayList<>(resolvers.getOrDefault(normalize(resolver.supports()), List.of()));
        chain.add(resolver);
        chain.sort(Comparator.comparingInt(ArgumentResolver::priority).reversed());
        resolvers.put(normalize(resolver.supports()), List.copyOf(chain));
    }

    public ArgumentResolver<?> resolve(ArgumentMetadata metadata, CommandContext context) {
        Class<?> targetType = normalize(metadata.type());
        List<ArgumentResolver<?>> candidates = new ArrayList<>();

        for (Map.Entry<Class<?>, List<ArgumentResolver<?>>> entry : resolvers.entrySet()) {
            if (entry.getKey().isAssignableFrom(targetType) || targetType.isAssignableFrom(entry.getKey())) {
                candidates.addAll(entry.getValue());
            }
        }

        candidates.sort(Comparator.comparingInt(ArgumentResolver::priority).reversed());
        for (ArgumentResolver<?> candidate : candidates) {
            if (candidate.canResolve(metadata)) {
                return candidate;
            }
        }

        if (targetType.isEnum()) {
            return new EnumArgumentResolver(targetType.asSubclass(Enum.class));
        }

        throw new UserInputException("No resolver found for argument '" + metadata.name() + "' of type " + metadata.type().getSimpleName());
    }

    private Class<?> normalize(Class<?> type) {
        return type.isPrimitive() ? PRIMITIVE_TO_WRAPPER.getOrDefault(type, type) : type;
    }
}
