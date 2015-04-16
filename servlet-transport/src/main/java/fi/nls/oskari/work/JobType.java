package fi.nls.oskari.work;

/**
 * Created by SMAKINEN on 10.3.2015.
 */
public enum JobType {
    NORMAL("normal"), HIGHLIGHT("highlight"), MAP_CLICK("mapClick"), GEOJSON(
            "geoJSON"), PROPERTY_FILTER("property_filter");

    protected final String name;

    private JobType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
