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

    public RawCapabilitiesResponse fetchCapabilities(String capabilitiesUrl, String user, String pass, String expectedContentType) throws IOException, ServiceException {
        HttpURLConnection conn = IOHelper.getConnection(capabilitiesUrl, user, pass);
        conn.setReadTimeout(TIMEOUT_MS);

        int sc = conn.getResponseCode();
        if (sc == HttpURLConnection.HTTP_FORBIDDEN || sc == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new ServiceUnauthorizedException("Wrong credentials for service");
        }
        if (sc != HttpURLConnection.HTTP_OK) {
            String msg = "Unexpected status code: " + sc  + " from: " + capabilitiesUrl;
            throw new ServiceException(msg, new IOException(msg));
        }

        String contentType = conn.getContentType();
        if (contentType != null && expectedContentType != null && contentType.toLowerCase().indexOf(expectedContentType) == -1) {
            throw new ServiceException("Unexpected Content-Type: " + contentType);
        }
        RawCapabilitiesResponse response = new RawCapabilitiesResponse(capabilitiesUrl);
        String encoding = IOHelper.getCharset(conn);
        response.setResponse(IOHelper.readBytes(conn), encoding);
        return response;
    }
}
