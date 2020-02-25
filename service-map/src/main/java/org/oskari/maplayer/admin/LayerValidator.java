package org.oskari.maplayer.admin;

import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.util.HtmlHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class LayerValidator {

    private static final Set<String> RESERVED_PARAMS = ConversionHelper.asSet("service", "request", "version");

    public static Map<String, Map<String, String>> validateLocale(Map<String, Map<String, String>> locale) throws ServiceRuntimeException {
        if (locale == null) {
            throw new ServiceRuntimeException("Localization for layer names missing");
        }
        String lang = PropertyUtil.getDefaultLanguage();
        Map<String, String> langLocale = locale.getOrDefault(lang, Collections.emptyMap());
        if (langLocale.get("name") == null) {
            // name for default language is required
            throw new ServiceRuntimeException("Name missing for default language: " + lang);
        }
        return locale;
    }

    public static String validateUrl(final String url) throws ServiceRuntimeException {
        try {
            String baseURL = IOHelper.removeQueryString(url);
            // remove problematic parameters from URL
            Map<String, List<String>> params = IOHelper.parseQuerystring(url);
            List<String> problematicParams = params.keySet()
                    .stream().filter(key -> RESERVED_PARAMS.contains(key.toLowerCase())).collect(Collectors.toList());
            // TODO: these are only problematic for OGC urls. Maybe we should skip validation for non-OGC layers?
            for (String param : problematicParams) {
                params.remove(param);
            }
            String querystring = IOHelper.createQuerystring(params);
            // return whitelisted params and base url
            return IOHelper.addQueryString(baseURL, querystring);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Invalid url: " + url, "invalid_field_value");
        }
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
        return HtmlHelper.cleanHTMLString(gfiContent, tags, attributes, protocols);
    }

}
