package me.penguinx13.wapi.commandframework.core;

import me.penguinx13.wapi.commandframework.model.ArgumentMeta;
import me.penguinx13.wapi.commandframework.model.CommandMethodMeta;

public final class UsageGenerator {
    private UsageGenerator() {
    }

    public static String usageOf(CommandMethodMeta meta) {
        return usageOf(meta.getRoot(), meta);
    }

    public static String usageOf(String root, CommandMethodMeta meta) {
        StringBuilder builder = new StringBuilder();
        builder.append('/').append(root);
        for (String token : meta.getPath()) {
            builder.append(' ').append(token);
        }
        appendArguments(builder, meta);
        return builder.toString();
    }


    private static void appendArguments(StringBuilder builder, CommandMethodMeta meta) {
        for (ArgumentMeta argument : meta.getArguments()) {
            if (argument.isOptional()) {
                builder.append(" [").append(argument.getName()).append(']');
            } else {
                builder.append(" <").append(argument.getName()).append('>');
            }
        }
    }
}
