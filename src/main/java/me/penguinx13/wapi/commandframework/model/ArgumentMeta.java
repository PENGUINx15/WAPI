package me.penguinx13.wapi.commandframework.model;

import me.penguinx13.wapi.commandframework.annotations.Range;

public class ArgumentMeta {
    private final String name;
    private final Class<?> type;
    private final boolean optional;
    private final String defaultValue;
    private final Range range;

    public ArgumentMeta(String name, Class<?> type, boolean optional, String defaultValue, Range range) {
        this.name = name;
        this.type = type;
        this.optional = optional;
        this.defaultValue = defaultValue;
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Range getRange() {
        return range;
    }
}
