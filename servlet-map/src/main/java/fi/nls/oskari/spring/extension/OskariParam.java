package fi.nls.oskari.spring.extension;

import java.lang.annotation.*;

/**
 * Annotation marker for Spring to process RequestMapping method parameter
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OskariParam {
}
