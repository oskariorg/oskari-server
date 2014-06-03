package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.net.URL;

/**
 * Convenience methods for handling http requests.
 */
public class RequestHelper {

    private static final Logger log = LogFactory.getLogger(RequestHelper.class);

    /**
     * Cleans up any XSS threats from given string. Removes all HTML tags also.
     * @param str
     * @return cleaned up version or null if param was null
     */
    public static final String cleanString(final String str) {
        if (str != null) {
            String s = Jsoup.clean(str, Whitelist.none());
            return StringEscapeUtils.unescapeHtml(s);
        }
        return str;
    }

    public static final String cleanHTMLString(final String str) {
        if (str != null) {
            Whitelist whitelist = Whitelist.relaxed();
            whitelist.addTags(
                    "button",
                    "datalist",
                    "fieldset",
                    "form",
                    "input",
                    "keygen",
                    "label",
                    "legend",
                    "option",
                    "optgroup",
                    "output",
                    "select",
                    "textarea"
            );
            String s = Jsoup.clean(str, whitelist);
            return StringEscapeUtils.unescapeHtml(s);
        }
        return str;
    }

    /**
     * Cleans up the string but returns defaultValue if given str was null
     * @see #cleanString(String)
     * @param str
     * @param defaultValue
     * @return
     */
    public static final String getString(final String str, final String defaultValue) {
        final String value = cleanString(str);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    /**
     * Returns host part from given url
     * @param referrer url to parse
     * @return host part or the URL or "" if an invalid url was given
     */
    public static final String getDomainFromReferer(final String referrer) {

        try {
            final URL url = new URL(referrer);
            return url.getHost();
        } catch (Exception e) {
            log.error("Error getting referer from URL:", referrer);
        }
        return "";
    }

}
