package me.lyric.skyfall.api.event.bus;

import java.lang.annotation.*;

/**
 * @author lyric
 * event system interface
 * credit to serenity for the name
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface ITheAnnotation {
    int priority() default 1000;
}
