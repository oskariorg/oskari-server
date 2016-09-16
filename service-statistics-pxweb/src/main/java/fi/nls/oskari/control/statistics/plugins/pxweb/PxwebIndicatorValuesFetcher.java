package fi.nls.oskari.control.statistics.plugins.pxweb;

import java.util.Map;

import org.json.JSONException;

import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.pxweb.parser.PxwebIndicatorDataParser;
import fi.nls.oskari.control.statistics.plugins.pxweb.requests.PxwebRequest;

/**
 * This fetches the indicator value tables transparently from pxweb.
 * We don't want to make a separate call to the plugin interface for this, because some
 * APIs / plugins might give all the information in the same response, or divide and key the responses differently.
 */
public class PxwebIndicatorValuesFetcher {
/*
    private PxwebIndicatorDataParser parser;

    public void init() {
        this.parser = new PxwebIndicatorDataParser();
    }

    /**
     * This returns the indicator data filtered by the selected selectors (eg. sex, year, layer).
     * @param selectors
     * @param indicator
     * @return
     */
/*
    public Map<String, IndicatorValue> get(StatisticalIndicatorSelectors selectors, String indicator) {
        PxwebRequest request = new PxwebRequest();
        String jsonResponse = request.getIndicatorData(indicator, selectors);
        Map<String, IndicatorValue> result;
        try {
            result = parser.parse(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new APIException("Error during parsing KaPa response.", e);
        }
      /*  // FIXME: Cache this result, key by plugin, selectors and indicator id.
        return result;
    }

}*/
}