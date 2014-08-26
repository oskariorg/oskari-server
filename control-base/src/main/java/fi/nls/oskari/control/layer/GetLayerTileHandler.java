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
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
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
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.ActionDeniedException;

@OskariActionRoute("GetLayerTile")
public class GetLayerTileHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetLayerTileHandler.class);
    final private static String RESOURCE_CACHE_NAME = "permission_resources";
    final private static String LAYER_CACHE_NAME = "layer_resources";
    final private static String LAYER_ID = "id";
    final private static List<String> RESERVED_PARAMETERS = Arrays.asList(new String[] {LAYER_ID, "action_route"});
    private OskariLayerService layerService = null;
    private PermissionsService permissionsService = null;
    Resource layerResource = null;
    Cache<Resource> resourceCache = CacheManager.getCache(RESOURCE_CACHE_NAME);
    Cache<OskariLayer> layerCache = CacheManager.getCache(LAYER_CACHE_NAME);

    /**
     *  Init method
     */
    public void init() {
        layerService = ServiceFactory.getMapLayerService();
        permissionsService = ServiceFactory.getPermissionsService();
        Cache<Resource> resourceCache = CacheManager.getCache(RESOURCE_CACHE_NAME);
        Cache<OskariLayer> layerCache = CacheManager.getCache(LAYER_CACHE_NAME);
    }

    /**
     * Action handler
     * @param params Parameters
     * @throws ActionException
     */
    public void handleAction(final ActionParameters params)
            throws ActionException {
        User user = params.getUser();

        // Layer cache
        OskariLayer layer = getLayer(params.getRequiredParam(LAYER_ID));
        if (layer == null) {
            throw new ActionParamsException("Layer not found");
        }

        // Resource cache
        layerResource = new OskariLayerResource(layer);
        Resource resource = getResource();

        // Permission check
        final boolean hasPermission = ((resource.hasPermission(user, Permissions.PERMISSION_TYPE_VIEW_LAYER))||
                (resource.hasPermission(user, Permissions.PERMISSION_TYPE_VIEW_PUBLISHED)));

        if (!hasPermission) {
            throw new ActionDeniedException("Session expired");
        }

        // Create connection
        final HttpURLConnection con = getConnection(params,layer);
        try {
            con.setRequestMethod("GET");
            con.setDoOutput(false);
            con.setDoInput(true);
            HttpURLConnection.setFollowRedirects(false);
            con.setUseCaches(false);
            con.connect();

            // read the image tile
            final byte[] presponse = IOHelper.readBytes(con.getInputStream());
            final HttpServletResponse response = params.getResponse();
            response.setContentType("image/png");
            response.getOutputStream().write(presponse, 0, presponse.length);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            throw new ActionException("Couldn't proxy request to geoserver", e);
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
    private OskariLayer getLayer(String id) throws ActionParamsException {
        OskariLayer layer = layerCache.get(id);
        if (layer != null) {
            return layer;
        }
        layer = layerService.find(id);
        if (layer == null) {
            throw new ActionParamsException("Layer not found");
        }
        layerCache.put(id, layer);
        return layer;
    }

    /**
     * Gets resource from cache
     * @return resource
     */
    private Resource getResource() {
        Resource resource = resourceCache.get(layerResource.getMapping());
        if (resource != null) {
            return resource;
        }
        log.debug("Caching a layer permission resource");
        resource = permissionsService.findResource(layerResource);
        if (resource != null) {
            resourceCache.put(layerResource.getMapping(),resource);
        }
        return resource;
    }

    /**
     * Creates connection
     * @param params parameters
     * @param layer layer
     * @return connection
     * @throws ActionException
     */
    private HttpURLConnection getConnection(final ActionParameters params, final OskariLayer layer)
            throws ActionException {
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
        try {
            final String username = layer.getUsername();
            final String password = layer.getPassword();
            log.debug("Getting layer tile from url:", url);
            return IOHelper.getConnection(url, username, password);
        } catch (Exception e) {
            throw new ActionException("Couldn't get connection to geoserver", e);
        }
    }
}
