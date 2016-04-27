package fi.nls.oskari.control.statistics.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the value object for the layer url and other metadata in the database.
 * MyBatis Type for the SQL table oskari_maplayers
 */
public class RegionSet {
    /**
     *  The layer id in Oskari, for example: 9.
     */
    private long oskariLayerId;
    
    /**
     *  The layer name in Oskari, for example: "oskari:kunnat2013".
     */
    private String oskariLayerName;
    
    /**
     * The base url to the data source, for example: .
     */
    private String url;

    private String attributes;
    
    private String type;
// -----------------
    public long getId() {
        return oskariLayerId;
    }
    public JSONObject asJSON() {

        JSONObject tags = new JSONObject();
        try {
            // only regionset id and regionIdTag is used in frontend, and regionIdTag should propably come from datasource...
            //tags.put("nameIdTag", getNameProperty());
            tags.put("regionIdTag", getIdProperty());
            //tags.put("url", getUrl());
            //tags.put("featuresUrl", getFeaturesUrl());
        } catch (JSONException e) {
            throw new RuntimeException("Something went wrong serializing the region set", e);
        }
        return tags;
    }
    public String getNameProperty() {
        return getStatsJSON().optString("nameIdTag");
    }
    public String getIdProperty() {
        return getStatsJSON().optString("regionIdTag");
    }
    public String getFeaturesUrl() {
        return getStatsJSON().optString("featuresUrl");
    }
    private JSONObject getStatsJSON() {
        JSONObject json = JSONHelper.createJSONObject(attributes);
        if(json == null) {
            return new JSONObject();
        }
        JSONObject stats = json.optJSONObject("statistics");

        if(stats == null) {
            return new JSONObject();
        }
        return stats;
    }

// -----------------

    public long getOskariLayerId() {
        return oskariLayerId;
    }

    public void setOskariLayerId(long oskariLayerId) {
        this.oskariLayerId = oskariLayerId;
    }

    public String getOskariLayerName() {
        return oskariLayerName;
    }

    public void setOskariLayerName(String oskariLayerName) {
        this.oskariLayerName = oskariLayerName;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }
    
    public String getUrl() {
        if (url == null || url.trim().equals("")) {
            // Default value.
            return "http://localhost:8080/geoserver";
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
