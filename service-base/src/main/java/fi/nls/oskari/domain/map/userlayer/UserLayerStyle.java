package fi.nls.oskari.domain.map.userlayer;

import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class UserLayerStyle {

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
