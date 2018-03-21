package fi.nls.oskari.control.statistics;

import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * Pairs of region codes and their respective names read from the geoserver layer region attributes.
 */
public class Region {
    public static final String KEY_CODE = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_JSON = "geojson";
    public static final String KEY_POINT = "point";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LON = "lon";

    private String code;
    private String name;
    private JSONObject geojson;
    private Point pointOnSurface;

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

    public Point getPointOnSurface() {
        return pointOnSurface;
    }

    public void setPointOnSurface(Point pointOnSurface) {
        this.pointOnSurface = pointOnSurface;
    }

    public JSONObject toJSON() {
        JSONObject item = new JSONObject();
        JSONHelper.putValue(item, KEY_CODE, getCode());
        JSONHelper.putValue(item, KEY_NAME, getName());
        JSONHelper.putValue(item, KEY_JSON, getGeojson());
        JSONObject point = JSONHelper.createJSONObject(KEY_LAT, getPointOnSurface().getLat());
        JSONHelper.putValue(point, KEY_LON, getPointOnSurface().getLon());
        JSONHelper.putValue(item, KEY_POINT, point);
        return item;
    }
}
