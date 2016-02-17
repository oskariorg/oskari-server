package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SotkaSpecificIndicatorParser {
    private final static Logger LOG = LogFactory.getLogger(SotkaSpecificIndicatorParser.class);

    public SotkaIndicator parse(String response, Map<String, Long> sotkaLayersToOskariLayers) {
        try {
            // We will simply map the response JSON into Oskari data model without fancy streaming and such.
            // Keeping it simple. If performance becomes an issue, this can be reimplemented in a streaming fashion.
            // However, this is unlikely before real-time data, because this data is cached.
            JSONObject responseJSON = new JSONObject(response);
            SotkaIndicator sotkaIndicator = new SotkaIndicator(sotkaLayersToOskariLayers);
            if (sotkaIndicator.parse(responseJSON)) {
                return sotkaIndicator;
            }
        } catch (JSONException e) {
            LOG.error("Error in mapping Sotka Indicators response to Oskari model: " + e.getMessage(), e);
        }
        return null;
    }
}
