package org.oskari.service.maplayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.json.JSONObject;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.MetadataHelper;
import org.oskari.capabilities.ogc.LayerCapabilitiesOGC;
import org.oskari.capabilities.ogc.LayerCapabilitiesWFS;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMTS;
import org.oskari.capabilities.ogc.wfs.FeaturePropertyType;
import org.oskari.capabilities.ogc.wmts.TileMatrixLink;
import org.oskari.domain.map.FeatureProperties;
import org.oskari.domain.map.LayerExtendedOutput;
import org.oskari.domain.map.LayerOutput;
import org.oskari.maplayer.util.OskariLayerUtil;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;
import org.oskari.service.util.ServiceFactory;
import org.oskari.user.User;

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
import fi.nls.oskari.service.capabilities.CapabilitiesConstants;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.WFSConversionHelper;

public class OskariMapLayerProvider extends LayerProvider {

    private static final Logger LOG = LogFactory.getLogger(OskariMapLayerProvider.class);

    // Lazy init service, doesn't work done in ctor/init()
    private OskariLayerService layerService;
    private PermissionService permissionService;
    private VectorStyleService vectorStyleService;

    private Set<String> preferredSRS;

    private OskariLayerService getLayerService() {
        if (layerService == null) {
            layerService = ServiceFactory.getMapLayerService();
        }
        return layerService;
    }

    private PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = ServiceFactory.getPermissionsService();
        }
        return permissionService;
    }

    private VectorStyleService getVectorStyleService() {
        if (vectorStyleService == null) {
            vectorStyleService = OskariComponentManager.getComponentOfType(VectorStyleService.class);
        }
        return vectorStyleService;
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

    public void setLayerService(OskariLayerService layerService) {
        this.layerService = layerService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public boolean maybeProvides(String layerId) {
        try {
            Integer.parseInt(layerId);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public List<LayerOutput> listLayers(User user, String lang) {
        OskariLayerService layerService = getLayerService();
        PermissionService permissionService = getPermissionService();

        return OskariLayerUtil.getLayersForUser(layerService, permissionService, user, false).stream()
            .map(l -> toLayerOutput(l, lang))
            .collect(Collectors.toList());
    }

    @Override
    public LayerExtendedOutput describeLayer(DescribeLayerQuery query)
            throws SecurityException {
        int id = Integer.parseInt(query.getLayerId());

        OskariLayer layer = getLayerService().find(id);
        if (layer == null) {
            return null;
        }

        Resource resource = getPermissionService().findResource(ResourceType.maplayer, Integer.toString(layer.getId()))
            .orElseThrow(() -> new SecurityException("User doesn't have permissions for requested layer"));

        final boolean hasPermission =
            resource.hasPermission(query.getUser(), PermissionType.VIEW_LAYER) ||
            resource.hasPermission(query.getUser(), PermissionType.VIEW_PUBLISHED);

        if (!hasPermission) {
            throw new SecurityException("User doesn't have permissions for requested layer");
        }

        return toExtendedLayerOutput(layer, query);
    }

    private static LayerOutput toLayerOutput(OskariLayer layer, String lang) {
        LayerOutput out = new LayerOutput();
        out.id = Integer.toString(layer.getId());
        out.type = layer.getType();
        out.name = layer.getName(lang);
        out.metadataUuid = getMetadataUuid(layer);
        out.dataproviderId = layer.getDataproviderId();
        out.created = layer.getCreated();
        out.updated = layer.getUpdated();
        return out;
    }

    private static String getMetadataUuid(OskariLayer layer) {
        String fixed = MetadataHelper.getIdFromMetadataUrl(layer.getMetadataId());
        if (fixed != null) {
            return fixed;
        }
        String olderMetadataCaps = layer.getCapabilities().optString(CapabilitiesConstants.KEY_METADATA, null);
        if (olderMetadataCaps != null && !olderMetadataCaps.trim().isEmpty()) {
            return olderMetadataCaps;
        }
        return layer.getCapabilities().optString(LayerCapabilitiesOGC.METADATA_UUID, null);
    }

    private LayerExtendedOutput toExtendedLayerOutput(OskariLayer layer, DescribeLayerQuery query) throws IllegalArgumentException {
        final int layerId = layer.getId();
        final String layerType = layer.getType();

        LayerExtendedOutput output = new LayerExtendedOutput();
        output.id = Integer.toString(layerId);
        output.name = layer.getName(query.getLang());
        JSONObject attributes = layer.getAttributes();
        if (!attributes.optBoolean(LayerJSONFormatter.KEY_ATTRIBUTE_IGNORE_COVERAGE, false)) {
            output.coverage = getCoverageWKT(layer.getGeometry(), query.getCrs());
        }
        if (VectorStyleHelper.isVectorLayer(layer)) {
            output.styles = getVectorStyles(query);
            output.hover = JSONHelper.getObjectAsMap(layer.getOptions().optJSONObject("hover"));
        }
        if (OskariLayer.TYPE_WFS.equals(layerType)) {
            setDetailsForWFS(output, layer, query.getLang());
        }
        if (OskariLayer.TYPE_WMTS.equals(layerType)) {
            output.capabilities = getCapabilitiesJSON(layer, query.getCrs());
        }
        return output;
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

    private Map<String, Object> getPropertyFormat(JSONObject format, String name) {
        JSONObject propFormat = format.optJSONObject(name);
        // return null if doesn't exist
        return propFormat != null ? JSONHelper.getObjectAsMap(propFormat) : null;
    }

    private Map<String, Object> getControlData(LayerCapabilitiesWFS caps, WFSLayerAttributes attr, WFSLayerOptions opts) {
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

    private List<VectorStyle> getVectorStyles(DescribeLayerQuery query) {
        VectorStyleService service = getVectorStyleService();
        final long userId = query.getUser().getId();
        List<VectorStyle> styles = service.getStyles(userId, Integer.parseInt(query.getLayerId()));

        if (query.getStyles() == null || query.getStyles().isEmpty()) {
            return styles;
        }
        // attach any styles that were specifically requested by the frontend
        for (String style : query.getStyles()) {
            int styleId = ConversionHelper.getInt(style, -1);
            if (styleId == -1) {
                // not a number, we only care about numbers as other styles are not saved in vector styles
                continue;
            }
            boolean isPresent = styles.stream().filter(s -> s.getId() == styleId).findFirst().isPresent();
            if (isPresent) {
                // already referenced in styles -> don't need to add it again
                continue;
            }
            VectorStyle selected = service.getStyleById(styleId);
            if (selected == null) {
                LOG.info("Requested selected style with id:", styleId, "which doesn't exist");
            } else {
                styles.add(selected);
            }
        }
        return styles;
    }

    // value will be not added if transform failed, that's ok since client can't handle it if it's in unknown projection
    private String getCoverageWKT(final String wktWGS84, CoordinateReferenceSystem mapCrs) {
        if (wktWGS84 == null || wktWGS84.isEmpty() || mapCrs == null) {
            return null;
        }
        try {
            // WTK is saved as EPSG:4326 in database
            return WKTHelper.transformLayerCoverage(wktWGS84, mapCrs);
        } catch (Exception ex) {
            LOG.debug("Error transforming coverage to", mapCrs.getName().toString(), "from", wktWGS84);
        }
        return null;
    }

    private void setDetailsForWFS(LayerExtendedOutput output, OskariLayer layer, String lang) {
        // UserDataLayers are handled in frontend by WFS plugin and embedded myplaces is using WFS type
        // so LayerJSONFormatterUSERDATA gathers values from options and attributes in same way than this
        LayerCapabilitiesWFS caps = CapabilitiesService.fromJSON(layer.getCapabilities().toString(), layer.getType());
        WFSLayerAttributes attr = new WFSLayerAttributes(layer.getAttributes());
        WFSLayerOptions opts = new WFSLayerOptions(layer.getOptions());
        output.properties = getProperties(caps, attr, lang);
        output.controlData = getControlData(caps, attr, opts);
    }

    private Map<String, Object> getCapabilitiesJSON(OskariLayer layer, CoordinateReferenceSystem crs) throws IllegalArgumentException {
        if (!OskariLayer.TYPE_WMTS.equals(layer.getType())) {
            return JSONHelper.getObjectAsMap(layer.getCapabilities());
        }
        try {
            String capsJSON = layer.getCapabilities().toString();
            LayerCapabilitiesWMTS caps = CapabilitiesService.fromJSON(layer.getCapabilities().toString(), OskariLayer.TYPE_WMTS);
            String crsName = CapabilitiesService.shortSyntaxEpsg(crs.getName().toString());
            TileMatrixLink link = determineTileMatrix(caps, crsName);

            // Make a copy so we don't mutate layer in cache
            JSONObject modifiedCapabilities = new JSONObject(capsJSON);
            // remove "tileMatrixLinks" (with all matrices) that is replaced with "tileMatrixSet" (just for current projection)
            modifiedCapabilities.remove("tileMatrixLinks");
            modifiedCapabilities.put("tileMatrixSet", link.getTileMatrixSet().getAsJSON());
            return JSONHelper.getObjectAsMap(modifiedCapabilities);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse JSON", e);
        }
    }

    private TileMatrixLink determineTileMatrix(LayerCapabilitiesWMTS caps, String crs) throws IllegalArgumentException {
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
                .orElseThrow(() -> new IllegalArgumentException("None of preferred SRS (" + LOG.getAsString(prefSRS) + ") supported by layer: " + LOG.getAsString(supportedSRS)));

        return caps.getTileMatrixLinks().stream()
                .filter(l -> matchedSRS.equals(l.getTileMatrixSet().getShortCrs()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No tilematrix matching srs: " + matchedSRS));
    }

}
