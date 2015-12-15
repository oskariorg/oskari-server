package fi.nls.oskari.control.statistics.plugins.sotka.db;

/**
 * This is the value object for the SotkaNET plugin layer mapping rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_sotka_layers
 */
public class SotkaLayer {
    /**
     *  One of: "Kunta","Maakunta","Erva","Aluehallintovirasto","Sairaanhoitopiiri","Seutukunta","Nuts1","ELY-KESKUS"
     *  Note: These are case independent and the above list is possibly not exhaustive.
     */
    private String sotkaLayerId;
    
    /**
     *  The layer name in Oskari, for example: "oskari:kunnat2013". This maps to the name in the oskari_maplayers table.
     */
    private String oskariLayerName;

    public String getSotkaLayerId() {
        return sotkaLayerId;
    }

    public void setSotkaLayerId(String sotkaLayerId) {
        this.sotkaLayerId = sotkaLayerId;
    }

    public String getOskariLayerName() {
        return oskariLayerName;
    }

    public void setOskariLayerName(String oskariLayerName) {
        this.oskariLayerName = oskariLayerName;
    }
}
