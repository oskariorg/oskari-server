package fi.nls.oskari.control.statistics.db;

import fi.nls.oskari.util.JSONHelper;

import org.json.JSONObject;

/**
 * This is the value object for the layer url and other metadata in the database.
 * MyBatis Type for the SQL table oskari_maplayers
 */
public class RegionSet {

    private long id;
    private String name;
    private String url;
    private String srs_name;
    private String attributes;

    private JSONObject stats; // Lazily populated by getStatsJSON()

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSrs_name() {
        return srs_name;
    }

    public void setSrs_name(String srs_name) {
        this.srs_name = srs_name;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
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
