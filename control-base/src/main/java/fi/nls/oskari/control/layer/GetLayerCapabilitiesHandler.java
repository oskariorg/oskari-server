package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesConstants;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.ResponseHelper;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.permissions.PermissionService;
import org.oskari.service.util.ServiceFactory;

import java.nio.charset.StandardCharsets;

/**
 * Returns the Capabilities for layer in XML format if the user has permissions for requested layer.
 */
@OskariActionRoute("GetLayerCapabilities")
public class GetLayerCapabilitiesHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(GetLayerCapabilitiesHandler.class);
    private CapabilitiesCacheService capabilitiesService;
    private PermissionHelper permissionHelper;

    public void init() {
        if (permissionHelper == null) {
            permissionHelper = new PermissionHelper(
                    ServiceFactory.getMapLayerService(),
                    OskariComponentManager.getComponentOfType(PermissionService.class));
        }
        if (capabilitiesService == null) {
            capabilitiesService = ServiceFactory.getCapabilitiesCacheService();
        }
    }

    public void setCapabilitiesCacheService(CapabilitiesCacheService capabilitiesService) {
        this.capabilitiesService = capabilitiesService;
    }

    public void setPermissionHelper(final PermissionHelper helper) {
        permissionHelper = helper;
    }

    @Override
    public void handleAction(final ActionParameters params)
            throws ActionException {
        final int layerId = params.getRequiredParamInt(ActionConstants.KEY_ID);
        final OskariLayer layer = permissionHelper.getLayer(layerId, params.getUser());
        if (params.getHttpParam("json", false)) {
            final String data = getCapabilitiesJSON(layer, params.getRequiredParam("srs"));
            ResponseHelper.writeResponse(params, HttpServletResponse.SC_OK,
                    "application/json", data.getBytes(StandardCharsets.UTF_8));
        } else {
            final String data = getCapabilities(layer);
            ResponseHelper.writeResponse(params, HttpServletResponse.SC_OK,
                    "text/xml", data.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String getCapabilities(OskariLayer layer) throws ActionException {
        try {
            OskariLayerCapabilities caps = capabilitiesService.getCapabilities(layer);
            // Do not cache this. We don't check whether or not we can parse this response
            if (caps.getId() == null) {
                LOG.info("Capabilities for layer", layer.getId(), "are not cached. Update them using the admin UI.");
            }
            return caps.getData();
        } catch (ServiceException ex) {
            throw new ActionException("Error reading capabilities", ex);
        }
    }

    private String getCapabilitiesJSON(OskariLayer layer, String crs) throws ActionException {
        try {
            JSONObject caps = layer.getCapabilities()
                    .optJSONObject(CapabilitiesConstants.KEY_LAYER_CAPABILITIES);
            JSONObject response = new JSONObject(caps.toString());
            JSONArray linkList = response.optJSONArray("links");
            JSONObject link = null;
            for (int i = 0; i < linkList.length(); i++) {
                link = linkList.optJSONObject(i);
                JSONObject tileMatrixSet = link.optJSONObject("tileMatrixSet");
                String projection = tileMatrixSet.optString("projection");
                String shortProj = ProjectionHelper.shortSyntaxEpsg(projection);
                if (crs.equals(shortProj)) {
                    // use the first tilematrix matching the projection that is used
                    JSONObject matrix = new JSONObject(tileMatrixSet.toString());
                    matrix.put("projection", shortProj);
                    break;
                } else {
                    // make sure we don't end up with non-null link after we have
                    //  searched for the correct projection and not found it
                    link = null;
                }
            }
            if (link == null) {
                throw new ActionParamsException("No tilematrix matching srs: " + crs);
            }
            JSONArray filteredLinkList = new JSONArray();
            filteredLinkList.put(link);
            response.put("links", filteredLinkList);
            return response.toString();
        } catch (Exception e) {
            throw new ActionParamsException("Unable to parse JSON", e);
        }
    }
}
