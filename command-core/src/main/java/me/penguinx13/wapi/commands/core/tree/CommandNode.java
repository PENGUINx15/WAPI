package me.penguinx13.wapi.commands.core.tree;

import me.penguinx13.wapi.commands.core.metadata.BoundCommandMethod;

import java.util.List;

public record CommandNode(
        String token,
        NodeType type,
        List<CommandNode> children,
        BoundCommandMethod handler
) {
    public CommandNode {
        children = List.copyOf(children);
    }
}
