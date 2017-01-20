package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.util.IOHelper;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class EurostatConfig {

    private long datasourceId;
    private String url;


    EurostatConfig(JSONObject json, long id) {
        datasourceId = id;
        url = json.optString("url");

    }

    public String getUrl() {
        return url;
    }
    public long getId() {
        return datasourceId;
    }

    public String getURLforData(StatisticalIndicatorDataModel selectors, String indicatorId){
        Map<String, String> params = new HashMap<>();
        for (StatisticalIndicatorDataDimension selector : selectors.getDimensions()) {
            if (EurostatStatisticalDatasourcePlugin.KEY_REGION.equalsIgnoreCase(selector.getId())) {
                // skip region key
                continue;
            }
            params.put(selector.getId(), selector.getValue());
        }
        String url = IOHelper.constructUrl(getUrl() +"/wdds/rest/data/v2.1/json/en/" + indicatorId, params);
        return url;
    }

}
