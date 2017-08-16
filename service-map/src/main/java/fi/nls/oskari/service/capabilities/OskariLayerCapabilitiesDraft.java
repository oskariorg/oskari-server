package fi.nls.oskari.service.capabilities;

/**
 * Not-yet-persisted draft of OskariLayerCapabilities
 * @see fi.nls.oskari.service.capabilities.OskariLayerCapabilities
 */
public class OskariLayerCapabilitiesDraft {

    protected final String url;
    protected final String layertype;
    protected final String version;
    protected final String data;

    public OskariLayerCapabilitiesDraft(String url, String layertype, String version, String data)
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

        this.url = url;
        this.layertype = layertype.toLowerCase();
        this.version = version;
        this.data = data;
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

    public boolean hasData() {
        return data == null || data.trim().isEmpty();
    }

}
