package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ResponseHelper;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.RawCapabilitiesResponse;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMTS;
import org.oskari.capabilities.ogc.wmts.TileMatrixLink;
import org.oskari.permissions.PermissionService;
import org.oskari.service.util.ServiceFactory;

import java.nio.charset.StandardCharsets;

/**
 * Returns the Capabilities for layer in XML format if the user has permissions for requested layer.
 * Used by admin functionality to show XML
 */
@OskariActionRoute("GetLayerCapabilities")
public class GetLayerCapabilitiesHandler extends ActionHandler {

    private PermissionHelper permissionHelper;

    public void init() {
        if (permissionHelper == null) {
            permissionHelper = new PermissionHelper(
                    ServiceFactory.getMapLayerService(),
                    OskariComponentManager.getComponentOfType(PermissionService.class));
        }
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
            try {
                final RawCapabilitiesResponse data = CapabilitiesService.getCapabilities(layer);
                ResponseHelper.writeResponse(params, HttpServletResponse.SC_OK,
                        data.getContentType(), data.getResponse());
            } catch (ServiceException e) {
                throw new ActionException("Unable to get capabilities", e);
            }
        }
    }

/*
    Filters matrices for WMTS layers to only include one for current projection:
    {
        "tileMatrixSet": {
          "identifier": "ETRS-TM35FIN",
          "projection":
          ...
        },
        ...
    }
 */
    private String getCapabilitiesJSON(OskariLayer layer, String crs) throws ActionException {
        if (!OskariLayer.TYPE_WMTS.equals(layer.getType())) {
            return layer.getCapabilities().toString();
        }
        try {
            String capsJSON = layer.getCapabilities().toString();
            LayerCapabilitiesWMTS caps = CapabilitiesService.fromJSON(layer.getCapabilities().toString(), OskariLayer.TYPE_WMTS);
            TileMatrixLink link = caps.getTileMatrixLinks().stream()
                    .filter(l -> crs.equals(l.getTileMatrixSet().getShortCrs()))
                    .findFirst()
                    .orElseThrow(() -> new ActionParamsException("No tilematrix matching srs: " + crs));

            // Make a copy so we don't mutate layer in cache
            JSONObject modifiedCapabilities = new JSONObject(capsJSON);
            // remove "tileMatrixLinks" (with all matrices) that is replaced with "tileMatrixSet" (just for current projection)
            modifiedCapabilities.remove("tileMatrixLinks");
            modifiedCapabilities.put("tileMatrixSet", link.getTileMatrixSet().getAsJSON());
            return modifiedCapabilities.toString();
        } catch (Exception e) {
            throw new ActionParamsException("Unable to parse JSON", e);
        }
    }
}
