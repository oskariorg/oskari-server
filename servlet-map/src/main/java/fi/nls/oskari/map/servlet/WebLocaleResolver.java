package fi.nls.oskari.map.servlet;

import fi.nls.oskari.util.PropertyUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Resolves locale based on http param with state handled with cookie.
 */
public class WebLocaleResolver {
    private String localeHttpParam = "lang";
    private String cookieName = "oskari.language";

    private final String[] supportedLocales;
    private final Locale defaultLocale;

    public WebLocaleResolver() {
        this.supportedLocales = PropertyUtil.getSupportedLocales();
        this.defaultLocale = new Locale(PropertyUtil.getDefaultLanguage());
    }

    public void setLocaleHttpParam(String localeHttpParam) {
        this.localeHttpParam = localeHttpParam;
    }

    public Locale resolveLocale(HttpServletRequest request, HttpServletResponse response) {

        boolean haveCookie = false;

        // possible Query parameter always overrides cookie value
        String requestedLocale = request.getParameter(localeHttpParam);
        if (requestedLocale == null) {
            // no parameter, check cookie
            requestedLocale = getFromCookies(request.getCookies());
            haveCookie = (requestedLocale != null);
        }

        if (isSupported(requestedLocale)) {
            if (!haveCookie) {
                Cookie cookie = new Cookie(cookieName, requestedLocale);
                cookie.setPath(request.getContextPath());
                cookie.setSecure(request.isSecure());
                response.addCookie(cookie);
            }
            return new Locale(requestedLocale);
        }

        return defaultLocale;
    }

    private String getFromCookies(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private boolean isSupported(final String requestedLocale) {
        if(requestedLocale == null) {
            return false;
        }
        for (String supportedLocale : supportedLocales) {
            if (supportedLocale.equals(requestedLocale)) {
                return true;
            }
        }
        return false;
    }

}
