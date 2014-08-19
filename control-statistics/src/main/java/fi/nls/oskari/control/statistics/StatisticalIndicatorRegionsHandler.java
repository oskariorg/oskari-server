package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static fi.nls.oskari.control.statistics.util.Constants.PARAM_ID;

/**
 * Returns regions (=features) available in given category (=statslayer) that are used to visualize indicators
 */
@OskariActionRoute("StatisticalIndicatorRegions")
public class StatisticalIndicatorRegionsHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorRegionsHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {

        final int categoryId = params.getRequiredParamInt(PARAM_ID);
        // 1) load layer with id <categoryId> (should be of type statslayer)
        // 2) list all features in the layer
        JSONArray response = new JSONArray();
        // these regions(features) should match regions referenced by indicators
        // id == layer.region_id (doesn't exist yet)

        Map<String, JSONObject> regions = null;
        // TODO: save these in oskariLayer of type statslayer, categoryId =~ layerId
        if(categoryId == 1) {
            regions = getRegions("oskari:kunnat2013", "kuntakoodi", "kuntanimi");
        }
        else if( categoryId == 2) {
            regions = getRegions("oskari:seutukunta", "seutukuntanro", "seutukunta");
        }
        else if( categoryId == 3) {
            regions = getRegions("oskari:sairaanhoitopiiri", "sairaanhoitopiirinro", "sairaanhoitopiiri");
        }
        if(regions != null) {
            /*
            response.put(JSONHelper.createJSONObject("{ \"id\" : \"091\", \"locale\" : { \"fi\" : \"Helsinki\"}}"));
            response.put(JSONHelper.createJSONObject("{ \"id\" : \"837\", \"locale\" : { \"fi\" : \"Tampere\"}}"));
            */
            for(Map.Entry<String, JSONObject> entry : regions.entrySet()) {
                final JSONObject region = JSONHelper.createJSONObject("id", entry.getKey());
                JSONHelper.putValue(region, "locale", entry.getValue());
                response.put(region);
            }
        }

/*
From SotkaNet:
{
"id": 833,
"code": "1", <- should be reference to statslayer.region_id
"category": "ALUEHALLINTOVIRASTO", <- should be reference to statslayer.id
"title": { <- should come from feature property in Vector-material that the statslayer presents (feature title)
"fi": "Etelä-Suomen AVIn alue",
"en": "Area for Southern Finland AVI",
"sv": "Området för Södra Finlands RFV"
}
         */
        ResponseHelper.writeResponse(params, response);
    }

    private Map<String, JSONObject> getRegions(final String layerName, final String idProp, final String localeProp) {
        final Map<String,JSONObject> result = new HashMap<String, JSONObject>();
        try {
            final String response = IOHelper.getURL(buildUrl(layerName, idProp, localeProp));
            final JSONObject parsed = JSONHelper.createJSONObject(response);
            final JSONArray features = JSONHelper.getJSONArray(parsed, "features");
            for(int i = 0; i < features.length(); ++i) {
                final JSONObject feat = features.optJSONObject(i);
                final JSONObject props = feat.optJSONObject("properties");
                result.put(props.optString(idProp), JSONHelper.createJSONObject("fi", props.optString(localeProp)));
            }
        } catch (IOException e) {
            log.error(e, "Error getting response from server");
        }
        return result;
    }

    private String buildUrl(final String layerName, final String idProp, final String localeProp) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("propertyName", idProp + "," + localeProp);
        params.put("typeName", layerName);
        final String baseUrl = PropertyUtil.get("statistics.geoserver.GetFeature.url");
        return IOHelper.constructUrl(baseUrl, params);
    }
}
