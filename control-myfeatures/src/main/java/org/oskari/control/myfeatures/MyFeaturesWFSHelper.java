package org.oskari.control.myfeatures;

import fi.nls.oskari.annotation.Oskari;
import org.oskari.user.User;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.oskari.geojson.GeoJSONFeatureCollection;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.map.myfeatures.service.MyFeaturesServiceMybatisImpl;
import org.oskari.service.user.UserLayerService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Oskari
public final class MyFeaturesWFSHelper extends UserLayerService {

    public static final String PREFIX_MYFEATURES = "myfeatures_";
    public static final String GEOM_PROP_NAME = "geom";
    public static final String CREATED_PROP_NAME = "created";
    public static final String UPDATED_PROP_NAME = "updated";

    private MyFeaturesService service;

    public MyFeaturesWFSHelper() {
        init();
    }

    @Override
    public void init() {
        service = OskariComponentManager.getComponentOfType(MyFeaturesServiceMybatisImpl.class);
    }

    @Override
    public boolean isUserContentLayer(String layerId) {
        return layerId.startsWith(PREFIX_MYFEATURES);
    }

    @Override
    public boolean hasViewPermission(String fullLayerId, User user) {
        MyFeaturesLayer layer = getLayer(parseLayerId(fullLayerId));
        return layer != null && (layer.getOwnerUuid().equals(user.getUuid()) || layer.isPublished());
    }

    @Override
    public WFSLayerOptions getWFSLayerOptions(String layerId) {
        MyFeaturesLayer layer = getLayer(layerId);
        return layer != null ? layer.getLayerOptions() : null;
    }

    @Override
    public SimpleFeatureCollection getFeatures(String fullLayerId, OskariLayer layer, Envelope bbox) throws ServiceException {
        try {
            UUID layerId = parseLayerId(fullLayerId);
            MyFeaturesLayer featuresLayer = getLayer(layerId);
            List<MyFeaturesFeature> features = service.getFeaturesByBbox(layerId, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
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
            b.add(field.getName(), field.getType());
        }
        return b.buildFeatureType();
    }

    private static UUID parseLayerId(String fullLayerId) {
        return UUID.fromString(fullLayerId.substring(PREFIX_MYFEATURES.length()));
    }

    private MyFeaturesLayer getLayer(String fullLayerId) {
        return getLayer(parseLayerId(fullLayerId));
    }

    private MyFeaturesLayer getLayer(UUID layerId) {
        return service.getLayer(layerId);
    }

    @Override
    public int getBaselayerId() {
        // MyFeatureLayers don't have a base OskariLayer
        return -1;
    }

}
