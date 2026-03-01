package me.penguinx13.wapi.commandframework.core;

import me.penguinx13.wapi.commandframework.model.CommandMethodMeta;
import me.penguinx13.wapi.commandframework.model.CommandTreeNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandTree {
    private final Map<String, CommandTreeNode> roots = new ConcurrentHashMap<>();

    public void add(CommandMethodMeta meta) {
        CommandTreeNode rootNode = roots.computeIfAbsent(meta.getRoot(), CommandTreeNode::new);
        CommandTreeNode current = rootNode;
        for (String token : meta.getPath()) {
            current = current.getOrCreateChild(token);
        }
        current.setCommand(meta);
    }

    public CommandTreeNode getRoot(String rootLiteral) {
        return roots.get(rootLiteral.toLowerCase());
    }
}
