package fi.nls.oskari.control.statistics.plugins.db;

import org.json.JSONObject;

/**
 * This is the value object for the statistical datasource layer
 * for example SotkaNET plugin layer mapping rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_layers
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
     *  For example one of:
     *  {"regionType" : "Kunta"}
     *  or "Maakunta","Erva","Aluehallintovirasto","Sairaanhoitopiiri","Seutukunta",
     *  "Nuts1","ELY-KESKUS" for SotkaNet data
     *  Note: These are case independent and the above list is possibly not exhaustive.
     *  These are the layer names used by the plugin.
     */
    private JSONObject config;

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

    public String getConfig(String key) {
        if(config == null) {
            return null;
        }
        return config.optString(key);
    }
    public JSONObject getConfig() {
        return config;
    }

    public void setConfig(JSONObject config) {
        this.config = config;
    }
}
