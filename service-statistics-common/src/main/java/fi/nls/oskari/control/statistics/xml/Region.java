package fi.nls.oskari.control.statistics.xml;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * Pairs of region codes and their respective names read from the geoserver layer region attributes.
 */
public class Region {
    public static final String KEY_CODE = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_JSON = "geojson";
    private String code;
    private String name;
    private JSONObject geojson;

    public Region(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public JSONObject getGeojson() {
        return geojson;
    }

    public void setGeojson(JSONObject geojson) {
        this.geojson = geojson;
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

    public JSONObject toJSON() {
        JSONObject item = new JSONObject();
        JSONHelper.putValue(item, KEY_CODE, getCode());
        JSONHelper.putValue(item, KEY_NAME, getName());
        JSONHelper.putValue(item, KEY_JSON, getGeojson());
        return item;
    }
}
