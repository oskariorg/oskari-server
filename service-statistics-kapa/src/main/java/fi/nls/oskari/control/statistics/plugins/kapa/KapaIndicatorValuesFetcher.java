package fi.nls.oskari.control.statistics.plugins.kapa;

import java.util.Map;

import org.json.JSONException;

import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.kapa.parser.KapaIndicatorDataParser;
import fi.nls.oskari.control.statistics.plugins.kapa.requests.KapaRequest;

/**
 * This fetches the indicator value tables transparently from KaPa.
 * We don't want to make a separate call to the plugin interface for this, because some
 * APIs / plugins might give all the information in the same response, or divide and key the responses differently.
 */
public class KapaIndicatorValuesFetcher {
    private KapaIndicatorDataParser parser;

    public void init() {
        this.parser = new KapaIndicatorDataParser();
    }

    /**
     * This returns the indicator data filtered by the selected selectors (eg. sex, year, layer).
     * @param selectors
     * @param indicator
     * @return
     */
    public Map<String, IndicatorValue> get(StatisticalIndicatorDataModel selectors, String indicator) {
        KapaRequest request = new KapaRequest();
        String jsonResponse = request.getIndicatorData(indicator, selectors);
        Map<String, IndicatorValue> result;
        try {
            result = parser.parse(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new APIException("Error during parsing KaPa response.", e);
        }
        // FIXME: Cache this result, key by plugin, selectors and indicator id.
        return result;
    }
}
