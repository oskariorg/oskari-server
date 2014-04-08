package fi.nls.oskari.domain.map;

public class MyPlaceCategory {
    
    private long id; 
    private String category_name;
    private long stroke_width;
    private String stroke_color;
    private String fill_color;
    private String uuid;
    private String dot_color;
    private long dot_size;
    private long border_width;
    private String border_color;
    private String publisher_name;

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
    
    
    public long getStroke_width() {
        return stroke_width;
    }
    public void setStroke_width(long stroke_width) {
        this.stroke_width = stroke_width;
    }
    public String getStroke_color() {
        return stroke_color;
    }
    public void setStroke_color(String stroke_color) {
        this.stroke_color = stroke_color;
    }
   
    public String getFill_color() {
        return fill_color;
    }
    public void setFill_color(String fill_color) {
        this.fill_color = fill_color;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getDot_color() {
        return dot_color;
    }
    public void setDot_color(String dot_color) {
        this.dot_color = dot_color;
    }
    public long getDot_size() {
        return dot_size;
    }
    public void setDot_size(long dot_size) {
        this.dot_size = dot_size;
    }
   
    public long getBorder_width() {
        return border_width;
    }
    public void setBorder_width(long border_width) {
        this.border_width = border_width;
    }
    public String getBorder_color() {
        return border_color;
    }
    public void setBorder_color(String border_color) {
        this.border_color = border_color;
    }
    
    public String getPublisher_name() {
        return publisher_name;
    }
    public void setPublisher_name(String publisher_name) {
        this.publisher_name = publisher_name;
    }

    public boolean isPublished() {
        return getPublisher_name() != null;
    }

    public boolean isOwnedBy(final String uuid) {
        if(uuid == null || getUuid() == null) {
            return false;
        }
        return getUuid().equals(uuid);
    }
    
}
