package org.oskari.control.myfeatures;

import fi.nls.oskari.annotation.Oskari;
import org.oskari.user.User;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.domain.map.FeatureProperties;
import org.oskari.domain.map.LayerExtendedOutput;
import org.oskari.geojson.GeoJSONFeatureCollection;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.service.user.UserLayerService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Oskari
public final class MyFeaturesWFSHelper extends UserLayerService {

    public static final String GEOM_PROP_NAME = "geom";
    public static final String CREATED_PROP_NAME = "created";
    public static final String UPDATED_PROP_NAME = "updated";

    private MyFeaturesService service;

    private MyFeaturesService getService() {
        // Lazy init service, doesn't work done in ctor/init()
        // due to some timing of components being initialized
        if (service == null) {
            service = OskariComponentManager.getComponentOfType(MyFeaturesService.class);
        }
        return service;
    }

    @Override
    public boolean isUserContentLayer(String layerId) {
        return layerId.startsWith(MyFeaturesLayer.PREFIX_LAYER_ID);
    }

    @Override
    public boolean hasViewPermission(String fullLayerId, User user) {
        MyFeaturesLayer layer = getLayer(parseLayerId(fullLayerId));
        return layer != null && (layer.getOwnerUuid().equals(user.getUuid()) || layer.isPublished());
    }

    @Override
    public SimpleFeatureCollection getFeatures(String fullLayerId, Envelope bbox) throws ServiceException {
        try {
            UUID layerId = parseLayerId(fullLayerId);
            MyFeaturesLayer featuresLayer = getLayer(layerId);
            List<MyFeaturesFeature> features = getService().getFeaturesByBbox(layerId, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
            return convertToSimpleFeatureCollection(featuresLayer, features);
        } catch(Exception e) {
            throw new ServiceException("Failed to get features", e);
        }
    }

    private static SimpleFeatureCollection convertToSimpleFeatureCollection(MyFeaturesLayer layer, List<MyFeaturesFeature> features) {
        List<MyFeaturesFieldInfo> fields = layer.getLayerFields();
        SimpleFeatureType ft = createType(fields);
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(ft);
        List<SimpleFeature> list = features.stream()
            .map(f -> convertToSimpleFeature(fields, b, f))
            .collect(Collectors.toList());
        return new GeoJSONFeatureCollection(list, ft);
    }

    private static SimpleFeature convertToSimpleFeature(List<MyFeaturesFieldInfo> fields, SimpleFeatureBuilder b, MyFeaturesFeature feature) {
        JSONObject properties = feature.getProperties();
        b.reset();
        // geom, created, updated
        b.set(0, feature.getGeometry());
        b.set(1, feature.getCreated());
        b.set(2, feature.getUpdated());
        for (int i = 0; i < fields.size(); i++) {
            MyFeaturesFieldInfo field = fields.get(i);
            b.set(i + 3, properties.opt(field.getName()));
        }
        return b.buildFeature(feature.getFid());
    }

    private static SimpleFeatureType createType(List<MyFeaturesFieldInfo> fields) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("myfeatures");
        b.setNamespaceURI("https://oskari.org");
        b.add(GEOM_PROP_NAME, Geometry.class);
        b.add(CREATED_PROP_NAME, OffsetDateTime.class);
        b.add(UPDATED_PROP_NAME, OffsetDateTime.class);
        b.setDefaultGeometry(GEOM_PROP_NAME);
        for (MyFeaturesFieldInfo field : fields) {
            b.add(field.getName(), field.getType().getOutputBinding());
        }
        return b.buildFeatureType();
    }

    private static UUID parseLayerId(String fullLayerId) {
        return MyFeaturesLayer.parseLayerId(fullLayerId).get();
    }

    private MyFeaturesLayer getLayer(UUID layerId) {
        return getService().getLayer(layerId);
    }

    @Override
    public WFSLayerOptions getWFSLayerOptions(String layerId) {
        return getLayer(parseLayerId(layerId)).getLayerOptions();
    }

    @Override
    public OskariLayer getOskariLayer(String layerId) {
        MyFeaturesLayer myLayer = getLayer(parseLayerId(layerId));
        OskariLayer layer = new OskariLayer();
        layer.setId(-1);
        layer.setType(OskariLayer.TYPE_MYFEATURES);
        layer.setInternal(true);
        layer.setName(layerId);
        layer.setOptions(myLayer.getOptions());
        layer.setAttributes(myLayer.getAttributes());
        return layer;
    }

    @Override
    public LayerExtendedOutput describeLayer(String layerId, String lang, CoordinateReferenceSystem crs) {
        MyFeaturesLayer layer = getLayer(parseLayerId(layerId));

        LayerExtendedOutput describe = new LayerExtendedOutput();
        describe.id = layerId;
        describe.type = OskariLayer.TYPE_WFS;
        describe.name = layer.getName(lang);
        describe.metadataUuid = null;
        describe.dataproviderId = null;
        describe.created = layer.getCreated() == null ? null : new Date(layer.getCreated().toEpochMilli());
        describe.updated = layer.getUpdated() == null ? null : new Date(layer.getUpdated().toEpochMilli());

        describe.coverage = getCoverageWKT(layer.getExtent(), crs);
        describe.styles = getVectorStyles(layer);
        describe.hover = null;
        describe.capabilities = null;

        describe.properties = getProperties(layer, lang);
        describe.controlData = null;

        return describe;
    }

    static List<VectorStyle> getVectorStyles(MyFeaturesLayer layer) {
        JSONObject defaultStyle = layer.getLayerOptions().getDefaultStyle();

        VectorStyle vectorStyle = new VectorStyle();
        vectorStyle.setType(VectorStyle.TYPE_OSKARI);
        vectorStyle.setName("default");
        vectorStyle.setStyle(defaultStyle);

        return Collections.singletonList(vectorStyle);
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
