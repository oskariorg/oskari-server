package org.oskari.control.userlayer;

import java.util.*;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;

import org.oskari.geojson.GeoJSONFeatureCollection;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.service.user.UserLayerService;

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

    @SuppressWarnings("unchecked")
    public SimpleFeatureCollection postProcess(SimpleFeatureCollection sfc) throws Exception {
        if (sfc.isEmpty()) {
            // return early as no need for processing and getSchema() throws npe if we move forward
            return sfc;
        }
        List<SimpleFeature> fc = new ArrayList<>();
        SimpleFeatureType schema;

        String geomAttrName = sfc.getSchema().getGeometryDescriptor().getLocalName();

        try (SimpleFeatureIterator it = sfc.features()) {
            SimpleFeature firstFeatureForSchemaGeneration = it.next();
            // parse the _first feature_ AND _generate schema_ based on it
            String property_json_for_first = (String) firstFeatureForSchemaGeneration.getAttribute(USERLAYER_ATTR_PROPERTY_JSON);
            JSONObject properties_for_first = new JSONObject(property_json_for_first);
            Set<String> featureAttributeNames = JSONHelper.getObjectAsMap(properties_for_first).keySet();

            schema = createType(sfc.getSchema(), properties_for_first);
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);

            // process and add the the first feature to the result collection
            builder.set(geomAttrName, firstFeatureForSchemaGeneration.getDefaultGeometry());
            for (String attrName : featureAttributeNames) {
                builder.set(attrName, properties_for_first.get(attrName));
            }
            fc.add(builder.buildFeature(firstFeatureForSchemaGeneration.getID()));

            // process and add the _remaining features_ to the result collection
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                builder.set(geomAttrName, feature.getDefaultGeometry());
                String property_json = (String) feature.getAttribute(USERLAYER_ATTR_PROPERTY_JSON);
                JSONObject properties = new JSONObject(property_json);
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

}
