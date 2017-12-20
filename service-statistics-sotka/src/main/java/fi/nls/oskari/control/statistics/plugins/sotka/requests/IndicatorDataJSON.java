package fi.nls.oskari.control.statistics.plugins.sotka.requests;


import fi.nls.oskari.util.IOHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Request class for SotkaNET statistics query to get indicator data in JSON format.
 * Data is: [{"indicator" : 4,"region": 231,"year": 2012,"gender": "total","value": 3.4,"absValue": 9}, ... ]
 * @author SMAKINEN
 */
public class IndicatorDataJSON extends SotkaRequest {
    public final static String NAME = "jsondata";

    public boolean isValid () {
        return getIndicator() != null && !getIndicator().isEmpty();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getRequestSpecificParams() {
        Map<String, String> params = new HashMap<>();
        params.put("indicator", getIndicator());
        params.put("genders", getGender());
        String url = IOHelper.constructUrl("/json", params);
        Map<String, String[]> years = new HashMap<>();
        years.put("years", getYears());
        return url + "&" + IOHelper.getParamsMultiValue(years);
    }
}
