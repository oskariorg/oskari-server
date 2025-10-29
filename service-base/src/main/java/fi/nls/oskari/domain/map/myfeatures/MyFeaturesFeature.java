package fi.nls.oskari.domain.map.myfeatures;

import java.time.Instant;

import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;

public class MyFeaturesFeature {

    private long id;
    private Instant created;
    private Instant updated;
    private String fid;
    private Geometry geometry;
    private JSONObject properties;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
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
