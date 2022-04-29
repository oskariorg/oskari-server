package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesConstants;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.RawCapabilitiesResponse;
import org.oskari.permissions.PermissionService;
import org.oskari.service.util.ServiceFactory;

import java.nio.charset.StandardCharsets;

/**
 * Returns the Capabilities for layer in XML format if the user has permissions for requested layer.
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

  "layerSpecific": {
    "tileMatrix": [
      {
        "limits": null,
        "tileMatrixSet": {
          "identifier": "ETRS-TM35FIN",
          "projection":
 */
    private String getCapabilitiesJSON(OskariLayer layer, String crs) throws ActionException {
        try {
            JSONObject modifiedCapabilities = new JSONObject(layer.getCapabilities().toString());
            // modify and remove matrices that are not used for current projection
            JSONArray linkList = (JSONArray) modifiedCapabilities.remove("tileMatrices");
            JSONObject link = null;
            for (int i = 0; i < linkList.length(); i++) {
                link = linkList.optJSONObject(i);
                JSONObject tileMatrixSet = link.optJSONObject("tileMatrixSet");
                String projection = tileMatrixSet.optString("projection");
                String shortProj = ProjectionHelper.shortSyntaxEpsg(projection);
                if (crs.equals(shortProj)) {
                    // use the first tilematrix matching the projection that is used
                    // clone it so we don't accidentally modify cached object
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
            // add the tilematrix link/data for current projection
            JSONArray filteredLinkList = new JSONArray();
            filteredLinkList.put(link);
            modifiedCapabilities.put("links", filteredLinkList);
            return modifiedCapabilities.toString();
        } catch (Exception e) {
            throw new ActionParamsException("Unable to parse JSON", e);
        }
    }
}
