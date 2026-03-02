package me.penguinx13.wapi.commands.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RootCommand {
    String value();
    String permission() default "";
}
