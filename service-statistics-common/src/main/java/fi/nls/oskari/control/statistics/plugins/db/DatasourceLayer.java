package fi.nls.oskari.control.statistics.plugins.db;

/**
 * This is the value object for the SotkaNET plugin layer mapping rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_sotka_layers
 */
public class DatasourceLayer {

    /**
     * Id from datasource
     */
    private long datasourceId;
    /**
     *  The layer id in Oskari, for example: 7. This maps to the name in the oskari_maplayers table.
     */
    private long maplayerId;

    /**
     *  For example one of: "Kunta","Maakunta","Erva","Aluehallintovirasto","Sairaanhoitopiiri","Seutukunta",
     *  "Nuts1","ELY-KESKUS"
     *  Note: These are case independent and the above list is possibly not exhaustive.
     *  These are the layer names used by the plugin.
     */
    private String sourceProperty;

    /**
     *  Property on the maplayer that's matches values on sourceProperty
     */
    private String layerProperty;

    public long getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(long datasourceId) {
        this.datasourceId = datasourceId;
    }

    public long getMaplayerId() {
        return maplayerId;
    }

    public void setMaplayerId(long maplayerId) {
        this.maplayerId = maplayerId;
    }

    public String getSourceProperty() {
        return sourceProperty;
    }

    public void setSourceProperty(String sourceProperty) {
        this.sourceProperty = sourceProperty;
    }

    public String getLayerProperty() {
        return layerProperty;
    }

    public void setLayerProperty(String layerProperty) {
        this.layerProperty = layerProperty;
    }
}
