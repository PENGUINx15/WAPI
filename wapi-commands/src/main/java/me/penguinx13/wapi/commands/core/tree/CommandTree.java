package me.penguinx13.wapi.commands.core.tree;

import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;
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

    public Optional<CommandNode> resolveNode(List<String> tokens) {
        CommandNode current = root;
        for (String token : tokens) {
            CommandNode literal = current.literalChildren().get(token.toLowerCase(Locale.ROOT));
            if (literal != null) {
                current = literal;
                continue;
            }
            if (current.argumentChildren().isEmpty()) {
                if (current.handler().isPresent()) {
                    return Optional.of(current);
                }
                return Optional.empty();
            }
            current = current.argumentChildren().get(0);
        }
        return Optional.of(current);
    }

    public List<String> suggestNextLiterals(List<String> tokens) {
        if (tokens.isEmpty()) {
            return root.literalChildren().values().stream()
                    .map(CommandNode::token)
                    .toList();
        }

        CommandNode current = root;
        for (int i = 0; i < tokens.size() - 1; i++) {
            String token = tokens.get(i);
            CommandNode literal = current.literalChildren().get(token.toLowerCase(Locale.ROOT));
            if (literal != null) {
                current = literal;
                continue;
            }
            if (current.argumentChildren().isEmpty()) {
                return List.of();
            }
            current = current.argumentChildren().get(0);
        }

        String prefix = tokens.get(tokens.size() - 1).toLowerCase(Locale.ROOT);
        return current.literalChildren().values().stream()
                .map(CommandNode::token)
                .filter(token -> token.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final MutableNode root = new MutableNode("", NodeType.LITERAL, null, null, false);

        public void add(BoundCommandMethod method) {
            MutableNode cursor = root;
            List<String> fullPath = new ArrayList<>();
            fullPath.add(method.metadata().root());
            cursor = cursor.child(method.metadata().root(), NodeType.LITERAL, null, null, false);

            for (String token : method.metadata().path()) {
                fullPath.add(token);
                cursor = cursor.child(token, NodeType.LITERAL, null, null, false);
            }

            for (ArgumentMetadata argument : method.metadata().arguments()) {
                if (argument.optional()) {
                    continue;
                }
                String token = "{" + argument.name() + "}";
                fullPath.add(token);
                cursor = cursor.child(token, NodeType.ARGUMENT, argument.name(), argument.placeholder(), false);
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
        private final String argumentPlaceholder;
        private final boolean argumentOptional;
        private BoundCommandMethod handler;
        private final Map<String, MutableNode> literalChildren = new LinkedHashMap<>();
        private final List<MutableNode> argumentChildren = new ArrayList<>();

        private MutableNode(String token, NodeType type, String argumentName, String argumentPlaceholder, boolean argumentOptional) {
            this.token = token;
            this.type = type;
            this.argumentName = argumentName;
            this.argumentPlaceholder = argumentPlaceholder;
            this.argumentOptional = argumentOptional;
        }

        private MutableNode child(String token, NodeType type, String argumentName, String argumentPlaceholder, boolean argumentOptional) {
            if (type == NodeType.LITERAL) {
                return literalChildren.computeIfAbsent(token.toLowerCase(Locale.ROOT), key -> new MutableNode(token, type, null, null, false));
            }
            if (!argumentChildren.isEmpty() && !Objects.equals(argumentChildren.get(0).argumentName, argumentName)) {
                throw new CommandTreeBuildException(
                        "Argument conflict at token '" + token
                                + "': only one argument edge per depth is allowed."
                );
            }
            if (argumentChildren.isEmpty()) {
                argumentChildren.add(new MutableNode(token, type, argumentName, argumentPlaceholder, argumentOptional));
            }
            return argumentChildren.get(0);
        }

        private CommandNode freeze() {
            Map<String, CommandNode> literals = new LinkedHashMap<>();
            for (Map.Entry<String, MutableNode> entry : literalChildren.entrySet()) {
                literals.put(entry.getKey(), entry.getValue().freeze());
            }
            List<CommandNode> args = argumentChildren.stream().map(MutableNode::freeze).toList();
            return new CommandNode(token, type, argumentName, argumentPlaceholder, argumentOptional, literals, args, Optional.ofNullable(handler));
        }
    }
}
