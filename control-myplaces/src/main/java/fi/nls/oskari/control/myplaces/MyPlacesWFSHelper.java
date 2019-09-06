package fi.nls.oskari.control.myplaces;

import java.util.*;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.geojson.GeoJSONFeatureCollection;
import org.oskari.service.user.UserLayerService;

@Oskari
public class MyPlacesWFSHelper extends UserLayerService {

    public static final String PROP_MYPLACES_BASELAYER_ID = "myplaces.baselayer.id";

    private static final String PREFIX_MYPLACES = "myplaces_";
    private static final String MYPLACES_ATTR_GEOMETRY = "oskari:geometry";
    private static final String MYPLACES_ATTR_CATEGORY_ID = "oskari:category_id";

    private static final List<String> VISIBLE_PROPERTIES = Arrays.asList("image_url", "link", "name", "place_desc");

    private FilterFactory ff;
    private int myPlacesLayerId;
    private MyPlacesService service;

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

    private MyPlaceCategory getLayer(int id) {
        if (service == null) {
            // might cause problems with timing of components being initialized if done in init/constructor
            service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        }
        return service.findCategory(id);
    }
    public JSONObject getOskariStyle (String id) {
        MyPlaceCategory layer = getLayer(parseId(id));
        if (layer == null) {
            return new JSONObject();
        }
        JSONObject style = layer.getStyle().parseUserLayerStyleToOskariJSON();
        JSONObject text = new JSONObject();
        JSONArray props = new JSONArray();
        props.put("attention_text");
        props.put("name");
        JSONHelper.put(text, "labelProperty", props);
        JSONHelper.putValue(style, "text", text);
        return style;
    }
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
        return VISIBLE_PROPERTIES.stream().anyMatch(propName -> propName.equals(name));
    }
}
