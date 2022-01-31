package org.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterUSERLAYER;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.oskari.geojson.GeoJSON;
import org.oskari.geojson.GeoJSONWriter;
import org.oskari.map.userlayer.input.KMLParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserLayerDataService {

    private static final Logger log = LogFactory.getLogger(UserLayerDataService.class);
    private static final OskariLayerService mapLayerService = new OskariLayerServiceMybatisImpl();
    private static final LayerJSONFormatterUSERLAYER FORMATTER = new LayerJSONFormatterUSERLAYER();

    private static final String USERLAYER_BASELAYER_ID = "userlayer.baselayer.id";

    private static final String USERLAYER_MAXFEATURES_COUNT = "userlayer.maxfeatures.count";
    private static final int USERLAYER_MAX_FEATURES_COUNT = PropertyUtil.getOptional(USERLAYER_MAXFEATURES_COUNT, -1);
    private static final String DEFAULT_LOCALES_LANGUAGE = "en";

    private static final int USERLAYER_BASE_LAYER_ID = PropertyUtil.getOptional(USERLAYER_BASELAYER_ID, -1);

    @Deprecated
    public static UserLayer createUserLayer(SimpleFeatureCollection fc,
            String uuid, String name, String desc, String source, String style) {
        final SimpleFeatureType ft = fc.getSchema();
        final UserLayer userLayer = new UserLayer();
        userLayer.setUuid(uuid);
        String ftName = ft.getTypeName();
        userLayer.setName(ConversionHelper.getString(ftName, name));
        userLayer.setName(PropertyUtil.getDefaultLanguage(), ConversionHelper.getString(name, ftName)); // FIXME: userLayer.setLocale(locale);
        userLayer.setLayer_desc(ConversionHelper.getString(desc, ""));
        userLayer.setLayer_source(ConversionHelper.getString(source, ""));
        WFSLayerOptions wfsOptions = userLayer.getWFSLayerOptions();
        wfsOptions.setDefaultFeatureStyle(JSONHelper.createJSONObject(style));
        userLayer.setFields(parseFields(ft));
        userLayer.setWkt(getWGS84ExtentAsWKT(fc));
        return userLayer;
    }
    public static UserLayer createUserLayer(SimpleFeatureCollection fc, String uuid, JSONObject locale, JSONObject style) {
        final SimpleFeatureType ft = fc.getSchema();
        final UserLayer userLayer = new UserLayer();
        userLayer.setUuid(uuid);
        userLayer.setLayer_name(ft.getTypeName());
        userLayer.setLocale(locale);
        WFSLayerOptions wfsOptions = userLayer.getWFSLayerOptions();
        wfsOptions.setDefaultFeatureStyle(style);
        userLayer.setFields(parseFields(ft));
        userLayer.setWkt(getWGS84ExtentAsWKT(fc));
        return userLayer;
    }

    private static String getWGS84ExtentAsWKT(SimpleFeatureCollection fc) {
        try {
            CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326", true);
            ReferencedEnvelope extentWGS84 = fc.getBounds().transform(wgs84, true);
            return WKTHelper.getBBOX(extentWGS84.getMinX(),
                    extentWGS84.getMinY(),
                    extentWGS84.getMaxX(),
                    extentWGS84.getMaxY());
        } catch (FactoryException | TransformException e) {
            // This shouldn't really happen since EPSG:4326 shouldn't be problematic
            // and transforming into it should always work. But if it does happen
            // there's probably something wrong with the geometries of the features
            throw new ServiceRuntimeException("Failed to transform bounding extent", e);
        }
    }

    private static JSONArray parseFields(SimpleFeatureType schema) {
        // parse FeatureType schema to JSONArray to keep same order in fields as in the imported file
        JSONArray jsfields = new JSONArray();
        try {
            Collection<PropertyDescriptor> types = schema.getDescriptors();
            for (PropertyDescriptor type : types) {
                JSONObject obj = new JSONObject();
                String name = type.getName().getLocalPart();
                obj.put("name", name);
                obj.put("type", type.getType().getBinding().getSimpleName());
                // don't add locale for geometry
                if (!KMLParser.KML_GEOM.equals(name)){
                    JSONObject locales = new JSONObject();
                    // use name as default localized value
                    String localizedName = name;
                    // KML handling
                    if (KMLParser.KML_NAME.equals(name)){
                        localizedName =  "Name";
                    } else if (KMLParser.KML_DESC.equals(name)){
                        localizedName =  "Description";
                    }
                    locales.put(DEFAULT_LOCALES_LANGUAGE, localizedName);
                    obj.put("locales", locales);
                }
                jsfields.put(obj);
            }
        } catch (Exception ex) {
            log.error(ex, "Couldn't parse field schema");
        }
        return jsfields;
    }
    public static List<UserLayerData> createUserLayerData(SimpleFeatureCollection fc, String uuid)
            throws UserLayerException {
        List<UserLayerData> userLayerDataList = new ArrayList<>();
        try (SimpleFeatureIterator it = fc.features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                if (f.getDefaultGeometry() == null) {
                    continue;
                }
                userLayerDataList.add(toUserLayerData(f, uuid));
                if (USERLAYER_MAX_FEATURES_COUNT != -1 && userLayerDataList.size() == USERLAYER_MAX_FEATURES_COUNT) {
                    break;
                }
            }
        }
        return userLayerDataList;
    }

    private static UserLayerData toUserLayerData(SimpleFeature f, String uuid) throws UserLayerException {
        try {
            JSONObject geoJSON = new GeoJSONWriter().writeFeature(f);
            String id = geoJSON.optString(GeoJSON.ID);
            JSONObject geometry = geoJSON.getJSONObject(GeoJSON.GEOMETRY);
            String geometryJson = geometry.toString();
            JSONObject properties = geoJSON.optJSONObject(GeoJSON.PROPERTIES);
            UserLayerData userLayerData = new UserLayerData();
            userLayerData.setUuid(uuid);
            userLayerData.setFeature_id(id);
            userLayerData.setGeometry(geometryJson);
            userLayerData.setProperty_json(properties);
            return userLayerData;
        } catch (JSONException e) {
            throw new UserLayerException("Failed to encode feature as GeoJSON", UserLayerException.ErrorType.INVALID_FEATURE); //no geometry
        }
    }

    /**
     * Returns the base WFS-layer for userlayers
     */
    public static OskariLayer getBaseLayer() {
        if (USERLAYER_BASE_LAYER_ID == -1) {
            log.error("Userlayer baseId not defined. Please define", USERLAYER_BASELAYER_ID,
                    "property with value pointing to the baselayer in database.");
            return null;
        }
        return mapLayerService.find(USERLAYER_BASE_LAYER_ID);
    }

    public static JSONObject parseUserLayer2JSON(UserLayer ulayer, String srs) {
        return parseUserLayer2JSON(ulayer, srs, PropertyUtil.getDefaultLanguage());
    }

    public static JSONObject parseUserLayer2JSON(final UserLayer layer, final String srs, final String lang) {
        OskariLayer baseLayer = getBaseLayer();
        return FORMATTER.getJSON(baseLayer,layer, srs, lang);
    }

}
