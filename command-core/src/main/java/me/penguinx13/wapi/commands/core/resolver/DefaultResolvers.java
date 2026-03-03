package me.penguinx13.wapi.commands.core.resolver;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.error.UserInputException;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class DefaultResolvers {
    private DefaultResolvers() {
    }

    public static void registerDefaults(ResolverRegistry registry) {
        registry.register(new StringResolver());
        registry.register(new IntegerResolver());
        registry.register(new BooleanResolver());
        registry.register(new UuidResolver());
    }

    private static final class StringResolver implements ArgumentResolver<String> {
        public Class<String> supports() {
            return String.class;
        }

        public int priority() {
            return 100;
        }

        public boolean canResolve(ArgumentMetadata argumentMetadata) {
            return argumentMetadata.type() == String.class;
        }

        public CompletionStage<String> parse(String input, ArgumentMetadata metadata, CommandContext context) {
            return CompletableFuture.completedFuture(input);
        }

        public CompletionStage<List<String>> suggest(String input, ArgumentMetadata metadata, CommandContext context) {
            return CompletableFuture.completedFuture(List.of());
        }
    }

    private static final class IntegerResolver implements ArgumentResolver<Integer> {
        public Class<Integer> supports() {
            return Integer.class;
        }

        public int priority() {
            return 100;
        }

        public boolean canResolve(ArgumentMetadata argumentMetadata) {
            return argumentMetadata.type() == Integer.class || argumentMetadata.type() == int.class;
        }

        public CompletionStage<Integer> parse(String input, ArgumentMetadata metadata, CommandContext context) {
            try {
                return CompletableFuture.completedFuture(Integer.parseInt(input));
            } catch (NumberFormatException ex) {
                return CompletableFuture.failedStage(
                        new UserInputException("Invalid integer for " + metadata.name())
                );
            }
        }

        public CompletionStage<List<String>> suggest(String input, ArgumentMetadata metadata, CommandContext context) {
            return CompletableFuture.completedFuture(List.of());
        }
    }

    private static final class BooleanResolver implements ArgumentResolver<Boolean> {
        public Class<Boolean> supports() {
            return Boolean.class;
        }

        public int priority() {
            return 100;
        }

        public boolean canResolve(ArgumentMetadata argumentMetadata) {
            return argumentMetadata.type() == Boolean.class || argumentMetadata.type() == boolean.class;
        }

        public CompletionStage<Boolean> parse(String input, ArgumentMetadata metadata, CommandContext context) {
            return CompletableFuture.completedFuture(Boolean.parseBoolean(input));
        }

        public CompletionStage<List<String>> suggest(String input, ArgumentMetadata metadata, CommandContext context) {
            return CompletableFuture.completedFuture(List.of("true", "false"));
        }
    }

    private static final class UuidResolver implements ArgumentResolver<UUID> {
        public Class<UUID> supports() {
            return UUID.class;
        }

        public int priority() {
            return 100;
        }

        public boolean canResolve(ArgumentMetadata argumentMetadata) {
            return argumentMetadata.type() == UUID.class;
        }

        public CompletionStage<UUID> parse(String input, ArgumentMetadata metadata, CommandContext context) {
            try {
                return CompletableFuture.completedFuture(UUID.fromString(input));
            } catch (IllegalArgumentException ex) {
                return CompletableFuture.failedStage(
                        new UserInputException("Invalid UUID for " + metadata.name())
                );
            }
        }

        public CompletionStage<List<String>> suggest(String input, ArgumentMetadata metadata, CommandContext context) {
            return CompletableFuture.completedFuture(List.of());
        }
    }
}
