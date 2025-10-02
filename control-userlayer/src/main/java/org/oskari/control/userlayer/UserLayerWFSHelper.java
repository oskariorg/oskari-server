package org.oskari.control.userlayer;

import fi.nls.oskari.annotation.Oskari;
import org.oskari.user.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.Expression;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSONFeatureCollection;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.service.user.UserLayerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Oskari
public class UserLayerWFSHelper extends UserLayerService {

    public static final String PROP_USERLAYER_BASELAYER_ID = "userlayer.baselayer.id";
    public static final String PREFIX_USERLAYER = "userlayer_";

    protected static final String USERLAYER_ATTR_GEOMETRY = "geometry";
    private static final String USERLAYER_ATTR_USER_LAYER_ID = "user_layer_id";
    private static final String USERLAYER_ATTR_PROPERTY_JSON = "property_json";

    private FilterFactory ff;
    private int userlayerLayerId;
    private UserLayerDbService service;

    public UserLayerWFSHelper() {
        init();
    }

    public void init() {
        this.ff = CommonFactoryFinder.getFilterFactory();
        this.userlayerLayerId = PropertyUtil.getOptional(PROP_USERLAYER_BASELAYER_ID, -2);
    }

    public int getBaselayerId() {
        return userlayerLayerId;
    }

    public boolean isUserlayerLayer(OskariLayer layer) {
        return layer.getId() == userlayerLayerId;
    }

    public boolean isUserContentLayer(String layerId) {
        return layerId.startsWith(PREFIX_USERLAYER);
    }

    public int parseId(String layerId) {
        return Integer.parseInt(layerId.substring(PREFIX_USERLAYER.length()));
    }

    public Filter getWFSFilter(String layerId,ReferencedEnvelope bbox) {
        int userlayerId = parseId(layerId);
        Expression _userlayerId = ff.property(USERLAYER_ATTR_USER_LAYER_ID);

        Filter userlayerIdEquals = ff.equals(_userlayerId, ff.literal(userlayerId));

        Filter bboxFilter = ff.bbox(USERLAYER_ATTR_GEOMETRY,
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY(),
                CRS.toSRS(bbox.getCoordinateReferenceSystem()));

        return ff.and(Arrays.asList(userlayerIdEquals, bboxFilter));
    }

    public SimpleFeatureCollection postProcess(SimpleFeatureCollection sfc) throws Exception {
        if (sfc.isEmpty()) {
            // return early as no need for processing and getSchema() throws npe if we move forward
            return sfc;
        }
        List<SimpleFeature> fc = new ArrayList<>();
        SimpleFeatureType schema = null;

        String geomAttrName = sfc.getSchema().getGeometryDescriptor().getLocalName();

        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                String propertyJson = feature.getAttribute(USERLAYER_ATTR_PROPERTY_JSON).toString();
                JSONObject properties = new JSONObject(propertyJson);
                // use the first feature's featuretype as schema for the final collection
                if (schema == null) {
                    schema = createType(feature.getFeatureType(), properties);
                }
                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(createType(feature.getFeatureType(), properties));
                builder.set(geomAttrName, feature.getDefaultGeometry());
                Set<String> featureAttributeNames = JSONHelper.getObjectAsMap(properties).keySet();
                for (String attrName : featureAttributeNames) {
                    builder.set(attrName, properties.opt(attrName));
                }
                fc.add(builder.buildFeature(feature.getID()));
            }
        }

        return new GeoJSONFeatureCollection(fc, schema);
    }

    public boolean hasViewPermission(String id, User user) {
        UserLayer layer = getLayer(parseId(id));
        if (layer == null) {
            return false;
        }
        return layer.isOwnedBy(user.getUuid()) || layer.isPublished();
    }

    protected UserLayer getLayer(int id) {
        if (service == null) {
            // might cause problems with timing of components being initialized if done in init/constructor
            service = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
        }
        return service.getUserLayerById(id);
    }

    protected OskariLayer getBaseLayer() {
        return UserLayerDataService.getBaseLayer();
    }

    private SimpleFeatureType createType(SimpleFeatureType schema, JSONObject properties) throws JSONException {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName(schema.getName());
        typeBuilder.add(schema.getGeometryDescriptor());
        typeBuilder.setDefaultGeometry(schema.getGeometryDescriptor().getLocalName());
        @SuppressWarnings("unchecked")
        Iterator<String> keys = properties.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object obj = properties.get(key);
            typeBuilder.add(key, obj.getClass());
        }
        return typeBuilder.buildFeatureType();
    }

    @Override
    public SimpleFeatureCollection getFeatures(String layerId, OskariLayer layer, Envelope bbox) throws ServiceException {
        try {
            int id = parseId(layerId);
            SimpleFeatureCollection featureCollection = service.getFeatures(id, bbox);
            return postProcess(featureCollection);
        } catch(Exception e) {
            throw new ServiceException("Failed to get features. ", e);
        }
    }

    @Override
    public WFSLayerOptions getWFSLayerOptions(String layerId) {
        int id = parseId(layerId);
        WFSLayerOptions wfsOpts = getLayer(id).getWFSLayerOptions();
        OskariLayer baseLayer = getBaseLayer();
        JSONObject baseOptions = baseLayer == null ? new JSONObject() : baseLayer.getOptions();
        wfsOpts.injectBaseLayerOptions(baseOptions);
        return wfsOpts;
    }
}
