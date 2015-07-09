package fi.nls.oskari.servlet;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Resolves locale based on http param with state handled with cookie.
 */
public class WebLocaleResolver {
    private static final Logger LOG = LogFactory.getLogger(WebLocaleResolver.class);
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


        // possible Query parameter always overrides cookie value
        String requestedLocale = request.getParameter(localeHttpParam);
        Cookie cookie = getCookie(request.getCookies());
        LOG.debug("Language param:", requestedLocale);
        if (requestedLocale == null && cookie != null) {
            // no parameter, check cookie
            requestedLocale = cookie.getValue();
            LOG.debug("Cookie language:", requestedLocale);
        }
        final boolean supportedLanguage = isSupported(requestedLocale);
        if (!supportedLanguage) {
            return defaultLocale;
        }
        if (cookie == null) {
            cookie = new Cookie(cookieName, requestedLocale);
            cookie.setPath("/"); // request.getContextPath()
            cookie.setSecure(request.isSecure());
        } else {
            cookie.setValue(requestedLocale);
        }
        // add or update language-cookie value
        response.addCookie(cookie);
        return getLocale(requestedLocale);
    }

    private Locale getLocale(final String requestedLocale) {
        final String[] split = requestedLocale.split("\\_");
        // TODO: setup country if found
        return new Locale(split[0]);
    }

    private Cookie getCookie(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    private boolean isSupported(final String requestedLocale) {
        if(requestedLocale == null) {
            return false;
        }
        String closeMatch = null;
        for (String supportedLocale : supportedLocales) {
            if (supportedLocale.equals(requestedLocale)) {
                return true;
            }
            if(supportedLocale.startsWith(requestedLocale)) {
                closeMatch = supportedLocale;
            }
        }
        return closeMatch != null;
    }

}
