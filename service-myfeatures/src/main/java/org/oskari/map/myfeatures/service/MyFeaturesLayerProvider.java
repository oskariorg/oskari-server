package org.oskari.map.myfeatures.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.service.OskariComponentManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;
import org.oskari.domain.map.FeatureProperties;
import org.oskari.domain.map.LayerExtendedOutput;
import org.oskari.domain.map.LayerOutput;
import org.oskari.service.maplayer.DescribeLayerQuery;
import org.oskari.service.maplayer.LayerProvider;
import org.oskari.user.User;

@Oskari("myfeatures")
public class MyFeaturesLayerProvider extends LayerProvider {

    private MyFeaturesService service;

    private MyFeaturesService getService() {
        // Lazy init service, doesn't work done in ctor/init()
        // due to some timing of components being initialized
        if (service == null) {
            service = OskariComponentManager.getComponentOfType(MyFeaturesService.class);
        }
        return service;
    }

    public void setService(MyFeaturesService service) {
        this.service = service;
    }

    @Override
    public boolean maybeProvides(String layerId) {
        return layerId.startsWith(MyFeaturesLayer.PREFIX_LAYER_ID);
    }

    @Override
    public List<LayerOutput> listLayers(User user, String lang) {
        List<MyFeaturesLayer> layers = getService().getLayersByOwnerUuid(user.getUuid());
        return layers.stream().map(l -> toLayerOutput(l, lang)).collect(Collectors.toList());
    }

    private static LayerOutput toLayerOutput(MyFeaturesLayer layer, String language) {
        LayerOutput out = new LayerOutput();
        out.id = layer.getId().toString();
        out.type = layer.getType();
        out.name = layer.getName(language);
        out.metadataUuid = null;
        out.dataproviderId = null;
        out.created = layer.getCreated() != null ? new Date(layer.getCreated().toEpochMilli()) : null;
        out.updated = layer.getUpdated() != null ? new Date(layer.getUpdated().toEpochMilli()) : null;
        return out;
    }

    @Override
    public LayerExtendedOutput describeLayer(DescribeLayerQuery query) throws SecurityException {
        UUID layerUuid = MyFeaturesLayer.parseLayerId(query.getLayerId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid layerId"));

        MyFeaturesService service = getService();

        MyFeaturesLayer layer = service.getLayer(layerUuid);
        if (layer == null) {
            return null;
        }
        if (layer.getOwnerUuid().equals(query.getUser().getUuid()) || layer.isPublished()) {
            throw new SecurityException("User doesn't have permissions for requested layer");
        }

        LayerExtendedOutput describe = new LayerExtendedOutput();
        describe.id = query.getLayerId();
        describe.type = OskariLayer.TYPE_WFS;
        describe.name = layer.getName(query.getLang());
        describe.metadataUuid = null;
        describe.dataproviderId = null;
        describe.created = layer.getCreated() == null ? null : new Date(layer.getCreated().toEpochMilli());
        describe.updated = layer.getUpdated() == null ? null : new Date(layer.getUpdated().toEpochMilli());

        describe.coverage = getCoverageWKT(layer.getExtent(), query.getCrs());
        describe.styles = getVectorStyles(layer);
        describe.hover = null;
        describe.capabilities = null;

        describe.properties = getProperties(layer, query.getLang());
        describe.controlData = null;

        return describe;
    }

    private String getCoverageWKT(Envelope extent, CoordinateReferenceSystem crs) {
        if (extent == null || crs == null) {
            return null;
        }
        try {
            CoordinateReferenceSystem sourceCRS = getService().getNativeCRS();
            if (CRS.equalsIgnoreMetadata(sourceCRS, crs)) {
                return WKTHelper.getBBOX(extent.getMinX(), extent.getMinY(), extent.getMaxX(), extent.getMaxY());
            }
            Polygon p = toGeometry(extent);
            Geometry transformed = WKTHelper.transform(p, sourceCRS, crs);
            return new WKTWriter(2).write(transformed);
        } catch (Exception ignore) {
            // Will not report back coverage if transform failed
            // that's ok since client can't handle it if it's in unknown projection
            return null;
        }
    }

    public static Polygon toGeometry(Envelope e) {
        if (e == null) {
            return null;
        }
        Coordinate[] cornerCoordinates = new Coordinate[] {
            new Coordinate(e.getMinX(), e.getMinY()),
            new Coordinate(e.getMinX(), e.getMaxY()),
            new Coordinate(e.getMaxX(), e.getMaxY()),
            new Coordinate(e.getMaxX(), e.getMinY()),
            new Coordinate(e.getMinX(), e.getMinY()),
        };
        return new GeometryFactory().createPolygon(cornerCoordinates);
    }

    static List<VectorStyle> getVectorStyles(MyFeaturesLayer layer) {
        JSONObject defaultStyle = layer.getLayerOptions().getDefaultStyle();

        VectorStyle vectorStyle = new VectorStyle();
        vectorStyle.setType(VectorStyle.TYPE_OSKARI);
        vectorStyle.setName("default");
        vectorStyle.setStyle(defaultStyle);

        return Collections.singletonList(vectorStyle);
    }

    private List<FeatureProperties> getProperties(MyFeaturesLayer layer, String lang) {
        List<FeatureProperties> props = new ArrayList<>();

        int i = 0;
        for (MyFeaturesFieldInfo field : layer.getLayerFields()) {
            FeatureProperties p = new FeatureProperties();
            p.name = field.getName();
            p.type = field.getType().getSimpleType();
            p.rawType = field.getType().getOutputBinding().getName();
            p.label = field.getName();
            p.hidden = false;
            p.format = null;
            p.order = i++;
            props.add(p);
        }

        return props;
    }

}
