package fi.nls.oskari.control.statistics.plugins.db;

/**
 * This is the value object for the statistical indicator data source plugin definition rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_datasource_plugins
 */
public class StatisticalDatasource {
    /**
     * A fully qualified name for the implementing class, for example:
     * 'fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin'
     * The plugin class must implement StatisticalDatasourcePlugin interface and be situated in the classpath.
     */
    private String className;
    /**
     * Localized strings in JSON, specifically for "name".
     */
    private String locale;
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
}
