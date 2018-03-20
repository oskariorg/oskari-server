package fi.nls.oskari.work;

import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.Units;
import fi.nls.oskari.pojo.WFSLayerPermissionsStore;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSExceptionHelper;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.HttpHelper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Helper methods for OWSMaplayerJob
 */
public class JobHelper {

    private static final Logger log = LogFactory.getLogger(JobHelper.class);

    // API
    public static final String PERMISSIONS_API = "GetLayerIds";
    public static final String LAYER_CONFIGURATION_API = "GetWFSLayerConfiguration&id=";
    // API URL (action routes)
    private final static String PARAM_ROUTE = "action_route";
    private static String SERVICE_URL = null; // default value perhaps?
    private static String SERVICE_URL_PATH = null;
    private static String SERVICE_URL_QUERYSTRING = null;
    private static String SESSION_COOKIE_NAME = PropertyUtil.get("oskari.cookie.session", "JSESSIONID") + "=";
    public static final String PARAM_MANUAL_REFRESH = "manualRefresh";
    public static final String PARAM_CASCADING = "cascading";

    // COOKIE
    public static final String ROUTE_COOKIE_NAME = PropertyUtil.get("oskari.cookie.route", "ROUTEID") + "=";

    private static Units units = new Units();

    static {
        setupAPIUrl();
    }

    private synchronized static void setupAPIUrl() {
        if(SERVICE_URL != null) {
            return;
        }
        SERVICE_URL = PropertyUtil.get("oskari.domain", null);
        final String[] path = PropertyUtil.get("oskari.ajax.url.prefix", "/?").split("\\?");
        SERVICE_URL_PATH = path[0];
        SERVICE_URL_QUERYSTRING = "?";
        if(path.length == 2) {
            SERVICE_URL_QUERYSTRING = SERVICE_URL_QUERYSTRING + path[1];
        }
        if(!SERVICE_URL_QUERYSTRING.endsWith("?") && !SERVICE_URL_QUERYSTRING.endsWith("&")) {
            SERVICE_URL_QUERYSTRING = SERVICE_URL_QUERYSTRING + "&";
        }
        SERVICE_URL_QUERYSTRING = SERVICE_URL_QUERYSTRING + PARAM_ROUTE + "=";
        // querystring should now be "?action_route=" or something like "?qwer=ty&action_route="
        log.debug("Constructed path:", SERVICE_URL_PATH, "- and querystr: ", SERVICE_URL_QUERYSTRING, "from:", path);
    }

    /**
     * Gets service path for local API
     *
     * Path for Layer configuration and permissions request
     *
     * @return URL
     */
    public static String getAPIUrl() {
        return SERVICE_URL + SERVICE_URL_PATH + SERVICE_URL_QUERYSTRING;
    }

    public static String getCookiesValue(String sessionId, String route) {
        StringWriter writer = new StringWriter();
        if(sessionId != null && !sessionId.isEmpty()) {
            writer.append(SESSION_COOKIE_NAME);
            writer.append(sessionId);
            writer.append("; ");
        }
        if(route != null && !route.isEmpty()) {
            writer.append(ROUTE_COOKIE_NAME);
            writer.append(route);
            writer.append("; ");
        }
        return writer.toString();
    }
    /**
     * Gets layer permissions (uses cache)
     *
     * @param layerId
     * @param sessionId
     * @param route
     * @return <code>true</code> if rights to use the layer; <code>false</code>
     *         otherwise.
     */
    public static boolean hasPermission(String layerId, String sessionId, String route) {
        String json = WFSLayerPermissionsStore.getCache(sessionId);
        boolean fromCache = json != null;
        if(!fromCache) {
            json = HttpHelper.getRequest(getAPIUrl() + PERMISSIONS_API, getCookiesValue(sessionId, route));
            if(json == null) {
                return false;
            }
        }
        try {
            WFSLayerPermissionsStore permissions = WFSLayerPermissionsStore.setJSON(json);
            return permissions.isPermission(layerId);
        } catch (IOException e) {
            log.error(e, "JSON parsing failed for WFSLayerPermissionsStore \n" + json);
        }

        return false;
    }

    /**
     * Gets layer configuration (uses cache)
     * throws ServiceRuntimeException, if Redis methods fails (not 1st get cache)
     *
     * @param layerId
     * @param sessionId
     * @param route
     * @return layer
     */
    public static WFSLayerStore getLayerConfiguration(String layerId, String sessionId, String route) {
        String json = WFSLayerStore.getCache(layerId);
        boolean fromCache = json != null;
        if(!fromCache) {
            final String apiUrl = getAPIUrl() + LAYER_CONFIGURATION_API + layerId;
            log.debug("Fetching layer data from", apiUrl);
            // NOTE: result is not handled as request triggers Redis write
            HttpHelper.getRequest(apiUrl, getCookiesValue(sessionId, route));
            // that we read here
            json = WFSLayerStore.getCacheNecessary(layerId);
            if(json == null) {
                log.error("Couldn't find JSON for WFSLayerStore with id:", layerId, " - API url:", apiUrl);
                return null;
            }
        }
        try {
            return WFSLayerStore.setJSON(json);
        } catch (Exception e) {
            log.error(e, "JSON parsing failed for WFSLayerStore \n" + json);
            throw new ServiceRuntimeException("JSON parsing failed for WFSLayerStore - json: " + json,
                    e.getCause(), WFSExceptionHelper.ERROR_WFSLAYERSTORE_PARSING_FAILED);
        }

    }

    public static boolean isRequestScalesInRange(List<Double> mapScales, int zoomLevel, WFSLayerConfiguration layer, final String targetSRS) {
        double scale = mapScales.get(zoomLevel);
        return isRequestScalesInRange(scale, layer, targetSRS);
    }

    /**
     * Checks if the map scale is valid
     *
     * @return <code>true</code> if map scale is valid;
     *         <code>false</code> otherwise.
     */
    public static boolean isRequestScalesInRange(double scale, WFSLayerConfiguration layer, final String targetSRS) {
        log.debug("Scale in:", layer.getSRSName(), scale, "[", layer.getMaxScale(), ",", layer.getMinScale(), "]");
        // if scale value is -1 -> ignore scale check on that boundary
        boolean minScaleOk = (layer.getMinScale() == -1);
        boolean maxScaleOk = (layer.getMaxScale() == -1);
        // min == biggest value
        if(!minScaleOk) {
            double minScaleInMapSrs = units.getScaleInSrs(layer.getMinScale(), layer.getSRSName(), targetSRS);
            log.debug("Scale in:", targetSRS, scale, "[min:", minScaleInMapSrs, "]");
            minScaleOk = (minScaleInMapSrs >= scale);
        }
        if(!maxScaleOk) {
            double maxScaleInMapSrs = units.getScaleInSrs(layer.getMaxScale(), layer.getSRSName(), targetSRS);
            log.debug("Scale in:", targetSRS, scale, "[max:", maxScaleInMapSrs, "]");
            maxScaleOk = maxScaleInMapSrs <= scale;
        }
        boolean scaleOk = minScaleOk && maxScaleOk;
        if(!scaleOk) {
            log.info("Layer out of scale limits:", layer.getId(), scale, "[", layer.getMaxScale(), ",", layer.getMinScale(), "]");
        }
        return scaleOk;
    }
}
