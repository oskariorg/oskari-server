package fi.nls.oskari.control.data;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.domain.permissions.Permissions;
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
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.domain.IntersectMethodParams;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.map.analysis.service.AnalysisWebProcessingService;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.permission.domain.Permission;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.Set;

@OskariActionRoute("CreateAnalysisLayer")
public class CreateAnalysisLayerHandler extends ActionHandler {

    private static final Logger log = LogFactory
            .getLogger(CreateAnalysisLayerHandler.class);
    private AnalysisDataService analysisDataService = new AnalysisDataService();
    private AnalysisWebProcessingService wpsService = new AnalysisWebProcessingService();
    private AnalysisParser analysisParser = new AnalysisParser();
    private OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();

    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    private static final String PARAM_ANALYSE = "analyse";
    private static final String PARAM_FILTER1 = "filter1";
    private static final String PARAM_FILTER2 = "filter2";

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
        JSONObject analyseJson = JSONHelper.createJSONObject(analyse);
        if (analyseJson == null) {
            this.MyError(ERROR_ANALYSE_PARAMETER_MISSING, params, null);
            return;
        }
        // </TODO>

        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }

        // filter conf data
        final String filter1 = params.getHttpParam(PARAM_FILTER1);
        final String filter2 = params.getHttpParam(PARAM_FILTER2);

        // Get baseProxyUrl
        final String baseUrl = getBaseProxyUrl(params);
        AnalysisLayer analysisLayer = null;

        // User
        String uuid = params.getUser().getUuid();
        try {
            analysisLayer = analysisParser.parseAnalysisLayer(analyseJson, filter1, filter2, baseUrl, uuid);
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
            if (featureSet.isEmpty() || featureSet.indexOf("numberOfFeatures=\"0\"") > -1) {
                this.MyError(ERROR_WPS_EXECUTE_RETURNS_NO_FEATURES, params, null);
                return;
            }
            if (analysisLayer.getMethod().equals(UNION)
                    || analysisLayer.getMethod().equals(INTERSECT)) {
                // Harmonize namespaces and element names
                featureSet = analysisParser.harmonizeElementNames(featureSet, analysisLayer);
            }

            // Add data to analysis db  - we must create still an union in aggregate case
            if (analysisLayer.getMethod().equals(AGGREGATE)) {
                // No store to analysis db for aggregate - set results in to the
                // response
                //Save analysis results - use union of input data
                analysisLayer.setWpsLayerId(-1);
                analysisLayer.setResult(analysisParser.parseAggregateResults(featureSet,
                        analysisLayer));

                try {
                    analysisLayer = analysisParser.parseSwitch2UnionLayer(analysisLayer, analyse, filter1, filter2, baseUrl);
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

        // copy permissions from source layer to new analysis
        final Resource sourceResource = getSourcePermission(analyseJson, params.getUser());
        if(sourceResource != null) {
            final Resource analysisResource = new Resource();
            analysisResource.setType(AnalysisLayer.TYPE);
            analysisResource.setMapping("analysis", Long.toString(analysis.getId()));
            for(Permission p : sourceResource.getPermissions()) {
                // check if user has role matching permission?
                if(p.isOfType(Permissions.PERMISSION_TYPE_PUBLISH) || p.isOfType(Permissions.PERMISSION_TYPE_VIEW_PUBLISHED)) {
                    analysisResource.addPermission(p.clonePermission());
                }
            }
            log.debug("Trying to save permissions for analysis", analysisResource, analysisResource.getPermissions());
            permissionsService.saveResourcePermissions(analysisResource);
        }
        else {
            log.warn("Couldn't get source permissions for analysis, result will have none");
        }

        // Get analysisLayer JSON for response to front
        final JSONObject analysisLayerJSON = AnalysisHelper.getlayerJSON(analysis);

        // Additional param for new layer creation when merging layers:
        // - Notify client to remove merged layers since they are removed from backend
        JSONArray mlayers = new JSONArray();
        if (analysisLayer.getMergeAnalysisLayers() != null) {
            for (String lay : analysisLayer.getMergeAnalysisLayers()) {
                mlayers.put(lay);
            }
        }
        JSONHelper.putValue(analysisLayerJSON, "mergeLayers", mlayers);

        Set<String> permissionsList = permissionsService.getPublishPermissions(AnalysisLayer.TYPE);
        Set<String> editAccessList = null;
        String permissionKey = "analysis+" + analysis.getId();
        JSONObject permissions = OskariLayerWorker.getPermissions(params.getUser(), permissionKey, permissionsList, editAccessList);
        JSONHelper.putValue(analysisLayerJSON, "permissions", permissions);

        ResponseHelper.writeResponse(params, analysisLayerJSON);
    }

    private Resource getSourcePermission(final JSONObject analyseData, final User user) {
        final String layerId = analysisParser.getSourceLayerId(analyseData);
        if(layerId == null) {
            return null;
        }

        if (layerId.startsWith(AnalysisParser.ANALYSIS_LAYER_PREFIX)) {

            final Resource resource = new Resource();
            resource.setType(AnalysisLayer.TYPE);
            resource.setMapping("analysis", Long.toString(AnalysisHelper.getAnalysisIdFromLayerId(layerId)));
            return permissionsService.findResource(resource);
        }
        else if (layerId.startsWith(AnalysisParser.MYPLACES_LAYER_PREFIX)  || layerId.equals("-1") || layerId.startsWith(AnalysisParser.USERLAYER_PREFIX)) {

            final Resource resource = new Resource();
            // permission to publish for self
            final Permission permPublish = new Permission();
            permPublish.setExternalType(Permissions.EXTERNAL_TYPE_USER);
            permPublish.setExternalId("" + user.getId());
            permPublish.setType(Permissions.PERMISSION_TYPE_PUBLISH);
            resource.addPermission(permPublish);
            try {
                // add VIEW_PUBLISHED for all roles currently in the system
                for(Role role: UserService.getInstance().getRoles()) {
                    final Permission perm = new Permission();
                    perm.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
                    perm.setExternalId("" + role.getId());
                    perm.setType(Permissions.PERMISSION_TYPE_VIEW_PUBLISHED);
                    resource.addPermission(perm);
                }
            }catch (Exception e) {
                log.error("Something went wrong when generating source permissions for myplaces layer or temporary or user data layer");

            }
            return resource;
        }
        // default to usual layer
        final OskariLayer layer = mapLayerService.find(layerId);
        // copy permissions from source layer to new analysis
        return permissionsService.getResource(Permissions.RESOURCE_TYPE_MAP_LAYER, new OskariLayerResource(layer).getMapping());
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
