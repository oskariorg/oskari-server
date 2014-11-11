package fi.nls.oskari.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Oskari annotation is used to detect services in Oskari backend. At compile time the annotated
 * classes are registered as SPI services and on runtime the value of the annotation is used
 * as based on the type of the component.
 * @see fi.nls.oskari.service.OskariComponent
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Oskari {

    String value() default "";
}
