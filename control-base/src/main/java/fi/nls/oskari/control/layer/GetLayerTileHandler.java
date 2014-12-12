package fi.nls.oskari.control.layer;

import java.net.HttpURLConnection;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Enumeration;
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
import fi.nls.oskari.util.ServiceFactory;
import fi.nls.oskari.domain.User;

@OskariActionRoute("GetLayerTile")
public class GetLayerTileHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetLayerTileHandler.class);
    final private static String RESOURCE_CACHE_NAME = "permission_resources";
    final private static String LAYER_CACHE_NAME = "layer_resources";
    final private static String LAYER_ID = "id";
    final private static List<String> RESERVED_PARAMETERS = Arrays.asList(new String[] {LAYER_ID, ActionControl.PARAM_ROUTE});
    private OskariLayerService layerService = null;
    private PermissionsService permissionsService = null;
    private final Cache<Resource> resourceCache = CacheManager.getCache(RESOURCE_CACHE_NAME);
    private final Cache<OskariLayer> layerCache = CacheManager.getCache(LAYER_CACHE_NAME);

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

        // Create connection
        final String url = getURL(params, layer);
        final HttpURLConnection con = getConnection(url, layer);
        try {
            con.setRequestMethod("GET");
            con.setDoOutput(false);
            con.setDoInput(true);
            con.setFollowRedirects(true);
            con.setUseCaches(false);
            con.connect();
            final int responseCode = con.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                log.warn("URL", url, "returned HTTP response code", responseCode,
                        "with message", con.getResponseMessage());
                String msg = IOHelper.readString(con);
                log.info("Response was:", msg);
                throw new ActionParamsException("Couldn't proxy request to actual service");
            }

            // read the image tile
            final byte[] presponse = IOHelper.readBytes(con);
            final HttpServletResponse response = params.getResponse();
            // TODO: check layer for content type!! don't assume png
            response.setContentType(params.getHttpParam("FORMAT", "image/png"));
            response.getOutputStream().write(presponse, 0, presponse.length);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            throw new ActionException("Couldn't proxy request to actual service", e);
        } finally {
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
            log.debug("Caching a layer with id ", id);
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
            log.debug("Caching a layer permission resource", resource, "Permissions", resource.getPermissions());
            resourceCache.put(layerResource.getMapping(),resource);
        }
        else {
            log.warn("Trying to cache layer with no resources");
        }
        return resource;
    }

    private String getURL(final ActionParameters params, final OskariLayer layer) {
        final HttpServletRequest httpRequest = params.getRequest();
        Enumeration<String> paramNames = httpRequest.getParameterNames();
        Map<String, String> urlParams = new HashMap<String, String>();
        // Refine parameters
        while (paramNames.hasMoreElements()){
            String paramName = paramNames.nextElement();
            if (!RESERVED_PARAMETERS.contains(paramName)) {
                urlParams.put(paramName, params.getHttpParam(paramName));
            }
        }
        final String url = IOHelper.constructUrl(layer.getUrl(),urlParams);
        return url;
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
            log.debug("Getting layer tile from url:", url);
            return IOHelper.getConnection(url, username, password);
        } catch (Exception e) {
            throw new ActionException("Couldn't get connection to service", e);
        }
    }
}
