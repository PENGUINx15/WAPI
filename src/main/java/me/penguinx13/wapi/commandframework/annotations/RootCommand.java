package me.penguinx13.wapi.commandframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RootCommand {
    String value();

    String permission() default "";

    boolean playerOnly() default false;

    String description() default "";
}
