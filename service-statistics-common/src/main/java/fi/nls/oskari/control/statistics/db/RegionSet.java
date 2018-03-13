package fi.nls.oskari.control.statistics.db;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * This is the value object for the layer url and other metadata in the database.
 * MyBatis Type for the SQL table oskari_maplayers
 */
public class RegionSet {

    private long oskariLayerId; // oskari_maplayer.id
    private String oskariLayerName; // oskari_maplayer.name
    private String url; // oskari_maplayer.url
    private String srs; // oskari_maplayer.srs_name
    private String attributes; // oskari_maplayer.attributes

    private JSONObject stats; // Lazily populated by getStatsJSON()

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
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSrs() {
        return srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }

    public JSONObject asJSON() {
        return JSONHelper.createJSONObject("regionIdTag", getIdProperty());
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
        if (stats == null) {
            JSONObject json = JSONHelper.createJSONObject(attributes);
            if (json != null) {
                stats = json.optJSONObject("statistics");
            }
            if (stats == null) {
                stats = new JSONObject();
            }
        }
        return stats;
    }

}
