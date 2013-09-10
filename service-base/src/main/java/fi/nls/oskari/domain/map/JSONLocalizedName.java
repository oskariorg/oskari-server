package fi.nls.oskari.domain.map;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 9.9.2013
 * Time: 12:26
 * To change this template use File | Settings | File Templates.
 */
public class JSONLocalizedName extends JSONLocalized {
    public String getName(final String language) {
        return getLocalizedValue(language, LOCALE_NAME);
    }

    public void setName(final String language, final String name) {
        setLocalizedValue(language, LOCALE_NAME, name);
    }

    public Map<String, String> getNames() {
        return getLocalizedValues(LOCALE_NAME);
    }

    public void setNames(Map<String, String> names) {
        for (String lang : names.keySet()) {
            setName(lang, names.get(lang));
        }
    }
}
