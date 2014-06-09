package fi.nls.oskari.control.layer;

import javax.servlet.http.HttpServletResponse;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.map.userlayer.service.UserLayerDbService;
import fi.nls.oskari.map.userlayer.service.UserLayerDbServiceIbatisImpl;
import org.json.JSONObject;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;

@OskariActionRoute("GetWFSLayerConfiguration")
public class GetWFSLayerConfigurationHandler extends ActionHandler {

    private static final Logger log = LogFactory
            .getLogger(GetWFSLayerConfigurationHandler.class);

    private final WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();
    private AnalysisDataService analysisDataService = new AnalysisDataService();
    private UserLayerDbService userLayerDbService = new UserLayerDbServiceIbatisImpl();

    private final static String ID = "id";

    private final static String ERROR = "error";
    private final static String ERROR_NO_ID = "id parameter wasn't given";
    private final static String ERROR_NOT_FOUND = "id wasn't found";
    private final static String ERROR_NO_PERMISSION = "no permissions to view the layer";

    private final static String RESULT = "result";
    private final static String RESULT_SUCCESS = "success";

    // Analysis
    public static final String ANALYSIS_BASELAYER_ID = "analysis.baselayer.id";
    public static final String ANALYSIS_PREFIX = "analysis_";

    // My places
    public static final String MYPLACES_BASELAYER_ID = "myplaces.baselayer.id";
    public static final String MYPLACES_PREFIX = "myplaces_";

    // User layer
    public static final String USERLAYER_BASELAYER_ID = "userlayer.baselayer.id";
    public static final String USERLAYER_PREFIX = "userlayer_";

    public void handleAction(ActionParameters params) throws ActionException {

        final JSONObject root = new JSONObject();

        // Because of analysis layers
        String sid = params.getHttpParam(ID, "n/a");
        final int id = ConversionHelper.getInt(getBaseLayerId(sid), 0);

        final HttpServletResponse response = params.getResponse();
        response.setContentType("application/json");

        if (id == 0) {
            JSONHelper.putValue(root, ERROR, ERROR_NO_ID);
            ResponseHelper.writeResponse(params, root);
            // FIXME: throw ActionParamsException instead and modify client
            // response parsing
            return;
        }

        String json = WFSLayerConfiguration.getCache(id + "");
        if (json == null) {
            WFSLayerConfiguration lc = layerConfigurationService
                    .findConfiguration(id);

            log.warn("id", id);
            log.warn(lc);

            // Extra manage for analysis
            if (sid.indexOf(ANALYSIS_PREFIX) > -1) {
                log.warn("sid", sid);
                // set id to original analysis layer id
                lc.setLayerId(sid);
                // Set analysis layer fields as id based
                lc.setSelectedFeatureParams(getAnalysisFeatureProperties(sid));
            }
            // Extra manage for analysis
            else if (sid.indexOf(MYPLACES_PREFIX) > -1) {
                log.warn("sid", sid);
                // set id to original my places layer id
                lc.setLayerId(sid);
            }
            else if (sid.indexOf(USERLAYER_PREFIX) > -1) {
                log.warn("sid", sid);
                // set id to original user layer id
                lc.setLayerId(sid);
                setAdditionalPublishedData(sid, lc);
            }
            if (lc == null) {
                JSONHelper.putValue(root, ERROR, ERROR_NOT_FOUND);
                ResponseHelper.writeResponse(params, root);
                // FIXME: throw ActionParamsException instead and modify client
                // response parsing
                return;
            }
            lc.save();
        }
        JSONHelper.putValue(root, RESULT, RESULT_SUCCESS);
        ResponseHelper.writeResponse(params, root);
    }

    /**
     * Return base wfs id, if analysis_ layer
     * 
     * @param sid
     * @return id
     */
    private String getBaseLayerId(String sid) {

        String id = sid;
        if (sid.indexOf(ANALYSIS_PREFIX) > -1) {
            id = PropertyUtil.get(ANALYSIS_BASELAYER_ID);
        }
        else if (sid.indexOf(MYPLACES_PREFIX) == 0) {
            id = PropertyUtil.get(MYPLACES_BASELAYER_ID);
        }
        else if (sid.indexOf(USERLAYER_PREFIX) == 0) {
            id = PropertyUtil.get(USERLAYER_BASELAYER_ID);
        }
        return id;
    }

    /**
     * Get properties (native fields) of analysis layer
     * 
     * @param sid
     * @return properties in array syntax eg. "{default:["t1","t2",...]}"
     */
    private String getAnalysisFeatureProperties(String sid) {
        String properties = null; // Field names
        if (sid.indexOf(ANALYSIS_PREFIX) > -1) {
            String[] values = sid.split("_");
            if (values.length > 0)
                properties = analysisDataService
                        .getAnalysisNativeColumns(values[values.length - 1]);

        }

        return properties;
    }

    private void setAdditionalPublishedData(String sid, WFSLayerConfiguration lc) {
        if (sid.indexOf(USERLAYER_PREFIX) > -1) {
            final long userLayerId = ConversionHelper
                    .getLong(sid.substring(USERLAYER_PREFIX.length()), -1);
            UserLayer userLayer = userLayerDbService.getUserLayerById(userLayerId);
            if (userLayer != null && userLayer.isPublished()) {
                lc.setPublished(true);
                lc.setUuid(userLayer.getUuid());
            }
        }
    }
}
