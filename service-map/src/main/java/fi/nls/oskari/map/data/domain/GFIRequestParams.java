package fi.nls.oskari.map.data.domain;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.nls.oskari.map.layer.formatters.LayerJSONFormatter.SUPPORTED_GET_FEATURE_INFO_FORMATS;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;

public class GFIRequestParams {
    private static final String DEFAULT_FORMAT = SUPPORTED_GET_FEATURE_INFO_FORMATS[0];
    private OskariLayer layer;

    private double lat;
    private double lon;
    private int zoom;
    private String x;
    private String y;
    private String bbox;
    private String width;
    private String height;
    private String currentStyle;
    private String srsName;
    private JSONObject additionalParams;

    private static final String WMS_GFI_FEATURE_COUNT = PropertyUtil.get("wms.gfi.feature.count", "50");
    private static final Map<String, String> WMS_GFI_BASE_PARAMS = new HashMap<String, String>() {{
        put("REQUEST", "GetFeatureInfo");
        put("EXCEPTIONS", "application/vnd.ogc.se_xml");
        put("VERSION", "1.1.1");
        put("FEATURE_COUNT", WMS_GFI_FEATURE_COUNT);
        put("FORMAT", "image/png");
        put("SERVICE", "WMS");
    }};

    public OskariLayer getLayer() {
        return layer;
    }

    public void setLayer(OskariLayer layer) {
        this.layer = layer;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getBbox() {
        return bbox;
    }

    public void setBbox(String bbox) {
        this.bbox = bbox;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getCurrentStyle() {
        if (currentStyle == null || "null".equals(currentStyle)) {
            currentStyle = layer.getStyle();
        }
        if (currentStyle == null) {
            currentStyle = "";
        }
        return currentStyle;
    }

    public void setCurrentStyle(String currentStyle) {
        this.currentStyle = currentStyle;
    }

    public String getSRSName() {
        return srsName;
    }

    public void setSRSName(String srsName) {
        this.srsName = srsName;
    }

    public JSONObject getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(JSONObject params) {
        this.additionalParams = params;
    }

    public String getGFIUrl() {
        return IOHelper.constructUrl(getBaseQueryURL(), getQueryMap());
    }

    private Map<String, String> getQueryMap() {
        String wmsName = layer.getName();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.putAll(WMS_GFI_BASE_PARAMS);
        queryMap.put("SRS", getSRSName());
        queryMap.put("BBOX", getBbox());
        queryMap.put("X", getX());
        queryMap.put("Y", getY());
        queryMap.put("INFO_FORMAT", getInfoFormat());
        queryMap.put("QUERY_LAYERS", wmsName);
        queryMap.put("WIDTH", getWidth());
        queryMap.put("HEIGHT", getHeight());
        queryMap.put("LAYERS", wmsName);

        if (additionalParams != null) {
            Map<String, String> additionalParamMap = JSONHelper.getObjectAsMap(additionalParams);
            queryMap.putAll(additionalParamMap);
        }
        return queryMap;
    }

    private String getBaseQueryURL() {
        String queryUrl = layer.getUrl();
        if (queryUrl.indexOf(',') != -1) {
            String[] urls = queryUrl.split(",");
            queryUrl = urls[0];
        }

        return queryUrl;
    }

    private String getInfoFormat() {
        String infoFormat = layer.getGfiType();
        if (infoFormat != null && !infoFormat.isEmpty()) {
            return infoFormat;
        }
        JSONObject formats = layer.getCapabilities().optJSONObject(KEY_FORMATS);
        if (formats == null) {
            return DEFAULT_FORMAT;
        }
        String value = formats.optString(KEY_VALUE);
        if (!value.isEmpty()) {
            return value;
        }
        List<String> available = JSONHelper.getArrayAsList(JSONHelper.getJSONArray(formats, KEY_AVAILABLE));
        for (String format : SUPPORTED_GET_FEATURE_INFO_FORMATS) {
            if (available.contains(format)) {
                return format;
            }
        }
        return DEFAULT_FORMAT;
    }

}
