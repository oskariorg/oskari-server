package fi.nls.oskari.control.statistics.plugins.kapa.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import org.json.JSONArray;
import org.json.JSONException;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class KapaIndicatorsParser {
    private final static Logger LOG = LogFactory.getLogger(KapaIndicatorsParser.class);

    public List<StatisticalIndicator> parse(String response, Map<String, Long> layerMappings) {
        List<StatisticalIndicator> indicatorList = new ArrayList<>();
        try {
            // We will simply map the response JSON into Oskari data model without fancy streaming and such.
            // Keeping it simple. If performance becomes an issue, this can be reimplemented in a streaming fashion.
            // However, this is unlikely before real-time data, because this data is cached.
            JSONArray responseJSON = new JSONArray(response);
            for (int i = 0; i < responseJSON.length(); i++) {
                KapaIndicator kapaIndicator = new KapaIndicator();
                if (kapaIndicator.parse(responseJSON.getJSONObject(i), layerMappings)) {
                    indicatorList.add(kapaIndicator);
                }
            }
        } catch (JSONException e) {
            LOG.error("Error in mapping KaPa Indicators response to Oskari model: " + e.getMessage(), e);
        }
        return indicatorList;
    }
}
