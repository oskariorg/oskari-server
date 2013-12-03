package fi.nls.oskari.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OskariActionRoute annotation is used to detect ActionHandler services. At compile time the annotated
 * ActionHandler are registered as SPI services and on runtime the value of the annotation is used
 * as the router key when registering it as a handler.
 * @see fi.nls.oskari.control.ActionHandler
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OskariActionRoute {

    /**
     * Returns the key which should be used when registering the ActionHandler
     * @return
     */
    String value() default "";
}
