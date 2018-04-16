package fi.nls.oskari.domain.map;

import java.util.Map;

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
        for (Map.Entry<String, String> entry : names.entrySet()) {
            setName(entry.getKey(), entry.getValue());
        }
    }
}
