package fi.nls.oskari.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.io.IOException;

public class WFSCustomStyleStore {
    // custom style params
    public static final String PARAM_FILL_COLOR = "fill_color";
    public static final String PARAM_FILL_PATTERN = "fill_pattern";
    public static final String PARAM_BORDER_COLOR = "border_color";
    public static final String PARAM_BORDER_LINEJOIN = "border_linejoin";
    public static final String PARAM_BORDER_DASHARRAY = "border_dasharray";
    public static final String PARAM_BORDER_WIDTH = "border_width";
    public static final String PARAM_STROKE_LINECAP = "stroke_linecap";
    public static final String PARAM_STROKE_COLOR = "stroke_color";
    public static final String PARAM_STROKE_LINEJOIN = "stroke_linejoin";
    public static final String PARAM_STROKE_DASHARRAY = "stroke_dasharray";
    public static final String PARAM_STROKE_WIDTH = "stroke_width";
    public static final String PARAM_DOT_COLOR = "dot_color";
    public static final String PARAM_DOT_SHAPE = "dot_shape";
    public static final String PARAM_DOT_SIZE = "dot_size";
    private static final Logger log = LogFactory.getLogger(WFSCustomStyleStore.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String KEY = "WFSCustomStyle_";

    public static final String HIGHLIGHT_FILL_COLOR = "#FAEBD7";
    public static final String HIGHLIGHT_BORDER_COLOR = "#000000";
    public static final String HIGHLIGHT_STROKE_COLOR = "#FAEBD7";
    public static final String HIGHLIGHT_DOT_COLOR = "#FAEBD7";

    public static final String POLYGON_FILL_PARTIAL = "polygon_fill_partial";
    public static final String POLYGON_STROKE_PARTIAL = "polygon_stroke_partial";

    public static final String GEOMETRY = "defaultGeometry";
    public static final String FILL_PATTERN_PARTIAL = "fill_pattern_partial";
    public static final String LINE_STYLE_PARTIAL = "line_style_partial";
    public static final String BORDER_STYLE_PARTIAL = "border_style_partial";

    public static final String POLYGON_FILL_ELEMENT = "<Fill>\n" +
            "    fill_pattern_partial\n" +
            "    <CssParameter name=\"fill-opacity\">0.7</CssParameter>\n" +
            "</Fill>";

    public static final String POLYGON_STROKE_ELEMENT = "<Stroke>\n" +
            "    <CssParameter name=\"stroke\">border_color</CssParameter>\n" +
            "    <CssParameter name=\"stroke-width\">border_width</CssParameter>\n" +
            "    <CssParameter name=\"stroke-linejoin\">border_linejoin</CssParameter>\n" +
            "    border_style_partial\n" +
            "</Stroke>";

    public static final String LINE_STYLE_PARTIAL_LINECAP = "<CssParameter name=\"stroke-linecap\">stroke_linecap</CssParameter>";
    public static final String LINE_STYLE_PARTIAL_DASHARRAY = "<CssParameter name=\"stroke-dasharray\">5 2</CssParameter>";
    public static final String BORDER_STYLE_PARTIAL_DASHARRAY = "<CssParameter name=\"stroke-dasharray\">5 2</CssParameter>";

    public static final String FILL_PATTERN_PARTIAL_DEFAULT = "<CssParameter name=\"fill\">fill_color</CssParameter>";
    public static final String FILL_PATTERN_PARTIAL_MARK = "<GraphicFill>\n" +
            "    <Graphic>\n" +
            "        <Mark>\n" +
            "            <WellKnownName>shape://fill_mark</WellKnownName>\n" +
            "            <Stroke>\n" +
            "                <CssParameter name=\"stroke\">fill_color</CssParameter>\n" +
            "                <CssParameter name=\"stroke-width\">fill_stroke_width</CssParameter>\n" +
            "            </Stroke>\n" +
            "        </Mark>\n" +
            "        <Size>fill_size</Size>\n" +
            "    </Graphic>\n" +
            "</GraphicFill>";


    private String client;
    private String layerId;

    private String fillColor;
    private int fillPattern;
    private String borderColor;
    private String borderLinejoin;
    private String borderDasharray;
    private int borderWidth;

    private String strokeLinecap;
    private String strokeColor;
    private String strokeLinejoin;
    private String strokeDasharray;
    private int strokeWidth;

    private String dotColor;
    private int dotShape;
    private int dotSize;

    private String geometry;
    private String sld;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        if(fillColor != null) {
            this.fillColor = addPrefixColor(fillColor);
        } else {
            this.fillColor = fillColor;
        }
    }

    public int getFillPattern() {
        return fillPattern;
    }

    public void setFillPattern(int fillPattern) {
        this.fillPattern = fillPattern;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        if(borderColor != null) {
            this.borderColor = addPrefixColor(borderColor);
        } else {
            this.borderColor = borderColor;
        }
    }

    public String getBorderLinejoin() {
        return borderLinejoin;
    }

    public void setBorderLinejoin(String borderLinejoin) {
        this.borderLinejoin = borderLinejoin;
    }

    public String getBorderDasharray() {
        return borderDasharray;
    }

    public void setBorderDasharray(String borderDasharray) {
        this.borderDasharray = borderDasharray;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public String getStrokeLinecap() {
        return strokeLinecap;
    }

    public void setStrokeLinecap(String strokeLinecap) {
        this.strokeLinecap = strokeLinecap;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = addPrefixColor(strokeColor);
    }

    public String getStrokeLinejoin() {
        return strokeLinejoin;
    }

    public void setStrokeLinejoin(String strokeLinejoin) {
        this.strokeLinejoin = strokeLinejoin;
    }

    public String getStrokeDasharray() {
        return strokeDasharray;
    }

    public void setStrokeDasharray(String strokeDasharray) {
        this.strokeDasharray = strokeDasharray;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public String getDotColor() {
        return dotColor;
    }

    public void setDotColor(String dotColor) {
        this.dotColor = addPrefixColor(dotColor);
    }

    public int getDotShape() {
        return dotShape;
    }

    public void setDotShape(int dotShape) {
        this.dotShape = dotShape;
    }

    public int getDotSize() {
        return dotSize;
    }

    public void setDotSize(int dotSize) {
        this.dotSize = dotSize;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    @JsonIgnore
    public String getSld() {
        return sld;
    }

    /**
     * Adds '#' in front of color codes for the back
     *
     * @param color
     * @return color with '#'
     */
    @JsonIgnore
    public String addPrefixColor(String color) {
        if(color.charAt(0) != '#') {
            return '#' + color;
        }
        return color;
    }

    /**
     * Removes '#' in front of color codes for the front
     *
     * @param color
     * @return color without '#'
     */
    @JsonIgnore
    public String removePrefixColor(String color) {
        if(color.charAt(0) == '#') {
            return color.substring(1);
        }
        return color;
    }

    /**
     * Sets sld by combining values with given template
     *
     * @param template
     * @param isHighlight
     */
    public void replaceValues(String template, boolean isHighlight) {
        if(geometry == null) {
            log.warn("No geometry set");
            return;
        }

        // all point scales
        for(int i = 18; i < 43; i+=2) {
            template = template.replaceAll(PARAM_DOT_SIZE + i, Integer.toString(dotSize*i));
        }

        // line styles
        String lineStylePartial;
        if(strokeDasharray.equals("")) {
            lineStylePartial = LINE_STYLE_PARTIAL_LINECAP;
        } else {
            lineStylePartial = LINE_STYLE_PARTIAL_DASHARRAY;
        }
        template = template.replaceAll(LINE_STYLE_PARTIAL, lineStylePartial);

        // Polygon Fill & Stroke elements
        if(fillColor != null || isHighlight){
            template = template.replaceAll(POLYGON_FILL_PARTIAL, POLYGON_FILL_ELEMENT);
        }
        if(borderColor != null || isHighlight){
            template = template.replaceAll(POLYGON_STROKE_PARTIAL, POLYGON_STROKE_ELEMENT);
        }

        // fill patterns
        String fillPatternPartial;
        if(fillPattern == -1) {
            fillPatternPartial = FILL_PATTERN_PARTIAL_DEFAULT;
        } else if(fillPattern == 0) {
            fillPatternPartial = FILL_PATTERN_PARTIAL_MARK;
            fillPatternPartial = fillPatternPartial.replaceAll("fill_mark", "slash");
            fillPatternPartial = fillPatternPartial.replaceAll("fill_stroke_width", Integer.toString(1));
            fillPatternPartial = fillPatternPartial.replaceAll("fill_size", Integer.toString(4));
        } else if(fillPattern == 1) {
            fillPatternPartial = FILL_PATTERN_PARTIAL_MARK;
            fillPatternPartial = fillPatternPartial.replaceAll("fill_mark", "slash");
            fillPatternPartial = fillPatternPartial.replaceAll("fill_stroke_width", Integer.toString(2));
            fillPatternPartial = fillPatternPartial.replaceAll("fill_size", Integer.toString(6));
        } else if(fillPattern == 2) {
            fillPatternPartial = FILL_PATTERN_PARTIAL_MARK;
            fillPatternPartial = fillPatternPartial.replaceAll("fill_mark", "horline");
            fillPatternPartial = fillPatternPartial.replaceAll("fill_stroke_width", Double.toString(0.4));
            fillPatternPartial = fillPatternPartial.replaceAll("fill_size", Double.toString(4.5));
        } else if(fillPattern == 3) {
            fillPatternPartial = FILL_PATTERN_PARTIAL_MARK;
            fillPatternPartial = fillPatternPartial.replaceAll("fill_mark", "horline");
            fillPatternPartial = fillPatternPartial.replaceAll("fill_stroke_width", Integer.toString(2));
            fillPatternPartial = fillPatternPartial.replaceAll("fill_size", Double.toString(5.5));
        } else {
            log.warn("Undefined fill pattern value");
            fillPatternPartial = "";
        }
        template = template.replaceAll(FILL_PATTERN_PARTIAL, fillPatternPartial);

        // border styles
        String borderStylePartial;
        if(borderDasharray.equals("")) {
            borderStylePartial = "";
        } else {
            borderStylePartial = BORDER_STYLE_PARTIAL_DASHARRAY;
        }
        template = template.replaceAll(BORDER_STYLE_PARTIAL, borderStylePartial);

        // colors
        if(isHighlight) {
            template = template.replaceAll(PARAM_FILL_COLOR, HIGHLIGHT_FILL_COLOR);
            template = template.replaceAll(PARAM_BORDER_COLOR, HIGHLIGHT_BORDER_COLOR);

            template = template.replaceAll(PARAM_STROKE_COLOR, HIGHLIGHT_STROKE_COLOR);

            template = template.replaceAll(PARAM_DOT_COLOR, HIGHLIGHT_DOT_COLOR);
        } else {
            template = template.replaceAll(PARAM_FILL_COLOR, fillColor);
            template = template.replaceAll(PARAM_BORDER_COLOR, borderColor);

            template = template.replaceAll(PARAM_STROKE_COLOR, strokeColor);

            template = template.replaceAll(PARAM_DOT_COLOR, dotColor);
        }

        template = template.replaceAll(PARAM_BORDER_LINEJOIN, borderLinejoin);
        template = template.replaceAll(PARAM_BORDER_WIDTH, Integer.toString(borderWidth));

        template = template.replaceAll(PARAM_STROKE_LINECAP, strokeLinecap);
        template = template.replaceAll(PARAM_STROKE_LINEJOIN, strokeLinejoin);
        template = template.replaceAll(PARAM_STROKE_WIDTH, Integer.toString(strokeWidth));

        template = template.replaceAll(PARAM_DOT_SHAPE, Integer.toString(dotShape));

        template = template.replaceAll(GEOMETRY, geometry);

        this.sld = template;
    }

    /**
     * Saves into redis
     *
     * @return <code>true</code> if saved a valid session; <code>false</code>
     *         otherwise.
     */
    public void save() {
        JedisManager.setex(KEY + client + "_" + layerId, 86400, getAsJSON());
    }

    /**
     * Transforms object to JSON String
     *
     * @return JSON String
     */
    @JsonIgnore
    public String getAsJSON() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonGenerationException e) {
            log.error(e, "JSON Generation failed");
        } catch (JsonMappingException e) {
            log.error(e, "Mapping from Object to JSON String failed");
        } catch (IOException e) {
            log.error(e, "IO failed");
        }
        return null;
    }

    /**
     * Creates store from cache
     *
     * @param client
     * @param layerId
     * @return object
     */
    @JsonIgnore
    public static WFSCustomStyleStore create(String client, String layerId)
            throws IOException {
        String json = getCache(client, layerId);
        if(json == null) {
            return null;
        }

        return setJSON(json);
    }

    /**
     * Transforms JSON String to object
     *
     * @param json
     * @return object
     */
    @JsonIgnore
    public static WFSCustomStyleStore setJSON(String json)
            throws IOException {
        return mapper.readValue(json,
                WFSCustomStyleStore.class);
    }

    /**
     * Gets saved session from redis
     *
     * @param client
     * @param layerId
     * @return style as JSON String
     */
    @JsonIgnore
    public static String getCache(String client, String layerId) {
        return JedisManager.get(KEY + client + "_" + layerId);
    }
}
