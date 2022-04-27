package org.oskari.capabilities;

import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceUnauthorizedException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

public abstract class CapabilitiesParser extends OskariComponent {

    // timeout capabilities request after 30 seconds (configurable)
    private static final int TIMEOUT_MS = PropertyUtil.getOptional("capabilities.timeout", 30) * 1000;

    public abstract Map<String, LayerCapabilities> getLayersFromService(ServiceConnectInfo src) throws IOException, ServiceException;

    /**
     * Provice a class to deserialize to from JSON. We could do:
     *
         @JsonTypeInfo(
         use = JsonTypeInfo.Id.NAME,
         property = "type")
         @JsonSubTypes({
         @JsonSubTypes.Type(value = LayerCapabilitiesWFS.class, name = "wfslayer"),
         @JsonSubTypes.Type(value = LayerCapabilitiesWMS.class, name = "wmslayer"),
         @JsonSubTypes.Type(value = LayerCapabilitiesWMTS.class, name = "wmtslayer")
         })

     * on LayerCapabilities but we would need to hardcode the subtypes there. This is more cumbersome but extendable way.
     * @return
     */
    public Class<? extends LayerCapabilities> getCapabilitiesClass() {
        return LayerCapabilities.class;
    }
    /*
     For optimization purposes to get single layer (this method can be overridden to optimize single layer, the base method is not optimized).
     For example wfs-layers require multiple requests/layer and this can be used to update single layer.
     Can be used to speed up update when service has multiple layers.
     */
    public LayerCapabilities getLayerFromService(ServiceConnectInfo src, String layer) throws IOException, ServiceException {
        if (layer == null || layer.isEmpty()) {
            throw new ServiceException("No layer specified");
        }
        Map<String, LayerCapabilities> layers = getLayersFromService(src);
        return layers.get(layer);
    }

    public RawCapabilitiesResponse fetchCapabilities(String capabilitiesUrl, String user, String pass, String expectedContentType) throws IOException, ServiceException {
        HttpURLConnection conn = IOHelper.getConnection(capabilitiesUrl, user, pass);
        IOHelper.addIdentifierHeaders(conn);
        conn = IOHelper.followRedirect(conn, user, pass, 5);
        conn.setReadTimeout(TIMEOUT_MS);

        int sc = conn.getResponseCode();
        if (sc == HttpURLConnection.HTTP_FORBIDDEN || sc == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new ServiceUnauthorizedException("Wrong credentials for service on " + capabilitiesUrl);
        }
        if (sc != HttpURLConnection.HTTP_OK) {
            String msg = "Unexpected status code: " + sc  + " from: " + capabilitiesUrl;
            throw new ServiceException(msg, new IOException(msg));
        }

        String contentType = conn.getContentType();
        if (contentType != null && expectedContentType != null && contentType.toLowerCase().indexOf(expectedContentType) == -1) {
            throw new ServiceException("Unexpected Content-Type: " + contentType + " from: " + capabilitiesUrl);
        }
        RawCapabilitiesResponse response = new RawCapabilitiesResponse(conn.getURL().toString());
        response.setContentType(contentType);
        String encoding = IOHelper.getCharset(conn);
        response.setResponse(IOHelper.readBytes(conn), encoding);
        return response;
    }
}
