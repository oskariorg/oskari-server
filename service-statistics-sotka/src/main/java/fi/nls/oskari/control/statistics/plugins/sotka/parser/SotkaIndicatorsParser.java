package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaConfig;
import org.json.JSONArray;
import org.json.JSONException;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SotkaIndicatorsParser {
    private final static Logger LOG = LogFactory.getLogger(SotkaIndicatorsParser.class);
    private SotkaConfig config;

    public void setConfig(SotkaConfig obj) {
        if(obj == null) {
            return;
        }
        config = obj;
    }
    public List<StatisticalIndicator> parse(String response, Map<String, Long> sotkaLayersToOskariLayers, boolean includeMetadata) {
        List<StatisticalIndicator> indicatorList = new ArrayList<>();
        try {
            // We will simply map the response JSON into Oskari data model without fancy streaming and such.
            // Keeping it simple. If performance becomes an issue, this can be reimplemented in a streaming fashion.
            // However, this is unlikely before real-time data, because this data is cached.
            JSONArray responseJSON = new JSONArray(response);
            LOG.info("Parsing indicator response of length: " + responseJSON.length());
            for (int i = 0; i < responseJSON.length(); i++) {
                SotkaIndicator sotkaIndicator = new SotkaIndicator(sotkaLayersToOskariLayers, config);
                if (sotkaIndicator.parse(responseJSON.getJSONObject(i), includeMetadata)) {
                    indicatorList.add(sotkaIndicator);
                }
            }
            LOG.info("Parsed indicator response.");
        } catch (JSONException e) {
            LOG.error("Error in mapping Sotka Indicators response to Oskari model: " + e.getMessage(), e);
        }
        return indicatorList;
    }
}
