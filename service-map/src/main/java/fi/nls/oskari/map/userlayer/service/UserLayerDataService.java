package fi.nls.oskari.map.userlayer.service;

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
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.DataUtilities;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import java.util.Map;


public class UserLayerDataService {

    private Logger log = LogFactory.getLogger(UserLayerDataService.class);
    private static final UserLayerDbService userLayerService = new UserLayerDbServiceIbatisImpl();
    private static final UserLayerDataDbService userLayerDataService = new UserLayerDataDbServiceIbatisImpl();
    private static final UserLayerStyleDbService styleService = new UserLayerStyleDbServiceIbatisImpl();
    private OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
    private final static LayerJSONFormatterUSERLAYER FORMATTER = new LayerJSONFormatterUSERLAYER();

    private static final String USERLAYER_LAYER_PREFIX = "userlayer_";
    private static final String USERLAYER_BASELAYER_ID = "userlayer.baselayer.id";
    private static final String USERLAYER_MAXFEATURES_COUNT = "userlayer.maxfeatures.count";
    private static final String KEY_DESC = "layer-desc";
    private static final String KEY_NAME = "layer-name";
    private static final String KEY_SOURCE = "layer-source";
    private static final String KEY_STYLE = "layer-style";

    final String userlayerBaseLayerId = PropertyUtil.get(USERLAYER_BASELAYER_ID);
    final int userlayerMaxFeaturesCount = PropertyUtil.getOptional(USERLAYER_MAXFEATURES_COUNT, -1);

    /**
     * @param gjsWorker geoJSON and featurecollection items
     * @param user      oskari user
     * @param fparams   user given attributes for layer
     * @return user layer data in user_layer table
     */

    public UserLayer storeUserData(GeoJsonWorker gjsWorker, User user, Map<String, String> fparams) {


        final UserLayer userLayer = new UserLayer();
        final UserLayerStyle style = new UserLayerStyle();

        log.info("user data store start: ", fparams);

        //TODO: Style insert

        try {
            //TODO: all inserts should be in one transaction

            // Insert style row
            style.setId(1);  // for default, even if style should be always valued
            if (fparams.containsKey(KEY_STYLE)) {
                final JSONObject stylejs = JSONHelper
                        .createJSONObject(fparams.get(KEY_STYLE));
                style.populateFromJSON(stylejs);
                styleService.insertUserLayerStyleRow(style);
                log.info("Add style: ", style.getId());
            }


            // Insert user_layer row
            // --------------------
            userLayer.setLayer_name(gjsWorker.getTypeName());
            userLayer.setLayer_desc("");
            userLayer.setLayer_source("");
            userLayer.setFields(parseFields(gjsWorker.getFeatureType()));
            userLayer.setUuid(user.getUuid());
            userLayer.setStyle_id(style.getId());
            if (fparams.containsKey(KEY_NAME)) userLayer.setLayer_name(fparams.get(KEY_NAME));
            if (fparams.containsKey(KEY_DESC)) userLayer.setLayer_desc(fparams.get(KEY_DESC));
            if (fparams.containsKey(KEY_SOURCE)) userLayer.setLayer_source(fparams.get(KEY_SOURCE));

            log.debug("Adding user_layer row", userLayer);
            userLayerService.insertUserLayerRow(userLayer);

            // Insert user_layer data rows
            // --------------------

            int count = this.storeUserLayerData(gjsWorker.getGeoJson(), user, userLayer.getId());
            log.info("stored ", count, " rows");

            if (count == 0) {
                return null;
                //TODO:  delete user_layer row if no rows
            }

        } catch (Exception e) {
            log.error(e, "Unable to store user layer data");
            return null;
        }

        return userLayer;
    }


    /**
     * @param geoJson import data in geojson format
     * @param user    oskari user
     * @param id      user layer id in user_layer table
     * @return
     */
    public int storeUserLayerData(JSONObject geoJson, User user, long id) {


        int count = 0;
        String uuid = user.getUuid();

        try {
            final JSONArray geofeas = geoJson.getJSONArray("features");

            // Loop json features and fix to user_layer_data structure
            for (int i = 0; i < geofeas.length(); i++) {

                JSONObject geofea = geofeas.optJSONObject(i);
                if (geofea == null) continue;
                if (!geofea.has("geometry")) continue;

                // Fix fea properties  (user_layer_id, uuid, property_json, feature_id
                final UserLayerData userLayerData = new UserLayerData();
                userLayerData.setUuid(uuid);
                userLayerData.setFeature_id(geofea.optString("id", ""));
                userLayerData.setGeometry(geofea.optJSONObject("geometry").toString());
                userLayerData.setProperty_json(geofea.optJSONObject("properties").toString());
                userLayerData.setUser_layer_id(id);

                userLayerDataService.insertUserLayerDataRow(userLayerData);

                count++;
                if (count > userlayerMaxFeaturesCount && userlayerMaxFeaturesCount != -1) break;

            }
        } catch (Exception e) {
            log.error(e, "Unable to store user layer data");
            return 0;
        }

        return count;
    }


    /**
     * @param ulayer data in user_layer table
     * @return
     * @throws ServiceException
     */
    public JSONObject parseUserLayer2JSON(UserLayer ulayer) {

        try {
            int id = ConversionHelper.getInt(userlayerBaseLayerId, 0);
            if (id == 0) return null;

            final OskariLayer wfsuserLayer = mapLayerService.find(id);

            // Merge userlayer values
            wfsuserLayer.setExternalId(USERLAYER_LAYER_PREFIX + ulayer.getId());
            wfsuserLayer.setName(ulayer.getLayer_name());
            wfsuserLayer.setType(OskariLayer.TYPE_USERLAYER);

            JSONObject json = FORMATTER.getJSON(wfsuserLayer, PropertyUtil.getDefaultLanguage(), false, ulayer);

            return json;

        } catch (Exception ex) {
            log.error(ex, "Couldn't parse userlayer to json");
            return null;
        }
    }

    public String parseFields(FeatureType schema) {

        JSONObject jsfields = new JSONObject();
        try {
            String fields = DataUtilities.encodeType((SimpleFeatureType) schema);
            String[] tfields = fields.split("[:,]");
            for (int i = 0; i < tfields.length - 1; i = i + 2) {
                jsfields.put(tfields[i], tfields[i + 1]);
            }

        } catch (Exception ex) {
            log.error(ex, "Couldn't parse field schema");
        }
        return JSONHelper.getStringFromJSON(jsfields, "{}");
    }
}
