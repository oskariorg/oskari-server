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
        this.url = trim(url, "url");
        this.layertype = trim(layertype, "layertype");
        this.data = trim(data, "data");
        this.version = version;
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

    /**
     * Returns a trimmed copy of String
     * @param str value to be trimmed
     * @param fieldName for exception message
     * @return trimmed value
     * @throws IllegalArgumentException if str is null or empty after trimming
     */
    public static String trim(final String str, final String fieldName)
            throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException(fieldName + " is null");
        }
        final String trimmed = str.trim();
        if (trimmed.isEmpty()) {
           throw new IllegalArgumentException(fieldName + " is empty!");
        }
        return trimmed;
    }

}
