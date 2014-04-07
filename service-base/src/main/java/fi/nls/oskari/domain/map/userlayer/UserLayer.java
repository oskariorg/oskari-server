package fi.nls.oskari.domain.map.userlayer;


import java.lang.reflect.Method;
import java.util.List;


public class UserLayer {

    private long id;
    private String uuid;
    private String layer_name;
    private String layer_desc;
    private String layer_source;
    private String fields;
    private String publisher_name;
    private long style_id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLayer_name() {
        return layer_name;
    }

    public void setLayer_name(String layer_name) {
        this.layer_name = layer_name;
    }

    public String getLayer_desc() {
        return layer_desc;
    }

    public void setLayer_desc(String layer_desc) {
        this.layer_desc = layer_desc;
    }

    public String getLayer_source() {
        return layer_source;
    }

    public void setLayer_source(String layer_source) {
        this.layer_source = layer_source;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getPublisher_name() {
        return publisher_name;
    }

    public void setPublisher_name(String publisher_name) {
        this.publisher_name = publisher_name;
    }

    public long getStyle_id() {
        return style_id;
    }

    public void setStyle_id(long style_id) {
        this.style_id = style_id;
    }

    public boolean isOwnedBy(final String uuid) {
        if(uuid == null || getUuid() == null) {
            return false;
        }
        return getUuid().equals(uuid);
    }

}
