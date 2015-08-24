package fi.nls.oskari.control.layer;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Enumeration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.map.layer.OskariLayerService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ServiceFactory;
import fi.nls.oskari.domain.User;

@OskariActionRoute("GetLayerTile")
public class GetLayerTileHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(GetLayerTileHandler.class);
    private static final String RESOURCE_CACHE_NAME = "permission_resources";
    private static final String LAYER_CACHE_NAME = "layer_resources";
    private static final String LAYER_ID = "id";
    private static final String LEGEND = "legend";
    private static final List<String> RESERVED_PARAMETERS = Arrays.asList(new String[] {LAYER_ID, ActionControl.PARAM_ROUTE, LEGEND});
    private OskariLayerService layerService = null;
    private PermissionsService permissionsService = null;
    private final Cache<Resource> resourceCache = CacheManager.getCache(RESOURCE_CACHE_NAME);
    private final Cache<OskariLayer> layerCache = CacheManager.getCache(LAYER_CACHE_NAME);
    private static final int TIMEOUT_CONNECTION = PropertyUtil.getOptional("GetLayerTile.timeout.connection", 1000);
    private static final int TIMEOUT_READ = PropertyUtil.getOptional("GetLayerTile.timeout.read", 5000);
    private static final boolean GATHER_METRICS = PropertyUtil.getOptional("GetLayerTile.metrics", true);
    private static final String METRICS_PREFIX = "Oskari.GetLayerTile";

    /**
     *  Init method
     */
    public void init() {
        layerService = ServiceFactory.getMapLayerService();
        permissionsService = ServiceFactory.getPermissionsService();
    }

    /**
     * Action handler
     * @param params Parameters
     * @throws ActionException
     */
    public void handleAction(final ActionParameters params)
            throws ActionException {

        // Resolve layer
        final String layerId = params.getRequiredParam(LAYER_ID);
        final OskariLayer layer = getLayer(layerId);
        if (layer == null) {
            throw new ActionParamsException("Layer not found for id: " + layerId);
        }

        // Check permissions
        final Resource resource = getResource(layer);
        final User user = params.getUser();
        final boolean hasPermission =
                resource.hasPermission(user, Permissions.PERMISSION_TYPE_VIEW_LAYER) ||
                resource.hasPermission(user, Permissions.PERMISSION_TYPE_VIEW_PUBLISHED);

        if (!hasPermission) {
            throw new ActionDeniedException("User doesn't have permissions for requested layer");
        }

        final MetricRegistry metrics = ActionControl.getMetrics();

        Timer.Context actionTimer = null;
        if(GATHER_METRICS) {
            final com.codahale.metrics.Timer timer = metrics.timer(METRICS_PREFIX + "." + layerId);
            actionTimer = timer.time();
        }
        // Create connection

        final String url = getURL(params, layer);
        final HttpURLConnection con = getConnection(url, layer);
        try {
            con.setRequestMethod("GET");
            con.setDoOutput(false);
            con.setConnectTimeout(TIMEOUT_CONNECTION);
            con.setReadTimeout(TIMEOUT_READ);
            con.setDoInput(true);
            con.setFollowRedirects(true);
            con.setUseCaches(false);
            con.connect();

            final int responseCode = con.getResponseCode();
            final String contentType = con.getContentType();
            if(responseCode != HttpURLConnection.HTTP_OK || !contentType.startsWith("image/")) {
                LOG.warn("URL", url, "returned HTTP response code", responseCode,
                        "with message", con.getResponseMessage(), "and content-type:", contentType);
                String msg = IOHelper.readString(con);
                LOG.info("Response was:", msg);
                throw new ActionParamsException("Problematic response from actual service");
            }

            // read the image tile
            final byte[] presponse = IOHelper.readBytes(con);
            final HttpServletResponse response = params.getResponse();
            response.setContentType(contentType);
            response.getOutputStream().write(presponse, 0, presponse.length);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch(ActionException e) {
            // just throw it as is if we already handled it
            throw e;
        } catch (Exception e) {
            throw new ActionParamsException("Couldn't proxy request to actual service", e.getMessage(), e);
        } finally {
            if(actionTimer != null) {
                actionTimer.stop();
            }
            if(con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * Returns layer from cache
     * @param id Layer id
     * @return layer
     */
    private OskariLayer getLayer(final String id) {
        OskariLayer layer = layerCache.get(id);
        if (layer != null) {
            return layer;
        }
        layer = layerService.find(id);
        if (layer != null) {
            LOG.debug("Caching a layer with id ", id);
            layerCache.put(id, layer);
        }
        return layer;
    }

    /**
     * Gets resource from cache
     * @return resource
     */
    private Resource getResource(final OskariLayer layer) {

        final Resource layerResource = new OskariLayerResource(layer);
        Resource resource = resourceCache.get(layerResource.getMapping());
        if (resource != null) {
            return resource;
        }
        resource = permissionsService.findResource(layerResource);
        if (resource != null && !resource.getPermissions().isEmpty()) {
            LOG.debug("Caching a layer permission resource", resource, "Permissions", resource.getPermissions());
            resourceCache.put(layerResource.getMapping(),resource);
        }
        else {
            LOG.warn("Trying to cache layer with no resources");
        }
        return resource;
    }

    private String getURL(final ActionParameters params, final OskariLayer layer) {
        if (params.getHttpParam(LEGEND, false)) {
            return layer.getLegendImage();
        }
        final HttpServletRequest httpRequest = params.getRequest();
        Enumeration<String> paramNames = httpRequest.getParameterNames();
        Map<String, String> urlParams = new HashMap<>();
        // Refine parameters
        while (paramNames.hasMoreElements()){
            String paramName = paramNames.nextElement();
            if (!RESERVED_PARAMETERS.contains(paramName)) {
                urlParams.put(paramName, params.getHttpParam(paramName));
            }
        }
        return IOHelper.constructUrl(layer.getUrl(),urlParams);
    }

    /**
     * Creates connection
     * @param url URL (with params) to call
     * @param layer layer
     * @return connection
     * @throws ActionException
     */
    private HttpURLConnection getConnection(final String url, final OskariLayer layer)
            throws ActionException {
        try {
            final String username = layer.getUsername();
            final String password = layer.getPassword();
            LOG.debug("Getting layer tile from url:", url);
            return IOHelper.getConnection(url, username, password);
        } catch (Exception e) {
            throw new ActionException("Couldn't get connection to service", e);
        }
    }
}
