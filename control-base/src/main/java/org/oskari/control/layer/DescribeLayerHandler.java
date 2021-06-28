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
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;
import org.oskari.control.layer.model.LayerExtendedOutput;
import org.oskari.control.layer.model.LayerOutput;
import org.oskari.permissions.PermissionService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.nls.oskari.control.ActionConstants.PARAM_ID;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;
import static fi.nls.oskari.domain.map.OskariLayer.PROPERTY_AJAXURL;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_ISQUERYABLE;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_VERSION;

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
        LayerOutput output = getLayerDetails(layer, params.getLocale().getLanguage(), crs);

        writeResponse(params, output);
    }

    private void writeResponse(ActionParameters params, LayerOutput output) throws ActionCommonException {
        try {
            ResponseHelper.writeResponse(params, MAPPER.writeValueAsString(output));
        } catch (Exception e) {
            throw new ActionCommonException("Error writing response", e);
        }
    }

    private LayerOutput getLayerDetails(OskariLayer layer, String lang, String crs) {
        LayerOutput output = new LayerExtendedOutput();
        output.id = layer.getId();
        output.name = layer.getName();
        output.version = layer.getVersion();
        if (output.version == null) {
            output.version = layer.getCapabilities().optString(KEY_VERSION);
        }
        if (useProxy(layer)) {
            output.url = getProxyUrl(layer);
        } else {
            output.url = layer.getUrl();
        }
        output.type = layer.getType();
        if (layer.isCollection()) {
            // fixing frontend type for collection layers
            output.type = layer.isBaseMap() ? "base" : "groupMap";
        }
        output.title = layer.getName(lang);
        output.dataprovider = layer.getDataproviderId();
        output.opacity = layer.getOpacity();
        output.minScale = layer.getMinScale();
        output.maxScale = layer.getMaxScale();
        // TODO: move refreshRate & realtime to options?
        output.refreshRate = layer.getRefreshRate();
        output.realtime = layer.getRealtime();
        output.baseLayerId = layer.getParentId();
        output.options = JSONHelper.getObjectAsMap(layer.getOptions());
        output.params = JSONHelper.getObjectAsMap(layer.getParams());
        output.metadataId = LayerJSONFormatter.getMetadataUuid(layer);
        output.srs = LayerJSONFormatter.getSRSs(layer.getAttributes(), layer.getCapabilities());
        output.created = layer.getCreated();

        output.sublayers = layer.getSublayers().stream()
                .map(sublayer -> getLayerDetails(sublayer, lang, crs))
                .collect(Collectors.toList());

        // cast for convenience so it's easier to separate between base info and extended in the IDE
        LayerExtendedOutput extended = (LayerExtendedOutput) output;
        // extended metadata
        extended.desc = layer.getTitle(lang);
        extended.gfiContent = layer.getGfiContent();
        JSONObject attributes = layer.getAttributes();
        if (!attributes.optBoolean(LayerJSONFormatter.KEY_ATTRIBUTE_IGNORE_COVERAGE, false)) {
            extended.coverage = getCoverageWKT(layer.getGeometry(), crs);
        }
        extended.attributes = JSONHelper.getObjectAsMap(attributes);

        if (attributes.has(KEY_ISQUERYABLE)) {
            // attributes can be used to force GFI for layer even if capabilities allow it or enable it not
            extended.isQueryable = attributes.optBoolean(KEY_ISQUERYABLE);
        } else {
            extended.isQueryable = layer.getCapabilities().optBoolean(KEY_ISQUERYABLE);
        }
        return cleanupModel(extended);
    }

    /**
     * Sets default/empty values to null so they don't get written on the JSON output
     * @param output
     * @return
     */
    private LayerOutput cleanupModel(LayerOutput output) {

        if (output.opacity == 100) {
            output.opacity = null;
        }
        if (output.minScale == -1) {
            output.minScale = null;
        }
        if (output.maxScale == -1) {
            output.maxScale = null;
        }
        if (output.refreshRate == 0) {
            output.refreshRate = null;
        }
        if (output.baseLayerId == -1) {
            output.baseLayerId = null;
        }
        if (!output.realtime) {
            output.realtime = null;
        }

        output.sublayers = output.sublayers.stream()
                .map(sublayer -> cleanupModel(sublayer))
                .collect(Collectors.toList());

        // remove style if there is only one style available?
        if (!(output instanceof LayerExtendedOutput)) {
            // anything beyond this point is extended so we can return early
            return output;
        }

        // cast for convenience so it's easier to separate between base info and extended in the IDE
        LayerExtendedOutput extended = (LayerExtendedOutput) output;

        if (!extended.isQueryable) {
            extended.isQueryable = null;
        }
        return extended;
    }

    protected boolean useProxy(final OskariLayer layer) {
        boolean forceProxy = false;
        if (layer.getAttributes() != null) {
            forceProxy = layer.getAttributes().optBoolean("forceProxy", false);
        }
        return ((layer.getUsername() != null) && (layer.getUsername().length() > 0)) || forceProxy;
    }

    public String getProxyUrl(final OskariLayer layer) {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("action_route", "GetLayerTile");
        urlParams.put(LayerJSONFormatter.KEY_ID, Integer.toString(layer.getId()));
        return IOHelper.constructUrl(PropertyUtil.get(PROPERTY_AJAXURL), urlParams);
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
