package fi.nls.oskari.control.statistics.plugins.db;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the value object for the statistical indicator data source definition rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_datasource
 */
public class StatisticalDatasource {

    private long id;
    private String locale;
    private String config;
    private String plugin;
    private List<DatasourceLayer> layers = new ArrayList<>();

    public List<DatasourceLayer> getLayers() {
        return layers;
    }

    public void setLayers(List<DatasourceLayer> layers) {
        this.layers = layers;
    }

    /**
     * The <plugin> in the plugin implementing class @Oskari("<name>") annotation.
     *
     * The plugin class must extend OskariComponent and implement StatisticalDatasourcePlugin interface
     * and be situated in the classpath.
     */
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public JSONObject getConfigJSON() {
        return JSONHelper.createJSONObject(config);
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
