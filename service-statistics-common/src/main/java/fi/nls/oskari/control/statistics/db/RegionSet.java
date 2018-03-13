package fi.nls.oskari.control.statistics.db;

import fi.nls.oskari.control.statistics.xml.Region;
import fi.nls.oskari.control.statistics.xml.WfsXmlParser;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private JSONObject stats; // oskari_maplayer.attributes.statistics, lazily populated by getStatsJSON()

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

    public List<Region> getRegions(String requestedSRS) throws IOException, ServiceException {
        final String propId = getIdProperty();
        final String propName = getNameProperty();

        // For example: http://localhost:8080/geoserver/wfs?service=wfs&version=1.1.0&request=GetFeature&typeNames=oskari:kunnat2013
        //&propertyName=kuntakoodi,kuntanimi,geom
        Map<String, String> params = new HashMap<>();
        params.put("service", "wfs");
        params.put("version", "1.1.0");
        params.put("request", "GetFeature");
        params.put("typeName", getOskariLayerName());
        params.put("srsName", requestedSRS);
        //params.put("propertyName", propId + "," + propName);

        final String url = IOHelper.constructUrl(getFeaturesUrl(), params);
        final HttpURLConnection connection = IOHelper.getConnection(url);
        return WfsXmlParser.parse(connection.getInputStream(), propId, propName);
    }

}
