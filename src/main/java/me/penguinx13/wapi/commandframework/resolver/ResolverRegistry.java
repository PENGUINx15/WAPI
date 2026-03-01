package me.penguinx13.wapi.commandframework.resolver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ResolverRegistry {
    private final List<ArgumentResolver<?>> resolvers = new CopyOnWriteArrayList<>();

    public void register(ArgumentResolver<?> resolver) {
        resolvers.add(resolver);
    }

    public ArgumentResolver<?> find(Class<?> type) {
        for (ArgumentResolver<?> resolver : resolvers) {
            if (resolver.supports(type)) {
                return resolver;
            }
        }
        return null;
    }
}
