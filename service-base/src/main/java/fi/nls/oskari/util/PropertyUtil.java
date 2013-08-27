package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.log.NullLogger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PropertyUtil {

    private final static Properties properties = new Properties();
    private final static Map<Locale, Properties> localization = new HashMap<Locale, Properties>();
    private final static Locale DEFAULT_LOCALE = new Locale("fi");

    // this cannot be fetched from LogFactory on init since LogFactory uses PropertyUtil -> results a ExceptionInInitializerError
    private static Logger log = new NullLogger(PropertyUtil.class.getCanonicalName());

    /**
     * PropertyUtil defaults to NullLogger. Set a different logger with this method if needed.
     * @param logger
     */
    public static void setLogger(Logger logger) {
        log = logger;
    }

    public static String getNecessary(final String propertyName) {
        if (!properties.contains(propertyName)) {
            throw new RuntimeException("Missing necassary property:"+propertyName);
        }
        return properties.getProperty(propertyName);
    }

    public static String[] getCommaSeparatedList(final String propertyName) {

        final String propertiesList = get(DEFAULT_LOCALE, propertyName, null);
        if (propertiesList == null || propertiesList.isEmpty()) {
            return new String[0];
        }
        // return separated properties as list
        return propertiesList.split("\\s*,\\s*");
    }

    public static String getOptional(final String propertyName) {
        return get(DEFAULT_LOCALE, propertyName, null);
    }

    public static String get(final String propertyName) {
        return get(DEFAULT_LOCALE, propertyName, "--" + propertyName + "--");
    }

    public static String get(final String propertyName, final String defaultValue) {
        return get(DEFAULT_LOCALE, propertyName, defaultValue);
    }
    public static String get(final Locale locale, final String propertyName) {
        return get(locale, propertyName, "--" + propertyName + "--");
    }

    public static String get(final Locale locale, final String propertyName, final String defaultValue) {
        if(localization.containsKey(locale)) {
            final String val = localization.get(locale).getProperty(propertyName);
            if(val != null) {
                return val;
            }
        }
        // not found in localized, check generic properties
        if(!properties.containsKey(propertyName)) {
            if(defaultValue != null) {
                log.warn("Missing property >", propertyName, "< for locale:", locale.getLanguage());
            }
            if(properties.isEmpty()) {
                log.error("Properties are empty. Preload them on deploy before calling services!");
            }
            return defaultValue;
        }

        return properties.getProperty(propertyName);
    }

    /**
     * Returns property names that start with given prefix
     * @param prefix start of the property name
     * @return List of matching property names
     */
    public static List<String> getPropertyNamesStartingWith(final String prefix) {
        final List<String> props = new ArrayList<String>();
        for(Object o : properties.keySet()) {
            final String key = ((String)o);
            if(key.startsWith(prefix)) {
                props.add(key);
            }
        }
        log.debug("Tried to find properties starting with'", prefix, "' - found ", props);
        return props;
    }
    /**
     * Returns property names that match the given regular expression
     * @param regex Regular expression
     * @return List of matching property names
     */
    public static List<String> getMatchingPropertyNames(final String regex) {
        final List<String> props = new ArrayList<String>();
        final Enumeration e = properties.propertyNames();
        final Pattern p = Pattern.compile(regex);

        while( e.hasMoreElements() ) {
            final String key = (String) e.nextElement();
            final Matcher m = p.matcher(key);
            if (m.matches()) {
                props.add(key);
            }
        }
        log.debug("Tried to find properties with regexp '",regex, "' - found ", props);
        return props;
    }

    public static void addProperties(final Properties props, final boolean overwrite) throws DuplicateException {
        for(Object key : props.keySet()) {
            addProperty((String)key, props.getProperty((String)key), overwrite);
        }
    }

    public static void addProperties(final Properties props) throws DuplicateException {
        addProperties(props, false);
    }
    
    public static void addProperty(final String key, final String value) throws DuplicateException {
        addProperty(key, value, false);
    }
    
    public static void addProperty(final String key, final String value, final boolean overwrite) throws DuplicateException {
        if(properties.containsKey(key)) {
            if(overwrite) {
                log.info("Overwriting property", key, "value:", properties.get(key), " -> ", value);
            }
            else {
                throw new DuplicateException("Key already exists: " + key + " with value: " + properties.getProperty(key) +
                        ". Tried to overwrite with value: " + value);
            }
        }
        properties.put(key, value);
    }
    
    private static void addProperty(final Properties props, final String key, final String value, final boolean overwrite) throws DuplicateException {
        if(props.containsKey(key)) {
            if(overwrite) {
                log.info("Overwriting property", key, "value:", props.get(key), " -> ", value);
            }
            else {
                throw new DuplicateException("Key already exists: " + key + " with value: " + props.getProperty(key) +
                        ". Tried to overwrite with value: " + value);
            }
        }
        props.put(key, value);
    }
    public static void addProperty(final String key, final String value, final Locale locale) throws DuplicateException {

        if(!localization.containsKey(locale)) {
            localization.put(locale, new Properties());
        }
        final Properties locProps = localization.get(locale);
        addProperty(locProps, key, value, false);
    }
}
