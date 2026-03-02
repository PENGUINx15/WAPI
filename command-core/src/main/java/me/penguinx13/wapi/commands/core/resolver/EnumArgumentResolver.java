package me.penguinx13.wapi.commands.core.resolver;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.error.UserInputException;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class EnumArgumentResolver implements ArgumentResolver<Enum<?>> {
    private final Class<? extends Enum<?>> enumType;

    public EnumArgumentResolver(Class<? extends Enum<?>> enumType) {
        this.enumType = enumType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Enum<?>> supports() {
        return (Class<Enum<?>>) Enum.class;
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean canResolve(ArgumentMetadata argumentMetadata) {
        return argumentMetadata.type().isEnum() && enumType.isAssignableFrom(argumentMetadata.type());
    }

    @Override
    public CompletionStage<Enum<?>> parse(String input, ArgumentMetadata metadata, CommandContext context) {
        for (Enum<?> value : enumType.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(input)) {
                return CompletableFuture.completedFuture(value);
            }
        }
        return CompletableFuture.failedStage(new UserInputException("Unknown value '" + input + "' for " + enumType.getSimpleName()));
    }

    @Override
    public CompletionStage<List<String>> suggest(String input, ArgumentMetadata metadata, CommandContext context) {
        String needle = input.toLowerCase(Locale.ROOT);
        List<String> values = Arrays.stream(enumType.getEnumConstants())
                .map(e -> e.name().toLowerCase(Locale.ROOT))
                .filter(name -> name.startsWith(needle))
                .toList();
        return CompletableFuture.completedFuture(values);
    }
}
