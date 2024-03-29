package org.oskari.util;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.HashMap;
import java.util.Map;

public class HtmlHelper {

    /**
     * Cleans up any XSS threats from given string. Removes all HTML tags also.
     * @param str
     * @return cleaned up version or null if param was null
     */
    public static final String cleanString(final String str) {
        if (str != null) {
            String s = Jsoup.clean(str, Safelist.none());
            return StringEscapeUtils.unescapeHtml4(s);
        }
        return str;
    }

    public static final String cleanHTMLString(final String str, final String[] tags,
                                               HashMap<String, String[]> attributes, HashMap<String[],String[]> protocols) {

        if (str != null) {
            Safelist safelist = Safelist.relaxed();

            // Tags
            safelist.addTags(tags);

            // Attributes
            for (Map.Entry<String, String[]> attribute : attributes.entrySet()) {
                safelist.addAttributes(attribute.getKey(),attribute.getValue());
            }

            // Protocols
            for (Map.Entry<String[], String[]> protocol : protocols.entrySet()) {
                String[] key = protocol.getKey();
                if ((key == null)||(key.length < 2)) {
                    continue;
                }
                safelist.addProtocols(key[0],key[1],protocol.getValue());
            }
            String s = Jsoup.clean(str, safelist);
            return StringEscapeUtils.unescapeHtml4(s);
        }
        return str;
    }

    public static final String cleanHTMLString(final String str) {
        if (str != null) {
            Safelist safelist = Safelist.relaxed();
            safelist.addTags(
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
            String s = Jsoup.clean(str, safelist);
            return StringEscapeUtils.unescapeHtml4(s);
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
}
