package fi.nls.oskari.control.statistics.plugins.db;

/**
 * This is the value object for the SotkaNET plugin layer mapping rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_sotka_layers
 */
public class PluginLayer {
    /**
     * For example: "SotkaNET"
     */
    private String pluginId;

    /**
     *  For example one of: "Kunta","Maakunta","Erva","Aluehallintovirasto","Sairaanhoitopiiri","Seutukunta",
     *  "Nuts1","ELY-KESKUS"
     *  Note: These are case independent and the above list is possibly not exhaustive.
     *  These are the layer names used by the plugin.
     */
    private String pluginLayerId;
    
    /**
     *  The layer id in Oskari, for example: 7. This maps to the name in the oskari_maplayers table.
     */
    private Long oskariLayerId;

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getPluginLayerId() {
        return pluginLayerId;
    }

    public void setPluginLayerId(String pluginLayerId) {
        this.pluginLayerId = pluginLayerId;
    }

    public Long getOskariLayerId() {
        return oskariLayerId;
    }

    public void setOskariLayerId(Long oskariLayerId) {
        this.oskariLayerId = oskariLayerId;
    }
}
