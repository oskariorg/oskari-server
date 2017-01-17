package fi.nls.oskari.control.statistics.data;

/**
 * Allowed types for indicator values.
 * In the future 2D and 3D types, and other kinds of types can be added when they can be visualized on the map.
 */
public enum IndicatorValueType {
    INTEGER("INTEGER"), BOOLEAN("BOOLEAN"), FLOAT("FLOAT");
    
    private String name;
    private IndicatorValueType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    @Override
    public String toString() {
        return name;
    }
}
