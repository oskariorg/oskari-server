package org.oskari.print.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * WMS GetMap Request (KVP) Builder
 */
public class GetMapBuilder {

    private static final String DEFAULT_VERSION = "1.1.1";

    private String endPoint;
    private String version;
    private String bbox;
    private String crs;
    private int width;
    private int height;
    private String format;
    private List<String> layers = new ArrayList<>();
    private List<String> styles = new ArrayList<>();
    private boolean transparent;
    private Color bgColor;

    public GetMapBuilder endPoint(String endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public GetMapBuilder version(String version) {
        if (version == null || version.isEmpty()) {
            version = DEFAULT_VERSION;
        }
        this.version = version;
        return this;
    }

    public GetMapBuilder width(int width) {
        this.width = width;
        return this;
    }

    public GetMapBuilder height(int height) {
        this.height = height;
        return this;
    }

    public GetMapBuilder format(String format) {
        this.format = format;
        return this;
    }

    public GetMapBuilder bbox(double[] bbox) throws IllegalArgumentException {
        if (bbox == null || bbox.length != 4) {
            throw new IllegalArgumentException("bbox length must be 4");
        }
        this.bbox = bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3];
        return this;
    }

    public GetMapBuilder srs(String srs) {
        if (!srs.startsWith("EPSG:")) {
            throw new IllegalArgumentException("srs must begin with EPSG:");
        }
        this.crs = srs;
        return this;
    }

    public GetMapBuilder crs(String crs) {
        if (!crs.startsWith("EPSG:")) {
            throw new IllegalArgumentException("crs must begin with EPSG:");
        }
        this.crs = crs;
        return this;
    }

    public GetMapBuilder transparent(boolean transparent) {
        this.transparent = transparent;
        return this;
    }

    public GetMapBuilder bgColor(Color bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public GetMapBuilder layer(String layer) {
        return layer(layer, "");
    }

    public GetMapBuilder layer(String layer, String style) {
        layers.add(layer);
        styles.add(style);
        return this;
    }

    public GetMapBuilder clearLayers() {
        this.layers.clear();
        this.styles.clear();
        return this;
    }

    public String toKVP() {
        StringBuilder sb = new StringBuilder();
        sb.append(endPoint);
        int j = endPoint.indexOf('?');
        if (j < 0) {
            sb.append('?');
        } else if (j != (endPoint.length() - 1)) {
            sb.append('&');
        }
        sb.append("SERVICE=WMS");
        sb.append("&REQUEST=GetMap");
        sb.append("&VERSION=").append(version);
        sb.append("&BBOX=").append(bbox);
        if ("1.3.0".equals(version)) {
            sb.append("&CRS=");
        } else {
            sb.append("&SRS=");
        }
        sb.append(crs);
        sb.append("&WIDTH=").append(width);
        sb.append("&HEIGHT=").append(height);
        sb.append("&FORMAT=").append(format);

        sb.append("&LAYERS=");
        for (int i = 0; i < layers.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(layers.get(i));
        }
        sb.append("&STYLES=");
        for (int i = 0; i < layers.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(styles.get(i));
        }

        sb.append("&TRANSPARENT=").append(transparent);

        if (bgColor != null) {
            sb.append("&BGCOLOR=").append(colorToHex(bgColor));
        }

        return sb.toString();
    }

    public static String colorToHex(Color color) {
        final int argb = color.getRGB();
        final char[] arr = new char[6];

        int shift = 20;
        for (int i = 0; i < 6; i++) {
            int v = (argb >> shift) & 0xF;
            int ch = v < 10 ? v + '0' :  v - 10 + 'A';
            arr[i] = (char) ch;
            shift -= 4;
        }

        return new String(arr);
    }

}