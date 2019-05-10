package fi.nls.oskari.control.statistics.plugins.unsd.parser;

import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * This class maps the UNSD indicator information to Oskari data model.
 */
public class UnsdIndicator extends StatisticalIndicator {
    private final static Logger LOG = LogFactory.getLogger(UnsdIndicator.class);

    private static Map<String, String> toLocalizationMap(String string) {
        Map<String, String> localizationMap = new HashMap<>();
        localizationMap.put("en", string);
        return localizationMap;
    }

    /**
     * @param jsonObject
     * @return true for valid parsing, false for validation errors.
     */
    public boolean parse(JSONObject jsonObject) {
        boolean valid = true;
        try {
            if (!jsonObject.has("code")) {
                return false;
            }
            setId(jsonObject.getString("code"));
            if (jsonObject.has("description")) {
                String description = jsonObject.getString("description");
                setDescription(toLocalizationMap(description));
                setName(toLocalizationMap(getId() + " " + description));
            }
        } catch (JSONException e) {
            LOG.error("Could not read data from UNSD Indicator JSON.", e);
            return false;
        }
        return valid;
    }

    /**
     * Parses dimensions response into StatisticalIndicatorDataModel.
     *
     * @param dimensions
     * @throws JSONException
     */
    public void parseDimensions(JSONArray dimensions) throws JSONException {
        StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
        for (int i = 0; i < dimensions.length(); i++) {
            JSONObject dimension = dimensions.getJSONObject(i);
            String id = dimension.optString("id");
            JSONArray codes = dimension.optJSONArray("codes");
            Collection<String> allowedValues = new ArrayList<>();
            for (int j = 0; j < codes.length(); j++) {
                JSONObject code = codes.getJSONObject(j);
                allowedValues.add(code.getString("code"));
            }
            if (allowedValues.size() > 0) {
                StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(id, allowedValues);
                selectors.addDimension(selector);
            }
        }
        setDataModel(selectors);
    }

    /**
     * Parses source from json.
     *
     * @param jsonObject
     * @throws JSONException
     */
    public void parseSource(JSONObject jsonObject) throws JSONException {
        setSource(toLocalizationMap(jsonObject.getString("source")));
    }

    private void setupLayers(JSONArray json, IndicatorValueType type,
                             String indicatorId, Map<String, Long> layerMappings)
            throws JSONException {
        for (int i = 0; i < json.length(); i++) {
            String kapaLayerId = json.getString(i).toLowerCase();
            if (layerMappings.containsKey(kapaLayerId)) {
                long layerId = layerMappings.get(kapaLayerId);
                StatisticalIndicatorLayer l = new StatisticalIndicatorLayer(layerId, indicatorId);
                l.setIndicatorValueType(type);
                addLayer(l);
            }
        }
    }
}
