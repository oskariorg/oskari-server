package fi.nls.oskari.service.capabilities;

import java.util.Date;

/**
 * Created by SMAKINEN on 24.8.2015.
 */
public class OskariLayerCapabilities {
    private long id;
    private String layertype;
    private String url;
    private String data;
    private Date created;
    private Date updated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLayertype() {
        return layertype;
    }

    public void setLayertype(String layertype) {
        this.layertype = layertype;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
