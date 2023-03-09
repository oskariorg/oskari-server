package fi.nls.oskari.domain.map.style;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonSetter;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VectorStyle implements Serializable {
    public static final String TYPE_OSKARI = "oskari";
    public static final String TYPE_MAPBOX = "mapbox";
    public static final String TYPE_3D = "cesium";

    private long id;
    private String layer;
    private String type;
    private long creator;
    private String name;
    private JSONObject style;
    private OffsetDateTime created;
    private OffsetDateTime updated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonIgnore
    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore
    public long getCreator() {
        return creator;
    }

    public void setCreator(long creator) {
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getStyle() {
        return style;
    }

    @JsonGetter("style")
    public Map getStyleMap() {
        return JSONHelper.getObjectAsMap(style);
    }

    public void setStyle(JSONObject style) {
        this.style = style;
    }

    @JsonSetter("style")
    public void setStyleMap(Map<String, Object> style) {
        this.style = new JSONObject(style);
    }

    @JsonIgnore
    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    @JsonIgnore
    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }
}
