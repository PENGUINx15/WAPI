package me.penguinx13.wapi.commandframework.core;

import me.penguinx13.wapi.commandframework.model.ArgumentMeta;
import me.penguinx13.wapi.commandframework.model.CommandMethodMeta;

public final class UsageGenerator {
    private UsageGenerator() {
    }

    public static String usageOf(CommandMethodMeta meta) {
        StringBuilder builder = new StringBuilder();
        builder.append('/').append(meta.getRoot());
        for (String token : meta.getPath()) {
            builder.append(' ').append(token);
        }
        for (ArgumentMeta argument : meta.getArguments()) {
            if (argument.isOptional()) {
                builder.append(" [").append(argument.getName()).append(']');
            } else {
                builder.append(" <").append(argument.getName()).append('>');
            }
        }
        return builder.toString();
    }
}
