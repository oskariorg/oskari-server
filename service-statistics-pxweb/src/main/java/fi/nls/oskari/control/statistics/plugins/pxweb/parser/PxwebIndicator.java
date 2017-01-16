package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.util.PropertyUtil;

public class PxwebIndicator extends StatisticalIndicator {

    public void setName(String name) {
        addLocalizedName(PropertyUtil.getDefaultLanguage(), name);
    }
}
