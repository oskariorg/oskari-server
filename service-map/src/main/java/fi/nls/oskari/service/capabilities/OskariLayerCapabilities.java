package fi.nls.oskari.service.capabilities;

import java.sql.Timestamp;

public class OskariLayerCapabilities {

    private Long id;
    private String url;
    private String layertype;
    private String version;
    private String data;
    private Timestamp created;
    private Timestamp updated;

    public OskariLayerCapabilities(String url, String layertype, String version, String data) {
        this.url = url;
        this.layertype = layertype;
        this.version = version;
        this.data = data;
    }

    protected OskariLayerCapabilities(Long id, String url, String layertype, String version, String data, Timestamp created, Timestamp updated) {
        this.id = id;
        this.url = url;
        this.layertype = layertype;
        this.version = version;
        this.data = data;
        this.created = created;
        this.updated = updated;
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public String getLayertype() {
        return layertype;
    }

    public String getVersion() {
        return version;
    }

    public String getData() {
        return data;
    }

    protected void setData(String data) {
        this.data = data;
    }

    public Timestamp getCreated() {
        return created;
    }

    protected void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    protected void setUpdated(Timestamp updated) {
        this.updated = updated;
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
