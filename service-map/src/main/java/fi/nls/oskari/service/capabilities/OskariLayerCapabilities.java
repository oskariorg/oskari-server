package fi.nls.oskari.service.capabilities;

import java.sql.Timestamp;

/**
 * Immutable representation of a single row in oskari_capabilities_cache table
 */
public final class OskariLayerCapabilities extends OskariLayerCapabilitiesDraft {

    private final long id;
    private final Timestamp created;
    private final Timestamp updated;

    protected OskariLayerCapabilities(long id, String url, String layertype, String version, String data, Timestamp created, Timestamp updated) {
        super(url, layertype, version, data);
        this.id = id;
        this.created = created;
        this.updated = updated;
    }

    public long getId() {
        return id;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("id=").append(id);
        sb.append(",url=").append(url);
        sb.append(",layertype=").append(layertype);
        sb.append(",version=").append(id);
        sb.append(",data=").append(data.length() > 30 ? data.substring(0,  27) + "..." : data);
        sb.append(",created=").append(created);
        sb.append(",updated=").append(updated);
        sb.append('}');
        return sb.toString();
    }

}
