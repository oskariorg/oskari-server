package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.plugins.*;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class PxwebStatisticalIndicatorLayer implements StatisticalIndicatorLayer {

    private long id;
    private String indicatorId;
    private String baseUrl;
    private String regionKey;

    public PxwebStatisticalIndicatorLayer(long id, String indicatorId, String baseUrl, String regionKey) {
        this.id = id;
        this.indicatorId = indicatorId;
        this.baseUrl = baseUrl;
        this.regionKey = regionKey;
    }

    @Override
    public long getOskariLayerId() {
        return id;
    }

    @Override
    public IndicatorValueType getIndicatorValueType() {
        return null;
    }

    /*
    "query": [
   {
     "code": "Alue",
     "selection": {
       "filter": "item",
       "values": [
         "0910000000",
         "0911000000",
         "0911101000"
       ]
     }
   },
   {
     "code": "Käyttötarkoitus",
     "selection": {
       "filter": "item",
       "values": [
         "all",
         "01",
         "02"
       ]
     }
   },
   {
     "code": "Toimenpide",
     "selection": {
       "filter": "item",
       "values": [
         "all",
         "1"
       ]
     }
   },
   {
     "code": "Yksikkö",
     "selection": {
       "filter": "item",
       "values": [
         "1",
         "2"
       ]
     }
   },
   {
     "code": "Vuosi",
     "selection": {
       "filter": "item",
       "values": [
         "0",
         "1",
         "2"
       ]
     }
   }
 ],
 "response": {
   "format": "csv"
 }
}
     */
    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorSelectors selectors) {
        Map<String, IndicatorValue> values = new HashMap<>();
        String url = IOHelper.fixPath(baseUrl + "/" + indicatorId);
        JSONArray query = new JSONArray();
        JSONObject payload = JSONHelper.createJSONObject("query", query);
        for (StatisticalIndicatorSelector selector : selectors.getSelectors()) {
            if (regionKey.equalsIgnoreCase(selector.getId())) {
                // skip the region property
                continue;
            }
            JSONObject param = new JSONObject();
            JSONHelper.putValue(param, "code", selector.getId());
            JSONObject selection = new JSONObject();
            JSONHelper.putValue(selection, "filter", "item");
            JSONArray paramValues = new JSONArray();
            paramValues.put(selector.getValue());
            JSONHelper.putValue(selection, "values", paramValues);

            JSONHelper.putValue(param, "selection", selection);
            query.put(param);
        }
        JSONHelper.putValue(payload, "response", JSONHelper.createJSONObject("format", "json-stat"));

        try {
            final HttpURLConnection con = IOHelper.getConnection(url);
            IOHelper.writeHeader(con, IOHelper.HEADER_CONTENTTYPE, IOHelper.CONTENT_TYPE_JSON + ";  charset=utf-8");
            IOHelper.writeToConnection(con, payload.toString().getBytes("UTF-8"));
            final String data = IOHelper.readString(con);
            JSONObject json = JSONHelper.createJSONObject(data);
            //dataset.dimension.Alue.category.index -> key==region id & value == index pointer to dataset.value
            JSONObject stats = json.optJSONObject("dataset").optJSONObject("dimension").optJSONObject(regionKey).optJSONObject("category").optJSONObject("index");
            JSONArray responseValues = json.optJSONObject("dataset").optJSONArray("value");
            JSONArray names = stats.names();
            for (int i = 0; i < names.length(); ++i) {
                String region = names.optString(i);
                Double val = responseValues.optDouble(stats.optInt(region));
                if (val.isNaN()) {
                    continue;
                }
                IndicatorValue indicatorValue = new IndicatorValueFloat(val);
                values.put(region, indicatorValue);
            }
        } catch (IOException e) {
            throw new APIException("Couldn't get data from service/parsing failed", e);
        }

        return values;
    }
}
