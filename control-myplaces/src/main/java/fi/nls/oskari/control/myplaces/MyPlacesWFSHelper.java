package fi.nls.oskari.control.myplaces;

import fi.nls.oskari.annotation.Oskari;
import org.oskari.user.User;
import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.service.MyPlacesFeaturesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.Expression;
import org.oskari.geojson.GeoJSONFeatureCollection;
import org.oskari.geojson.GeoJSONReader;
import org.oskari.myplaces.service.mybatis.MyPlacesFeaturesServiceMybatisImpl;
import org.oskari.service.user.UserLayerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Oskari
public class MyPlacesWFSHelper extends UserLayerService {

    public static final String PROP_MYPLACES_BASELAYER_ID = "myplaces.baselayer.id";

    private static final String PREFIX_MYPLACES = "myplaces_";
    private static final String MYPLACES_ATTR_GEOMETRY = "oskari:geometry";
    private static final String MYPLACES_ATTR_CATEGORY_ID = "oskari:category_id";

    private static final List<String> WHITELISTED_PROPERTIES = Arrays.asList(
            "image_url", "link", "name", "place_desc", "attention_text");

    private FilterFactory ff;
    private int myPlacesLayerId;
    private MyPlacesService service;

    private MyPlacesFeaturesService featureService = new MyPlacesFeaturesServiceMybatisImpl();
    public MyPlacesWFSHelper() {
        init();
    }

    public void init() {
        this.ff = CommonFactoryFinder.getFilterFactory();
        this.myPlacesLayerId = PropertyUtil.getOptional(PROP_MYPLACES_BASELAYER_ID, -2);
    }

    public void getLayers(User user) throws ServiceException {
        throw new ServiceException("Not implemented");
    }
    public void getLayer(String layerId, User user) throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    public int getBaselayerId() {
        return myPlacesLayerId;
    }
    protected OskariLayer getBaseLayer() {
        return MyPlacesService.getBaseLayer();
    }

    public boolean isMyPlacesLayer(OskariLayer layer) {
        return layer.getId() == myPlacesLayerId;
    }

    public boolean isUserContentLayer(String layerId) {
        return layerId.startsWith(PREFIX_MYPLACES);
    }

    public int parseId(String layerId) {
        return Integer.parseInt(layerId.substring(PREFIX_MYPLACES.length()));
    }

    public Filter getWFSFilter(String layerId, ReferencedEnvelope bbox) {
        int categoryId = parseId(layerId);
        Expression _categoryId = ff.property(MYPLACES_ATTR_CATEGORY_ID);

        Filter categoryIdEquals = ff.equals(_categoryId, ff.literal(categoryId));
        Filter bboxFilter = ff.bbox(MYPLACES_ATTR_GEOMETRY,
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY(),
                CRS.toSRS(bbox.getCoordinateReferenceSystem()));

        return ff.and(Arrays.asList(categoryIdEquals, bboxFilter));
    }

    public boolean hasViewPermission(String id, User user) {
        MyPlaceCategory layer = getLayer(parseId(id));
        if (layer == null) {
            return false;
        }
        return layer.isOwnedBy(user.getUuid()) || layer.isPublished();
    }

    protected MyPlaceCategory getLayer(int id) {
        if (service == null) {
            // might cause problems with timing of components being initialized if done in init/constructor
            service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        }
        return service.findCategory(id);
    }

    // TODO: remove this and references
    public SimpleFeatureCollection postProcess(SimpleFeatureCollection sfc) throws Exception {
        List<SimpleFeature> fc = new ArrayList<>();
        SimpleFeatureType schema;

        try (SimpleFeatureIterator it = sfc.features()) {
            if (!it.hasNext()) {
                return sfc;
            }
            SimpleFeature ftr = it.next();
            schema = createType(sfc.getSchema(), ftr);
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
            List<AttributeDescriptor> attributes = schema.getAttributeDescriptors();
            attributes.stream().forEach(attr ->
                    builder.set(attr.getLocalName(), ftr.getAttribute(attr.getLocalName())));
            fc.add(builder.buildFeature(ftr.getID()));

            while (it.hasNext()) {
                SimpleFeature f = it.next();
                attributes.stream().forEach(attr ->
                        builder.set(attr.getLocalName(), f.getAttribute(attr.getLocalName())));
                fc.add(builder.buildFeature(f.getID()));
            }
        }

        return new GeoJSONFeatureCollection(fc, schema);
    }

    private SimpleFeatureType createType(SimpleFeatureType schema, SimpleFeature f) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName(schema.getName());
        typeBuilder.setDefaultGeometry(schema.getGeometryDescriptor().getLocalName());
        f.getFeatureType().getAttributeDescriptors().stream()
                .filter(attr -> isVisibleProperty(attr.getLocalName()))
                .forEach(attr -> typeBuilder.add(attr));
        typeBuilder.add(schema.getGeometryDescriptor());
        return typeBuilder.buildFeatureType();
    }

    private boolean isVisibleProperty(String name) {
        return WHITELISTED_PROPERTIES.stream().anyMatch(propName -> propName.equals(name));
    }

    public static List<MyPlace> parseMyPlaces(String input, boolean shouldSetId)
            throws JSONException {
        JSONObject featureCollection = new JSONObject(input);
        // Expect custom key featureCollection.srsName to contain srid in pattern of 'EPSG:srid'
        // if that doesn't exist or if we fail to parse the srid part out of it use 0 (unknown)
        String srsName = JSONHelper.optString(featureCollection, "srsName");
        int srid = getSrid(srsName, 0);
        JSONArray features = featureCollection.getJSONArray("features");
        final int n = features.length();
        List<MyPlace> myPlaces = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            JSONObject feature = features.getJSONObject(i);
            myPlaces.add(parseMyPlace(feature, shouldSetId, srid));
        }
        return myPlaces;
    }

    private static int getSrid(String srsName, int defaultValue) {
        if (srsName != null) {
            int i = srsName.lastIndexOf(':');
            if (i > 0) {
                srsName = srsName.substring(i + 1);
            }
            try {
                return Integer.parseInt(srsName);
            } catch (NumberFormatException ignroe) {}
        }
        return defaultValue;
    }

    private static MyPlace parseMyPlace(JSONObject feature, boolean shouldSetId, int srid)
            throws JSONException {
        MyPlace myPlace = new MyPlace();

        if (shouldSetId) {
            myPlace.setId(feature.getLong("id"));
        }
        myPlace.setCategoryId(feature.getLong("category_id"));

        JSONObject geomJSON = feature.getJSONObject("geometry");
        Geometry geom = GeoJSONReader.toGeometry(geomJSON);
        geom.setSRID(srid);
        myPlace.setGeometry(geom);

        JSONObject properties = feature.getJSONObject("properties");
        myPlace.setName(JSONHelper.getString(properties, "name"));

        // Optional fields
        myPlace.setAttentionText(JSONHelper.optString(properties, "attention_text"));
        myPlace.setDesc(JSONHelper.optString(properties, "place_desc"));
        myPlace.setLink(JSONHelper.optString(properties, "link"));
        myPlace.setImageUrl(JSONHelper.optString(properties, "image_url"));

        return myPlace;
    }

    @Override
    public SimpleFeatureCollection getFeatures(String layerId, OskariLayer layer, Envelope bbox) throws ServiceException{
        int categoryId = parseId(layerId);
        SimpleFeatureCollection featureCollection = featureService.getFeatures(categoryId, bbox);
        return featureCollection != null ? featureCollection : new EmptyFeatureCollection(null);
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
