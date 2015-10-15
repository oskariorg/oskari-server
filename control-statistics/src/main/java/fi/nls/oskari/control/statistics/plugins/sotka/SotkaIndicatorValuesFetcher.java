package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.Map;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValuesFetcher;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicatorDataParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.IndicatorData;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;

/**
 * This fetches the indicator value tables transparently from Sotka.
 * We don't want to make a separate call to the plugin interface for this, because some
 * APIs / plugins might give all the information in the same response, or divide and key the responses differently.
 */
public class SotkaIndicatorValuesFetcher implements IndicatorValuesFetcher {
    private static final SotkaIndicatorDataParser parser = new SotkaIndicatorDataParser();

    @Override
    public Map<String, IndicatorValue> get(StatisticalIndicatorSelectors selectors) {
        SotkaRequest request = SotkaRequest.getInstance(IndicatorData.NAME);
        try {
            String jsonResponse = request.getData();
            return parser.parse(jsonResponse);
            
        } catch (ActionException e) {
            throw new APIException("Something went wrong calling SotkaNET getIndicators API.", e);
        }
    }

}
