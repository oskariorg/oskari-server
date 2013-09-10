package fi.nls.oskari.domain.map;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 9.9.2013
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
public class JSONLocalizedTitleAndAbstract extends JSONLocalizedTitle {

    public String getAbstract(final String language) {
        return getLocalizedValue(language, LOCALE_ABSTRACT);
    }

    public void setAbstract(final String language, final String abstr) {
        setLocalizedValue(language, LOCALE_ABSTRACT, abstr);
    }

    public Map<String, String> getAbstracts() {
        return getLocalizedValues(LOCALE_ABSTRACT);
    }

    public void setTitles(Map<String, String> abstracts) {
        setLocalizedValues(LOCALE_ABSTRACT, abstracts);
    }
}
