package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.control.statistics.plugins.IndicatorValuesFetcher;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelector;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaIndicatorValuesFetcher;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * This class maps the SotkaNET indicator information to Oskari data model.
 */
public class SotkaIndicator implements StatisticalIndicator {
    private final static Logger LOG = LogFactory.getLogger(SotkaIndicator.class);
    private String id;
    private Map<String, String> localizedName;
    private Map<String, String> localizedSource;
    private List<StatisticalIndicatorLayer> layers;
    private StatisticalIndicatorSelectors selectors;

    public SotkaIndicator(String id, Map<String, String> localizedName, List<StatisticalIndicatorLayer> layers,
            StatisticalIndicatorSelectors selectors, Map<String, String> localizedSource) {
        this.id = id;
        this.localizedName = localizedName;
        this.layers = layers;
        this.selectors = selectors;
        this.localizedSource = localizedSource;
    }
    public SotkaIndicator(JSONObject jsonObject) {
        try {
            this.id = String.valueOf(jsonObject.getInt("id"));
            // Note: Organization id is ignored here. At the moment it doesn't make sense to add to Oskari data model.
            // If in the future it is added, the id must be combined with the plugin id to make a global id of the source.
            // Mappings between the same source, different plugin are nontrivial.
            this.localizedSource = toLocalizationMap(jsonObject.getJSONObject("organization").getJSONObject("title"));
            this.localizedName = toLocalizationMap(jsonObject.getJSONObject("title"));
            // SotkaNET gives indicators with integer values. In the future this might change.
            this.layers = toIndicatorLayers(jsonObject.getJSONObject("classifications").getJSONObject("region")
                    .getJSONArray("values"), IndicatorValueType.INTEGER);
            // Note that the following will just skip the "region" part already projected into layers.
            this.selectors = toSotkaIndicatorSelectors(jsonObject.getJSONObject("classifications"));
            
        } catch (JSONException e) {
            e.printStackTrace();
            LOG.error("Could not read data from Sotka Indicator JSON.", e);
        }
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
    private StatisticalIndicatorSelectors toSotkaIndicatorSelectors(JSONObject jsonObject) throws JSONException {
        // Note that the key "region" must be skipped, because it was already serialized as layers.
        StatisticalIndicatorSelectors selectors = new StatisticalIndicatorSelectors();
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
    public List<StatisticalIndicatorLayer> getLayers() {
        return layers;
    }
    @Override
    public StatisticalIndicatorSelectors getSelectors() {
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
    private static List<StatisticalIndicatorLayer> toIndicatorLayers(JSONArray json, IndicatorValueType type) throws JSONException {
        List<StatisticalIndicatorLayer> layers = new ArrayList<>();
        // TODO: This should come from an upper layer.
        IndicatorValuesFetcher fetcher = new SotkaIndicatorValuesFetcher();
        for (int i = 0; i < json.length(); i++) {
            layers.add(new SotkaStatisticalIndicatorLayer(json.getString(i), type, fetcher));
        }
        return layers;
    }
    @Override
    public String toString() {
        return "{id: " + id + ", localizedName: " + String.valueOf(localizedName) + ", localizedSource: " +
                String.valueOf(localizedSource) + ", layers: " + String.valueOf(layers) + ", selectors: " +
                String.valueOf(selectors)+ "}";
    }
}
