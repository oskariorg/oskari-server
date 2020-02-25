package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.oskari.util.HtmlHelper;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience methods for handling http requests.
 *
 * Passthrough calls to HtmlHelper for backwards compatibility.
 */
public class RequestHelper {

    private static final Logger log = LogFactory.getLogger(RequestHelper.class);

    /**
     * Cleans up any XSS threats from given string. Removes all HTML tags also.
     * @param str
     * @return cleaned up version or null if param was null
     */
    public static final String cleanString(final String str) {
        return HtmlHelper.cleanString(str);
    }

    public static final String cleanHTMLString(final String str, final String[] tags,
            HashMap<String, String[]> attributes, HashMap<String[],String[]> protocols) {
        return HtmlHelper.cleanHTMLString(str, tags, attributes, protocols);
    }

    public static final String cleanHTMLString(final String str) {
        return HtmlHelper.cleanHTMLString(str);
    }

    /**
     * Cleans up the string but returns defaultValue if given str was null
     * @see #cleanString(String)
     * @param str
     * @param defaultValue
     * @return
     */
    public static final String getString(final String str, final String defaultValue) {
        return HtmlHelper.getString(str, defaultValue);
    }

    /**
     * Returns host part from given url
     * @param referrer url to parse
     * @return host part or the URL or "" if an invalid url was given
     */
    public static final String getDomainFromReferer(final String referrer) {
        if(referrer == null) {
            return "";
        }
        try {
            final URL url = new URL(referrer);
            return url.getHost().toLowerCase();
        } catch (Exception e) {
            log.warn("Error getting referer from URL:", referrer);
        }
        return "";
    }

    public static Map<String, String> parsePrefixedParamsMap(final HttpServletRequest request, final String paramNamePrefix) {
        final Map<String, String> result = new HashMap<String, String>();
        final int prefixLength = paramNamePrefix.length();
        final Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final String nextName = paramNames.nextElement();
            if (nextName.indexOf(paramNamePrefix) == 0) {
                result.put(nextName.substring(prefixLength), request.getParameter(nextName));
            }
        }
        return result;
    }
}
