package org.oskari.map.userlayer.service;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.domain.map.userlayer.UserLayerStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterUSERLAYER;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.oskari.geojson.GeoJSON;
import org.oskari.geojson.GeoJSONWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class UserLayerDataService {

    private Logger log = LogFactory.getLogger(UserLayerDataService.class);
    private static final UserLayerDbService userLayerService = new UserLayerDbServiceMybatisImpl();
    private OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
    private final static LayerJSONFormatterUSERLAYER FORMATTER = new LayerJSONFormatterUSERLAYER();

    private static final String USERLAYER_LAYER_PREFIX = "userlayer_";
    private static final String USERLAYER_BASELAYER_ID = "userlayer.baselayer.id";
    private static final String USERLAYER_MAXFEATURES_COUNT = "userlayer.maxfeatures.count";
    private static final String KEY_DESC = "layer-desc";
    private static final String KEY_NAME = "layer-name";
    private static final String KEY_SOURCE = "layer-source";
    private static final String KEY_STYLE = "layer-style";

    static final int USERLAYER_BASE_LAYER_ID = PropertyUtil.getOptional(USERLAYER_BASELAYER_ID, -1);
    static final int USERLAYER_MAX_FEATURES_COUNT = PropertyUtil.getOptional(USERLAYER_MAXFEATURES_COUNT, -1);

    public UserLayer storeUserData(SimpleFeatureCollection fc, User user, Map<String, String> fparams) throws ServiceException {
        try {
            //TODO: Style insert
            log.info("user data store start: ", fparams);
            final UserLayer userLayer = new UserLayer();
            final UserLayerStyle style = new UserLayerStyle();

            style.setId(1);  // for default, even if style should be always valued
            //set style from json
            if (fparams.containsKey(KEY_STYLE)) {
                final JSONObject stylejs = JSONHelper
                        .createJSONObject(fparams.get(KEY_STYLE));
                style.populateFromJSON(stylejs);
            }
            //set userLayer
            SimpleFeatureType ft = fc.getSchema();
            userLayer.setLayer_name(ft.getTypeName());
            userLayer.setLayer_desc("");
            userLayer.setLayer_source("");
            userLayer.setFields(parseFields(ft));
            userLayer.setUuid(user.getUuid());
            // TODO: Store the bounds in WGS84
            // ReferencedEnvelope env = fc.getBounds();

            if (fparams.containsKey(KEY_NAME)) {
                userLayer.setLayer_name(fparams.get(KEY_NAME));
            }
            if (fparams.containsKey(KEY_DESC)) {
                userLayer.setLayer_desc(fparams.get(KEY_DESC));
            }
            if (fparams.containsKey(KEY_SOURCE)) {
                userLayer.setLayer_source(fparams.get(KEY_SOURCE));
            }

            //get userLayerData list
            List<UserLayerData> userLayerDataList = getUserLayerData(fc, user, userLayer);

            if (userLayerDataList.isEmpty()){
                throw new ServiceException ("no_features");
            }
            //insert layer, style and data in one transaction
            int count = userLayerService.insertUserLayer(userLayer, style, userLayerDataList);
            log.info("stored:",count, "rows from", userLayer.getFeatures_count(), "features and skipped:",userLayer.getFeatures_skipped());
            return userLayer;
        } catch (Exception e) {
            log.error(e, "Unable to store user layer  data");
            throw new ServiceException ("unable_to_store_data");
        }
    }

    private List<UserLayerData> getUserLayerData(SimpleFeatureCollection fc, User user, UserLayer userLayer) throws ServiceException{
        try (SimpleFeatureIterator it = fc.features()) {
            final List<UserLayerData> userLayerDataList = new ArrayList<>();
            final String uuid = user.getUuid();

            int count = 0;
            int noGeometry = 0;

            while (it.hasNext()) {
                SimpleFeature f = it.next();
                if (f.getDefaultGeometry() == null) {
                    noGeometry++;
                    continue;
                }
                UserLayerData uld = toUserLayerData(f, uuid);
                userLayerDataList.add(uld);
                count++;
                if (USERLAYER_MAX_FEATURES_COUNT != -1 && count > USERLAYER_MAX_FEATURES_COUNT) {
                    break;
                }
            }
            userLayer.setFeatures_count(count);
            userLayer.setFeatures_skipped(noGeometry);
            return userLayerDataList;
        } catch (Exception e) {
            log.error(e, "Failed to parse geojson features to userlayer data list");
            throw new ServiceException("failed_to_parse_geojson");
        }
    }

    private UserLayerData toUserLayerData(SimpleFeature f, String uuid) throws JSONException {
        JSONObject geoJSON = new GeoJSONWriter().writeFeature(f);
        String id = geoJSON.optString(GeoJSON.ID);
        JSONObject geometry = geoJSON.getJSONObject(GeoJSON.GEOMETRY);
        String geometryJson = geometry.toString();
        JSONObject properties = geoJSON.optJSONObject(GeoJSON.PROPERTIES);
        String propertiesJson = properties != null ? properties.toString() : null;

        UserLayerData userLayerData = new UserLayerData();
        userLayerData.setUuid(uuid);
        userLayerData.setFeature_id(id);
        userLayerData.setGeometry(geometryJson);
        userLayerData.setProperty_json(propertiesJson);
        return userLayerData;
    }


    /**
     * Returns the base WFS-layer for userlayers
     * @return
     */
    public OskariLayer getBaseLayer() {
        if (USERLAYER_BASE_LAYER_ID == -1) {
            log.error("Userlayer baseId not defined. Please define", USERLAYER_BASELAYER_ID,
                    "property with value pointing to the baselayer in database.");
            return null;
        }
        return mapLayerService.find(USERLAYER_BASE_LAYER_ID);
    }

    /**
     * Creates the layer JSON for userlayer. When creating a bunch of layer JSONs prefer the overloaded version
     * with baselayer as parameter.
     * @param ulayer
     * @return
     */
    public JSONObject parseUserLayer2JSON(UserLayer ulayer) {
        return parseUserLayer2JSON(ulayer, getBaseLayer());
    }
    /**
     * @param ulayer data in user_layer table
     * @param baseLayer base WFS-layer for userlayers
     * @return
     * @throws ServiceException
     */
    public JSONObject parseUserLayer2JSON(final UserLayer ulayer, final OskariLayer baseLayer) {

        try {
            final String id = baseLayer.getExternalId();
            final String name = baseLayer.getName();
            final String type = baseLayer.getType();

            // Merge userlayer values
            baseLayer.setExternalId(USERLAYER_LAYER_PREFIX + ulayer.getId());
            baseLayer.setName(ulayer.getLayer_name());
            baseLayer.setType(OskariLayer.TYPE_USERLAYER);
            // create the JSON
            final JSONObject json = FORMATTER.getJSON(baseLayer, PropertyUtil.getDefaultLanguage(), false, null, ulayer);

            // restore the previous values for baseLayer
            baseLayer.setExternalId(id);
            baseLayer.setName(name);
            baseLayer.setType(type);

            return json;
        } catch (Exception ex) {
            log.error(ex, "Couldn't parse userlayer to json");
            return null;
        }
    }
    // parse FeatureType schema to JSONArray to keep same order in fields as in the imported file
    public String parseFields(FeatureType schema) {

        JSONArray jsfields = new JSONArray();
        try {
            Collection<PropertyDescriptor> types = schema.getDescriptors();
            for (PropertyDescriptor type : types) {
                JSONObject obj = new JSONObject();
                obj.put("name", type.getName().getLocalPart());
                obj.put("type", type.getType().getBinding().getSimpleName());
                jsfields.put(obj);
            }
        } catch (Exception ex) {
            log.error(ex, "Couldn't parse field schema");
        }
        return JSONHelper.getStringFromJSON(jsfields, "[]");
    }
}
