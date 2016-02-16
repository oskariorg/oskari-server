package fi.nls.oskari.control.statistics.db;

/**
 * This is the value object for the layer attribute mapping rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_layers
 */
public class Layer {
    /**
     *  The layer id in Oskari, for example: 9. This maps to the name in the oskari_maplayers table.
     */
    private long oskariLayerId;
    
    /**
     * The attribute name for the region id in the geoserver. For example: "kuntakoodi"
     * Note that the attribute mapping is here, and not in the Oskari side to allow different attributes
     * for one layer to designate different ids.
     */
    private String oskariRegionIdTag;
    
    /**
     *  The attribute name for the region name in the geoserver. For example: "kuntanimi"
     *  This could also be in the Oskari side of processing, and not in the plugin, but it's more convenient here
     *  implementationwise.
     */
    private String oskariNameIdTag;

    public long getOskariLayerId() {
        return oskariLayerId;
    }

    public void setOskariLayerId(long oskariLayerId) {
        this.oskariLayerId = oskariLayerId;
    }

    public String getOskariRegionIdTag() {
        return oskariRegionIdTag;
    }

    public void setOskariRegionIdTag(String oskariRegionIdTag) {
        this.oskariRegionIdTag = oskariRegionIdTag;
    }

    public String getOskariNameIdTag() {
        return oskariNameIdTag;
    }

    public void setOskariNameIdTag(String oskariNameIdTag) {
        this.oskariNameIdTag = oskariNameIdTag;
    }
}
