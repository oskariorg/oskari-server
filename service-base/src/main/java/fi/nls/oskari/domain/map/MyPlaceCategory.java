package fi.nls.oskari.domain.map;

public class MyPlaceCategory extends UserDataLayer {

    private long id;
    private String category_name;
    private boolean isDefault;

    private int stroke_width;
    private String stroke_color;
    private String stroke_linejoin;
    private String stroke_linecap;
    private String stroke_dasharray;

    private String fill_color;
    private int fill_pattern;

    private String dot_color;
    private int dot_size;
    private String dot_shape;

    private int border_width;
    private String border_color;
    private String border_linejoin;
    private String border_dasharray;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public int getStroke_width() {
        return stroke_width;
    }

    public void setStroke_width(int stroke_width) {
        this.stroke_width = stroke_width;
    }

    public String getStroke_color() {
        return stroke_color;
    }

    public void setStroke_color(String stroke_color) {
        this.stroke_color = stroke_color;
    }

    public String getStroke_linejoin() {
        return stroke_linejoin;
    }

    public void setStroke_linejoin(String stroke_linejoin) {
        this.stroke_linejoin = stroke_linejoin;
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

    public String getFill_color() {
        return fill_color;
    }

    public void setFill_color(String fill_color) {
        this.fill_color = fill_color;
    }

    public int getFill_pattern() {
        return fill_pattern;
    }

    public void setFill_pattern(int fill_pattern) {
        this.fill_pattern = fill_pattern;
    }

    public String getDot_color() {
        return dot_color;
    }

    public void setDot_color(String dot_color) {
        this.dot_color = dot_color;
    }

    public int getDot_size() {
        return dot_size;
    }

    public void setDot_size(int dot_size) {
        this.dot_size = dot_size;
    }

    public String getDot_shape() {
        return dot_shape;
    }

    public void setDot_shape(String dot_shape) {
        this.dot_shape = dot_shape;
    }

    public int getBorder_width() {
        return border_width;
    }

    public void setBorder_width(int border_width) {
        this.border_width = border_width;
    }

    public String getBorder_color() {
        return border_color;
    }

    public void setBorder_color(String border_color) {
        this.border_color = border_color;
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
