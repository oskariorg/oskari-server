package fi.nls.oskari.service.capabilities;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

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
    public abstract OskariLayerCapabilities save(final OskariLayerCapabilities capabilities);

    /**
     * Tries to load capabilities from the database
     * @return null if not saved to db
     */
    public OskariLayerCapabilities find(final OskariLayer layer) {
        return find(layer.getSimplifiedUrl(true), layer.getType(), layer.getVersion());
    }

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

    public OskariLayerCapabilities getCapabilities(final OskariLayer layer, String encoding, final boolean loadFromService) throws ServiceException {

        final OskariLayerCapabilities cap = createTemplate(layer);
        try {
            // prefer saved db version over network call by default (only when encoding null == don't check twice)
            if(!loadFromService) {
                OskariLayerCapabilities dbCapabilities = find(layer);
                if(dbCapabilities != null) {
                    return dbCapabilities;
                }
            }
            // get xml from service
            final String xml = loadCapabilitiesFromService(layer, encoding, loadFromService);
            cap.setData(xml);
            // save before returning
            save(cap);
            LOG.debug("Saved capabilities", cap.getId());
            return cap;
        }
        catch (IOException e) {
            if(e instanceof SocketTimeoutException) {
                LOG.warn("Getting capabilities for layer timed out. You can adjust timeout with property",
                        PROP_TIMEOUT, ". Current value is:", TIMEOUT_SECONDS, "seconds");
            }
            // save empty result so we don't hang the system when having multiple layers from problematic service
            cap.setData("");
            save(cap);
            throw new ServiceException("Error loading capabilities from URL:" + layer.getUrl(), e);
        }
    }

    public static String loadCapabilitiesFromService(OskariLayer layer, String encoding) throws IOException {
        return loadCapabilitiesFromService(layer, encoding, false);
    }

    private static String loadCapabilitiesFromService(OskariLayer layer, String encoding, final boolean norecursion) throws IOException {

        final String url = contructCapabilitiesUrl(layer);
        if(encoding == null) {
            encoding = IOHelper.DEFAULT_CHARSET;
        }
        final HttpURLConnection conn = IOHelper.getConnection(url, layer.getUsername(), layer.getPassword());
        conn.setReadTimeout(TIMEOUT_MS);
        final String response = IOHelper.readString(conn, encoding);
        final String charset = getEncodingFromXml(response);

        //if encoding differs from that of the xml, we always have to re-read from service.
        if (charset != null && !encoding.equalsIgnoreCase(charset))  {
            return loadCapabilitiesFromService(layer, charset, true);
        } else if(norecursion || charset == null || encoding.equalsIgnoreCase(charset)) {
            return response;
        }
        return loadCapabilitiesFromService(layer, charset, true);
    }

    public static OskariLayerCapabilities createTemplate(OskariLayer layer) {
        OskariLayerCapabilities cap = new OskariLayerCapabilities();
        cap.setUrl(layer.getSimplifiedUrl(true));
        cap.setLayertype(layer.getType());
        cap.setVersion(layer.getVersion());
        return cap;
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