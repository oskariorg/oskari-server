package fi.nls.oskari.domain.map;

import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

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


        return json;
    }
    private String dashToUserDataStyle (String dashArray) {
        switch (dashArray) {
            case "solid":
                return "";
            case "dash":
                return "5 2";
            case "double":
                return "D";
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
            case "D":
                return "double"; // TODO: "solid" ??
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
}
