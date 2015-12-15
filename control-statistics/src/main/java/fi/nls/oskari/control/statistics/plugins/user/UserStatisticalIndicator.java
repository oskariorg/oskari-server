package fi.nls.oskari.control.statistics.plugins.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValueFloat;
import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.domain.map.indicator.UserIndicator;

/**
 * This maps the old user indicators to the statistical data source plugin model.
 * 
 */
public class UserStatisticalIndicator implements StatisticalIndicator {
    /*
     * An example message from old Oskari:
     * {"id":859,"category":"NUTS1","organization":{"fi":"a"},"title":{"fi":"Testi"},
     * "description":{"fi":"b"},"data":[{"region":"727","primary value":"15"},
     * {"region":"728","primary value":"20"}],"year":2015,"public":true,"layerId":519}
     */

    private String id;
    private String name;
    private String source;
    private String description;
    private String year;
    private Boolean published;
    private String layerName;
    private String data;
    private long userId;

    public UserStatisticalIndicator(UserIndicator userIndicator) {
        this.id = String.valueOf(userIndicator.getId());
        this.name = userIndicator.getTitle();
        this.source = userIndicator.getSource();
        this.description = userIndicator.getDescription();
        this.year = String.valueOf(userIndicator.getYear());
        this.published = userIndicator.isPublished();
        this.layerName = categoryToLayerName(userIndicator.getCategory());
        this.data = userIndicator.getData();
        this.userId = userIndicator.getUserId();
    }

    @Override
    public String getPluginId() {
        return UserIndicatorsStatisticalDatasourcePlugin.class.getCanonicalName();
    }

    @Override
    public String getId() {
        return this.id;
    }

    private static Map<String, String> categoriesToLayers = new HashMap<>();
    static {
        // Categories are (old) Sotka keywords for legacy and backward compatibility reasons.
        // We will map them here by hard coding, because these are not updated when Sotka is updated.
        // The downside is that the oskari layers must follow this convention for the old user indicators.
        // Note: When kunnat are updated, this should not be updated, hence hardcoded.
        // Instead, the new indicators will use Oskari layer names directly.
        categoriesToLayers.put("kunta", "oskari:kunnat2013");
        categoriesToLayers.put("maakunta", "oskari:maakunta");
        categoriesToLayers.put("erva", "oskari:erva-alueet");
        categoriesToLayers.put("aluehallintovirasto", "oskari:avi");
        categoriesToLayers.put("sairaanhoitopiiri", "oskari:sairaanhoitopiiri");
        categoriesToLayers.put("ely-keskus", "oskari:ely");
        categoriesToLayers.put("seutukunta", "oskari:seutukunta");
        categoriesToLayers.put("nuts1", "oskari:nuts1");
    }
    
    public static String categoryToLayerName(String category) {
        // If a category is unknown, it is assumed to be an Oskari layer name.
        if (categoriesToLayers.containsKey(category.toLowerCase())) {
            return categoriesToLayers.get(category.toLowerCase());
        }
        return category;
    }
    
    @Override
    public List<StatisticalIndicatorLayer> getLayers() {
        List<StatisticalIndicatorLayer> layers = new ArrayList<>();
        StatisticalIndicatorLayer layer = new StatisticalIndicatorLayer() {

            @Override
            public String getOskariLayerName() {
                return layerName;
            }

            @Override
            public IndicatorValueType getIndicatorValueType() {
                return IndicatorValueType.FLOAT;
            }

            @Override
            public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorSelectors selectors) {
                // Data is a serialized JSON for legacy and backwards compatibility reasons:
                // "data":[{"region":"727","primary value":"15"},{"region":"728","primary value":"20"}]
                Map<String, IndicatorValue> valueMap = new HashMap<>();
                try {
                    JSONArray jsonData = new JSONArray(data);
                    for (int i = 0; i < jsonData.length(); i++) {
                        JSONObject value = jsonData.getJSONObject(i);
                        IndicatorValueFloat indicatorValue = new IndicatorValueFloat(value.getDouble("primary value"));
                        valueMap.put(value.getString("region"), indicatorValue);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return valueMap;
            }
            
        };
        layers.add(layer);
        return layers;
    }

    @Override
    public StatisticalIndicatorSelectors getSelectors() {
        return new StatisticalIndicatorSelectors();
    }

    @Override
    public Map<String, String> getLocalizedName() {
        // The value is an already serialized JSON for legacy and backward compatibility reasons.
        try {
            JSONObject localizedValue = new JSONObject(this.name);
            return toMap(localizedValue);
        } catch (JSONException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, String> getLocalizedSource() {
        // The value is an already serialized JSON for legacy and backward compatibility reasons.
        try {
            JSONObject localizedValue = new JSONObject(this.source);
            return toMap(localizedValue);
        } catch (JSONException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private static Map<String, String> toMap(JSONObject value) {
        Map<String, String> map = new HashMap<>();
        for (String name: JSONObject.getNames(value)) {
            try {
                map.put(name, value.getString(name));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    @Override
    public Map<String, String> getLocalizedDescription() {
        // The value is an already serialized JSON for legacy and backward compatibility reasons.
        try {
            JSONObject localizedValue = new JSONObject(this.description);
            return toMap(localizedValue);
        } catch (JSONException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public long getUserId() {
        return userId;
    }

    public String getYear() {
        return year;
    }

    public Boolean getPublished() {
        return published;
    }
}
