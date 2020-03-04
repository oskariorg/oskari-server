package org.oskari.maplayer.admin;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.maplayer.model.MapLayer;
import org.oskari.util.HtmlHelper;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class LayerValidator {

    private static Map<String, Set<String>> MANDATORY_FIELDS = new HashMap<>();
    static {
        MANDATORY_FIELDS.put(OskariLayer.TYPE_WMS, ConversionHelper.asSet("name", "url", "version"));
        MANDATORY_FIELDS.put(OskariLayer.TYPE_WFS, ConversionHelper.asSet("name", "url", "version"));
        MANDATORY_FIELDS.put(OskariLayer.TYPE_WMTS, ConversionHelper.asSet("name", "url", "version"));
        MANDATORY_FIELDS.put(OskariLayer.TYPE_VECTOR_TILE, ConversionHelper.asSet("url"));
        MANDATORY_FIELDS.put(OskariLayer.TYPE_ARCGIS93, ConversionHelper.asSet("url"));
        //MANDATORY_FIELDS.put(OskariLayer.TYPE_3DTILES, ConversionHelper.asSet("url"));
    }

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

    // TODO: relay mandatory field info to frontend for UI so frontend can show what is mandatory
    //  and validate before server is even called
    public static Set<String> getMandatoryFields(String type) {
        if (type == null || !MANDATORY_FIELDS.containsKey(type)) {
            return Collections.emptySet();
        }
        return MANDATORY_FIELDS.get(type);
    }

    /**
     * Validates input to be saved as a maplayer to the databse
     * Note! Might modify values in input object like URL!!!!!
     * @param input
     * @throws ServiceRuntimeException if input is not valid and can't be automatically fixed
     */
    public static void validateLayerInput(MapLayer input) throws ServiceRuntimeException {
        // TODO: add more validation for values
        if (!hasValue(input.getType())) {
            throw new ServiceRuntimeException("Required field missing 'type'");
        }

        Set<String> mandatoryFields = getMandatoryFields(input.getType());
        for (String field: mandatoryFields) {
            if (!hasValue(input, field)) {
                throw new ServiceRuntimeException("Required field missing '" + field + "'");
            }
        }
        if (mandatoryFields.contains("url")) {
            // if url is mandatory -> validate that it's usable
            input.setUrl(LayerValidator.validateUrl(input.getUrl()));
        }
        // at least default language must have name for any layer type
        input.setLocale(LayerValidator.validateLocale(input.getLocale()));
        // Run HTML-content through JSOUP
        input.setGfi_content(LayerValidator.cleanGFIContent(input.getGfi_content()));
    }

    /**
     * Tries to find a getter for field through reflection and assumes it returns a string.
     * throws an exception if value is not set or is empty or getter can't be called.
     * @param input maplayer input for initial db or admin functionality
     * @param field name of the field to search for "url" for getUrl() etc. case-insensitive
     * @return
     */
    private static boolean hasValue(MapLayer input, String field) {
        for (Method m : MapLayer.class.getDeclaredMethods()) {
            if (!m.getName().toLowerCase().equals("get" + field.toLowerCase())) {
                continue;
            }
            try {
                String value = (String) m.invoke(input);
                return hasValue(value);
            } catch (Exception e) {
                throw new ServiceRuntimeException("Unable to call getter for " + field, e);
            }
        }
        return false;
    }

    private static boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
