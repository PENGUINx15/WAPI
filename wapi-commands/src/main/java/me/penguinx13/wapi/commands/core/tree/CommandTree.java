package me.penguinx13.wapi.commands.core.tree;

import me.penguinx13.wapi.commands.core.metadata.BoundCommandMethod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class CommandTree {
    private final CommandNode root;

    private CommandTree(CommandNode root) {
        this.root = root;
    }

    public Optional<RouteResult> route(List<String> tokens) {
        CommandNode current = root;
        Map<String, String> captures = new LinkedHashMap<>();
        List<String> path = new ArrayList<>();

        for (String token : tokens) {
            CommandNode literal = current.literalChildren().get(token.toLowerCase(Locale.ROOT));
            if (literal != null) {
                current = literal;
                path.add(token);
                continue;
            }
            if (current.argumentChildren().isEmpty()) {
                if (current.handler().isPresent()) {
                    break;
                }
                return Optional.empty();
            }

            CommandNode selected = current.argumentChildren().get(0);
            current = selected;
            path.add(token);
            if (selected.argumentName() != null && !selected.argumentName().isBlank()) {
                captures.put(selected.argumentName(), token);
            }
        }

        return current.handler().map(handler -> new RouteResult(handler, List.copyOf(path), Map.copyOf(captures)));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final MutableNode root = new MutableNode("", NodeType.LITERAL, null);

        public void add(BoundCommandMethod method) {
            MutableNode cursor = root;
            List<String> fullPath = new ArrayList<>();
            fullPath.add(method.metadata().root());
            fullPath.addAll(method.metadata().path());
            method.metadata().arguments().stream()
                    .filter(arg -> !arg.optional())
                    .map(arg -> "{" + arg.name() + "}")
                    .forEach(fullPath::add);

            for (String token : fullPath) {
                boolean argument = token.startsWith("{") && token.endsWith("}");
                NodeType type = argument ? NodeType.ARGUMENT : NodeType.LITERAL;
                String argumentName = argument ? token.substring(1, token.length() - 1) : null;
                cursor = cursor.child(token, type, argumentName);
            }
            if (cursor.handler != null) {
                throw new CommandTreeBuildException("Path conflict: " + String.join(" ", fullPath));
            }
            cursor.handler = method;
        }

        public CommandTree build() {
            return new CommandTree(root.freeze());
        }
    }

    private static final class MutableNode {
        private final String token;
        private final NodeType type;
        private final String argumentName;
        private BoundCommandMethod handler;
        private final Map<String, MutableNode> literalChildren = new LinkedHashMap<>();
        private final List<MutableNode> argumentChildren = new ArrayList<>();

        private MutableNode(String token, NodeType type, String argumentName) {
            this.token = token;
            this.type = type;
            this.argumentName = argumentName;
        }

        private MutableNode child(String token, NodeType type, String argumentName) {
            if (type == NodeType.LITERAL) {
                return literalChildren.computeIfAbsent(token.toLowerCase(Locale.ROOT), key -> new MutableNode(token, type, null));
            }
            if (!argumentChildren.isEmpty() && !Objects.equals(argumentChildren.get(0).argumentName, argumentName)) {
                throw new CommandTreeBuildException(
                        "Argument conflict at token '" + token
                                + "': only one argument edge per depth is allowed."
                );
            }
            if (argumentChildren.isEmpty()) {
                argumentChildren.add(new MutableNode(token, type, argumentName));
            }
            return argumentChildren.get(0);
        }

        private CommandNode freeze() {
            Map<String, CommandNode> literals = new LinkedHashMap<>();
            for (Map.Entry<String, MutableNode> entry : literalChildren.entrySet()) {
                literals.put(entry.getKey(), entry.getValue().freeze());
            }
            List<CommandNode> args = argumentChildren.stream().map(MutableNode::freeze).toList();
            return new CommandNode(token, type, argumentName, literals, args, Optional.ofNullable(handler));
        }
    }
}
