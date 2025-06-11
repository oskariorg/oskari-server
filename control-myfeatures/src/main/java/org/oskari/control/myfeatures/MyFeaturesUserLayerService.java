package org.oskari.control.myfeatures;

import fi.nls.oskari.annotation.Oskari;
import org.oskari.user.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
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
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.Expression;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSONFeatureCollection;
import org.oskari.service.user.UserLayerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Oskari
public class MyFeaturesUserLayerService extends UserLayerService {

    public static final String PREFIX_MYFEATURES = "myfeatures_";

    protected static final String USERLAYER_ATTR_GEOMETRY = "geometry";
    private static final String USERLAYER_ATTR_USER_LAYER_ID = "user_layer_id";
    private static final String USERLAYER_ATTR_PROPERTY_JSON = "property_json";

    private FilterFactory ff;
    private int userlayerLayerId;
    private UserLayerDbService service;
    private OskariWFSClient wfsClient = new CachingOskariWFSClient();

    public MyFeaturesUserLayerService() {
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
    public SimpleFeatureCollection getFeatures(String layerId, OskariLayer layer, ReferencedEnvelope bbox, CoordinateReferenceSystem crs) throws ServiceException {
        try {
            int id = parseId(layerId);
            SimpleFeatureCollection featureCollection = service.getFeatures(id, bbox, crs);
            return postProcess(featureCollection);
        } catch(Exception e) {
            throw new ServiceException("Failed to get features. ", e);
        }
    }
}
