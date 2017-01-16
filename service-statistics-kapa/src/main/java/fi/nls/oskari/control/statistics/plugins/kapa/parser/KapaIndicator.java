package fi.nls.oskari.control.statistics.plugins.kapa.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.control.statistics.data.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.control.statistics.plugins.kapa.KapaIndicatorValuesFetcher;
import fi.nls.oskari.control.statistics.plugins.kapa.KapaStatisticalDatasourcePlugin;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * This class maps the KaPa indicator information to Oskari data model.
 */
public class KapaIndicator extends StatisticalIndicator {
    private final static Logger LOG = LogFactory.getLogger(KapaIndicator.class);
    private String id;
    private Map<String, String> localizedName;
    private Map<String, String> localizedSource;
    private Map<String, String> localizedDescription;
    private List<StatisticalIndicatorLayer> layers;
    private StatisticalIndicatorDataModel selectors;
    private boolean valid = true;
    /**
     * The fetcher object is shared between all KapaIndicators.
     */
    private static final KapaIndicatorValuesFetcher fetcher = new KapaIndicatorValuesFetcher();
    static {
        fetcher.init();
    }

    public KapaIndicator() {
    }

    /**
     * @param jsonObject
     * @param layerMappings 
     * @return true for valid parsing, false for validation errors.
     */
    public boolean parse(JSONObject jsonObject, Map<String, Long> layerMappings) {
        try {
            this.id = String.valueOf(jsonObject.getInt("id"));
            // Note: Organization id is ignored here. At the moment it doesn't make sense to add to Oskari data model.
            // If in the future it is added, the id must be combined with the plugin id to make a global id of the source.
            // Mappings between the same source, different plugin are nontrivial.
            this.localizedSource = toLocalizationMap(jsonObject.getJSONObject("organization").getJSONObject("title"));
            this.localizedName = toLocalizationMap(jsonObject.getJSONObject("title"));
            // KaPa can give indicators with integer and float values. Both are handled as floats.
            // In the future this might change.
            if (jsonObject.getJSONObject("selectors").has("layer")) {
                this.layers = toIndicatorLayers(jsonObject.getJSONObject("selectors").getJSONArray("layer"),
                        IndicatorValueType.FLOAT, this.id, layerMappings);
            } else {
                LOG.error("Layer selector missing from indicator: " + this.id + ": " + String.valueOf(this.localizedName));
                this.valid = false;
            }
            // Note that the following will just skip the "layer" part already projected into layers.
            this.selectors = toKapaIndicatorSelectors(jsonObject.getJSONObject("selectors"));
            // TODO: Add information about the "interpretation", "limits", "legislation", and source "description" also here.
            if (jsonObject.has("description")) {
                this.localizedDescription = toLocalizationMap(jsonObject.getJSONObject("description"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LOG.error("Could not read data from KaPa Indicator JSON.", e);
            this.valid = false;
        }
        return this.valid;
    }

    private StatisticalIndicatorDataModel toKapaIndicatorSelectors(JSONObject jsonObject) throws JSONException {
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
    @Override
    public String getId() {
        return this.id;
    }
    @Override
    public Map<String, String> getName() {
        return this.localizedName;
    }
    @Override
    public Map<String, String> getSource() {
        return this.localizedSource;
    }
    @Override
    public Map<String, String> getDescription() {
        return this.localizedDescription;
    }
    
    @Override
    public List<StatisticalIndicatorLayer> getLayers() {
        return layers;
    }
    @Override
    public StatisticalIndicatorDataModel getDataModel() {
        return selectors;
    }
    private static Map<String, String> toLocalizationMap(JSONObject json) throws JSONException {
        Map<String, String> localizationMap = new HashMap<>();
        @SuppressWarnings("unchecked")
        Iterator<String> names = json.keys();
        while (names.hasNext()) {
            String key = names.next();
            localizationMap.put(key, json.getString(key));
        }
        return localizationMap;
    }
    private static List<StatisticalIndicatorLayer> toIndicatorLayers(JSONArray json, IndicatorValueType type,
            String indicatorId, Map<String, Long> layerMappings) throws JSONException {
        List<StatisticalIndicatorLayer> layers = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            String kapaLayerId = json.getString(i).toLowerCase();
            if (layerMappings.containsKey(kapaLayerId)) {
                long layerId = layerMappings.get(kapaLayerId);
                layers.add(new KapaStatisticalIndicatorLayer(layerId, indicatorId, type, fetcher));
            }
        }
        return layers;
    }
    @Override
    public String toString() {
        return "{pluginId: " + KapaStatisticalDatasourcePlugin.class.getCanonicalName() + ", id: " +
                id + ", localizedName: " + String.valueOf(localizedName) + ", localizedSource: " +
                String.valueOf(localizedSource) + ", layers: " + String.valueOf(layers) + ", selectors: " +
                String.valueOf(selectors)+ "}" + ", public: " + isPublic();
    }
    /**
     * This is used to merge additional selector and description information to the indicator metadata.
     * @param infoToAdd
     */
    public void merge(KapaIndicator infoToAdd) {
        this.selectors.merge(infoToAdd.getDataModel());
        if (this.localizedDescription == null || this.localizedDescription.size() == 0) {
            this.localizedDescription = infoToAdd.getDescription();
        }
    }

    @Override
    public Boolean isPublic() {
        // All Kapa indicators are public.
        return true;
    }
}
