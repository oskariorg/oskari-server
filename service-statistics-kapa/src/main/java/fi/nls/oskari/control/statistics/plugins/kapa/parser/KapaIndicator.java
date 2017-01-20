package fi.nls.oskari.control.statistics.plugins.kapa.parser;

import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * This class maps the KaPa indicator information to Oskari data model.
 */
public class KapaIndicator extends StatisticalIndicator {
    private final static Logger LOG = LogFactory.getLogger(KapaIndicator.class);

    public KapaIndicator() {
    }

    private static Map<String, String> toLocalizationMap(JSONObject json)
            throws JSONException {
        Map<String, String> localizationMap = new HashMap<>();
        @SuppressWarnings("unchecked")
        Iterator<String> names = json.keys();
        while (names.hasNext()) {
            String key = names.next();
            localizationMap.put(key, json.getString(key));
        }
        return localizationMap;
    }

    /**
     * @param jsonObject
     * @param layerMappings
     * @return true for valid parsing, false for validation errors.
     */
    public boolean parse(JSONObject jsonObject, Map<String, Long> layerMappings) {
        boolean valid = true;
        try {
            setId(String.valueOf(jsonObject.getInt("id")));
            // Note: Organization id is ignored here. At the moment it doesn't make sense to add to Oskari data model.
            // If in the future it is added, the id must be combined with the plugin id to make a global id of the source.
            // Mappings between the same source, different plugin are nontrivial.
            setSource(toLocalizationMap(jsonObject.getJSONObject("organization").getJSONObject("title")));
            setName(toLocalizationMap(jsonObject.getJSONObject("title")));
            // KaPa can give indicators with integer and float values. Both are handled as floats.
            // In the future this might change.
            if (jsonObject.getJSONObject("selectors").has("layer")) {
                setupLayers(jsonObject.getJSONObject("selectors").getJSONArray("layer"),
                        IndicatorValueType.FLOAT, getId(), layerMappings);
            } else {
                LOG.error("Layer selector missing from indicator: " + getId() + ": " + String.valueOf(getName()));
                valid = false;
            }
            // Note that the following will just skip the "layer" part already projected into layers.
            setDataModel(toKapaIndicatorSelectors(jsonObject.getJSONObject("selectors")));
            // TODO: Add information about the "interpretation", "limits", "legislation", and source "description" also here.
            if (jsonObject.has("description")) {
                setDescription(toLocalizationMap(jsonObject.getJSONObject("description")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LOG.error("Could not read data from KaPa Indicator JSON.", e);
            return false;
        }
        return valid;
    }

    private StatisticalIndicatorDataModel toKapaIndicatorSelectors(JSONObject jsonObject)
            throws JSONException {
        // Note that the key "region" must be skipped, because it was already serialized as layers.
        StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
        @SuppressWarnings("unchecked")
        Iterator<String> names = jsonObject.keys();
        while (names.hasNext()) {
            String key = names.next();
            if (key.equals("layer")) {
                // This was already handled and put to layers.
            } else {
                Collection<String> allowedValues = new ArrayList<>();
                JSONArray jsonSelector = jsonObject.getJSONArray(key);
                for (int i = 0; i < jsonSelector.length(); i++) {
                    allowedValues.add(jsonSelector.getString(i));
                }
                if (allowedValues.size() > 0) {
                    StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(key, allowedValues);
                    selectors.addDimension(selector);
                }
            }
        }
        return selectors;
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

    /**
     * This is used to merge additional selector and description information to the indicator metadata.
     *
     * @param infoToAdd
     */
    public void merge(KapaIndicator infoToAdd) {
        List<StatisticalIndicatorDataDimension> dimensions = getDataModel().getDimensions();

        // A naive array lookup is fastest for small arrays.
        for (StatisticalIndicatorDataDimension selector : infoToAdd.getDataModel().getDimensions()) {
            StatisticalIndicatorDataDimension foundSelector = null;
            for (StatisticalIndicatorDataDimension originalSelector : dimensions) {
                if (originalSelector.getId().equals(selector.getId())) {
                    // Found match. We can assume these are identical here.
                    foundSelector = originalSelector;
                }
            }
            if (foundSelector == null) {
                // The selector is a new one which does not exist in dimensions. Adding.
                dimensions.add(selector);
            }
        }
        if (getDescription() == null || getDescription().isEmpty()) {
            setDescription(infoToAdd.getDescription());
        }
    }
}
