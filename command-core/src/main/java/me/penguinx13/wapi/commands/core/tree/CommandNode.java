package me.penguinx13.wapi.commands.core.tree;

import me.penguinx13.wapi.commands.core.metadata.BoundCommandMethod;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record CommandNode(
        String token,
        NodeType type,
        String argumentName,
        Map<String, CommandNode> literalChildren,
        List<CommandNode> argumentChildren,
        Optional<BoundCommandMethod> handler
) {
    public CommandNode {
        literalChildren = Map.copyOf(literalChildren);
        argumentChildren = List.copyOf(argumentChildren);
        handler = handler == null ? Optional.empty() : handler;
    }
}
