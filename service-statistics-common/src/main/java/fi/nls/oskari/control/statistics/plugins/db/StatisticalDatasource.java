package fi.nls.oskari.control.statistics.plugins.db;

/**
 * This is the value object for the statistical indicator data source plugin definition rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_datasource_plugins
 */
public class StatisticalDatasource {
    /**
     * The <name> in the plugin implementing class @Oskari("<name>") annotation.
     * 
     * The plugin class must extend OskariComponent and implement StatisticalDatasourcePlugin interface
     * and be situated in the classpath.
     */
    private String componentId;
    /**
     * Localized strings in JSON, specifically for "name".
     */
    private String locale;
    public String getComponentId() {
        return componentId;
    }
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
}
