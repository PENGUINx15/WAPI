package me.penguinx13.wapiexample.showcase.core.validation;

import me.penguinx13.wapi.commands.annotations.NotReservedName;
import me.penguinx13.wapi.commands.core.error.ValidationException;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;
import me.penguinx13.wapi.commands.core.validation.Validator;

import java.util.Locale;
import java.util.Set;

public final class NotReservedNameValidator implements Validator<NotReservedName> {
    private static final Set<String> RESERVED = Set.of("admin", "console", "system", "root");

    @Override
    public Class<NotReservedName> annotationType() {
        return NotReservedName.class;
    }

    @Override
    public void validate(ArgumentMetadata meta, Object value, NotReservedName annotation) {
        if (value == null) return;
        String normalized = String.valueOf(value).toLowerCase(Locale.ROOT);
        if (RESERVED.contains(normalized)) {
            throw new ValidationException(meta.name() + " uses reserved name");
        }
    }
}
