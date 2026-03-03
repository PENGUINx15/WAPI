package me.penguinx13.wapi.commands.core.validation;

import me.penguinx13.wapi.commands.annotations.Max;
import me.penguinx13.wapi.commands.annotations.Min;
import me.penguinx13.wapi.commands.annotations.Range;
import me.penguinx13.wapi.commands.annotations.Regex;
import me.penguinx13.wapi.commands.core.error.ValidationException;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ValidationService {
    private final Map<Class<? extends Annotation>, Validator<?>> validators = new ConcurrentHashMap<>();

    public ValidationService() {
        register(new RangeValidator());
        register(new MinValidator());
        register(new MaxValidator());
        register(new RegexValidator());
    }

    public void register(Validator<?> validator) {
        validators.put(validator.annotationType(), validator);
    }

    public void validate(ArgumentMetadata meta, Object value) {
        for (Annotation annotation : meta.validationAnnotations()) {
            @SuppressWarnings("unchecked")
            Validator<Annotation> validator = (Validator<Annotation>) validators.get(annotation.annotationType());
            if (validator != null) {
                validator.validate(meta, value, annotation);
            }
        }
    }

    static final class RangeValidator implements Validator<Range> {
        public Class<Range> annotationType() {
            return Range.class;
        }

        public void validate(ArgumentMetadata meta, Object value, Range annotation) {
            if (!(value instanceof Number n)) {
                return;
            }
            double number = n.doubleValue();
            if (number < annotation.min() || number > annotation.max()) {
                throw new ValidationException(
                        meta.name() + " must be in range [" + annotation.min() + ", " + annotation.max() + "]"
                );
            }
        }
    }

    static final class MinValidator implements Validator<Min> {
        public Class<Min> annotationType() {
            return Min.class;
        }

        public void validate(ArgumentMetadata meta, Object value, Min annotation) {
            if (value instanceof Number n && n.longValue() < annotation.value()) {
                throw new ValidationException(meta.name() + " must be >= " + annotation.value());
            }
        }
    }

    static final class MaxValidator implements Validator<Max> {
        public Class<Max> annotationType() {
            return Max.class;
        }

        public void validate(ArgumentMetadata meta, Object value, Max annotation) {
            if (value instanceof Number n && n.longValue() > annotation.value()) {
                throw new ValidationException(meta.name() + " must be <= " + annotation.value());
            }
        }
    }

    static final class RegexValidator implements Validator<Regex> {
        public Class<Regex> annotationType() {
            return Regex.class;
        }

        public void validate(ArgumentMetadata meta, Object value, Regex annotation) {
            if (value != null && !String.valueOf(value).matches(annotation.value())) {
                throw new ValidationException(meta.name() + " has invalid format");
            }
        }
    }
}
