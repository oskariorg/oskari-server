package fi.nls.oskari.domain.map;

import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.util.Arrays;
import java.util.stream.Collectors;

public class UserDataStyle {

    private long id;
    private long stroke_width;
    private String stroke_color;
    private String fill_color;
    private String dot_color;
    private long dot_size;
    private long border_width;
    private String border_color;
    private String dot_shape;
    private String stroke_linejoin;
    private int fill_pattern;
    private String stroke_linecap;
    private String stroke_dasharray;
    private String border_linejoin;
    private String border_dasharray;

    private String text_fill_color;
    private String text_stroke_color;
    private Integer text_stroke_width;
    private String[] text_label_property;
    private String text_label;
    private String text_align;
    private Integer text_offset_x;
    private Integer text_offset_y;
    private String font;


    private static final String KEY_DEFAULT_STYLE = "default";
    private static final String KEY_FEATURE_STYLE = "featureStyle";
    private static final String KEY_RENDER_MODE = "renderMode";
    private static final String KEY_LABEL_PROPERTY = "labelProperty";
    private static final String KEY_COLOR = "color";
    private static final String KEY_TEXT_ALIGN = "textAlign";
    private static final String KEY_OFFSET_X = "10";
    private static final String KEY_FILL = "fill";
    private static final String KEY_STROKE = "stroke";
    private static final String KEY_TEXT = "text";
    private static final String KEY_FONT = "font";

    public void initDefaultStyle () {
        //point
        setDot_color("#000000");
        setDot_size(3);
        setDot_shape("1");
        //line
        setStroke_width(1);
        setStroke_dasharray("");
        setStroke_linecap("butt");
        setStroke_linejoin("mitre");
        setStroke_color("#3233ff");
        //area
        setFill_pattern(-1);
        setBorder_dasharray("");
        setBorder_linejoin("mitre");
        setBorder_width(1);
        setBorder_color("#000000");
        setFill_color("#ffde00");
        setFill_pattern(-1);
    }

    public void populateDefaultTextStyle() {
        setFont("bold 14px sans-serif");
        setText_fill_color("#000000");
        setText_stroke_color("#FFFFFF");
        setText_stroke_width(2);
        setText_align("left");
        setText_offset_x(10);
    }

    public JSONObject getStyleForLayerOptions() {
        JSONObject featStyle = JSONHelper.createJSONObject("featureStyle", parseUserLayerStyleToOskariJSON());
        return JSONHelper.createJSONObject("default", featStyle);
    }
    // This becomes redundant when oskari style json is used only
    public void populateFromJSON(final JSONObject stylejs) throws JSONException {
        try {
            // {"area":{"fillColor":"FFDC00","lineColor":"CC9900","size":"2"},"line":{"color":"CC9900","size":"2"},"dot":{"color":"CC9900","size":"4"}}
            JSONObject areaSubObject = stylejs.getJSONObject("area");

            setBorder_color(areaSubObject.isNull("lineColor") ? null : areaSubObject.optString("lineColor")); // null is valid color value for "no stroke"
            setBorder_width(areaSubObject.optInt("lineWidth"));
            setDot_color(stylejs.getJSONObject("dot").optString("color"));
            setDot_size(stylejs.getJSONObject("dot").optInt("size"));
            setFill_color(areaSubObject.isNull("fillColor") ? null : areaSubObject.optString("fillColor")); // null is valid color value for "no fill"
            setStroke_color(stylejs.getJSONObject("line").optString("color"));
            setStroke_width(stylejs.getJSONObject("line").optInt("width"));
            setDot_shape(stylejs.getJSONObject("dot").optString("shape"));
            setStroke_linejoin(stylejs.getJSONObject("line").optString("corner"));
            setFill_pattern(ConversionHelper.getInt(areaSubObject.optString("fillStyle"), -1));
            setStroke_linecap(stylejs.getJSONObject("line").optString("cap"));
            setStroke_dasharray(stylejs.getJSONObject("line").optString("style"));
            setBorder_linejoin(areaSubObject.optString("lineCorner"));
            setBorder_dasharray(areaSubObject.optString("lineStyle"));
        } catch (Exception ex) {
            throw new JSONException(ex);
        }

    }

    public void populateFromOskariJSON(final JSONObject style) throws JSONException {
        try {
            setDot_color(style.getJSONObject("image").getJSONObject("fill").optString("color"));
            setDot_shape(style.getJSONObject("image").optString("shape"));
            setDot_size(style.getJSONObject("image").optInt("size"));

            setStroke_color(style.getJSONObject("stroke").optString("color"));
            setStroke_width(style.getJSONObject("stroke").optInt("width"));
            setStroke_linejoin(style.getJSONObject("stroke").getString("lineJoin"));
            setStroke_linecap(style.getJSONObject("stroke").getString("lineCap"));
            setStroke_dasharray(dashToUserDataStyle(style.getJSONObject("stroke").getString("lineDash")));

            // null is valid color value for "no fill"
            setFill_color(style.getJSONObject("fill").isNull("color") ? null : style.getJSONObject("fill")
                    .optString("color"));
            // null is valid color value for "no stroke"
            setBorder_color(style.getJSONObject("stroke").getJSONObject("area").isNull("color") ? null : style
                    .getJSONObject("stroke").getJSONObject("area").optString("color"));
            setFill_pattern(ConversionHelper.getInt(style.getJSONObject("fill").getJSONObject("area").
                    optString("pattern"), -1));
            setBorder_width(style.getJSONObject("stroke").getJSONObject("area").optInt("width"));
            setBorder_linejoin(style.getJSONObject("stroke").getJSONObject("area").optString("lineJoin"));
            setBorder_dasharray(dashToUserDataStyle(style.getJSONObject("stroke").getJSONObject("area").optString("lineDash")));

        } catch (Exception e) {
            throw new JSONException(e);
        }
    }
    // This becomes redundant when oskari style json is used only
    public JSONObject parseUserLayerStyle2JSON(){
        JSONObject json = new JSONObject();
        //dot
        JSONObject dot = new JSONObject();
        JSONHelper.putValue(dot, "shape", getDot_shape());
        JSONHelper.putValue(dot, "color", getDot_color());
        JSONHelper.putValue(dot, "size", getDot_size());
        JSONHelper.putValue(json, "dot", dot);
        //line
        JSONObject line = new JSONObject();
        JSONHelper.putValue(line, "style", getStroke_dasharray());
        JSONHelper.putValue(line, "cap", getStroke_linecap());
        JSONHelper.putValue(line, "corner", getStroke_linejoin());
        JSONHelper.putValue(line, "width", getStroke_width());
        JSONHelper.putValue(line, "color", getStroke_color());
        JSONHelper.putValue(json, "line", line);
        //area
        JSONObject area = new JSONObject();
        JSONHelper.putValue(area, "lineStyle", getBorder_dasharray());
        JSONHelper.putValue(area, "lineCorner", getBorder_linejoin());
        JSONHelper.putValue(area, "lineWidth", getBorder_width());
        JSONHelper.putValue(area, "lineColor", getBorder_color());
        JSONHelper.putValue(area, "fillStyle", getFill_pattern());
        JSONHelper.putValue(area, "fillColor", getFill_color());
        JSONHelper.putValue(json, "area", area);

        return json;
    }

    public JSONObject parseUserLayerStyleToOskariJSON() {
        JSONObject json = new JSONObject();
        // dot
        JSONObject image = new JSONObject();
        JSONObject imageFill = new JSONObject();
        JSONHelper.putValue(imageFill, "color", getDot_color());
        JSONHelper.putValue(image, "fill", imageFill);
        JSONHelper.putValue(image, "shape", getDot_shape());
        JSONHelper.putValue(image, "size", getDot_size());
        JSONHelper.putValue(json, "image", image);
        // line
        JSONObject stroke = new JSONObject();
        JSONHelper.putValue(stroke, "color", getStroke_color());
        JSONHelper.putValue(stroke, "width", getStroke_width());
        JSONHelper.putValue(stroke, "lineDash", dashToOskariJSON(getStroke_dasharray()));
        JSONHelper.putValue(stroke, "lineCap", getStroke_linecap());
        JSONHelper.putValue(stroke, "lineJoin", getStroke_linejoin());
        // area
        JSONObject strokeArea = new JSONObject();
        JSONHelper.putValue(strokeArea, "color", getBorder_color());
        JSONHelper.putValue(strokeArea, "width", getBorder_width());
        JSONHelper.putValue(strokeArea, "lineDash", dashToOskariJSON(getBorder_dasharray()));
        JSONHelper.putValue(strokeArea, "lineJoin", getBorder_linejoin());
        JSONHelper.putValue(stroke, "area", strokeArea);
        JSONHelper.putValue(json, "stroke", stroke);
        JSONObject fill = new JSONObject();
        JSONHelper.putValue(fill, "color", getFill_color());
        JSONObject fillArea = new JSONObject();
        JSONHelper.putValue(fillArea, "pattern", getFill_pattern());
        JSONHelper.putValue(fill, "area", fillArea);
        JSONHelper.putValue(json, "fill", fill);
        // text
        if (text_label != null || text_label_property != null) {
            JSONObject text = JSONHelper.createJSONObject( "font", font);
            JSONHelper.putValue(text, "textAlign", text_align);
            JSONHelper.putValue(text, "offsetX", text_offset_x);
            JSONObject textStroke = JSONHelper.createJSONObject( "color", text_stroke_color);
            JSONHelper.putValue(stroke, "width", text_stroke_width);
            JSONHelper.putValue(text, "stroke", textStroke);
            JSONHelper.putValue(text, "fill", JSONHelper.createJSONObject("color", text_fill_color));
            if (text_label != null) {
                JSONHelper.putValue(text, "label", text_label);
            } else if (text_label_property != null) {
                JSONArray properties = new JSONArray();
                properties.addAll(Arrays.asList(text_label_property));
                JSONHelper.putValue(text, "labelProperty", properties);
            }
            JSONHelper.putValue(json, "text", text);
        }

        return json;
    }
    private String dashToUserDataStyle (String dashArray) {
        switch (dashArray) {
            case "solid":
                return "";
            case "dash":
                return "5 2";
        }
        return "";
    }
    private String dashToOskariJSON (String dashArray) {
        if (dashArray == null) {
            return "";
        }
        switch (dashArray) {
            case "":
                return "solid";
            case "5 2":
                return "dash";
        }
        return "solid";
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getStroke_width() {
        return stroke_width;
    }
    public void setStroke_width(long strokeWidth) {
        stroke_width = strokeWidth;
    }
    public String getStroke_color() {
        return stroke_color;
    }
    public void setStroke_color(String strokeColor) {
        stroke_color = strokeColor;
    }
    public String getFill_color() {
        return fill_color;
    }
    public void setFill_color(String fillColor) {
        fill_color = fillColor;
    }
    public String getDot_color() {
        return dot_color;
    }
    public void setDot_color(String dotColor) {
        dot_color = dotColor;
    }
    public long getDot_size() {
        return dot_size;
    }
    public void setDot_size(long dotSize) {
        dot_size = dotSize;
    }
    public long getBorder_width() {
        return border_width;
    }
    public void setBorder_width(long borderWidth) {
        border_width = borderWidth;
    }
    public String getBorder_color() {
        return border_color;
    }
    public void setBorder_color(String borderColor) {
        border_color = borderColor;
    }

    public String getDot_shape() {
        return dot_shape;
    }

    public void setDot_shape(String dot_shape) {
        this.dot_shape = dot_shape;
    }

    public String getStroke_linejoin() {
        return stroke_linejoin;
    }

    public void setStroke_linejoin(String stroke_linejoin) {
        this.stroke_linejoin = stroke_linejoin;
    }

    public int getFill_pattern() {
        return fill_pattern;
    }

    public void setFill_pattern(int fill_pattern) {
        this.fill_pattern = fill_pattern;
    }

    public String getStroke_linecap() {
        return stroke_linecap;
    }

    public void setStroke_linecap(String stroke_linecap) {
        this.stroke_linecap = stroke_linecap;
    }

    public String getStroke_dasharray() {
        return stroke_dasharray;
    }

    public void setStroke_dasharray(String stroke_dasharray) {
        this.stroke_dasharray = stroke_dasharray;
    }

    public String getBorder_linejoin() {
        return border_linejoin;
    }

    public void setBorder_linejoin(String border_linejoin) {
        this.border_linejoin = border_linejoin;
    }

    public String getBorder_dasharray() {
        return border_dasharray;
    }

    public void setBorder_dasharray(String border_dasharray) {
        this.border_dasharray = border_dasharray;
    }

    public String getText_stroke_color() {
        return text_stroke_color;
    }

    public void setText_stroke_color(String text_stroke_color) {
        this.text_stroke_color = text_stroke_color;
    }

    public String getText_fill_color() {
        return text_fill_color;
    }

    public void setText_fill_color(String text_fill_color) {
        this.text_fill_color = text_fill_color;
    }

    public String[] getText_label_property() {
        return text_label_property;
    }

    public void setText_label_property(String text_label_property) {
        this.text_label_property = new String[]{text_label_property};
        if (text_label_property != null && this.font == null) {
            this.populateDefaultTextStyle();
        }
    }

    public void setText_label_property(String[] text_label_property) {
        this.text_label_property = text_label_property;
        if (text_label_property != null && this.font == null) {
            this.populateDefaultTextStyle();
        }
    }

    public String getText_label() {
        return text_label;
    }

    public void setText_label(String text_label) {
        this.text_label = text_label;
        if (text_label != null && this.font == null) {
            this.populateDefaultTextStyle();
        }
    }

    public String getText_align() {
        return text_align;
    }

    public void setText_align(String text_align) {
        this.text_align = text_align;
    }

    public Integer getText_offset_x() {
        return text_offset_x;
    }

    public void setText_offset_x(Integer text_offset_x) {
        this.text_offset_x = text_offset_x;
    }

    public Integer getText_offset_y() {
        return text_offset_y;
    }

    public void setText_offset_y(Integer text_offset_y) {
        this.text_offset_y = text_offset_y;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public Integer getText_stroke_width() {
        return text_stroke_width;
    }

    public void setText_stroke_width(Integer text_stroke_width) {
        this.text_stroke_width = text_stroke_width;
    }
}
