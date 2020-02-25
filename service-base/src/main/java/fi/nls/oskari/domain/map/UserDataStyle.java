package fi.nls.oskari.domain.map;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.util.JSONHelper;

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

    public void populateFromOskariJSON(JSONObject json) throws JSONException {
        try {
            // image
            JSONObject image = json.getJSONObject("image");
            setDot_color(image.getJSONObject("fill").optString("color"));
            setDot_shape(image.optString("shape"));
            setDot_size(image.optInt("size"));

            // stroke
            JSONObject stroke = json.getJSONObject("stroke");
            setStroke_color(stroke.optString("color"));
            setStroke_width(stroke.optInt("width"));
            setStroke_dasharray(dashToUserDataStyle(stroke.getString("lineDash")));
            setStroke_linecap(stroke.getString("lineCap"));
            setStroke_linejoin(stroke.getString("lineJoin"));

            // stroke.area
            JSONObject strokeArea = stroke.getJSONObject("area");
            setBorder_color(JSONHelper.optString(strokeArea, "color", null));
            setBorder_width(strokeArea.optInt("width"));
            setBorder_linejoin(strokeArea.optString("lineJoin"));
            setBorder_dasharray(dashToUserDataStyle(json.getJSONObject("stroke").getJSONObject("area").optString("lineDash")));

            // fill
            JSONObject fill = json.getJSONObject("fill");
            setFill_color(JSONHelper.optString(fill, "color", null));
            setFill_pattern(fill.getJSONObject("area").optInt("pattern", -1));
            // text
            JSONObject text = json.getJSONObject("text");
            if (text != null) {
                JSONObject textFill = text.optJSONObject("fill");
                if (textFill != null) {
                    setText_fill_color(textFill.optString("color", null));
                }
                JSONObject textStroke = text.optJSONObject("stroke");
                if (textStroke != null) {
                    setText_stroke_color(textStroke.optString("color", null));
                    setText_stroke_width(textStroke.optInt("width"));
                }
                setFont(text.optString("font"));
                setText_align(text.optString("textAlign"));
                setText_offset_x(text.optInt("offsetX"));
                setText_offset_y(text.optInt("offsetY"));
                setText_label(text.optString("labelText"));
                setText_label_property(text.optString("labelProperty"));
            }
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public JSONObject parseUserLayerStyleToOskariJSON() {
        JSONObject json = new JSONObject();

        // image
        JSONObject image = new JSONObject();
        JSONHelper.putValue(json, "image", image);
        JSONObject imageFill = new JSONObject();
        JSONHelper.putValue(imageFill, "color", getDot_color());
        JSONHelper.putValue(image, "fill", imageFill);
        JSONHelper.putValue(image, "shape", getDot_shape());
        JSONHelper.putValue(image, "size", getDot_size());

        // stroke
        JSONObject stroke = new JSONObject();
        JSONHelper.putValue(json, "stroke", stroke);
        JSONHelper.putValue(stroke, "color", getStroke_color());
        JSONHelper.putValue(stroke, "width", getStroke_width());
        JSONHelper.putValue(stroke, "lineDash", dashToOskariJSON(getStroke_dasharray()));
        JSONHelper.putValue(stroke, "lineCap", getStroke_linecap());
        JSONHelper.putValue(stroke, "lineJoin", getStroke_linejoin());

        // stroke.area
        JSONObject strokeArea = new JSONObject();
        JSONHelper.putValue(stroke, "area", strokeArea);
        JSONHelper.putValue(strokeArea, "color", getBorder_color());
        JSONHelper.putValue(strokeArea, "width", getBorder_width());
        JSONHelper.putValue(strokeArea, "lineDash", dashToOskariJSON(getBorder_dasharray()));
        JSONHelper.putValue(strokeArea, "lineJoin", getBorder_linejoin());

        // fill
        JSONObject fill = new JSONObject();
        JSONHelper.putValue(json, "fill", fill);
        JSONHelper.putValue(fill, "color", getFill_color());
        JSONObject fillArea = new JSONObject();
        JSONHelper.putValue(fillArea, "pattern", getFill_pattern());
        JSONHelper.putValue(fill, "area", fillArea);

        // text
        if (text_label != null || text_label_property != null) {
            JSONObject text = new JSONObject();
            JSONHelper.putValue(json, "text", text);

            JSONObject textFill = new JSONObject();
            JSONHelper.putValue(textFill, "color", text_fill_color);
            JSONHelper.putValue(text, "fill", textFill);

            JSONObject textStroke = new JSONObject();
            JSONHelper.putValue(textStroke, "color", text_stroke_color);
            JSONHelper.putValue(textStroke, "width", text_stroke_width);
            JSONHelper.putValue(text, "stroke", textStroke);

            JSONHelper.putValue(text, "font", font);
            JSONHelper.putValue(text, "textAlign", text_align);
            JSONHelper.putValue(text, "offsetX", text_offset_x);
            JSONHelper.putValue(text, "offsetY", text_offset_y);

            JSONHelper.putValue(text, "labelText", text_label);
            JSONHelper.putValue(text, "labelProperty", text_label_property == null ? null : text_label_property[0]);
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
    /**
     * Generated
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UserDataStyle)) {
            return false;
        }
        UserDataStyle other = (UserDataStyle) obj;
        if (border_color == null) {
            if (other.border_color != null) {
                return false;
            }
        } else if (!border_color.equals(other.border_color)) {
            return false;
        }
        if (border_dasharray == null) {
            if (other.border_dasharray != null) {
                return false;
            }
        } else if (!border_dasharray.equals(other.border_dasharray)) {
            return false;
        }
        if (border_linejoin == null) {
            if (other.border_linejoin != null) {
                return false;
            }
        } else if (!border_linejoin.equals(other.border_linejoin)) {
            return false;
        }
        if (border_width != other.border_width) {
            return false;
        }
        if (dot_color == null) {
            if (other.dot_color != null) {
                return false;
            }
        } else if (!dot_color.equals(other.dot_color)) {
            return false;
        }
        if (dot_shape == null) {
            if (other.dot_shape != null) {
                return false;
            }
        } else if (!dot_shape.equals(other.dot_shape)) {
            return false;
        }
        if (dot_size != other.dot_size) {
            return false;
        }
        if (fill_color == null) {
            if (other.fill_color != null) {
                return false;
            }
        } else if (!fill_color.equals(other.fill_color)) {
            return false;
        }
        if (fill_pattern != other.fill_pattern) {
            return false;
        }
        if (font == null) {
            if (other.font != null) {
                return false;
            }
        } else if (!font.equals(other.font)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (stroke_color == null) {
            if (other.stroke_color != null) {
                return false;
            }
        } else if (!stroke_color.equals(other.stroke_color)) {
            return false;
        }
        if (stroke_dasharray == null) {
            if (other.stroke_dasharray != null) {
                return false;
            }
        } else if (!stroke_dasharray.equals(other.stroke_dasharray)) {
            return false;
        }
        if (stroke_linecap == null) {
            if (other.stroke_linecap != null) {
                return false;
            }
        } else if (!stroke_linecap.equals(other.stroke_linecap)) {
            return false;
        }
        if (stroke_linejoin == null) {
            if (other.stroke_linejoin != null) {
                return false;
            }
        } else if (!stroke_linejoin.equals(other.stroke_linejoin)) {
            return false;
        }
        if (stroke_width != other.stroke_width) {
            return false;
        }
        if (text_align == null) {
            if (other.text_align != null) {
                return false;
            }
        } else if (!text_align.equals(other.text_align)) {
            return false;
        }
        if (text_fill_color == null) {
            if (other.text_fill_color != null) {
                return false;
            }
        } else if (!text_fill_color.equals(other.text_fill_color)) {
            return false;
        }
        if (text_label == null) {
            if (other.text_label != null) {
                return false;
            }
        } else if (!text_label.equals(other.text_label)) {
            return false;
        }
        if (!Arrays.equals(text_label_property, other.text_label_property)) {
            return false;
        }
        if (text_offset_x == null) {
            if (other.text_offset_x != null) {
                return false;
            }
        } else if (!text_offset_x.equals(other.text_offset_x)) {
            return false;
        }
        if (text_offset_y == null) {
            if (other.text_offset_y != null) {
                return false;
            }
        } else if (!text_offset_y.equals(other.text_offset_y)) {
            return false;
        }
        if (text_stroke_color == null) {
            if (other.text_stroke_color != null) {
                return false;
            }
        } else if (!text_stroke_color.equals(other.text_stroke_color)) {
            return false;
        }
        if (text_stroke_width == null) {
            if (other.text_stroke_width != null) {
                return false;
            }
        } else if (!text_stroke_width.equals(other.text_stroke_width)) {
            return false;
        }
        return true;
    }
}
