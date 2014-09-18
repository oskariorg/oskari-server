package fi.nls.oskari.map.data.domain;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GFIRequestParams {

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

    private static final String WMS_GFI_FEAUTURE_COUNT = PropertyUtil.get("wms.gfi.feature.count","50");
    private static final String WMS_GFI_BASE_PARAMS = "REQUEST=GetFeatureInfo"
            + "&EXCEPTIONS=application/vnd.ogc.se_xml" + "&VERSION=1.1.1"
            + "&FEATURE_COUNT="+ WMS_GFI_FEAUTURE_COUNT  + "&FORMAT=image/png" + "&SERVICE=WMS";

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
        if(currentStyle == null) {
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

    public String getGFIUrl() {
        return getBaseQueryURL() + getAsQueryString();
    }

    private String getAsQueryString() {
        String infoFormat = layer.getGfiType();
        if (infoFormat == null || "".equals(infoFormat)) {
            infoFormat = "text/html";
        }

        String wmsName = layer.getName();

        try { // try encode
            wmsName =  URLEncoder.encode(layer.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // encode unsupported then ignore it and use without encode
        }


        return WMS_GFI_BASE_PARAMS + "&SRS=" + getSRSName() + "&BBOX=" + getBbox() + "&X=" + getX()
                + "&Y=" + getY() + "&INFO_FORMAT=" + infoFormat
                + "&QUERY_LAYERS=" + wmsName + "&WIDTH="
                + getWidth() + "&HEIGHT=" + getHeight() + "&STYLES="
                + getCurrentStyle() + "&LAYERS=" + wmsName;
    }

    private String getBaseQueryURL() {
        String queryUrl = layer.getUrl();
        if (queryUrl.indexOf(',') != -1) {
            String[] urls = queryUrl.split(",");
            queryUrl = urls[0];
        }

        if (queryUrl.indexOf("?") < 0) {
            queryUrl += "?";
        }
        return queryUrl;
    }

}
