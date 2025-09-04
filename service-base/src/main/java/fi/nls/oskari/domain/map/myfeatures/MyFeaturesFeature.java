package fi.nls.oskari.domain.map.myfeatures;

import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;

import java.time.OffsetDateTime;

public class MyFeaturesFeature {

    private long id;
    private OffsetDateTime created;
    private OffsetDateTime updated;
    private String fid;
    private Geometry geometry;
    private JSONObject properties;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public JSONObject getProperties() {
        return properties;
    }

    public void setProperties(JSONObject properties) {
        this.properties = properties;
    }

}
