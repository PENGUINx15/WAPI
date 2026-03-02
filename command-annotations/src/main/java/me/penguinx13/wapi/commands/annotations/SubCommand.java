package me.penguinx13.wapi.commands.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubCommand {
    String value();
    String permission() default "";
    boolean playerOnly() default false;
    String description() default "";
}
