package org.oskari.control.userlayer;

import fi.nls.oskari.annotation.Oskari;
import org.oskari.user.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.WFSConversionHelper;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.domain.map.FeatureProperties;
import org.oskari.domain.map.LayerExtendedOutput;
import org.oskari.geojson.GeoJSONFeatureCollection;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.service.user.UserLayerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Oskari
public class UserLayerWFSHelper extends UserLayerService {

    public static final String PREFIX_USERLAYER = OskariLayer.TYPE_USERLAYER + "_";

    protected static final String USERLAYER_ATTR_GEOMETRY = "geometry";
    private static final String USERLAYER_ATTR_PROPERTY_JSON = "property_json";

    private UserLayerDbService service;

    @Override
    public OskariLayer getOskariLayer(String layerId) {
        return getBaseLayer();
    }

    @Override
    public boolean isUserContentLayer(String layerId) {
        return layerId.startsWith(PREFIX_USERLAYER);
    }

    private int parseId(String layerId) {
        return Integer.parseInt(layerId.substring(PREFIX_USERLAYER.length()));
    }

    protected SimpleFeatureCollection postProcess(SimpleFeatureCollection sfc) throws Exception {
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

    @Override
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
        Iterator<String> keys = properties.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object obj = properties.get(key);
            typeBuilder.add(key, obj.getClass());
        }
        return typeBuilder.buildFeatureType();
    }

    @Override
    public SimpleFeatureCollection getFeatures(String layerId, Envelope bbox) throws ServiceException {
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

    @Override
    public LayerExtendedOutput describeLayer(String layerId, String lang, CoordinateReferenceSystem crs) {
        UserLayer layer = service.getUserLayerById(parseId(layerId));

        LayerExtendedOutput describe = new LayerExtendedOutput();
        describe.id = layerId;
        describe.type = OskariLayer.TYPE_WFS;
        describe.name = layer.getName(lang);
        describe.metadataUuid = null;
        describe.dataproviderId = null;
        describe.created = layer.getCreated() == null ? null : new Date(layer.getCreated().toInstant().toEpochMilli());
        describe.updated = null;

        describe.coverage = layer.getWkt() == null ? null : WKTHelper.transformLayerCoverage(layer.getWkt(), crs);
        describe.styles = getVectorStyles(layer);
        describe.hover = null;
        describe.capabilities = null;

        describe.properties = getProperties(layer, lang);
        describe.controlData = null;

        return describe;
    }

    static List<VectorStyle> getVectorStyles(UserLayer layer) {
        JSONObject defaultStyle = layer.getWFSLayerOptions().getDefaultStyle();

        VectorStyle vectorStyle = new VectorStyle();
        vectorStyle.setType(VectorStyle.TYPE_OSKARI);
        vectorStyle.setName("default");
        vectorStyle.setStyle(defaultStyle);

        return Collections.singletonList(vectorStyle);
    }

    private List<FeatureProperties> getProperties(UserLayer layer, String lang) {
        List<FeatureProperties> props = new ArrayList<>();

        JSONArray fields = layer.getFields();
        for(int i = 0; i < fields.length(); i++) {
            JSONObject field = JSONHelper.getJSONObject(fields, i); // {locales: {en}, name, type }

            String name = field.optString("name");
            String rawType = field.optString("type");
            JSONObject locales = field.optJSONObject("locales");
            // For now UserLayerDataService.parseFields() adds only "en" localization
            String label = locales != null ? locales.optString("en", null) : null;

            FeatureProperties p = new FeatureProperties();
            p.name = name;
            p.rawType = rawType;
            p.type = WFSConversionHelper.getSimpleType(rawType);
            p.label = label;
            p.hidden = false;
            p.format = null;
            p.order = i;
            props.add(p);
        }

        return props;
    }


}
