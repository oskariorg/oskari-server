package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelector;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicatorDataParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.IndicatorData;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;

/**
 * This fetches the indicator value tables transparently from Sotka.
 * We don't want to make a separate call to the plugin interface for this, because some
 * APIs / plugins might give all the information in the same response, or divide and key the responses differently.
 */
public class SotkaIndicatorValuesFetcher {
    private static final SotkaIndicatorDataParser parser = new SotkaIndicatorDataParser();

    public Map<String, IndicatorValue> get(StatisticalIndicatorSelectors selectors, String indicator) {
        SotkaRequest request = SotkaRequest.getInstance(IndicatorData.NAME);
        // If there is no defined values for gender or year, we will use "total" and an empty list.
        String gender = "total";
        List<String> years = new ArrayList<>();
        for (StatisticalIndicatorSelector selector : selectors.getSelectors()) {
            switch(selector.getId()) {
            case "sex":
                gender = selector.getValue();
                break;
            case "year":
                // Even though SotkaNEW API supports fetching multiple years, we don't do that here.
                // Multiple years can be fetched over several requests.
                years.add(selector.getValue());
                break;
            default:
            }
        }
        try {
            request.setGender(gender);
            String[] yearsArray = years.toArray(new String[years.size()]);
            request.setYears(yearsArray);
            request.setIndicator(indicator);
            String jsonResponse = request.getData();
            return parser.parse(jsonResponse);
            
        } catch (ActionException e) {
            e.printStackTrace();
            throw new APIException("Something went wrong calling SotkaNET getIndicatorValues API.", e);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new APIException("Something went wrong parsing JSON from SotkaNET getIndicatorValues API.", e);
        }
    }

}
