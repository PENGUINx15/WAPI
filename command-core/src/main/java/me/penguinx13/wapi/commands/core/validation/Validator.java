package me.penguinx13.wapi.commands.core.validation;

import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;

import java.lang.annotation.Annotation;

public interface Validator<A extends Annotation> {
    Class<A> annotationType();
    void validate(ArgumentMetadata meta, Object value, A annotation);
}
