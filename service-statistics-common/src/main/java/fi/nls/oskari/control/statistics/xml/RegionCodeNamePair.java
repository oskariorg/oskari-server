package fi.nls.oskari.control.statistics.xml;

/**
 * Pairs of region codes and their respective names read from the geoserver layer region attributes.
 */
public class RegionCodeNamePair {
    private String code;
    private String name;

    public RegionCodeNamePair(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return "[" + code + ", " + name + "]";
    }
}
