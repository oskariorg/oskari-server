package fi.nls.oskari.control.statistics.plugins.pxweb;

import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.pxweb.parser.PxwebIndicatorsParser;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PxwebStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {

    private static final Logger LOG = LogFactory.getLogger(PxwebStatisticalDatasourcePlugin.class);
    private PxwebIndicatorsParser indicatorsParser;

    private PxwebConfig config;

    @Override
    public void update() {
        List<StatisticalIndicator> indicators = indicatorsParser.parse(getSource().getLayers());
        for (StatisticalIndicator ind : indicators) {
            onIndicatorProcessed(ind);
        }
    }

    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        config = new PxwebConfig(source.getConfigJSON(), source.getId());
        indicatorsParser = new PxwebIndicatorsParser(config);
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
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicator indicator,
                                                          StatisticalIndicatorDataModel params,
                                                          StatisticalIndicatorLayer regionset) {
        Map<String, IndicatorValue> values = new HashMap<>();
        String url = createUrl(regionset.getParam("baseUrl"), indicator.getId());
        JSONArray query = new JSONArray();
        JSONObject payload = JSONHelper.createJSONObject("query", query);
        final String regionKey = config.getRegionKey();
        for (StatisticalIndicatorDataDimension selector : params.getDimensions()) {
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
            IOHelper.writeToConnection(con, payload.toString().getBytes(IOHelper.CHARSET_UTF8));
            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new APIException("Couldn't connect to API at " + url);
            }
            final String data = IOHelper.readString(con);
            JSONObject json = JSONHelper.createJSONObject(data);
            if(json == null) {
                LOG.debug("Got non-json response:", data);
                throw new APIException("Response wasn't JSON");
            }
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

    private String createUrl(String baseUrl, String pathId) {
        return IOHelper.fixPath(baseUrl + "/" + IOHelper.urlEncode(pathId));
    }
}
