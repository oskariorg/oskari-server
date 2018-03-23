package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.UserDataLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.json.JSONObject;
import org.oskari.map.userlayer.service.UserLayerDbService;

@OskariActionRoute("GetWFSLayerConfiguration")
public class GetWFSLayerConfigurationHandler extends ActionHandler {

    private static final Logger log = LogFactory
            .getLogger(GetWFSLayerConfigurationHandler.class);

    private final WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();
    private AnalysisDataService analysisDataService = new AnalysisDataService();
    private UserLayerDbService userLayerDbService;
    private MyPlacesService myPlacesService = null;

    private final static String PARAMS_ID = "id";

    private final static String RESULT = "result";
    private final static String RESULT_SUCCESS = "success";
    private final static String RESULT_WFSLAYER = "wfslayer";

    // Analysis
    public static final String ANALYSIS_BASELAYER_ID = "analysis.baselayer.id";
    public static final String ANALYSIS_PREFIX = "analysis_";

    // My places
    public static final String MYPLACES_BASELAYER_ID = "myplaces.baselayer.id";
    public static final String MYPLACES_PREFIX = "myplaces_";

    // User layer
    public static final String USERLAYER_BASELAYER_ID = "userlayer.baselayer.id";
    public static final String USERLAYER_PREFIX = "userlayer_";

    public void init() {
        myPlacesService = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        userLayerDbService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
    }

    public void handleAction(ActionParameters params) throws ActionException {

        // Because of analysis layers
        final String sid = params.getHttpParam(PARAMS_ID, "n/a");
        final int id = ConversionHelper.getInt(getBaseLayerId(sid), -1);
        if(id == -1) {
            throw new ActionParamsException("Required parameter '" + PARAMS_ID + "' missing!");
        }
        final WFSLayerConfiguration lc = getLayerInfoForRedis(id, sid);

        if (lc == null) {
            throw new ActionParamsException("Couldn't find matching layer for id " + id +
                    ". Requested layer: " + sid);
        }

        // lc.save() saves the layer info to redis as JSON so transport can use this
        lc.save();

        final JSONObject root = new JSONObject();
        if (params.getUser().isAdmin()) {
            // for admin, write the JSON to response as admin-layerselector uses this
            JSONHelper.putValue(root, RESULT_WFSLAYER, lc.getAsJSONObject());
        } else {
            // for all other users, just reply success (==data can be read from Redis)
            JSONHelper.putValue(root, RESULT, RESULT_SUCCESS);
        }
        ResponseHelper.writeResponse(params, root);
    }

    private WFSLayerConfiguration getLayerInfoForRedis(final int id, final String requestedLayerId) {

        log.info("Loading wfs layer with id:", id, "requested layer id:", requestedLayerId);
        final WFSLayerConfiguration lc = layerConfigurationService.findConfiguration(id);
        log.debug(lc);
        final long userDataLayerId = extractId(requestedLayerId);
        UserDataLayer userLayer = null;

        // Extra manage for analysis
        if (requestedLayerId.startsWith(ANALYSIS_PREFIX)) {
            final Analysis analysis = analysisDataService.getAnalysisById(userDataLayerId);
            // Set analysis layer fields as id based
            lc.setSelectedFeatureParams(analysisDataService.getAnalysisNativeColumns(analysis));
            userLayer = analysis;
        }
        // Extra manage for myplaces
        else if (requestedLayerId.startsWith(MYPLACES_PREFIX)) {
            userLayer = myPlacesService.findCategory(userDataLayerId);
        }
        // Extra manage for imported data
        else if (requestedLayerId.startsWith(USERLAYER_PREFIX)) {
            userLayer = userLayerDbService.getUserLayerById(userDataLayerId);
        }
        if(userLayer != null) {
            log.debug("Was a user created layer");
            // set id to user data layer id for redis
            lc.setLayerId(requestedLayerId);
            if(userLayer.isPublished()) {
                // Transport uses this uuid in WFS query instead of users id if published is true.
                lc.setPublished(true);
                lc.setUuid(userLayer.getUuid());
                log.debug("Was published", lc);
            }
        }
        return lc;
    }

    /**
     * Return base wfs id
     * 
     * @param sid
     * @return id
     */
    private String getBaseLayerId(final String sid) {
        if (sid.startsWith(ANALYSIS_PREFIX)) {
            return PropertyUtil.get(ANALYSIS_BASELAYER_ID);
        }
        else if (sid.startsWith(MYPLACES_PREFIX)) {
            return PropertyUtil.get(MYPLACES_BASELAYER_ID);
        }
        else if (sid.startsWith(USERLAYER_PREFIX)) {
            return PropertyUtil.get(USERLAYER_BASELAYER_ID);
        }
        return sid;
    }

    private long extractId(final String layerId) {
        if (layerId == null) {
            return -1;
        }
        // takeout the last _-separated token
        // -> this is the actual id in analysis, myplaces, userlayer
        final String[] values = layerId.split("_");
        if(values.length < 2) {
            // wasn't valid id!
            return -1;
        }
        final String id = values[values.length - 1];
        return ConversionHelper.getLong(id, -1);
    }
}
