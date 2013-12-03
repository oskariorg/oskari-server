package fi.nls.oskari.domain.map.analysis;

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

    public void populateFromJSON(final JSONObject stylejs) throws JSONException {
        try {
            // {"area":{"fillColor":"FFDC00","lineColor":"CC9900","size":"2"},"line":{"color":"CC9900","size":"2"},"dot":{"color":"CC9900","size":"4"}}
            setBorder_color(stylejs.getJSONObject("area").optString("lineColor"));
            setBorder_width(stylejs.getJSONObject("area").optInt("size"));
            setDot_color(stylejs.getJSONObject("dot").optString("color"));
            setDot_size(stylejs.getJSONObject("dot").optInt("size"));
            setFill_color(stylejs.getJSONObject("area").optString("fillColor"));
            setStroke_color(stylejs.getJSONObject("line").optString("color"));
            setStroke_width(stylejs.getJSONObject("line").optInt("size"));
        } catch(Exception ex) {
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
   
}
