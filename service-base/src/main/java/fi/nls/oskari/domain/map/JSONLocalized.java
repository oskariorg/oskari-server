package fi.nls.oskari.domain.map;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 6.9.2013
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
public abstract class JSONLocalized {

    private Logger log = LogFactory.getLogger(JSONLocalized.class);

    public static final String LOCALE_ABSTRACT = "abstract";
    public static final String LOCALE_NAME = "name";
    public static final String LOCALE_SUBTITLE = "subtitle";

    final String DEFAULT_LANG = PropertyUtil.getDefaultLanguage();

    private JSONObject locale;

    protected String getLocalizedValue(String language, String key) {
        try {
            JSONObject loc = JSONHelper.getJSONObject(locale, language);
            final String value = JSONHelper.getStringFromJSON(loc, key , "");
            if(!value.isEmpty() || DEFAULT_LANG.equals(language)) {
                return value;
            }
            return getLocalizedValue(DEFAULT_LANG, key);
        } catch(Exception ignored) { }
        log.info("Couldn't get", key, "from", locale, "for language", language, "in", this.getClass());
        return "";
    }

    protected void setLocalizedValue(String language, String key, String value) {
        JSONObject localeJSON = new JSONObject();
        if (locale != null) {
            localeJSON =  locale;
        }

        JSONObject loc =  new JSONObject();
        if (localeJSON.has(language)) {
            loc = JSONHelper.getJSONObject(localeJSON, language);
        }

        JSONHelper.putValue(loc, key, value);
        JSONHelper.putValue(localeJSON, language, loc);
        locale = localeJSON;
    }

    protected Map<String, String> getLocalizedValues(String key) {
        Map<String, String> names = new HashMap<String, String>();
        if (locale == null) {
            return names;
        }
        JSONObject localeJSON =  locale;
        String locale;
        Iterator keys = localeJSON.sortedKeys();
        while (keys.hasNext()) {
            locale = (String)keys.next();
            names.put(locale, JSONHelper.getStringFromJSON(JSONHelper.getJSONObject(localeJSON, locale), key, ""));
        }
        return names;
    }

    protected void setLocalizedValues(String key, Map<String, String> values) {
        for (String lang : values.keySet()) {
            setLocalizedValue(lang, key, values.get(lang));
        }
    }

    public List<String> getLanguages() {
        List<String> languages = new ArrayList<String>();
        if (locale == null) {
            return languages;
        }
        JSONObject localeJSON =  locale;
        Iterator it = localeJSON.keys();
        while (it.hasNext()) {
            languages.add((String) it.next());
        }
        return languages;
    }

    public JSONObject getLocale() {
        return locale;
    }

    public void setLocale(JSONObject locale) {
        this.locale = locale;
    }
}
