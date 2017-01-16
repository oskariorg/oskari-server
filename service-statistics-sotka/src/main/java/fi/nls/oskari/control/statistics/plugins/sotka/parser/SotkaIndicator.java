package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.*;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.control.statistics.plugins.sotka.SotkaIndicatorSelectorsFetcher;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaIndicatorValuesFetcher;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * This class maps the SotkaNET indicator information to Oskari data model.
 */
public class SotkaIndicator extends StatisticalIndicator {
    private final static Logger LOG = LogFactory.getLogger(SotkaIndicator.class);
    private String id;
    private Map<String, String> localizedName;
    private Map<String, String> localizedSource;
    private Map<String, String> localizedDescription;
    private List<StatisticalIndicatorLayer> layers;
    private StatisticalIndicatorDataModel selectors;
    private Map<String, Long> sotkaLayersToOskariLayers;
    private boolean valid = true;
    /**
     * If the metadata is not fetched for this indicator, we must fetch it separately.
     */
    private boolean metadataFetched = false;

    private static final SotkaIndicatorSelectorsFetcher metadataFetcher = new SotkaIndicatorSelectorsFetcher();

    /**
     * The fetcher object is shared between all SotkaIndicators.
     */
    private static final SotkaIndicatorValuesFetcher fetcher = new SotkaIndicatorValuesFetcher();

    public SotkaIndicator(Map<String, Long> sotkaLayersToOskariLayers, SotkaConfig config) {
        this.sotkaLayersToOskariLayers = sotkaLayersToOskariLayers;
        fetcher.init(config);
        metadataFetcher.init(config);
    }

    /**
     * @param jsonObject
     * @return true for valid parsing, false for validation errors.
     */
    public boolean parse(JSONObject jsonObject) {
        return parse(jsonObject, true);
    }
    public boolean parse(JSONObject jsonObject, boolean includeMetadata) {
        try {
            this.id = String.valueOf(jsonObject.getInt("id"));
            this.localizedName = toLocalizationMap(jsonObject.getJSONObject("title"));
            // SotkaNET gives indicators with integer and float values. In the future this might change.
            if (jsonObject.getJSONObject("classifications").has("region")) {
                this.layers = toIndicatorLayers(jsonObject.getJSONObject("classifications").getJSONObject("region")
                    .getJSONArray("values"), IndicatorValueType.FLOAT, this.id, sotkaLayersToOskariLayers);
                this.valid = !this.layers.isEmpty();
            } else {
                LOG.error("Region missing from indicator: " + this.id + ": " + String.valueOf(this.localizedName));
                this.valid = false;
            }
            if(!includeMetadata) {
                this.metadataFetched = true;
                return this.valid;
            }
            // Note: Organization id is ignored here. At the moment it doesn't make sense to add to Oskari data model.
            // If in the future it is added, the id must be combined with the plugin id to make a global id of the source.
            // Mappings between the same source, different plugin are nontrivial.
            this.localizedSource = toLocalizationMap(jsonObject.getJSONObject("organization").getJSONObject("title"));
            // Note that the following will just skip the "region" part already projected into layers.
            this.selectors = toSotkaIndicatorSelectors(jsonObject.getJSONObject("classifications"));
            // TODO: Add information about the "interpretation", "limits", "legislation", and source "description" also here.
            if (jsonObject.has("description")) {
                // The description field only exists in the specific Sotka Indicator response, so it is handled
                // as optional here. This SotkaIndicator class also describes the general indicator information
                // which is missing from the indicator list JSON.
                this.localizedDescription = toLocalizationMap(jsonObject.getJSONObject("description"));
                this.metadataFetched = true;
            }
            if (jsonObject.has("range")) {
                JSONObject range = jsonObject.getJSONObject("range");
                Integer start = range.getInt("start");
                Integer end = range.getInt("end");
                // TODO: Update this before the year 3000. Validating to prevent a DOS attack using insane numbers.
                
                if (start >= 1000 && end <= 3000) {
                    List<String> allowedYears = new ArrayList<>();
                    for (int year = start; year <= end; year++) {
                        allowedYears.add(String.valueOf(year));
                    }
                    StatisticalIndicatorSelector yearSelector = new StatisticalIndicatorSelector("year", allowedYears);
                    this.selectors.addSelector(yearSelector);
                }
                this.metadataFetched = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LOG.error("Could not read data from Sotka Indicator JSON.", e);
            this.valid = false;
        }
        return this.valid;
        /*
         * Sample JSON message:
        {
            "id" : 1942,
            "classifications" : {
               "region" : {
                  "values" : [
                     "Kunta",
                     "Maakunta",
                     "Erva",
                     "Aluehallintovirasto",
                     "Sairaanhoitopiiri",
                     "Maa",
                     "Suuralue",
                     "Seutukunta",
                     "Nuts1"
                  ]
               },
               "sex" : {
                  "values" : [
                     "total"
                  ]
               }
            },
            "organization" : {
               "title" : {
                  "fi" : "Tilastokeskus",
                  "sv" : "Statistikcentralen",
                  "en" : "Statistics Finland"
               },
               "id" : 3
            },
            "title" : {
               "fi" : "Perhehoidossa olleet vanhukset 31.12., kunnan kustantamat palvelut",
               "sv" : "Äldre i familjevård 31.12, tjänster finansierade av kommuner",
               "en" : "Family care, older people in services funded by the municipality, on 31 Dec"
            }
         }
        ...
      */
    }
    private StatisticalIndicatorDataModel toSotkaIndicatorSelectors(JSONObject jsonObject) throws JSONException {
        // Note that the key "region" must be skipped, because it was already serialized as layers.
        StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
        @SuppressWarnings("unchecked")
        Iterator<String> names = jsonObject.keys();
        while (names.hasNext()) {
            String key = names.next();
            if (key.equals("region")) {
                // This was already handled and put to layers.
            } else {
                Collection<String> allowedValues = new ArrayList<>();
                JSONObject jsonSelector = jsonObject.getJSONObject(key);
                JSONArray valuesJSON = jsonSelector.getJSONArray("values");
                for (int i = 0; i < valuesJSON.length(); i++) {
                    allowedValues.add(valuesJSON.getString(i));
                }
                if (allowedValues.size() > 0) {
                    // Sotka has many indicators with empty allowed values for "sex" for example.
                    StatisticalIndicatorSelector selector = new StatisticalIndicatorSelector(key, allowedValues);
                    selectors.addSelector(selector);
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
    public Map<String, String> getLocalizedName() {
        return this.localizedName;
    }
    @Override
    public Map<String, String> getLocalizedSource() {
        return this.localizedSource;
    }
    @Override
    public Map<String, String> getLocalizedDescription() {
        if (!metadataFetched) {
            this.merge(metadataFetcher.get(id, sotkaLayersToOskariLayers));
            metadataFetched = true;
        }
        return this.localizedDescription;
    }
    
    @Override
    public List<StatisticalIndicatorLayer> getLayers() {
        if (!metadataFetched) {
            this.merge(metadataFetcher.get(id, sotkaLayersToOskariLayers));
            metadataFetched = true;
        }
        return layers;
    }
    @Override
    public StatisticalIndicatorDataModel getDataModel() {
        if (!metadataFetched) {
            this.merge(metadataFetcher.get(id, sotkaLayersToOskariLayers));
            metadataFetched = true;
        }
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
            String indicatorId, Map<String, Long> sotkaLayersToOskariLayers) throws JSONException {
        List<StatisticalIndicatorLayer> layers = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            String sotkaLayerName = json.getString(i);
            Long oskariLayerId = sotkaLayersToOskariLayers.get(sotkaLayerName.toLowerCase());
            if (oskariLayerId != null) {
                layers.add(new SotkaStatisticalIndicatorLayer(oskariLayerId, indicatorId, sotkaLayerName, type, fetcher));
            }
        }
        return layers;
    }

    @Override
    public String toString() {
        return "{pluginId: " + SotkaStatisticalDatasourcePlugin.class.getCanonicalName() + ", id: " +
                id + ", localizedName: " + String.valueOf(localizedName) + ", localizedSource: " +
                String.valueOf(localizedSource) + ", layers: " + String.valueOf(layers) + ", selectors: " +
                String.valueOf(selectors)+ "}" + ", public: " + isPublic();
    }
    /**
     * This is used to merge additional selector and description information to the indicator metadata.
     * @param infoToAdd
     */
    public void merge(SotkaIndicator infoToAdd) {
        if (infoToAdd == null) {
            return;
        }
        this.selectors.merge(infoToAdd.getDataModel());
        if (this.localizedDescription == null || this.localizedDescription.size() == 0) {
            this.localizedDescription = infoToAdd.getLocalizedDescription();
        }
    }
}
