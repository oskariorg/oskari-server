package fi.nls.oskari.service.capabilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.XmlHelper;

public abstract class CapabilitiesCacheService extends OskariComponent {

    private static final Logger LOG = LogFactory.getLogger(CapabilitiesCacheService.class);
    private static final Map<String, String> TYPE_MAPPING = new HashMap<>(5);
    static {
        TYPE_MAPPING.put(OskariLayer.TYPE_WMS, "WMS");
        TYPE_MAPPING.put(OskariLayer.TYPE_WFS, "WFS");
        TYPE_MAPPING.put(OskariLayer.TYPE_WMTS, "WMTS");
    }
    // timeout capabilities request after 30 seconds (configurable)
    private static final String PROP_TIMEOUT = "capabilities.timeout";
    private static final int TIMEOUT_SECONDS = PropertyUtil.getOptional(PROP_TIMEOUT, 30);
    private static final int TIMEOUT_MS = TIMEOUT_SECONDS * 1000;

    public abstract OskariLayerCapabilities find(final String url, final String layertype, final String version);
    public abstract OskariLayerCapabilities save(final OskariLayerCapabilities capabilities);

    protected abstract void updateMultiple(final List<OskariLayerCapabilities> capabilities);
    protected abstract List<OskariLayerCapabilities> getAllOlderThan(final long maxAgeMs);

    public void updateAllOlderThan(final long maxAgeMs) {
        List<OskariLayerCapabilities> updates = new ArrayList<>();
        for (OskariLayerCapabilities capabilities : getAllOlderThan(maxAgeMs)) {
            String url = capabilities.getUrl();
            String type = capabilities.getLayertype();
            String version = capabilities.getVersion();
            String dataOld = capabilities.getData();

            OskariLayer layer = createTempOskariLayer(url, type, null, null, version);
            String data = loadCapabilitiesFromService(layer);

            if (data.isEmpty()) {
                LOG.warn("Getting Capabilities from service failed for url:", url, "- skipping!");
                continue;
            }

            if (dataOld.equals(data)) {
                LOG.warn("New data is equal to old data for url:", url, "- skipping!");
                continue;
            }

            capabilities.setData(data);
            updates.add(capabilities);
        }

        updateMultiple(updates);
    }

    public OskariLayerCapabilities getCapabilities(String url, String type, String version)
            throws ServiceException {
        return getCapabilities(url, type, null, null, version);
    }

    public OskariLayerCapabilities getCapabilities(String url, String type, final String user, final String passwd, final String version)
            throws ServiceException {
        return getCapabilities(url, type, user, passwd, version, false);
    }

    public OskariLayerCapabilities getCapabilities(String url, String type, final String user, final String passwd, final String version, final boolean loadFromService)
            throws ServiceException {
        return getCapabilities(createTempOskariLayer(url, type, user, passwd, version), loadFromService);
    }

    private OskariLayer createTempOskariLayer(String url, String type, final String user, final String passwd, final String version) {
        OskariLayer layer = new OskariLayer();
        layer.setUrl(url);
        layer.setType(type);
        layer.setVersion(version);
        layer.setUsername(user);
        layer.setPassword(passwd);
        return layer;
    }

    public OskariLayerCapabilities getCapabilities(final OskariLayer layer)
            throws ServiceException {
        return getCapabilities(layer, false);
    }

    public OskariLayerCapabilities getCapabilities(final OskariLayer layer, final boolean loadFromService)
            throws ServiceException {
        final String url = layer.getSimplifiedUrl(true);
        final String type = layer.getType();
        final String version = layer.getVersion();

        // prefer saved db version over network call by default
        if (!loadFromService) {
            OskariLayerCapabilities dbCapabilities = find(url, type, version);
            if (dbCapabilities != null) {
                return dbCapabilities;
            }
        }

        // get xml from service
        final String data = loadCapabilitiesFromService(layer);
        if (data == null || data.trim().isEmpty()) {
            throw new ServiceException("Failed to load capabilities from service!");
        }

        try {
            return save(new OskariLayerCapabilities(url, type, version, data));
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Failed to save capabilities: " + e.getMessage());
        }
    }

    public static String loadCapabilitiesFromService(OskariLayer layer) {
        final String url = contructCapabilitiesUrl(layer);
        if (url.isEmpty()) {
            return null;
        }

        String encoding = null;
        byte[] data = null;
        try {
            final HttpURLConnection conn = IOHelper.getConnection(url, layer.getUsername(), layer.getPassword());
            conn.setReadTimeout(TIMEOUT_MS);

            final int sc = conn.getResponseCode();
            if (sc != HttpURLConnection.HTTP_OK) {
                LOG.warn("Unexpected Status code:", sc, " url:", url);
                return null;
            }

            final String contentType = conn.getContentType();
            if (contentType != null && contentType.toLowerCase().indexOf("xml") == -1) {
                // not xml based on contentType
                LOG.warn("Unexpected Content-Type:", contentType, "url:", url);
                return null;
            }

            encoding = IOHelper.getCharset(conn);
            data = IOHelper.readBytes(conn);
        } catch (IOException e) {
            LOG.warn(e, "IOException occured, url:", url);
            return null;
        }

        try {
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
            XMLStreamReader xsr = xif.createXMLStreamReader(new ByteArrayInputStream(data));

            // Check XML prolog for character encoding
            String xmlEncoding = xsr.getCharacterEncodingScheme();
            if (xmlEncoding != null) {
                if (encoding != null && !xmlEncoding.equalsIgnoreCase(encoding)) {
                    LOG.error("Content-Type header specified a different encoding than XML prolog!");
                    return null;
                }
                encoding = xmlEncoding;
            }
            if (encoding == null) {
                LOG.debug("Charset wasn't set on either the Content-Type or the XML prolog"
                        + "using UTF-8 as default value");
                encoding = IOHelper.DEFAULT_CHARSET;
            }

            // Check that the response is what we expect
            if (!checkCapabilities(xsr, layer.getType(), layer.getVersion())) {
                return null;
            }

            // Convert "utf-8" to "UTF-8" for example
            encoding = encoding.toUpperCase();
            String xml = new String(data, encoding);
            // Strip the potential prolog from XML so that we
            // don't have to worry about the specified charset
            return XmlHelper.stripPrologFromXML(xml);
        } catch (FactoryConfigurationError | XMLStreamException e) {
            LOG.warn(e, "Failed to parse XML from response");
        } catch (UnsupportedEncodingException e) {
            LOG.warn(e, "Failed to Encode byte[] to String encoding:", encoding);
        }
        return null;
    }

    public static String contructCapabilitiesUrl(final OskariLayer layer) {
        if (layer == null) {
            return "";
        }

        final String url = layer.getSimplifiedUrl(true);
        final String urlLC = url.toLowerCase();
        final String serviceType = TYPE_MAPPING.get(layer.getType());

        final Map<String, String> params = new HashMap<String, String>();
        // check existing params
        if (!urlLC.contains("service=")) {
            params.put("service", serviceType);
        }
        if (!urlLC.contains("request=")) {
            params.put("request", "GetCapabilities");
        }
        if (!urlLC.contains("version=") && layer.getVersion() != null) {
            params.put(getVersionNegotiationKey(serviceType), layer.getVersion());
        }

        return IOHelper.constructUrl(url, params);
    }

    private static String getVersionNegotiationKey(String service) {
        if (service != null) {
            switch (service) {
            case "WMS":
                return "version";
            case "WFS":
            case "WMTS":
                return "acceptVersions";
            }
        }
        return "";
    }

    private static boolean checkCapabilities(XMLStreamReader xsr, String type, String version) {
        if (!advanceToRootElement(xsr)) {
            // Could not advance to root element
            return false;
        }
        String ns = xsr.getNamespaceURI();
        String name = xsr.getLocalName();
        return checkCapabilities(type, version, ns, name);
    }

    private static boolean advanceToRootElement(XMLStreamReader xsr) {
        try {
            if (xsr.nextTag() != XMLStreamConstants.START_DOCUMENT) {
                LOG.warn("Document did not start with a START_DOCUMENT!");
                return false;
            }
            if (xsr.nextTag() != XMLStreamConstants.START_ELEMENT) {
                LOG.warn("Could not find root element!");
                return false;
            }
            return true;
        } catch (XMLStreamException e) {
            LOG.warn(e, "Failed to find root element!");
            return false;
        }
    }

    private static boolean checkCapabilities(String type, String version, String ns, String name) {
        LOG.debug("Checking capabilities, type:", type, "version:", version, 
                "namespace", ns, "root element", name);

        switch (type) {
        case OskariLayer.TYPE_WMS:
            if (version == null) {
                // Layer didn't specify a version - response could could be anything
                return isWMS130Capabilities(ns, name) || isWMSLessThan130Capabilities(ns, name);
            } else if ("1.3.0".equals(version)) {
                return isWMS130Capabilities(ns, name);
            } else {
                return isWMSLessThan130Capabilities(ns, name);
            }
        case OskariLayer.TYPE_WFS:
            return ns.startsWith("http://www.opengis.net/wfs/") && "WFS_Capabilities".equals(name);
        case OskariLayer.TYPE_WMTS:
            return ns.startsWith("http://www.opengis.net/wmts/") && "Capabilities".equals(name);
        }
        return false;
    }

    private static boolean isWMSLessThan130Capabilities(String ns, String name) {
        return ns == null
                && "WMT_MS_Capabilities".equals(name);
    }

    private static boolean isWMS130Capabilities(String ns, String name) {
        return ns != null 
                && ns.startsWith("http://www.opengis.net/wms/") 
                && "WMS_Capabilities".equals(name);
    }

}