package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.util.PropertyUtil;

public class PxwebIndicator extends StatisticalIndicator {

    public void setName(String name) {
        addName(PropertyUtil.getDefaultLanguage(), name);
    }
}
