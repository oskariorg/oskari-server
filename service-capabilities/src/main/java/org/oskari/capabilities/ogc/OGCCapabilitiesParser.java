package org.oskari.capabilities.ogc;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import org.oskari.capabilities.CapabilitiesParser;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.RawCapabilitiesResponse;
import org.oskari.capabilities.ServiceConnectInfo;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class OGCCapabilitiesParser extends CapabilitiesParser {

    public String getType() {
        return this.getName().replaceAll("layer", "").toUpperCase();
    }

    protected String getExpectedContentType() {
        return "xml";
    }

    public String validateResponse(RawCapabilitiesResponse response, String version) throws ServiceException {
        return CapabilitiesValidator.validateXmlResponse(response, this, version);
    }

    public Map<String, LayerCapabilities> getLayersFromService(ServiceConnectInfo src) throws IOException, ServiceException {

        String capabilitiesUrl = contructCapabilitiesUrl(src.getUrl(), src.getVersion());
        RawCapabilitiesResponse response = fetchCapabilities(capabilitiesUrl, src.getUser(), src.getPass(), getExpectedContentType());
        String validResponse = validateResponse(response, src.getVersion());
        return parseLayers(validResponse);
    }

    protected abstract Map<String, LayerCapabilities> parseLayers(String capabilities) throws ServiceException;

    protected String contructCapabilitiesUrl(String url, String version) {
        String urlLC = url.toLowerCase();

        final Map<String, String> params = new HashMap<>();
        // check existing params
        if (!urlLC.contains("service=")) {
            params.put("service", getType());
        }
        if (!urlLC.contains("request=")) {
            params.put("request", "GetCapabilities");
        }
        if (!urlLC.contains("version=") && version != null && !version.isEmpty()) {
            params.put(getVersionParamName(), version);
        }

        return IOHelper.constructUrl(url, params);
    }

    protected String getVersionParamName() {
        return "version";
    }

    public void validateCapabilities(XMLStreamReader xsr, String version)
        throws ServiceException, XMLStreamException {
        advanceToRootElement(xsr);
        String ns = xsr.getNamespaceURI();
        String name = xsr.getLocalName();
        validateCapabilities(version, ns, name);
    }

    protected abstract void validateCapabilities(String version, String ns, String name) throws ServiceException;

    private boolean advanceToRootElement(XMLStreamReader xsr)
            throws XMLStreamException {
        while (xsr.hasNext()) {
            if (xsr.next() == XMLStreamConstants.START_ELEMENT) {
                return true;
            }
        }
        return false;
    }

    protected static void checkNamespaceStartsWith(final String ns, final String expected)
            throws ServiceException {
        if (ns == null) {
            throw new ServiceException("Expected non-null namespace!");
        }
        if (!ns.startsWith(expected)) {
            throw new ServiceException(String.format(
                    "Expected namespace starting with '%s', got '%s'", expected, ns));
        }
    }

    protected static void checkRootElementNameEquals(final String name, final String expected)
            throws ServiceException {
        if (!expected.equals(name)) {
            throw new ServiceException(String.format(
                    "Expected root element with name '%s', got '%s'", expected, name));
        }
    }
}
