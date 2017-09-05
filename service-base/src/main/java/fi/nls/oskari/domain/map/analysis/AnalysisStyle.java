package fi.nls.oskari.domain.map.analysis;

import fi.nls.oskari.util.ConversionHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class AnalysisStyle {

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
            setBorder_width(areaSubObject.optInt("size"));
            setDot_color(stylejs.getJSONObject("dot").optString("color"));
            setDot_size(stylejs.getJSONObject("dot").optInt("size"));
            setFill_color(areaSubObject.isNull("fillColor") ? null : areaSubObject.optString("fillColor")); // null is valid color value for "no fill"
            setStroke_color(stylejs.getJSONObject("line").optString("color"));
            setStroke_width(stylejs.getJSONObject("line").optInt("size"));
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
