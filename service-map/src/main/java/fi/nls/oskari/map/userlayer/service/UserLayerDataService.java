package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterUSERLAYER;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;

import org.geotools.feature.DefaultFeatureCollection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;


public class UserLayerDataService {

    private Logger log = LogFactory.getLogger(UserLayerDataService.class);
    private static final UserLayerDbService userLayerService = new UserLayerDbServiceIbatisImpl();
    private static final UserLayerDataDbService userLayerDataService = new UserLayerDataDbServiceIbatisImpl();
    private OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
    private final static LayerJSONFormatterUSERLAYER FORMATTER = new LayerJSONFormatterUSERLAYER();

    private static final String USERLAYER_LAYER_PREFIX = "userlayer_";
    private static final String USERLAYER_BASELAYER_ID = "userlayer.baselayer.id";
    private static final String KEY_DESC = "layer-desc";
    private static final String KEY_NAME = "layer-name";
    private static final String KEY_SOURCE = "layer-source";


    final String userlayerBaseLayerId = PropertyUtil.get(USERLAYER_BASELAYER_ID);

    /**
     *
     * @param geoJson import data in geojson format
     * @param user  oskari user
     * @param layername  layer name in import file
     * @param fparams  user given attributes for layer
     * @return user layer data in user_layer table
     */

    public UserLayer storeUserData(JSONObject geoJson, User user, String layername, Map<String, String> fparams) {


        final UserLayer userLayer = new UserLayer();

        //TODO: Style insert

        try {
            // Insert user_layer row
            // --------------------
            userLayer.setLayer_name(layername);
            userLayer.setLayer_desc("");
            userLayer.setLayer_source("");
            userLayer.setUuid(user.getUuid());
            userLayer.setStyle_id(1);
            if (fparams.containsKey(KEY_NAME)) userLayer.setLayer_name(fparams.get(KEY_NAME));
            if (fparams.containsKey(KEY_DESC)) userLayer.setLayer_desc(fparams.get(KEY_DESC));
            if (fparams.containsKey(KEY_SOURCE)) userLayer.setLayer_source(fparams.get(KEY_SOURCE));

            log.debug("Adding user_layer row", userLayer);
            userLayerService.insertUserLayerRow(userLayer);

            // Insert user_layer data rows
            // --------------------

            int count = this.storeUserLayerData(geoJson, user, userLayer.getId());
            log.info("stored ", count, " rows");

            if (count == 0) {
                return null;
                //TODO:  delete user_layer row if no rows
            }

        } catch (Exception e) {
            log
                    .debug(
                            "Unable to store user layer data",
                            e);
            return null;
        }

        return userLayer;
    }

    /**
     *
     * @param geoJson   import data in geojson format
     * @param user   oskari user
     * @param id user layer id in user_layer table
     * @return
     */
    public int storeUserLayerData(JSONObject geoJson, User user, long id) {


        int count = 0;
        String uuid = user.getUuid();

        try {
            JSONArray geofeas = geoJson.getJSONArray("features");
            DefaultFeatureCollection fc = new DefaultFeatureCollection();

            // Loop json features and fix to user_layer_data structure
            for (int i = 0; i < geofeas.length(); i++) {
                JSONObject geofea = geofeas.getJSONObject(i);

                // Fix fea properties  (user_layer_id, uuid, property_json, feature_id
                final UserLayerData userLayerData = new UserLayerData();
                userLayerData.setUuid(uuid);
                userLayerData.setFeature_id(geofea.optString("id", ""));
                userLayerData.setGeometry(geofea.optJSONObject("geometry").toString());
                userLayerData.setProperty_json(geofea.optJSONObject("properties").toString());
                userLayerData.setUser_layer_id(id);

                userLayerDataService.insertUserLayerDataRow(userLayerData);
                count++;

            }
        } catch (Exception e) {
            log
                    .debug(
                            "Unable to store user layer data",
                            e);
            return 0;
        }

        return count;
    }


    /**
     *
     * @param ulayer   data in user_layer table
     * @return
     * @throws ServiceException
     */
    public JSONObject parseUserLayer2JSON(UserLayer ulayer) throws ServiceException {

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
            log.error("Couldn't parse userlayer to json", ex);
            return null;
        }
    }

}
