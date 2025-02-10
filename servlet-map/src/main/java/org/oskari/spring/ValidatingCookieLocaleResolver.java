package org.oskari.spring;

import java.util.Locale;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

/**
 * Spring MVC LocaleResolver implementation that adds support for restricting allowed locales.
 */
public class ValidatingCookieLocaleResolver extends CookieLocaleResolver {


    protected String[] supportedLocales;
    protected String[] supportedLanguages;

    public ValidatingCookieLocaleResolver() {
        super();
    }

    public void setSupportedLocales(String[] locales) {
        this.supportedLocales = locales;
    }

    public void setSupportedLanguages(String[] languages) {
        this.supportedLanguages = languages;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        Locale validatedLocale = validateLocale(request, locale);
        super.setLocale(request, response, validatedLocale);
    }
    private Locale validateLocale(HttpServletRequest request, Locale locale) {
        if (supportedLocales != null && locale != null) {
            for (String supportedLocale : supportedLocales) {
                if (supportedLocale.equals(locale.getLanguage())) {
                    StringUtils.parseLocaleString(supportedLocale);
                    request.setAttribute(CookieLocaleResolver.LOCALE_REQUEST_ATTRIBUTE_NAME, supportedLocale);
                    return StringUtils.parseLocaleString(supportedLocale);
                }
                if (supportedLocale.startsWith(locale.getLanguage())) {
                    return StringUtils.parseLocaleString(supportedLocale);
                }
            }
        } else if (locale != null) {
            // this means someone called lang=[no value], don't set it as language as it breaks things later on
            // -> instead set the default locale
            request.setAttribute(CookieLocaleResolver.LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
            return locale;
        }
        return super.determineDefaultLocale(request);
    }

}
