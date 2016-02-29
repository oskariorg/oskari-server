package fi.nls.oskari.spring.extension;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Locale;

/**
 * OskariViewResolver extends InternalResourceViewResolver, but doesn't give HTTP 404 if page is not found.
 * Passes control to next view resolver if resource is not found.
 */
public class OskariViewResolver extends InternalResourceViewResolver  {

    /**
     * Checks if the resource is present:
     * - if NOT found -> pass control to next view resolver
     * - else -> use it
     *
     * The InternalResourceViewResolver gives 404 if view is not present
     */
    protected View loadView(String viewName, Locale locale) throws Exception {
        if (getServletContext().getResourceAsStream(getPrefix() + viewName + getSuffix()) == null) {
            return null;
        }
        return super.loadView(viewName, locale);
    }
}
