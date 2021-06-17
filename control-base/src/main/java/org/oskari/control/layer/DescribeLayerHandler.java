package org.oskari.control.layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionCommonException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;
import org.oskari.control.layer.model.LayerExtendedOutput;
import org.oskari.control.layer.model.LayerOutput;
import org.oskari.permissions.PermissionService;

import static fi.nls.oskari.control.ActionConstants.PARAM_ID;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

/**
 * An action route that returns metadata for layers
 */
@OskariActionRoute("DescribeLayer")
public class DescribeLayerHandler extends RestActionHandler {
    private PermissionHelper permissionHelper;
    private final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger LOG = LogFactory.getLogger(DescribeLayerHandler.class);

    @Override
    public void init() {
        if (permissionHelper != null) {
            return;
        }
        try {
            final OskariLayerService layerService = OskariComponentManager.getComponentOfType(OskariLayerService.class);
            final PermissionService permissionService = OskariComponentManager.getComponentOfType(PermissionService.class);
            permissionHelper = new PermissionHelper(layerService, permissionService);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Exception occurred while initializing map layer service", e);
        }
    }

    public void setPermissionHelper(PermissionHelper helper) {
        permissionHelper = helper;
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final int layerId = params.getRequiredParamInt(PARAM_ID);
        final OskariLayer layer = permissionHelper.getLayer(layerId, params.getUser());
        final String crs = params.getHttpParam(PARAM_SRS);
        LayerExtendedOutput output = getLayerDetails(layer, params.getLocale().getLanguage(), crs);

        writeResponse(params, output);
    }

    private void writeResponse(ActionParameters params, LayerOutput output) throws ActionCommonException {
        try {
            ResponseHelper.writeResponse(params, MAPPER.writeValueAsString(output));
        } catch (Exception e) {
            throw new ActionCommonException("Error writing response", e);
        }
    }

    private LayerExtendedOutput getLayerDetails(OskariLayer layer, String lang, String crs) {
        LayerExtendedOutput output = new LayerExtendedOutput();
        output.id = layer.getId();
        output.name = layer.getName(lang);
        JSONObject attributes = layer.getAttributes();
        if (!attributes.optBoolean(LayerJSONFormatter.KEY_ATTRIBUTE_IGNORE_COVERAGE, false)) {
            output.coverage = getCoverageWKT(layer.getGeometry(), crs);
        }
        return output;
    }

    // value will be not added if transform failed, that's ok since client can't handle it if it's in unknown projection
    private String getCoverageWKT(final String wktWGS84, final String mapSRS) {
        if (wktWGS84 == null || wktWGS84.isEmpty() || mapSRS == null || mapSRS.isEmpty()) {
            return null;
        }
        try {
            // WTK is saved as EPSG:4326 in database
            return WKTHelper.transformLayerCoverage(wktWGS84, mapSRS);
        } catch (Exception ex) {
            LOG.debug("Error transforming coverage to", mapSRS, "from", wktWGS84);
        }
        return null;
    }
}
