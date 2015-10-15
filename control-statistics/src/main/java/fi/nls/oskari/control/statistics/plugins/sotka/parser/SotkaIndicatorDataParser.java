package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import java.util.HashMap;
import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SotkaIndicatorDataParser {
    private final static Logger LOG = LogFactory.getLogger(SotkaIndicatorDataParser.class);

    public Map<String, IndicatorValue> parse(String response) {
        Map<String, IndicatorValue> indicatorMap = new HashMap<>();
        // FIXME: Implement this.
        System.out.println("Got response: " + response);
        return indicatorMap;
    }
}
