package fi.nls.oskari.service.capabilities;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

public abstract class CapabilitiesCacheService extends OskariComponent {

    private static final Logger LOG = LogFactory.getLogger(CapabilitiesCacheService.class);
    private static final String ENCODE_ATTRIBUTE =  "encoding=\"";

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
    public abstract OskariLayerCapabilities save(final OskariLayerCapabilitiesDraft draft);

    public OskariLayerCapabilities getCapabilities(String url, String serviceType, String serviceVersion) throws ServiceException {
        return getCapabilities(url, serviceType, null, null, serviceVersion);
    }

    public OskariLayerCapabilities getCapabilities(String url, String serviceType, final String user, final String passwd, final String version) throws ServiceException {
        return getCapabilities(url, serviceType, user, passwd,  version, false);
    }

    public OskariLayerCapabilities getCapabilities(String url, String serviceType, final String user, final String passwd, final String version, final boolean loadFromService) throws ServiceException {
        OskariLayer layer = new OskariLayer();
        layer.setUrl(url);
        layer.setType(serviceType);
        layer.setUsername(user);
        layer.setPassword(passwd);
        layer.setVersion(version);
        return getCapabilities(layer, loadFromService);
    }

    public OskariLayerCapabilities getCapabilities(final OskariLayer layer) throws ServiceException {
        return getCapabilities(layer, false);
    }
    public OskariLayerCapabilities getCapabilities(final OskariLayer layer, final boolean loadFromService) throws ServiceException {
        return getCapabilities(layer, null, loadFromService);
    }

    public OskariLayerCapabilities getCapabilities(final OskariLayer layer, String encoding, final boolean loadFromService) {
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
        final String data = loadCapabilitiesFromService(layer, encoding, loadFromService);
        return save(new OskariLayerCapabilitiesDraft(url, type, version, data));
    }

    public static String loadCapabilitiesFromService(OskariLayer layer, String encoding) {
        return loadCapabilitiesFromService(layer, encoding, false);
    }

    private static String loadCapabilitiesFromService(OskariLayer layer, String encoding, final boolean norecursion) {
        final String url = contructCapabilitiesUrl(layer);
        if (encoding == null) {
            encoding = IOHelper.DEFAULT_CHARSET;
        }

        try {
            final HttpURLConnection conn = IOHelper.getConnection(url, layer.getUsername(), layer.getPassword());
            conn.setReadTimeout(TIMEOUT_MS);

            final int sc = conn.getResponseCode();
            if (sc != HttpURLConnection.HTTP_OK) {
                LOG.warn("Unexpected Status code: ", sc, " url: ", url);
                return "";
            }

            final String contentType = conn.getContentType();
            if (contentType != null && contentType.toLowerCase().indexOf("xml") == -1) {
                // not xml based on contentType
                LOG.warn("Unexpected Content-Type: ", contentType, " url: ", url);
                return "";
            }

            final String response = IOHelper.readString(conn, encoding);
            final String charset = getEncodingFromXml(response);

            if (norecursion || charset == null || encoding.equalsIgnoreCase(charset)) {
                return response;
            }
            return loadCapabilitiesFromService(layer, charset, true);
        } catch (IOException e) {
            LOG.warn(e, "IOException occured, url: ", url, " error message: ", e.getMessage());
            return "";
        }
    }

    public static String contructCapabilitiesUrl(final OskariLayer layer) {
        if (layer == null) {
            return "";
        }
        final String url  = layer.getSimplifiedUrl(true);
        final Map<String, String> params = new HashMap<String, String>();
        // check existing params
        if(!url.toLowerCase().contains("service=")) {
            params.put("service", TYPE_MAPPING.get(layer.getType()));
        }
        if(!url.toLowerCase().contains("request=")) {
            params.put("request", "GetCapabilities");
        }
        if(!url.toLowerCase().contains("version=") && layer.getVersion() != null) {
            params.put("version", layer.getVersion());
        }

        return IOHelper.constructUrl(url, params);
    }

    // TODO: maybe use some lib instead?
    public static String getEncodingFromXml(final String response) {
        if(response == null) {
            return null;
        }

        final String[] processingSplit  = response.split("\\?>");

        if (processingSplit == null || processingSplit.length == 0) {
            return null;
        }

        int encodeAttributeStart = processingSplit[0].indexOf(ENCODE_ATTRIBUTE);
        if (encodeAttributeStart > 0) {
            encodeAttributeStart = encodeAttributeStart + ENCODE_ATTRIBUTE.length();
            return processingSplit[0].substring(encodeAttributeStart, processingSplit[0].indexOf('"', encodeAttributeStart));
        }

        return null;
    }
}