package me.penguinx13.wapi.commands.core.tree;

import me.penguinx13.wapi.commands.core.metadata.BoundCommandMethod;

import java.util.*;

public final class CommandTree {
    private final CommandNode root;

    private CommandTree(CommandNode root) { this.root = root; }

    public Optional<BoundCommandMethod> route(List<String> tokens) {
        CommandNode current = root;
        for (String token : tokens) {
            Optional<CommandNode> next = current.children().stream().filter(n -> n.type() == NodeType.LITERAL && n.token().equalsIgnoreCase(token)).findFirst();
            if (next.isEmpty()) {
                next = current.children().stream().filter(n -> n.type() == NodeType.ARGUMENT).findFirst();
            }
            if (next.isEmpty()) return Optional.empty();
            current = next.get();
        }
        return Optional.ofNullable(current.handler());
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final MutableNode root = new MutableNode("", NodeType.LITERAL);
        public Builder add(BoundCommandMethod method) {
            MutableNode cursor = root;
            List<String> fullPath = new ArrayList<>();
            fullPath.add(method.metadata().root());
            fullPath.addAll(method.metadata().path());
            for (String token : fullPath) {
                NodeType type = token.startsWith("{") ? NodeType.ARGUMENT : NodeType.LITERAL;
                cursor = cursor.child(token, type);
            }
            if (cursor.handler != null) throw new CommandTreeBuildException("Path conflict: " + String.join(" ", fullPath));
            cursor.handler = method;
            return this;
        }
        public CommandTree build() { return new CommandTree(root.freeze()); }
    }

    private static final class MutableNode {
        String token; NodeType type; BoundCommandMethod handler; Map<String, MutableNode> children = new LinkedHashMap<>();
        MutableNode(String token, NodeType type) { this.token = token; this.type = type; }
        MutableNode child(String token, NodeType type) {
            MutableNode existing = children.get(token.toLowerCase(Locale.ROOT));
            if (existing != null && existing.type != type) throw new CommandTreeBuildException("Node type conflict for token: " + token);
            if (existing == null) { existing = new MutableNode(token, type); children.put(token.toLowerCase(Locale.ROOT), existing); }
            return existing;
        }
        CommandNode freeze() { return new CommandNode(token, type, children.values().stream().map(MutableNode::freeze).toList(), handler); }
    }
}
