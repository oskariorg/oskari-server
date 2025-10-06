package org.oskari.control.layer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.map.style.VectorStyleHelper;
import fi.nls.oskari.map.style.VectorStyleService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.*;
import org.json.JSONObject;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.ogc.LayerCapabilitiesWFS;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMTS;
import org.oskari.capabilities.ogc.wfs.FeaturePropertyType;
import org.oskari.capabilities.ogc.wmts.TileMatrixLink;
import org.oskari.control.layer.model.FeatureProperties;
import org.oskari.control.layer.model.LayerExtendedOutput;
import org.oskari.control.layer.model.LayerOutput;
import org.oskari.permissions.PermissionService;
import org.oskari.service.user.UserLayerService;
import org.oskari.user.User;

import java.util.*;
import java.util.stream.Collectors;

import static fi.nls.oskari.control.ActionConstants.PARAM_ID;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

/**
 * An action route that returns metadata for layers
 */
@OskariActionRoute("DescribeLayer")
public class DescribeLayerHandler extends RestActionHandler {

    private static final String ERR_INVALID_ID = "Invalid id";

    private PermissionHelper permissionHelper;
    private Collection<UserLayerService> userContentProcessors;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private Set<String> preferredSRS;
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

        Map<String, UserLayerService> components = OskariComponentManager.getComponentsOfType(UserLayerService.class);
        this.userContentProcessors = components.values();
    }

    public void setPermissionHelper(PermissionHelper helper) {
        permissionHelper = helper;
    }

    private VectorStyleService getVectorStyleService() {
        return OskariComponentManager.getComponentOfType(VectorStyleService.class);
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final String layerId = params.getRequiredParam(PARAM_ID);
        final User user = params.getUser();

        final Optional<UserLayerService> processor = getUserContentProsessor(layerId);
        final OskariLayer layer = findLayer(layerId, user, processor);

        LayerExtendedOutput output = getLayerDetails(params, layer);

        writeResponse(params, output);
    }

    private Optional<UserLayerService> getUserContentProsessor(String layerId) {
        return userContentProcessors.stream()
                .filter(proc -> proc.isUserContentLayer(layerId))
                .findAny();
    }

    private OskariLayer findLayer(String layerId, User user, Optional<UserLayerService> processor) throws ActionException {
        return processor.isPresent()
            ? findUserContentLayer(layerId, user, processor.get())
            : findMapLayer(layerId, user);
    }

    private OskariLayer findUserContentLayer(String layerId, User user, UserLayerService processor) throws ActionDeniedException {
        if (!processor.hasViewPermission(layerId, user)) {
            throw new ActionDeniedException("User doesn't have permissions for requested layer");
        }
        return processor.getOskariLayer(layerId);
    }

    private OskariLayer findMapLayer(String layerId, User user) throws ActionException {
        int id;
        try {
            id = Integer.parseInt(layerId);
        } catch (NumberFormatException e) {
            throw new ActionParamsException(ERR_INVALID_ID);
        }
        return permissionHelper.getLayer(id, user);
    }

    private void writeResponse(ActionParameters params, LayerOutput output) throws ActionCommonException {
        try {
            ResponseHelper.writeResponse(params, MAPPER.writeValueAsString(output));
        } catch (Exception e) {
            throw new ActionCommonException("Error writing response", e);
        }
    }

    private LayerExtendedOutput getLayerDetails(ActionParameters params, OskariLayer layer) throws ActionException {
        final String lang = params.getLocale().getLanguage();
        final String crs = params.getHttpParam(PARAM_SRS);
        final int layerId = layer.getId();
        final String layerType = layer.getType();

        LayerExtendedOutput output = new LayerExtendedOutput();
        output.id = Integer.toString(layerId);
        output.name = layer.getName(lang);
        JSONObject attributes = layer.getAttributes();
        if (!attributes.optBoolean(LayerJSONFormatter.KEY_ATTRIBUTE_IGNORE_COVERAGE, false)) {
            output.coverage = getCoverageWKT(layer.getGeometry(), crs);
        }
        if (VectorStyleHelper.isVectorLayer(layer)) {
            output.styles = getVectorStyles(params, layerId);
            output.hover = JSONHelper.getObjectAsMap(layer.getOptions().optJSONObject("hover"));
        }
        if (OskariLayer.TYPE_WFS.equals(layerType)) {
            setDetailsForWFS(output, layer, lang);
        }
        if (OskariLayer.TYPE_WMTS.equals(layerType)) {
            output.capabilities = getCapabilitiesJSON(layer, crs);
        }
        return output;
    }
    private void setDetailsForWFS (LayerExtendedOutput output, OskariLayer layer, String lang) {
        // UserDataLayers are handled in frontend by WFS plugin and embedded myplaces is using WFS type
        // so LayerJSONFormatterUSERDATA gathers values from options and attributes in same way than this
        LayerCapabilitiesWFS caps = CapabilitiesService.fromJSON(layer.getCapabilities().toString(), layer.getType());
        WFSLayerAttributes attr = new WFSLayerAttributes(layer.getAttributes());
        WFSLayerOptions opts = new WFSLayerOptions(layer.getOptions());
        output.properties = getProperties(caps, attr, lang);
        output.controlData = getControlData(caps, attr, opts);
    }

    private List<FeatureProperties> getProperties(LayerCapabilitiesWFS caps, WFSLayerAttributes attr , String lang) {
        List<FeatureProperties> props = new ArrayList<>();

        List<String> selected = attr.getSelectedAttributes(lang);
        JSONObject locale = attr.getLocalization(lang).orElse(new JSONObject());
        JSONObject format = attr.getFieldFormatMetadata().orElse(new JSONObject());

        caps.getFeatureProperties().stream().forEach(prop -> {
            FeatureProperties p = new FeatureProperties();
            p.name = prop.name;
            p.type = WFSConversionHelper.getSimpleType(prop.type);
            p.rawType = prop.type;
            p.label = locale.optString(prop.name, null);
            p.format = getPropertyFormat(format, prop.name);
            if (!selected.isEmpty()) {
                int index = selected.indexOf(p.name);
                if (index == -1) {
                    p.hidden = true;
                    p.order = selected.size();
                } else {
                    p.order = index;
                }
            }
            props.add(p);
        });
        if (!selected.isEmpty()) {
            props.sort(Comparator.comparingInt(a -> a.order));
        }
        return props;
    }
    private Map<String, Object> getPropertyFormat (JSONObject format, String name) {
        JSONObject propFormat = format.optJSONObject(name);
        // return null if doesn't exist
        return propFormat != null ? JSONHelper.getObjectAsMap(propFormat) : null;
    }

    private Map<String, Object> getControlData (LayerCapabilitiesWFS caps, WFSLayerAttributes attr, WFSLayerOptions opts) {
        Map<String, Object> data = new HashMap<>();

        JSONObject attrData = attr.getAttributesData();
        data.put(WFSLayerAttributes.KEY_NO_DATA_VALUE, attr.getNoDataValue());
        data.put(WFSLayerAttributes.KEY_COMMON_ID, attr.getCommonId());
        data.put(WFSLayerAttributes.KEY_REPLACE_ID, attrData.optString(WFSLayerAttributes.KEY_REPLACE_ID, null));

        String geomType = attrData.optString(WFSLayerAttributes.KEY_GEOMETRY_TYPE, null);
        if (geomType == null) {
            String geomName = caps.getGeometryField();
            FeaturePropertyType fpt = caps.getFeatureProperty(geomName);
            if (fpt != null) {
                geomType = WFSConversionHelper.getStyleType(fpt.type);
            }
        }
        data.put(WFSLayerAttributes.KEY_GEOMETRY_TYPE, geomType);

        data.put(WFSLayerOptions.KEY_RENDER_MODE, opts.getRenderMode());
        data.put(WFSLayerOptions.KEY_CLUSTER, opts.getClusteringDistance());
        return data;
    }

    private List<VectorStyle> getVectorStyles (ActionParameters params, int layerId) {
        VectorStyleService service = getVectorStyleService();
        final long userId = params.getUser().getId();
        List<VectorStyle> styles = service.getStyles(userId, layerId);
        // link params or published map could have selected style which is created by another user
        String styleIdList = params.getHttpParam("styleId");
        if (styleIdList == null || styleIdList.isEmpty()) {
            return styles;
        }
        // attach any styles that were specifically requested by the frontend
        Arrays.stream(styleIdList.split(","))
                .forEach(style -> {
            int styleId = ConversionHelper.getInt(style, -1);
            if (styleId == -1) {
                // not a number, we only care about numbers as other styles are not saved in vector styles
                return;
            }
            boolean isPresent = styles.stream().filter(s -> s.getId() == styleId).findFirst().isPresent();
            if (isPresent) {
                // already referenced in styles -> don't need to add it again
                return;
            }
            VectorStyle selected = service.getStyleById(styleId);
            if (selected == null) {
                LOG.info("Requested selected style with id:", styleId, "which doesn't exist");
            } else {
                styles.add(selected);
            }
        });
        return styles;
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

    private Map<String, Object> getCapabilitiesJSON(OskariLayer layer, String crs) throws ActionException {
        if (!OskariLayer.TYPE_WMTS.equals(layer.getType())) {
            return JSONHelper.getObjectAsMap(layer.getCapabilities());
        }
        try {
            String capsJSON = layer.getCapabilities().toString();
            LayerCapabilitiesWMTS caps = CapabilitiesService.fromJSON(layer.getCapabilities().toString(), OskariLayer.TYPE_WMTS);
            TileMatrixLink link = determineTileMatrix(caps, crs);

            // Make a copy so we don't mutate layer in cache
            JSONObject modifiedCapabilities = new JSONObject(capsJSON);
            // remove "tileMatrixLinks" (with all matrices) that is replaced with "tileMatrixSet" (just for current projection)
            modifiedCapabilities.remove("tileMatrixLinks");
            modifiedCapabilities.put("tileMatrixSet", link.getTileMatrixSet().getAsJSON());
            return JSONHelper.getObjectAsMap(modifiedCapabilities);
        } catch (Exception e) {
            throw new ActionParamsException("Unable to parse JSON", e);
        }
    }

    private TileMatrixLink determineTileMatrix(LayerCapabilitiesWMTS caps, String crs) throws ActionException {
        TileMatrixLink link = caps.getTileMatrixLinks().stream()
                .filter(l -> crs.equals(l.getTileMatrixSet().getShortCrs()))
                .findFirst()
                .orElse(null);
        if (link != null) {
            // happy case, we found a matching tilematrix for projection
            return link;
        }
        // set of srs that layer supports
        Set<String> supportedSRS = caps.getTileMatrixLinks().stream()
                .map(l -> l.getTileMatrixSet().getShortCrs())
                .collect(Collectors.toSet());
        // first of preferred srs that layer supports
        Set<String> prefSRS = getPreferredSRS();
        String matchedSRS = prefSRS.stream()
                .filter(srs -> supportedSRS.contains(srs))
                .findFirst()
                .orElseThrow(() -> new ActionParamsException("None of preferred SRS (" + LOG.getAsString(prefSRS) + ") supported by layer: " + LOG.getAsString(supportedSRS)));

        return caps.getTileMatrixLinks().stream()
                .filter(l -> matchedSRS.equals(l.getTileMatrixSet().getShortCrs()))
                .findFirst()
                .orElseThrow(() -> new ActionParamsException("No tilematrix matching srs: " + matchedSRS));
    }

    private Set<String> getPreferredSRS() {
        if (preferredSRS == null) {
            preferredSRS = new HashSet<>(5);
            String nativeCRS = PropertyUtil.get("oskari.native.srs", "EPSG:3857");
            preferredSRS.add(nativeCRS);
            preferredSRS.add("EPSG:3857");
            preferredSRS.add("EPSG:4326");
            preferredSRS.add("EPSG:900913");
        }
        return preferredSRS;
    }
}
