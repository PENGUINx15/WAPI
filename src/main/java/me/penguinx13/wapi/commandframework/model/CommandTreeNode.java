package me.penguinx13.wapi.commandframework.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandTreeNode {
    private final String literal;
    private final Map<String, CommandTreeNode> children = new LinkedHashMap<>();
    private CommandMethodMeta command;

    public CommandTreeNode(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    public CommandTreeNode getOrCreateChild(String key) {
        return children.computeIfAbsent(key, CommandTreeNode::new);
    }

    public CommandTreeNode getChild(String key) {
        return children.get(key);
    }

    public Collection<CommandTreeNode> getChildren() {
        return Collections.unmodifiableCollection(children.values());
    }

    public void setCommand(CommandMethodMeta command) {
        this.command = command;
    }

    public CommandMethodMeta getCommand() {
        return command;
    }
}
