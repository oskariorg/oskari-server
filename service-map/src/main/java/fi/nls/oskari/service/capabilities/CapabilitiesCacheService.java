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
    // timeout capabilities request after 15 seconds (configurable)
    private static final int TIMEOUT_MS = PropertyUtil.getOptional("capabilities.timeout", 30) * 1000;

    public abstract OskariLayerCapabilities find(final String url, final String layertype);
    public abstract OskariLayerCapabilities save(final OskariLayerCapabilities capabilities);

    /**
     * Tries to load capabilities from the database
     * @return null if not saved to db
     */
    public OskariLayerCapabilities find(final OskariLayer layer) {
        return find(layer.getSimplifiedUrl(true), layer.getType());
    }

    public OskariLayerCapabilities getCapabilities(String url, String serviceType) throws ServiceException {
        return getCapabilities(url, serviceType, null, null);
    }

    public OskariLayerCapabilities getCapabilities(String url, String serviceType, final String user, final String passwd) throws ServiceException {
        OskariLayer layer = new OskariLayer();
        layer.setUrl(url);
        layer.setType(serviceType);
        layer.setUsername(user);
        layer.setPassword(passwd);
        return getCapabilities(layer, false);
    }

    public OskariLayerCapabilities getCapabilities(final OskariLayer layer) throws ServiceException {
        return getCapabilities(layer, false);
    }
    public OskariLayerCapabilities getCapabilities(final OskariLayer layer, final boolean loadFromService) throws ServiceException {
        return getCapabilities(layer, null, loadFromService);
    }

    public OskariLayerCapabilities getCapabilities(final OskariLayer layer, String encoding, final boolean loadFromService) throws ServiceException {
        return getCapabilities(layer, encoding, loadFromService, false);
    }

    private OskariLayerCapabilities getCapabilities(final OskariLayer layer, String encoding, final boolean loadFromService, final boolean norecursion) throws ServiceException {
        final String url = contructCapabilitiesUrl(layer);
        try {
            // prefer saved db version over network call by default (only when encoding null == don't check twice)
            if(!loadFromService && encoding == null) {
                OskariLayerCapabilities cap = find(layer);
                if(cap != null) {
                    return cap;
                }
            }
            if(encoding == null) {
                encoding = IOHelper.DEFAULT_CHARSET;
            }
            final HttpURLConnection conn = IOHelper.getConnection(url, layer.getUsername(), layer.getPassword());
            conn.setReadTimeout(TIMEOUT_MS);
            final String response = IOHelper.readString(conn, encoding);
            final String charset = getEncodingFromXml(response);
            if(norecursion || charset == null || encoding.equalsIgnoreCase(charset)) {
                LOG.debug("saving capabilities with charset", charset, "encoding:", encoding);

                OskariLayerCapabilities cap = createForLayer(layer);
                cap.setData(response);
                // save before returning
                save(cap);
                LOG.debug("Saved capabilities", cap.getId());
                return cap;
            }
            return getCapabilities(layer, charset, loadFromService, true);
        } catch (IOException e) {
            OskariLayerCapabilities cap = createForLayer(layer);
            // save empty result so we don't hang the system
            cap.setData("");
            save(cap);
            throw new ServiceException("Error loading capabilities from URL:" + url, e);
        }
    }

    private OskariLayerCapabilities createForLayer(OskariLayer layer) {
        OskariLayerCapabilities cap = new OskariLayerCapabilities();
        cap.setUrl(layer.getSimplifiedUrl(true));
        cap.setLayertype(layer.getType());
        return cap;
    }

    private static String contructCapabilitiesUrl(final OskariLayer layer) {
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

        return IOHelper.constructUrl(url, params);
    }

    // TODO: maybe use some lib instead?
    private String getEncodingFromXml(final String response) {
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