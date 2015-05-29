package fi.nls.oskari.util;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.log.NullLogger;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PropertyUtil {

    private final static Properties properties = new Properties();
    private final static ConcurrentMap<Locale, Properties> localization = new ConcurrentHashMap<Locale, Properties>();

    // this cannot be fetched from LogFactory on init since LogFactory uses PropertyUtil -> results a ExceptionInInitializerError
    private static Logger log = new NullLogger(PropertyUtil.class.getCanonicalName());

    public static String[] getSupportedLocales() {
        String sl = properties.getProperty("oskari.locales", null);
        if (sl == null) {
            sl = "en_US";

        }
        return sl.split("\\s*,\\s*");
    }

    public static String getDefaultLocale() {
        String[] supportedLocales = getSupportedLocales();
        if (supportedLocales != null && supportedLocales.length > 0 && supportedLocales[0] != null) {
            return supportedLocales[0];
        }
        return "en_US";
    }

    public static String[] getSupportedLanguages() {
        String[] supportedLanguages = getSupportedLocales();
        log.error("getSupportedLanguages", supportedLanguages.length);
        for (int i = 0; i < supportedLanguages.length; i++) {
            supportedLanguages[i] = supportedLanguages[i].split("_")[0];
        }
        log.error("returning");
        return supportedLanguages;
    }

    public static String getDefaultLanguage() {
        return getDefaultLocale().split("_")[0];
    }

    /**
     * PropertyUtil defaults to NullLogger. Set a different logger with this method if needed.
     * @param logger
     */
    public static void setLogger(Logger logger) {
        log = logger;
    }

    static Set<String> propertiesFiles = new HashSet<String>();

    /**
     * Loads given properties file relative to PropertyUtil class (use root relative path "/my-properties.properties").
     * Overwrites any existing properties
     * @param propertiesFile
     */
    public static void loadProperties(final String propertiesFile) {
        loadProperties(propertiesFile, true);
    }

    /**
     * Loads given properties file relative to PropertyUtil class.
     * @param propertiesFile use root relative path "/my-properties.properties" for this to be useful.
     * @param overwrite true to overwrite existing properties
     */
    public static void loadProperties(final String propertiesFile, final boolean overwrite) {
        if (propertiesFiles.contains(propertiesFile)) {
            return;
        }
        InputStream in = null;
        try {
            Properties prop = new Properties();
            in = PropertyUtil.class.getResourceAsStream(propertiesFile);
            prop.load(in);
            addProperties(prop, overwrite);

            propertiesFiles.add(propertiesFile);

        } catch (Exception ignored) {

        } finally {
            IOHelper.close(in);
        }
    }
    /**
     * Clears all previously loaded properties, use with caution!
     */
    public static void clearProperties() {
        properties.clear();
        localization.clear();
    }

    public static String getNecessary(final String propertyName) {
        return getNecessary(propertyName, null);
    }

    /**
     * Returns a property value or throws an exception if it doesn't exist.
     * @param propertyName  property to find
     * @param detailMessage if not null, message is attached to the exception that is thrown. Use it to describe why the property is necessary.
     * @return
     */
    public static String getNecessary(final String propertyName, final String detailMessage) {
        String prop = getOptional(propertyName);
        if (prop == null) {
            String msg = "Missing necessary property: " + propertyName;
            if(detailMessage != null) {
                msg = msg + ". Message: " + detailMessage;
            }
            throw new RuntimeException(msg);
        }
        return prop;
    }

    public static String[] getCommaSeparatedList(final String propertyName) {
        final String propertiesList = get(new Locale(getDefaultLanguage()), propertyName, null);
        if (propertiesList == null || propertiesList.isEmpty()) {
            return new String[0];
        }
        // return separated properties as list
        return propertiesList.split("\\s*,\\s*");
    }

    /**
     * Returns a property formatted like this: key1|val1,key2|val2 as a map.
     * @param propertyName
     * @return
     */
    public static Map<String, String> getMap(final String propertyName) {

        final String[] propertiesList = getCommaSeparatedList(propertyName);
        if (propertiesList.length == 0) {
            return Collections.emptyMap();
        }
        final Map<String, String> map = new HashMap<String, String>();
        for(String str : propertiesList) {
            log.debug(str);
            final String[] vals = str.split("\\s*\\|\\s*");
            log.debug(vals);
            if(vals.length == 2) {
                map.put(vals[0], vals[1]);
            }
        }
        return map;
    }

    public static String getOptionalNonLocalized(final String propertyName) {
        return properties.getProperty(propertyName);
    }

    public static String getOptional(final String propertyName) {
        return get(new Locale(getDefaultLanguage()), propertyName, null);
    }

    public static String get(final String propertyName) {
        return get(new Locale(getDefaultLanguage()), propertyName, "--" + propertyName + "--");
    }

    public static String get(final String propertyName, final String defaultValue) {
        return get(new Locale(getDefaultLanguage()), propertyName, defaultValue);
    }

    public static int getOptional(final String propertyName, final int defaultValue) {
        final String prop = getOptional(propertyName);
        return ConversionHelper.getInt(prop, defaultValue);
    }

    public static double getOptional(final String propertyName, final double defaultValue) {
        final String prop = getOptional(propertyName);
        return ConversionHelper.getDouble(prop, defaultValue);
    }
    public static boolean getOptional(final String propertyName, final boolean defaultValue) {
        final String prop = getOptional(propertyName);
        return ConversionHelper.getBoolean(prop, defaultValue);
    }

    public static String get(final Locale locale, final String propertyName) {
        return get(locale, propertyName, "--" + propertyName + "--");
    }

    public static String get(final Locale locale, final String propertyName, final String defaultValue) {
        if (localization.containsKey(locale)) {
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

    public static Object getLocalizableProperty(final String key) {
        return getLocalizableProperty(key, null);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static Object getLocalizableProperty(final String key, final String defaultValue) {
        List<String> names = PropertyUtil.getPropertyNamesStartingWith(key);
        if(names.size() > 1) {
            Map<String, String> values = new HashMap<String, String>(names.size());
            final int prefixLength = key.length() + 1;
            for (String singleKey : names) {
                if(key.equals(singleKey)) {
                    // skip the non-localized version
                    continue;
                }
                final String value = get(singleKey, defaultValue);
                final String modifier = singleKey.substring(prefixLength);
                values.put(modifier, value);
            }
            return values;
        }

        return get(key, defaultValue);
    }

    /**
     * Returns property names that start with given prefix
     * @param prefix start of the property name
     * @return List of matching property names
     */
    public static List<String> getPropertyNamesStartingWith(final String prefix) {
        final List<String> props = new ArrayList<String>();
        for (Object o : properties.keySet()) {
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

        while (e.hasMoreElements()) {
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
        for (final Object key : props.keySet()) {
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
        properties.put(key, value.trim());
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
        props.put(key, value.trim());
    }

    public static void addProperty(final String key, final String value, final Locale locale) throws DuplicateException {
        if(!localization.containsKey(locale)) {
            localization.put(locale, new Properties());
        }
        final Properties locProps = localization.get(locale);
        addProperty(locProps, key, value, false);
    }

    /**
     * @return a immutable copy of currently loaded properties, changes to this object won't be reflected back to global properties
     */
    public static Properties getProperties() {
        return new Properties(properties);
    }

}
