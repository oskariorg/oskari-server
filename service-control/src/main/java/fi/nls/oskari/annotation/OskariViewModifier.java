package fi.nls.oskari.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OskariViewModifier annotation is used to detect components that modify a Oskari apps view at runtime (when loading from db).
 * Modifiers are registered as SPI services and on runtime the value of the annotation is used based on the implementing classes
 * type. It can be f.ex. an http parameter name or name of a bundle that should be processed.
 * @see fi.nls.oskari.view.modifier.ViewModifierManager
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OskariViewModifier {

    /**
     * Returns the key which should be used when registering the modifier
     * @return
     */
    String value() default "";
}
