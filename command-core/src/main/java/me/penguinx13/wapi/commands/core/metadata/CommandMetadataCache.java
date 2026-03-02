package me.penguinx13.wapi.commands.core.metadata;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandMetadataCache {
    private final Map<Class<?>, List<CommandMethodMetadata>> immutableMetadata = new ConcurrentHashMap<>();

    public List<CommandMethodMetadata> getOrScan(Class<?> type, MetadataScanner scanner) {
        return immutableMetadata.computeIfAbsent(type, scanner::scan);
    }

    public List<BoundCommandMethod> bind(Object instance, MetadataScanner scanner) {
        return getOrScan(instance.getClass(), scanner).stream().map(meta -> new BoundCommandMethod(meta, instance)).toList();
    }

    @FunctionalInterface
    public interface MetadataScanner {
        List<CommandMethodMetadata> scan(Class<?> type);
    }
}
