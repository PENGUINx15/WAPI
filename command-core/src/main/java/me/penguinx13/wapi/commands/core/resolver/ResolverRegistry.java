package me.penguinx13.wapi.commands.core.resolver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ResolverRegistry {
    private final Map<Class<?>, List<ArgumentResolver<?>>> index = new ConcurrentHashMap<>();

    public synchronized void register(ArgumentResolver<?> resolver) {
        List<ArgumentResolver<?>> chain = new ArrayList<>(index.getOrDefault(resolver.supports(), List.of()));
        if (chain.stream().anyMatch(r -> r.priority() == resolver.priority())) {
            throw new IllegalStateException("Conflicting resolver priority for " + resolver.supports().getName());
        }
        chain.add(resolver);
        chain.sort(Comparator.comparingInt(ArgumentResolver::priority).reversed());
        index.put(resolver.supports(), List.copyOf(chain));
    }

    public List<ArgumentResolver<?>> chain(Class<?> type) {
        return index.getOrDefault(type, List.of());
    }

    public ArgumentResolver<?> best(Class<?> type) {
        List<ArgumentResolver<?>> chain = chain(type);
        if (chain.isEmpty()) throw new IllegalArgumentException("No resolver for " + type.getName());
        return chain.get(0);
    }
}
