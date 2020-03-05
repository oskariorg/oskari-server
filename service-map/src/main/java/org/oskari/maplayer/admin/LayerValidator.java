package org.oskari.maplayer.admin;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
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

    private static final Logger LOG = LogFactory.getLogger(LayerValidator.class);
    private static Map<String, Set<String>> MANDATORY_FIELDS = new HashMap<>();
    static {
        MANDATORY_FIELDS.put(OskariLayer.TYPE_WMS, ConversionHelper.asSet("name", "url", "version"));
        MANDATORY_FIELDS.put(OskariLayer.TYPE_WFS, ConversionHelper.asSet("name", "url", "version"));
        MANDATORY_FIELDS.put(OskariLayer.TYPE_WMTS, ConversionHelper.asSet("name", "url"));
        MANDATORY_FIELDS.put(OskariLayer.TYPE_VECTOR_TILE, ConversionHelper.asSet("url"));
        MANDATORY_FIELDS.put(OskariLayer.TYPE_ARCGIS93, ConversionHelper.asSet("url"));
        MANDATORY_FIELDS.put(OskariLayer.TYPE_ARCGIS_CACHE, ConversionHelper.asSet("url"));
        MANDATORY_FIELDS.put(OskariLayer.TYPE_3DTILES, Collections.emptySet());
    }

    private static final Set<String> RESERVED_PARAMS = ConversionHelper.asSet("service", "request", "version");

    public static Map<String, Map<String, String>> validateLocale(Map<String, Map<String, String>> locale) throws IllegalArgumentException {
        if (locale == null) {
            throw new IllegalArgumentException("Localization for layer names missing");
        }
        String lang = PropertyUtil.getDefaultLanguage();
        Map<String, String> langLocale = locale.getOrDefault(lang, Collections.emptyMap());
        if (langLocale.get("name") == null) {
            // name for default language is required
            throw new IllegalArgumentException("Name missing for default language: " + lang);
        }
        return locale;
    }

    /**
     * Removes reserved params from url (service/request/version) and returns a sanitized url
     * @param url
     * @return
     * @throws IllegalArgumentException if parameter is not a proper url
     */
    public static String sanitizeUrl(final String url) throws IllegalArgumentException {
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
            throw new IllegalArgumentException("Invalid url: " + url);
        }
    }

    public static String sanitizeGFIContent(String gfiContent) {
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

    /**
     * Returns a list of fields/type that layer type requires to work technically
     * In addition there are things like layer "locale" == the name that is shown to user that is required.
     * @param type
     * @return
     */
    public static Set<String> getMandatoryFields(String type) {
        if (type == null || !MANDATORY_FIELDS.containsKey(type)) {
            return Collections.emptySet();
        }
        return MANDATORY_FIELDS.get(type);
    }

    public static Set<String> getRecognizerLayerTypes() {
        return MANDATORY_FIELDS.keySet();
    }

    /**
     * Validates input to be saved as a maplayer to the databse
     * Note! Might modify values in input object like URL!!!!!
     * @param input
     * @throws IllegalArgumentException if input is not valid and can't be automatically fixed
     */
    public static void validateAndSanitizeLayerInput(MapLayer input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("Layer data missing");
        }
        // TODO: add more validation for values
        if (!hasValue(input.getType())) {
            throw new IllegalArgumentException("Required field missing 'type'");
        }
        Set<String> recognizedTypes = getRecognizerLayerTypes();
        if (!recognizedTypes.contains(input.getType())) {
            throw new IllegalArgumentException("Layer type is not recognized: " + input.getType() +
                    ". Recognized types are: " + LOG.getAsString(recognizedTypes));
        }

        Set<String> mandatoryFields = getMandatoryFields(input.getType());
        for (String field: mandatoryFields) {
            if (!hasValue(input, field)) {
                throw new IllegalArgumentException("Required field missing '" + field + "'");
            }
        }
        if (mandatoryFields.contains("url")) {
            // if url is mandatory -> validate that it's usable
            input.setUrl(LayerValidator.sanitizeUrl(input.getUrl()));
        }
        // at least default language must have name for any layer type
        input.setLocale(LayerValidator.validateLocale(input.getLocale()));
        // Run HTML-content through JSOUP
        input.setGfi_content(LayerValidator.sanitizeGFIContent(input.getGfi_content()));
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
