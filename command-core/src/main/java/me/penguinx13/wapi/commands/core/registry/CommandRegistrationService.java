package me.penguinx13.wapi.commands.core.registry;

import me.penguinx13.wapi.commands.core.metadata.*;
import me.penguinx13.wapi.commands.core.scan.AnnotationCommandScanner;
import me.penguinx13.wapi.commands.core.tree.CommandTree;

import java.util.ArrayList;
import java.util.List;

public final class CommandRegistrationService {
    private final CommandMetadataCache cache = new CommandMetadataCache();
    private final AnnotationCommandScanner scanner = new AnnotationCommandScanner();
    private final List<BoundCommandMethod> bound = new ArrayList<>();

    public synchronized void register(Object commandInstance) {
        bound.addAll(cache.bind(commandInstance, scanner));
    }

    public synchronized CommandTree buildTree() {
        CommandTree.Builder builder = CommandTree.builder();
        bound.forEach(builder::add);
        return builder.build();
    }
}
