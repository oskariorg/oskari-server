package fi.nls.oskari.domain.map;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 9.9.2013
 * Time: 12:27
 * To change this template use File | Settings | File Templates.
 */
public class JSONLocalizedTitle extends JSONLocalized {
    public String getTitle(final String language) {
        return getLocalizedValue(language, LOCALE_SUBTITLE);
    }

    public void setTitle(final String language, final String title) {
        setLocalizedValue(language, LOCALE_SUBTITLE, title);
    }

    public Map<String, String> getTitles() {
        return getLocalizedValues(LOCALE_SUBTITLE);
    }

    public void setTitles(Map<String, String> titles) {
        setLocalizedValues(LOCALE_SUBTITLE, titles);
    }
}
