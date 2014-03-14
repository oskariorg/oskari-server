package fi.nls.oskari.control.data;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.domain.permissions.OskariLayerResourceName;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.domain.permissions.UniqueResourceName;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.analysis.AnalysisParser;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.map.analysis.service.AnalysisWebProcessingService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.Set;

@OskariActionRoute("CreateAnalysisLayer")
public class CreateUserLayerHandler extends ActionHandler {

    private static final Logger log = LogFactory
            .getLogger(CreateUserLayerHandler.class);
    private AnalysisDataService analysisDataService = new AnalysisDataService();
    private AnalysisWebProcessingService wpsService = new AnalysisWebProcessingService();
    private AnalysisParser analysisParser = new AnalysisParser();
    private OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();

    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    private static final String PARAM_ANALYSE = "analyse";
    private static final String PARAM_FILTER = "filter";

    private static final String PARAMS_PROXY = "action_route=GetProxyRequest&serviceId=wfsquery&wfs_layer_id=";

    private static final String INTERSECT = "intersect";
    private static final String AGGREGATE = "aggregate";
    private static final String UNION = "union";
    private static final String LAYER_UNION = "layer_union";

    private static final String ERROR_ANALYSE_PARAMETER_MISSING = "Analyse_parameter_missing";
    private static final String ERROR_UNABLE_TO_PARSE_ANALYSE = "Unable_to_parse_analysis";
    private static final String ERROR_UNABLE_TO_GET_WPS_FEATURES = "Unable_to_get_WPS_features";
    private static final String ERROR_WPS_EXECUTE_RETURNS_EXCEPTION = "WPS_execute_returns_Exception";
    private static final String ERROR_WPS_EXECUTE_RETURNS_NO_FEATURES = "WPS_execute_returns_no_features";
    private static final String ERROR_UNABLE_TO_PROCESS_AGGREGATE_UNION = "Unable_to_process_aggregate_union";
    private static final String ERROR_UNABLE_TO_GET_FEATURES_FOR_UNION = "Unable_to_get_features_for_union";
    private static final String ERROR_UNABLE_TO_STORE_ANALYSIS_DATA = "Unable_to_store_analysis_data";
    private static final String ERROR_UNABLE_TO_GET_ANALYSISLAYER_DATA = "Unable_to_get_analysisLayer_data";


    final private static String GEOSERVER_PROXY_BASE_URL = PropertyUtil.getOptional("analysis.baseproxy.url");

    /**
     * Handles action_route CreateAnalysisLayer
     *
     * @param params Ajax request parameters
     *               **********************************************************************
     */
    public void handleAction(ActionParameters params) throws ActionException {

        // TODO: use params.getRequiredParam(PARAM_ANALYSE, ERROR_ANALYSE_PARAMETER_MISSING); instead
        final String analyse = params.getHttpParam(PARAM_ANALYSE);
        if (analyse == null) {
            this.MyError(ERROR_ANALYSE_PARAMETER_MISSING, params, null);
            return;
        }
        // </TODO>

        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }

        // filter conf data
        final String filter = params.getHttpParam(PARAM_FILTER);

        // Get baseProxyUrl
        final String baseUrl = getBaseProxyUrl(params);
        AnalysisLayer analysisLayer = null;

        // User
        String uuid = params.getUser().getUuid();
        try {
            analysisLayer = analysisParser.parseAnalysisLayer(analyse, filter, baseUrl, uuid);
        } catch (ServiceException e) {
            this.MyError(ERROR_UNABLE_TO_PARSE_ANALYSE, params, e);
            return;
        }
        Analysis analysis = null;

        if (analysisLayer.getMethod().equals(LAYER_UNION)) {
            // no WPS for merge analysis
            analysis = analysisDataService.mergeAnalysisData(
                    analysisLayer, analyse, params.getUser());
        } else {
            // Generate WPS XML
            String featureSet;
            try {
                featureSet = wpsService.requestFeatureSet(analysisLayer);
            } catch (ServiceException e) {
                this.MyError(ERROR_UNABLE_TO_GET_WPS_FEATURES, params, e);
                return;
            }
            // Check, if exception result set
            if (featureSet.indexOf("ows:Exception") > -1) {
                this.MyError(ERROR_WPS_EXECUTE_RETURNS_EXCEPTION, params, featureSet);
                return;
            }

            // Check, if any data in result set
            if (featureSet.indexOf("numberOfFeatures=\"0\"") > -1) {
                this.MyError(ERROR_WPS_EXECUTE_RETURNS_NO_FEATURES, params, null);
                return;
            }
            if (analysisLayer.getMethod().equals(UNION)
                    || analysisLayer.getMethod().equals(INTERSECT)) {
                // Harmonize namespaces and element names
                featureSet = analysisParser.harmonizeElementNames(featureSet, analysisLayer);
            }

            // Add data to analysis db if NOT aggregate
            if (analysisLayer.getMethod().equals(AGGREGATE)) {
                // No store to analysis db for aggregate - set results in to the
                // response
                //Save analysis results - use union of input data
                analysisLayer.setWpsLayerId(-1);
                analysisLayer.setResult(analysisParser.parseAggregateResults(featureSet,
                        analysisLayer));

                try {
                    analysisLayer = analysisParser.parseSwitch2UnionLayer(analysisLayer, analyse, filter, baseUrl);
                } catch (ServiceException e) {
                    this.MyError(ERROR_UNABLE_TO_PROCESS_AGGREGATE_UNION, params, e);
                    return;
                }
                try {
                    featureSet = wpsService.requestFeatureSet(analysisLayer);
                    // Harmonize namespaces and element names
                    featureSet = analysisParser.harmonizeElementNames(featureSet, analysisLayer);
                    featureSet = analysisParser.mergeAggregateResults2FeatureSet(featureSet, analysisLayer);
                } catch (ServiceException e) {
                    this.MyError(ERROR_UNABLE_TO_GET_FEATURES_FOR_UNION, params, e);
                    return;
                }

            }

            analysis = analysisDataService.storeAnalysisData(
                    featureSet, analysisLayer, analyse, params.getUser());
        }

        if (analysis == null) {
            this.MyError(ERROR_UNABLE_TO_STORE_ANALYSIS_DATA, params, null);
            return;
        }

        analysisLayer.setWpsLayerId(analysis.getId()); // aka. analysis_id
        // Analysis field mapping
        analysisLayer.setLocaleFields(analysis);
        analysisLayer.setNativeFields(analysis);

        // FIXME: For analysis and myplaces sources permissions will be copied from base layer
        final OskariLayer wfsLayer = mapLayerService.find(analysisLayer.getId());
        // copy permissions from source layer to new analysis
        List<Permissions> sourcePermissions = permissionsService.getResourcePermissions(new OskariLayerResourceName(wfsLayer), Permissions.EXTERNAL_TYPE_ROLE);

        UniqueResourceName analysisResourceName = new UniqueResourceName();
        analysisResourceName.setType(AnalysisLayer.TYPE);
        analysisResourceName.setName(Long.toString(analysis.getId()));
        analysisResourceName.setNamespace("analysis");
        for (Permissions permission : sourcePermissions) {
            List<String> grantedPermissions = permission.getGrantedPermissions();
            for (String grantedPermission : grantedPermissions) {
                if ((grantedPermission.equals(Permissions.PERMISSION_TYPE_PUBLISH)) || (grantedPermission.equals(Permissions.PERMISSION_TYPE_VIEW_PUBLISHED))) {
                    permissionsService.insertPermissions(analysisResourceName, permission.getExternalId(), permission.getExternalIdType(), grantedPermission);
                }
            }
        }


        // Get analysisLayer JSON for response to front
        final JSONObject analysisLayerJSON = AnalysisHelper.getlayerJSON(analysis); //analysisLayer.getJSON();

        // notify client to remove layers defined in mergeLayers since they are removed from backend
        JSONArray mlayers = new JSONArray();
        if (analysisLayer.getMergeAnalysisLayers() != null) {
            for (String lay : analysisLayer.getMergeAnalysisLayers()) {
                mlayers.put(lay);
            }
        }
        JSONHelper.putValue(analysisLayerJSON,"mergeLayers", mlayers);

        Set<String> permissionsList = permissionsService.getPublishPermissions(AnalysisLayer.TYPE);
        Set<String> editAccessList = null;
        String permissionKey = "analysis+" + analysis.getId();
        JSONObject permissions = OskariLayerWorker.getPermissions(params.getUser(), permissionKey, permissionsList, editAccessList);
        JSONHelper.putValue(analysisLayerJSON, "permissions", permissions);

        ResponseHelper.writeResponse(params, analysisLayerJSON);
    }


    /**
     * Parses WPS Proxy url via Oskari action route
     *
     * @param params Action parameters
     * @return String baseurl for Geoserver WPS reference WFS data input
     *         **********************************************************************
     */
    public String getBaseProxyUrl(ActionParameters params) {
        String baseurl = GEOSERVER_PROXY_BASE_URL;
        if (baseurl == null) {
            try {
                final URL url = new URL(params.getRequest().getRequestURL().toString());
                baseurl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
            } catch (Exception ignored) {
            }
        }

        final String baseAjaxUrl = PropertyUtil.get(params.getLocale(),
                GetAppSetupHandler.PROPERTY_AJAXURL);
        baseurl = baseurl + baseAjaxUrl + PARAMS_PROXY;
        log.debug("Analysis baseURL:", baseurl);
        return baseurl;
    }

    /**
     * Break analyse and inform error to client
     */
    private void MyError(String mes, ActionParameters params, Object ee) {

        JSONObject errorResponse = new JSONObject();

        log.error(mes.replace("_", " "), ee);
        JSONHelper.putValue(errorResponse, "error", mes);
        ResponseHelper.writeResponse(params, errorResponse);
    }
}
