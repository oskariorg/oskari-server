package fi.nls.oskari.control.statistics.db;

/**
 * This is the value object for the layer url and other metadata in the database.
 * MyBatis Type for the SQL table oskari_maplayers
 */
public class LayerMetadata {
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
}
