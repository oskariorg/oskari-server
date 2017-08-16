package fi.nls.oskari.service.capabilities;

import java.util.Date;

public class OskariLayerCapabilities {

    private final long id;
    private final String url;
    private final String layertype;
    private final String version;
    private final String data;
    private final Date created;
    private final Date updated;

    public OskariLayerCapabilities(String url, String layertype, String version, String data)
            throws IllegalArgumentException {
        this(-1L, url, layertype, version, data, null, null);
    }

    /**
     * @throws IllegalArgumentException if url, layertype, data is null or empty
     */
    public OskariLayerCapabilities(long id, String url, String layertype, String version, String data, Date created, Date updated)
            throws IllegalArgumentException {
        if (layertype == null || layertype.isEmpty()) {
            throw new IllegalArgumentException("layertype is null or empty!");
        }
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("url is null or empty!");
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("data is null or empty!");
        }

        this.id = id;
        this.url = url.toLowerCase();
        this.layertype = layertype.toLowerCase();
        this.version = version;
        this.data = data;
        this.created = created;
        this.updated = updated;
    }

    public long getId() {
        return id;
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

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }

    public boolean hasData() {
        return data == null || data.trim().isEmpty();
    }

    public OskariLayerCapabilities setId(long newId) {
        return new OskariLayerCapabilities(newId, url, layertype, version, data, created, updated);
    }

    public static class Builder {

        private String url;
        private String layertype;
        private String version;
        private String data;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder layertype(String layertype) {
            this.layertype = layertype;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public OskariLayerCapabilities build() throws IllegalArgumentException {
            return new OskariLayerCapabilities(url, layertype, version, data);
        }

    }

}
