package fi.nls.oskari.control.statistics.plugins.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.control.statistics.data.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.domain.map.indicator.UserIndicator;

/**
 * This maps the old user indicators to the statistical data source plugin model.
 * 
 */
public class UserStatisticalIndicator extends StatisticalIndicator {
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
    private long layerId;
    private String data;
    private long userId;

    public UserStatisticalIndicator(UserIndicator userIndicator) {
        this.id = String.valueOf(userIndicator.getId());
        this.name = userIndicator.getTitle();
        this.source = userIndicator.getSource();
        this.description = userIndicator.getDescription();
        this.year = String.valueOf(userIndicator.getYear());
        this.published = userIndicator.isPublished();
        this.layerId = userIndicator.getMaterial();
        this.data = userIndicator.getData();
        this.userId = userIndicator.getUserId();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public List<StatisticalIndicatorLayer> getLayers() {
        List<StatisticalIndicatorLayer> layers = new ArrayList<>();
        StatisticalIndicatorLayer layer = new StatisticalIndicatorLayer(layerId, id) {

            @Override
            public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorDataModel selectors) {
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
    public StatisticalIndicatorDataModel getDataModel() {
        return new StatisticalIndicatorDataModel();
    }

    @Override
    public Map<String, String> getName() {
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
    public Map<String, String> getSource() {
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
    public Map<String, String> getDescription() {
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

    @Override
    public Boolean isPublic() {
        return published;
    }
}
