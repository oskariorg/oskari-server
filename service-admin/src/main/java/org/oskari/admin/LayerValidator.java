package org.oskari.admin;

import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.RequestHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayerValidator {

    public static Map<String, Map<String, String>> validateLocale(Map<String, Map<String, String>> locale) throws ServiceRuntimeException {
        String lang = PropertyUtil.getDefaultLanguage();
        Map<String, String> langLocale = locale.getOrDefault(lang, Collections.emptyMap());
        if (langLocale.get("name") == null) {
            // name for default language is required
            throw new ServiceRuntimeException("Name missing for default language: " + lang);
        }
        return locale;
    }

    public static String validateUrl(final String url) throws ServiceRuntimeException {
        // TODO remove query part with ? check or with URL object
        // TODO: can we remove the whole querystring or should we just remove problematic params like service and request?
        String baseUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        try {
            // check that it's a valid url by creating an URL object...
            new URL(url);
        } catch (MalformedURLException e) {
            throw new ServiceRuntimeException("Invalid url: " + url, "invalid_field_value");
        }
        return baseUrl;
    }

    public static String cleanGFIContent(String gfiContent) {
        if (gfiContent == null) {
            return null;
        }
        // Clean GFI content
        final String[] tags = PropertyUtil.getCommaSeparatedList("gficontent.whitelist");
        HashMap<String, String[]> attributes = new HashMap<>();
        HashMap<String[], String[]> protocols = new HashMap<>();
        String[] allAttributes = PropertyUtil.getCommaSeparatedList("gficontent.whitelist.attr");
        if (allAttributes.length > 0) {
            attributes.put(":all", allAttributes);
        }
        List<String> attrProps = PropertyUtil.getPropertyNamesStartingWith("gficontent.whitelist.attr.");
        for (String attrProp : attrProps) {
            String[] parts = attrProp.split("\\.");
            if (parts[parts.length - 2].equals("protocol")) {
                protocols.put(new String[]{parts[parts.length - 3], parts[parts.length - 1]}, PropertyUtil.getCommaSeparatedList(attrProp));
            } else {
                attributes.put(parts[parts.length - 1], PropertyUtil.getCommaSeparatedList(attrProp));
            }
        }
        return RequestHelper.cleanHTMLString(gfiContent, tags, attributes, protocols);
    }

}
