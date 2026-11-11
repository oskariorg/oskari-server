package org.oskari.print.mvt;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.print.request.PDPrintStyle.LineCap;
import org.oskari.print.request.PDPrintStyle.LineJoin;
import org.oskari.print.util.ColorUtil;


import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * A single layer of a Mapbox GL style document with the paint properties
 * resolved for the zoom level that is being printed.
 */
public class MVTStyleLayer {

    private static final Logger LOG = LogFactory.getLogger(MVTStyleLayer.class);

    public enum Type {
        FILL, LINE, CIRCLE, SYMBOL
    }

    /**
     * Which part of the label is placed at the anchor point. The names are the
     * Mapbox text-anchor values.
     */
    public enum TextAnchor {
        CENTER(-0.5f, -0.5f),
        LEFT(0f, -0.5f),
        RIGHT(-1f, -0.5f),
        TOP(-0.5f, -1f),
        BOTTOM(-0.5f, 0f),
        TOP_LEFT(0f, -1f),
        TOP_RIGHT(-1f, -1f),
        BOTTOM_LEFT(0f, 0f),
        BOTTOM_RIGHT(-1f, 0f);

        private final float x;
        private final float y;

        TextAnchor(float x, float y) {
            this.x = x;
            this.y = y;
        }

        /**
         * @return how far to shift the label, as a fraction of its width
         */
        public float getXFactor() {
            return x;
        }

        /**
         * @return how far to shift the label, as a fraction of its height
         */
        public float getYFactor() {
            return y;
        }

        public static TextAnchor get(String value) {
            if (value == null) {
                return CENTER;
            }
            switch (value) {
            case "left":
                return LEFT;
            case "right":
                return RIGHT;
            case "top":
                return TOP;
            case "bottom":
                return BOTTOM;
            case "top-left":
                return TOP_LEFT;
            case "top-right":
                return TOP_RIGHT;
            case "bottom-left":
                return BOTTOM_LEFT;
            case "bottom-right":
                return BOTTOM_RIGHT;
            default:
                return CENTER;
            }
        }
    }

    /**
     * Where a symbol layer puts its symbols. line-center is drawn like line but
     * only once, at the middle of the line.
     */
    public enum Placement {
        POINT, LINE, LINE_CENTER;

        public static Placement get(String value) {
            if (value == null) {
                return POINT;
            }
            switch (value) {
            case "line":
                return LINE;
            case "line-center":
                return LINE_CENTER;
            default:
                return POINT;
            }
        }

        public boolean isAlongLine() {
            return this != POINT;
        }
    }

    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final double DEFAULT_LINE_WIDTH = 1;
    private static final double DEFAULT_CIRCLE_RADIUS = 5;
    private static final double DEFAULT_TEXT_SIZE = 16;
    private static final double DEFAULT_TEXT_MAX_WIDTH = 10;
    private static final double DEFAULT_TEXT_MAX_ANGLE = 45;
    private static final double DEFAULT_SYMBOL_SPACING = 250;
    private static final double DEFAULT_PADDING = 2;

    private final String id;
    private final Type type;
    private final String sourceLayer;
    private final Predicate<Map<String, Object>> filter;

    private Color fillColor;
    private Color lineColor;
    /** Set in place of the colour above when the style picks it per feature */
    private Function<Map<String, Object>, Color> fillColorByFeature;
    private Function<Map<String, Object>, Color> lineColorByFeature;
    private double lineWidth;
    private float[] lineDash;
    private LineCap lineCap = LineCap.butt;
    private LineJoin lineJoin = LineJoin.miter;
    private double circleRadius;
    private Object textField;
    private double textSize;
    private TextAnchor textAnchor;
    private double textOffsetX;
    private double textOffsetY;
    private String textTransform;
    private double textLetterSpacing;
    private double textMaxWidth;
    private double textPadding;
    private Placement placement;
    /** Whether the filter asks for $type, so the geometry is only looked at when it does */
    private boolean usesGeometryType;
    private double symbolSpacing;
    private double textMaxAngle;
    private boolean textKeepUpright;
    private Color haloColor;
    private double haloWidth;
    private boolean textAllowOverlap;
    private boolean textIgnorePlacement;
    private boolean textOptional;

    private List<String> textFont = Collections.emptyList();
    private Object iconImage;
    private double iconSize;
    private double iconRotate;
    private TextAnchor iconAnchor;
    private double iconOffsetX;
    private double iconOffsetY;
    private double iconPadding;
    private float iconOpacity;
    private boolean iconAllowOverlap;
    private boolean iconIgnorePlacement;
    private boolean iconOptional;

    private MVTStyleLayer(String id, Type type, String sourceLayer,
            Predicate<Map<String, Object>> filter) {
        this.id = id;
        this.type = type;
        this.sourceLayer = sourceLayer;
        this.filter = filter;
    }

    /**
     * @param zoom the zoom level the print is rendered at, zoom dependent
     *             properties are resolved against it
     * @return null if the layer isn't drawn at this zoom or isn't a supported type
     */
    static MVTStyleLayer parse(JSONObject styleLayer, double zoom) {
        if (styleLayer == null) {
            return null;
        }
        String id = styleLayer.optString("id", "");
        Type type = parseType(styleLayer.optString("type", null));
        if (type == null) {
            LOG.debug("Skipping unsupported style layer type on layer:", id);
            return null;
        }
        if (!isVisible(styleLayer, zoom)) {
            return null;
        }

        JSONArray filter = styleLayer.optJSONArray("filter");
        MVTStyleLayer layer = new MVTStyleLayer(id, type,
                styleLayer.optString("source-layer", null),
                MVTFilter.parse(filter));
        String filterText = filter == null ? "" : filter.toString();
        layer.usesGeometryType = filterText.contains(MVTFilter.TYPE_KEY)
                || filterText.contains(MVTFilter.GEOMETRY_TYPE);

        JSONObject paint = optObject(styleLayer, "paint");
        JSONObject layout = optObject(styleLayer, "layout");
        // A pattern replaces the colour rather than tinting it, so a layer that
        // asks for one and can't have it is left undrawn: filling it with the
        // default colour would paint over the layers below
        if (hasPattern(paint, type)) {
            LOG.debug("Skipping layer with an unsupported pattern fill:", id);
            return null;
        }
        switch (type) {
        case FILL:
            layer.fillColorByFeature = parseColorMatch(paint, "fill-color", "fill-opacity", zoom);
            if (layer.fillColorByFeature == null) {
                // A fill colour the print can't work out would cover what is
                // below it in black, so the default only stands in for a
                // missing one
                Color fallback = isAbsent(paint.opt("fill-color")) ? DEFAULT_COLOR : null;
                layer.fillColor = parseColor(paint, "fill-color", "fill-opacity", zoom, fallback);
            }
            if (paint.has("fill-outline-color")) {
                layer.lineColorByFeature =
                        parseColorMatch(paint, "fill-outline-color", "fill-opacity", zoom);
                if (layer.lineColorByFeature == null) {
                    layer.lineColor =
                            parseColor(paint, "fill-outline-color", "fill-opacity", zoom, null);
                }
                layer.lineWidth = DEFAULT_LINE_WIDTH;
            }
            break;
        case LINE:
            layer.lineColorByFeature = parseColorMatch(paint, "line-color", "line-opacity", zoom);
            if (layer.lineColorByFeature == null) {
                layer.lineColor = parseColor(paint, "line-color", "line-opacity", zoom, DEFAULT_COLOR);
            }
            layer.lineWidth = MVTExpressions.getNumber(paint.opt("line-width"), zoom, DEFAULT_LINE_WIDTH);
            layer.lineDash = parseDashArray(paint.opt("line-dasharray"));
            // Mapbox names the ends and corners of a line the same way PDF does
            LineCap cap = LineCap.get(MVTExpressions.getString(layout.opt("line-cap"), zoom, null));
            if (cap != null) {
                layer.lineCap = cap;
            }
            LineJoin join = LineJoin.get(
                    MVTExpressions.getString(layout.opt("line-join"), zoom, null));
            if (join != null) {
                layer.lineJoin = join;
            }
            break;
        case CIRCLE:
            layer.fillColor = parseColor(paint, "circle-color", "circle-opacity", zoom, DEFAULT_COLOR);
            layer.circleRadius = MVTExpressions.getNumber(paint.opt("circle-radius"), zoom, DEFAULT_CIRCLE_RADIUS);
            double strokeWidth = MVTExpressions.getNumber(paint.opt("circle-stroke-width"), zoom, 0);
            if (strokeWidth > 0) {
                layer.lineColor = parseColor(paint, "circle-stroke-color", "circle-stroke-opacity", zoom, DEFAULT_COLOR);
                layer.lineWidth = strokeWidth;
            }
            break;
        case SYMBOL:
            layer.textField = layout.opt("text-field");
            layer.iconImage = layout.opt("icon-image");
            if (isAbsent(layer.textField) && isAbsent(layer.iconImage)) {
                LOG.debug("Skipping symbol layer with neither text-field nor icon-image:", id);
                return null;
            }
            layer.textSize = MVTExpressions.getNumber(layout.opt("text-size"), zoom, DEFAULT_TEXT_SIZE);
            layer.fillColor = parseColor(paint, "text-color", "text-opacity", zoom, DEFAULT_COLOR);
            layer.textAnchor = TextAnchor.get(MVTExpressions.getString(layout.opt("text-anchor"), zoom, null));
            double[] offset = parseOffset(layout.opt("text-offset"));
            layer.textOffsetX = offset[0];
            layer.textOffsetY = offset[1];
            layer.textTransform = MVTExpressions.getString(layout.opt("text-transform"), zoom, null);
            layer.textFont = parseFontstack(layout.opt("text-font"));
            layer.textLetterSpacing = MVTExpressions.getNumber(
                    layout.opt("text-letter-spacing"), zoom, 0);
            layer.textMaxWidth = MVTExpressions.getNumber(
                    layout.opt("text-max-width"), zoom, DEFAULT_TEXT_MAX_WIDTH);
            layer.textPadding = MVTExpressions.getNumber(
                    layout.opt("text-padding"), zoom, DEFAULT_PADDING);
            layer.iconPadding = MVTExpressions.getNumber(
                    layout.opt("icon-padding"), zoom, DEFAULT_PADDING);

            layer.placement = Placement.get(MVTExpressions.getString(layout.opt("symbol-placement"), zoom, null));
            layer.symbolSpacing = MVTExpressions.getNumber(
                    layout.opt("symbol-spacing"), zoom, DEFAULT_SYMBOL_SPACING);
            layer.textMaxAngle = Math.toRadians(MVTExpressions.getNumber(
                    layout.opt("text-max-angle"), zoom, DEFAULT_TEXT_MAX_ANGLE));
            layer.textKeepUpright = layout.optBoolean("text-keep-upright", true);

            layer.haloColor = parseColor(paint, "text-halo-color", "text-opacity", zoom, null);
            layer.haloWidth = MVTExpressions.getNumber(paint.opt("text-halo-width"), zoom, 0);

            layer.textAllowOverlap = layout.optBoolean("text-allow-overlap", false);
            layer.textIgnorePlacement = layout.optBoolean("text-ignore-placement", false);
            layer.textOptional = layout.optBoolean("text-optional", false);
            layer.iconAllowOverlap = layout.optBoolean("icon-allow-overlap", false);
            layer.iconIgnorePlacement = layout.optBoolean("icon-ignore-placement", false);
            layer.iconOptional = layout.optBoolean("icon-optional", false);

            layer.iconSize = MVTExpressions.getNumber(layout.opt("icon-size"), zoom, 1);
            layer.iconRotate = Math.toRadians(MVTExpressions.getNumber(
                    layout.opt("icon-rotate"), zoom, 0));
            layer.iconAnchor = TextAnchor.get(MVTExpressions.getString(layout.opt("icon-anchor"), zoom, null));
            double[] iconOffset = parseOffset(layout.opt("icon-offset"));
            layer.iconOffsetX = iconOffset[0];
            layer.iconOffsetY = iconOffset[1];
            layer.iconOpacity = (float) MVTExpressions.getNumber(
                    paint.opt("icon-opacity"), zoom, 1);
            break;
        }
        return layer;
    }

    private static List<String> parseFontstack(Object value) {
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            if (array.length() > 0 && "literal".equals(array.optString(0, null))) {
                return parseFontstack(array.opt(1));
            }
            List<String> fonts = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                String font = array.optString(i, null);
                if (font != null && !font.isEmpty()) {
                    fonts.add(font);
                }
            }
            return fonts;
        }
        if (value instanceof String) {
            return Collections.singletonList((String) value);
        }
        if (value instanceof JSONObject) {
            // Legacy stop function: take the last stop's value, fonts rarely
            // change with zoom and the largest zoom is the most detailed
            JSONArray stops = ((JSONObject) value).optJSONArray("stops");
            if (stops != null && stops.length() > 0) {
                JSONArray last = stops.optJSONArray(stops.length() - 1);
                if (last != null) {
                    return parseFontstack(last.opt(1));
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * @return true when the style layer doesn't set the property at all
     */
    private static boolean isAbsent(Object value) {
        return value == null || value == JSONObject.NULL
                || (value instanceof String && ((String) value).isEmpty());
    }

    private static Type parseType(String type) {
        if (type == null) {
            return null;
        }
        switch (type) {
        case "fill":
            return Type.FILL;
        case "line":
            return Type.LINE;
        case "circle":
            return Type.CIRCLE;
        case "symbol":
            return Type.SYMBOL;
        default:
            // background, raster, hillshade, heatmap, fill-extrusion
            return null;
        }
    }

    private static boolean isVisible(JSONObject styleLayer, double zoom) {
        JSONObject layout = optObject(styleLayer, "layout");
        if ("none".equals(layout.optString("visibility", null))) {
            return false;
        }
        // maxzoom is exclusive, minzoom inclusive
        if (styleLayer.has("minzoom") && zoom < styleLayer.optDouble("minzoom", 0)) {
            return false;
        }
        if (styleLayer.has("maxzoom") && zoom >= styleLayer.optDouble("maxzoom", Double.MAX_VALUE)) {
            return false;
        }
        return true;
    }

    private static JSONObject optObject(JSONObject obj, String key) {
        JSONObject value = obj.optJSONObject(key);
        return value != null ? value : new JSONObject();
    }

    /**
     * Mapbox keeps color and opacity in separate properties, combine them into
     * a single color with an alpha channel.
     */
    private static Color parseColor(JSONObject paint, String colorKey, String opacityKey,
            double zoom, Color fallback) {
        String value = MVTExpressions.getString(paint.opt(colorKey), zoom, null);
        Color color = value == null ? fallback : parseColor(value);
        return withOpacity(color, MVTExpressions.getNumber(paint.opt(opacityKey), zoom, 1));
    }

    /**
     * A colour that depends on the feature, as ["match", ["get", key], ...]
     * gives one. Each output is parsed once, a feature only looks its own up.
     * A transparent output is null, so its features are not drawn at all.
     *
     * @return null when the colour is not picked this way
     */
    private static Function<Map<String, Object>, Color> parseColorMatch(JSONObject paint,
            String colorKey, String opacityKey, double zoom) {
        Object value = paint.opt(colorKey);
        if (!(value instanceof JSONArray) || !"match".equals(((JSONArray) value).optString(0, null))) {
            return null;
        }
        Function<Map<String, Object>, Object> match = MVTFilter.match((JSONArray) value);
        if (match == null) {
            return null;
        }
        double opacity = MVTExpressions.getNumber(paint.opt(opacityKey), zoom, 1);
        Map<Object, Color> colors = new HashMap<>();
        return attributes -> colors.computeIfAbsent(match.apply(attributes), output -> {
            Color color = output instanceof String
                    ? withOpacity(parseColor((String) output), opacity)
                    : null;
            return color == null || color.getAlpha() == 0 ? null : color;
        });
    }

    private static Color withOpacity(Color color, double opacity) {
        if (color == null) {
            return null;
        }
        if (opacity >= 1) {
            return color;
        }
        if (opacity <= 0) {
            return null;
        }
        int alpha = (int) Math.round(color.getAlpha() * opacity);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private static boolean hasPattern(JSONObject paint, Type type) {
        switch (type) {
        case FILL:
            return !isAbsent(paint.opt("fill-pattern"));
        case LINE:
            return !isAbsent(paint.opt("line-pattern"));
        default:
            return false;
        }
    }

    /**
     * ColorUtil throws on values it doesn't recognize (named colors),
     * skipping the styling is better than failing the whole print.
     */
    private static Color parseColor(String value) {
        try {
            return ColorUtil.parseColor(value);
        } catch (Exception e) {
            LOG.debug("Unsupported color value:", value);
            return null;
        }
    }

    /**
     * @return [x, y], zeroes when missing or malformed
     */
    private static double[] parseOffset(Object value) {
        JSONArray offset = value instanceof JSONArray ? unwrapLiteral((JSONArray) value) : null;
        if (offset == null || offset.length() != 2) {
            return new double[2];
        }
        double x = offset.optDouble(0, Double.NaN);
        double y = offset.optDouble(1, Double.NaN);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return new double[2];
        }
        return new double[] { x, y };
    }

    private static float[] parseDashArray(Object value) {
        JSONArray dash = value instanceof JSONArray ? unwrapLiteral((JSONArray) value) : null;
        if (dash == null || dash.length() == 0) {
            return null;
        }
        float[] pattern = new float[dash.length()];
        for (int i = 0; i < dash.length(); i++) {
            double length = dash.optDouble(i, -1);
            if (length < 0) {
                return null;
            }
            pattern[i] = (float) length;
        }
        return pattern;
    }

    private static JSONArray unwrapLiteral(JSONArray array) {
        return "literal".equals(array.optString(0, null)) ? array.optJSONArray(1) : array;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getSourceLayer() {
        return sourceLayer;
    }

    /**
     * $type has only Point, LineString and Polygon; multi geometries answer as
     * their single counterparts.
     */
    public boolean matches(Map<String, Object> attributes, Geometry geometry) {
        if (geometry == null || !usesGeometryType) {
            return filter.test(attributes);
        }
        Map<String, Object> withType = new HashMap<>(attributes == null
                ? Collections.emptyMap()
                : attributes);
        withType.put(MVTFilter.TYPE_KEY, getFilterType(geometry));
        return filter.test(withType);
    }

    /**
     * @return the geometry as the spec's $type names it, null for a type it has
     *         no name for
     */
    private static String getFilterType(Geometry geometry) {
        if (geometry instanceof Point || geometry instanceof MultiPoint) {
            return "Point";
        }
        if (geometry instanceof LineString || geometry instanceof MultiLineString) {
            return "LineString";
        }
        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            return "Polygon";
        }
        return null;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public Color getLineColor() {
        return lineColor;
    }

    /**
     * @return the fill colour of one feature, which is the layer's own unless
     *         the style picks it by the feature's attributes
     */
    public Color getFillColor(Map<String, Object> attributes) {
        return fillColorByFeature != null ? fillColorByFeature.apply(attributes) : fillColor;
    }

    public Color getLineColor(Map<String, Object> attributes) {
        return lineColorByFeature != null ? lineColorByFeature.apply(attributes) : lineColor;
    }

    /**
     * @return true when the colours are picked per feature and have to be set
     *         for each one rather than once for the layer
     */
    public boolean isColoredByFeature() {
        return fillColorByFeature != null || lineColorByFeature != null;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public LineCap getLineCap() {
        return lineCap;
    }

    public LineJoin getLineJoin() {
        return lineJoin;
    }

    public float[] getLineDash() {
        return lineDash;
    }

    public double getCircleRadius() {
        return circleRadius;
    }

    public Object getTextField() {
        return textField;
    }

    /**
     * @return the layer's text-font, in the order the style gives it. Empty when
     *         the layer names no font, which leaves the style's default.
     */
    public List<String> getTextFont() {
        return textFont;
    }

    public Object getIconImage() {
        return iconImage;
    }

    public boolean hasText() {
        return !isAbsent(textField);
    }

    public boolean hasIcon() {
        return !isAbsent(iconImage);
    }

    public String getTextTransform() {
        return textTransform;
    }

    /**
     * @return extra space between characters, in ems
     */
    public double getTextLetterSpacing() {
        return textLetterSpacing;
    }

    /**
     * @return the width a label wraps at, in ems
     */
    public double getTextMaxWidth() {
        return textMaxWidth;
    }

    /**
     * @return space kept clear around a label when testing for collisions, in pixels
     */
    public double getTextPadding() {
        return textPadding;
    }

    public double getIconPadding() {
        return iconPadding;
    }

    public Placement getPlacement() {
        return placement;
    }

    /**
     * @return distance between repeats of a label along a line, in pixels
     */
    public double getSymbolSpacing() {
        return symbolSpacing;
    }

    /**
     * @return largest turn a label follows along a line, in radians
     */
    public double getTextMaxAngle() {
        return textMaxAngle;
    }

    public boolean isTextKeepUpright() {
        return textKeepUpright;
    }

    /**
     * @return halo colour, or null when the layer draws no halo
     */
    public Color getHaloColor() {
        return haloColor;
    }

    /**
     * @return halo width in pixels
     */
    public double getHaloWidth() {
        return haloWidth;
    }

    public boolean isTextAllowOverlap() {
        return textAllowOverlap;
    }

    public boolean isTextIgnorePlacement() {
        return textIgnorePlacement;
    }

    public boolean isTextOptional() {
        return textOptional;
    }

    public boolean isIconAllowOverlap() {
        return iconAllowOverlap;
    }

    public boolean isIconIgnorePlacement() {
        return iconIgnorePlacement;
    }

    public boolean isIconOptional() {
        return iconOptional;
    }

    public double getIconSize() {
        return iconSize;
    }

    /**
     * @return how far the icon is turned, in radians
     */
    public double getIconRotate() {
        return iconRotate;
    }

    public TextAnchor getIconAnchor() {
        return iconAnchor;
    }

    public double getIconOffsetX() {
        return iconOffsetX;
    }

    public double getIconOffsetY() {
        return iconOffsetY;
    }

    public float getIconOpacity() {
        return iconOpacity;
    }

    /**
     * @return text size in screen pixels, as in the Mapbox style
     */
    public double getTextSize() {
        return textSize;
    }

    public TextAnchor getTextAnchor() {
        return textAnchor;
    }

    /**
     * @return label offset in ems, positive y points down as in the Mapbox style
     */
    public double getTextOffsetX() {
        return textOffsetX;
    }

    public double getTextOffsetY() {
        return textOffsetY;
    }

    /**
     * @return false when the layer resolved to nothing visible at this zoom
     */
    boolean isDrawable() {
        switch (type) {
        case FILL:
            return fillColor != null || lineColor != null || isColoredByFeature();
        case LINE:
            return (lineColor != null || lineColorByFeature != null) && lineWidth > 0;
        case CIRCLE:
            return (fillColor != null || lineColor != null) && circleRadius > 0;
        case SYMBOL:
            return fillColor != null && textSize > 0;
        default:
            return false;
        }
    }

    public static List<MVTStyleLayer> parseAll(JSONArray styleLayers, double zoom) {
        List<MVTStyleLayer> layers = new ArrayList<>();
        if (styleLayers == null) {
            return layers;
        }
        for (int i = 0; i < styleLayers.length(); i++) {
            MVTStyleLayer layer = parse(styleLayers.optJSONObject(i), zoom);
            if (layer != null && layer.isDrawable()) {
                layers.add(layer);
            }
        }
        return layers;
    }
}
