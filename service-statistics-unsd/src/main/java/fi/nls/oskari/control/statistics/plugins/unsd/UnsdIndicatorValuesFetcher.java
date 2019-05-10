package fi.nls.oskari.control.statistics.plugins.unsd;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.unsd.parser.UnsdParser;
import fi.nls.oskari.control.statistics.plugins.unsd.requests.UnsdRequest;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UnsdIndicatorValuesFetcher {
    private UnsdParser parser;
    private UnsdConfig config;

    public void init(UnsdConfig config) {
        this.config = config;
        this.parser = new UnsdParser();
    }

    /**
     * @param selectors        Used to query UNSD with.
     * @param indicator        The indicator we want.
     * @param areaCodes        List of areas we are interested in
     * @return
     */
    public Map<String, IndicatorValue> get(StatisticalIndicatorDataModel selectors, String indicator, List<String> areaCodes) {
        UnsdRequest request = new UnsdRequest(config);
        request.setIndicator(indicator);
        request.setAreaCodes(areaCodes);

        Map<String, IndicatorValue> result = new HashMap<>();
        boolean allFetched = false;
        String response;
        while(!allFetched) {
            try {
                response = request.getIndicatorData(selectors);
                result.putAll(parser.parseIndicatorData(response));
                allFetched = parser.isLastPage(response);
                if (!allFetched) {
                    request.nextPage();
                    response = request.getIndicatorData(selectors);
                }
            } catch (JSONException ex) {
                throw new APIException("Error during parsing UNSD data response.", ex);
            }
        }
        return result;
    }

}
