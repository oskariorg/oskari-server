package fi.nls.oskari.control.data;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.analysis.AnalysisParser;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AggregateMethodParams;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.domain.AnalysisMethodParams;
import fi.nls.oskari.map.analysis.domain.SpatialJoinStatisticsMethodParams;
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
import java.util.*;

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
    private static final String PARAM_SAVE_BLN = "saveAnalyse";
    private static final String JSONFORMAT = "application/json";

    private static final String PARAMS_PROXY = "action_route=GetProxyRequest&serviceId=wfsquery&wfs_layer_id=";
    private static final String JSON_KEY_METHODPARAMS = "methodParams";
    private static final String JSON_KEY_LOCALES = "locales";
    private static final String JSON_KEY_FUNCTIONS = "functions";
    private static final String JSON_KEY_AGGREGATE_RESULT = "aggregate";
    private static final String JSON_KEY_GEOJSON = "geojson";

    private static final String AGGREGATE_STDDEV_WPS_IN = "StdDev";
    private static final String AGGREGATE_STDDEV_WPS_OUT = "StandardDeviation";

    private static final String COUNT_FUNCTION = "Count";

    private static final String ERROR_ANALYSE_PARAMETER_MISSING = "Analyse_parameter_missing";
    private static final String ERROR_UNABLE_TO_PARSE_ANALYSE = "Unable_to_parse_analysis";
    private static final String ERROR_UNABLE_TO_GET_WPS_FEATURES = "Unable_to_get_WPS_features";
    private static final String ERROR_WPS_EXECUTE_RETURNS_EXCEPTION = "WPS_execute_returns_Exception";
    private static final String ERROR_WPS_EXECUTE_RETURNS_NO_FEATURES = "WPS_execute_returns_no_features";
    private static final String ERROR_UNABLE_TO_MERGE_ANALYSIS_DATA = "Unable_to_merge_analysis_data";
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
        params.requireLoggedInUser();

        final String analyse = params.getRequiredParam(PARAM_ANALYSE, ERROR_ANALYSE_PARAMETER_MISSING);
        JSONObject analyseJson = JSONHelper.createJSONObject(analyse);
        if (analyseJson == null) {
            // json corrupted/parsing failed
            throw new ActionParamsException(ERROR_ANALYSE_PARAMETER_MISSING);
        }

        // filter conf data
        final String filter1 = params.getHttpParam(PARAM_FILTER1);
        final String filter2 = params.getHttpParam(PARAM_FILTER2);

        // Get baseProxyUrl
        final String baseUrl = getBaseProxyUrl(params);

        // User
        String uuid = params.getUser().getUuid();
        // note! analysisLayer is replaced in aggregate handling!!
        AnalysisLayer analysisLayer = getAnalysisLayer(analyseJson, filter1, filter2, baseUrl, uuid);
        Analysis analysis = null;

        if (analysisLayer.getMethod().equals(AnalysisParser.LAYER_UNION)) {
            // no WPS for merge analysis
            try {
                analysis = analysisDataService.mergeAnalysisData(
                    analysisLayer, analyse, params.getUser());
            } catch (ServiceException e) {
                throw new ActionException(ERROR_UNABLE_TO_MERGE_ANALYSIS_DATA, e);
            }
        } else {
            // Generate WPS XML
            String featureSet = executeWPSprocess(analysisLayer);
            if (analysisLayer.getMethod().equals(AnalysisParser.UNION)
                    || analysisLayer.getMethod().equals(AnalysisParser.INTERSECT)
                    || analysisLayer.getMethod().equals(AnalysisParser.SPATIAL_JOIN)) {
                // Harmonize namespaces and element names
                featureSet = analysisParser.harmonizeElementNames(featureSet, analysisLayer);
            }

            // Add data to analysis db  - we must create still an union in aggregate case
            if (analysisLayer.getMethod().equals(AnalysisParser.AGGREGATE)) {
                // No store to analysis db for aggregate - set results in to the
                // response
                //Save analysis results - use union of input data
                analysisLayer.setWpsLayerId(-1);
                final String aggregateResult = this.localiseAggregateResult(
                        analysisParser.parseAggregateResults(featureSet, analysisLayer), analyseJson);
                log.debug("Aggregate results:", aggregateResult);

                analysisLayer.setResult(aggregateResult);

                // Get geometry for aggretage features
                try {
                    // Just return result as JSON and don't save analysis to DB
                    if (!params.getHttpParam(PARAM_SAVE_BLN, true)) {
                        // NOTE!! Replacing the analysisLayer content for executing wps union method!
                        // Get response as geojson when no db store
                        analysisLayer = getAggregateLayer(analyse, filter1, filter2, baseUrl, analysisLayer, JSONFORMAT);
                        // Get geometry as geojson for hilighting features of aggregate result
                        featureSet = wpsService.requestFeatureSet(analysisLayer);
                        // Just return result as JSON and don't save analysis to DB
                        JSONObject geojson = JSONHelper.createJSONObject(featureSet);
                        JSONObject jsaggregate = JSONHelper.createJSONObject(aggregateResult);
                        //reorder resultset columns and row accoding to input params order
                        JSONArray jsaggreOrdered = analysisParser.reorderAggregateResult(jsaggregate,this.getRowOrder(analysisLayer),
                                this.getColumnOrder(analyseJson));
                        JSONObject results = new JSONObject();
                        JSONHelper.putValue(results, JSON_KEY_GEOJSON, geojson);
                        JSONHelper.putValue(results, JSON_KEY_AGGREGATE_RESULT,jsaggreOrdered);
                        ResponseHelper.writeResponse(params, results);
                        return;
                    }
                    // NOTE!! Replacing the analysisLayer!  - response is gml
                    analysisLayer = getAggregateLayer(analyse, filter1, filter2, baseUrl, analysisLayer, null);
                    featureSet = wpsService.requestFeatureSet(analysisLayer);
                    // Harmonize namespaces and element names
                    featureSet = analysisParser.harmonizeElementNames(featureSet, analysisLayer);
                    featureSet = analysisParser.mergeAggregateResults2FeatureSet(featureSet, analysisLayer, this.getRowOrder(analysisLayer),
                            this.getColumnOrder(analyseJson));
                    // Redefine column types
                    analysisLayer.setFieldtypeMap(this.getAggregateFieldTypes(this.getColumnOrder(analyseJson)));
                } catch (ServiceException e) {
                    throw new ActionException(ERROR_UNABLE_TO_GET_FEATURES_FOR_UNION, e);
                }

            }
            // Add extra TypeNames (depends on wps method)
            analysisParser.fixTypeNames(analysisLayer, analyseJson);

            // Fix property names for WFST (property names might be renamed in Wps method )
            featureSet = fixPropertyNames(featureSet, analysisLayer);

            try {
                analysis = analysisDataService.storeAnalysisData(
                        featureSet, analysisLayer, analyse, params.getUser());
            } catch (ServiceException e) {
                throw new ActionException(ERROR_UNABLE_TO_STORE_ANALYSIS_DATA, e);
            }
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
        final Resource sourceResource =
                getSourcePermission(analysisParser.getSourceLayerId(analyseJson), params.getUser());
        if(sourceResource != null) {
            final Resource analysisResource = new Resource();
            analysisResource.setType(AnalysisLayer.TYPE);
            analysisResource.setMapping("analysis", Long.toString(analysis.getId()));
            for(Permission p : sourceResource.getPermissions()) {
                // check if user has role matching permission?
                if(p.isOfType(Permissions.PERMISSION_TYPE_PUBLISH) || p.isOfType(Permissions.PERMISSION_TYPE_VIEW_PUBLISHED) || p.isOfType(Permissions.PERMISSION_TYPE_DOWNLOAD)) {
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
        Set<String> downloadPermissionsList = permissionsService.getDownloadPermissions(AnalysisLayer.TYPE);
        Set<String> editAccessList = null;
        String permissionKey = "analysis+" + analysis.getId();
        JSONObject permissions = OskariLayerWorker.getPermissions(params.getUser(), permissionKey, permissionsList, downloadPermissionsList, editAccessList);
        JSONHelper.putValue(analysisLayerJSON, "permissions", permissions);

        ResponseHelper.writeResponse(params, analysisLayerJSON);
    }

    private AnalysisLayer getAggregateLayer(String analyse, String filter1, String filter2,
                                      String baseUrl, AnalysisLayer analysisLayer, String outputFormat) throws ActionParamsException {
        try {
            return analysisParser.parseSwitch2UnionLayer(analysisLayer, analyse, filter1, filter2, baseUrl, outputFormat);
        } catch (ServiceException e) {
            throw new ActionParamsException(ERROR_UNABLE_TO_PROCESS_AGGREGATE_UNION, e.getMessage());
        }
    }

    private AnalysisLayer getAnalysisLayer(JSONObject analyseJson, String filter1, String filter2, String baseUrl,
                                           String uuid) throws ActionParamsException {
        try {
            return analysisParser.parseAnalysisLayer(analyseJson, filter1, filter2, baseUrl, uuid);
        } catch (ServiceException e) {
            throw new ActionParamsException(ERROR_UNABLE_TO_PARSE_ANALYSE, e.getMessage());
        }
    }

    private String executeWPSprocess(AnalysisLayer analysisLayer) throws ActionParamsException {
        String featureSet;
        try {
            featureSet = this.requestFeatureSets(analysisLayer);
        } catch (ServiceException e) {
            throw new ActionParamsException(ERROR_UNABLE_TO_GET_WPS_FEATURES, e.getMessage());
        }
        // Check, if exception result set
        if (featureSet == null || featureSet.indexOf("ows:Exception") > -1) {
            throw new ActionParamsException(ERROR_WPS_EXECUTE_RETURNS_EXCEPTION, featureSet);
        }

        // Check, if any data in result set
        if (featureSet.isEmpty() || featureSet.indexOf("numberOfFeatures=\"0\"") > -1) {
            throw new ActionParamsException(ERROR_WPS_EXECUTE_RETURNS_NO_FEATURES);
        }
        return featureSet;
    }

    private Resource getSourcePermission(final String layerId, final User user) {
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
                baseurl = url.getProtocol() + "://" + url.getHost();
                if(url.getPort() != -1) {
                    baseurl = baseurl + ":" + url.getPort();
                }
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

    /**
     * Get GeoServer WPS execute responses
     *
     * @param analysisLayer analysis method parameters
     * @return one or more Wps execute responses
     * @throws ServiceException
     */
    private String requestFeatureSets(AnalysisLayer analysisLayer) throws ServiceException {

        String featureSet = null;
        Boolean doRequest = true;
        if (analysisLayer.getMethod().equals(AnalysisParser.AGGREGATE)) {
            StringBuilder sb = new StringBuilder();
            // Loop aggregate attribute fields
            // Temp save  aggregate function setup
            List<String> aggre_funcs = ((AggregateMethodParams) analysisLayer.getAnalysisMethodParams()).getAggreFunctions();
            List<String> aggre_text_funcs = new ArrayList<String>();
            aggre_text_funcs.add(COUNT_FUNCTION);
            for (String field : analysisLayer.getFields()) {
                ((AggregateMethodParams) analysisLayer.getAnalysisMethodParams()).setAggreField1(field);
                if (analysisLayer.getFieldtypeMap().containsKey(field)) {
                    if (analysisLayer.getFieldtypeMap().get(field).equals("numeric")) {
                        ((AggregateMethodParams) analysisLayer.getAnalysisMethodParams()).setAggreFunctions(aggre_funcs);
                        if (aggre_funcs.size() == 0) {
                            doRequest = false;
                        }
                    } else {

                        ((AggregateMethodParams) analysisLayer.getAnalysisMethodParams()).setAggreFunctions(aggre_text_funcs);
                        doRequest = true;

                    }
                }
                sb.append("<fieldResult>");
                sb.append("<field>" + field + "</field>");
                if (doRequest) sb.append(wpsService.requestFeatureSet(analysisLayer));
                if (analysisLayer.isNodataCount() && analysisLayer.getFieldtypeMap().get(field).equals("numeric")) {
                    // Special aggregate process for NoDataCount - use count method with specific filter
                    ((AggregateMethodParams) analysisLayer.getAnalysisMethodParams()).setAggreFunctions(aggre_text_funcs);
                    ((AggregateMethodParams) analysisLayer.getAnalysisMethodParams()).setDoNoDataCount(true);
                    sb.append("<fieldNoDataCount>");
                    String nodataresponse = wpsService.requestFeatureSet(analysisLayer);
                    // Wps wps input features fails, if no nodata feature fields (exception) --> skip
                    // Could be another wps exception as well
                    if (nodataresponse.indexOf("ows:Exception") == -1) sb.append(nodataresponse);
                    sb.append("</fieldNoDataCount>");
                    ((AggregateMethodParams) analysisLayer.getAnalysisMethodParams()).setDoNoDataCount(false);
                }
                sb.append("</fieldResult>");
            }

            featureSet = sb.toString();
        }
        else if (analysisLayer.getMethod().equals(AnalysisParser.DIFFERENCE)) {
            // Get feature set via WFS 2.0 GetFeature
            // WFS join select is not available in wfs 1.1.0
            featureSet = wpsService.requestWFS2FeatureSet(analysisLayer);

        } else {
            featureSet = wpsService.requestFeatureSet(analysisLayer);
        }
        return featureSet;
    }

    /**
     * Pure localisation of aggregate result function names
     * @param result
     * @param analysejs
     * @return localised result text
     */
    private String localiseAggregateResult(String result, JSONObject analysejs) {

        try {
            JSONArray locales = analysejs.getJSONObject(JSON_KEY_METHODPARAMS)
                    .optJSONArray(JSON_KEY_LOCALES);
            JSONArray aggre_funcs = analysejs.getJSONObject(
                    JSON_KEY_METHODPARAMS).optJSONArray(JSON_KEY_FUNCTIONS);
            if (locales != null && aggre_funcs.length() == locales.length()) {
                for (int i = 0; i < locales.length(); i++) {
                    String funcName = aggre_funcs.getString(i);
                    // Plääh WPS funcnames are not equal to result names
                    if(funcName.equals(AGGREGATE_STDDEV_WPS_IN)) funcName = AGGREGATE_STDDEV_WPS_OUT;
                    result = result.replace(funcName,locales.getString(i));

                }
            }
        } catch (Exception e) {
            log.warn("Aggregate result localisation failed ", e);

        }
        return result;
    }
    /**
     * Get Aggregate result field types (
     * @param newcols  new column names in resultset
     * @return List of localised method names
     */
    private Map<String,String> getAggregateFieldTypes( List<String> newcols) {
        Map<String,String> map = new HashMap<String,String>();
        for (String col : newcols){
            map.put(col.replace(" ","_"),"numeric");
        }
        return map;
    }
    /**
     * Get property column order of aggregate result (
     * @param analysejs  analysis params
     * @return List of localised method names
     */
    private List<String> getColumnOrder( JSONObject analysejs) {
        List<String> list = new ArrayList<String>();
        try {
            JSONArray locales = analysejs.getJSONObject(JSON_KEY_METHODPARAMS)
                    .optJSONArray(JSON_KEY_LOCALES);
            JSONArray aggre_funcs = analysejs.getJSONObject(
                    JSON_KEY_METHODPARAMS).optJSONArray(JSON_KEY_FUNCTIONS);
            if (locales != null && aggre_funcs.length() == locales.length()) {
                for (int i = 0; i < locales.length(); i++) {
                    list.add(locales.getString(i));
                }
            }
        } catch (Exception e) {
            log.warn("Aggregate column order fetch failed ", e);

        }
        return list;
    }
    /**
     * Get property row order of aggregate result (
     * @param analysisLayer  analysis params
     * @return List of row names
     */
    private List<String> getRowOrder( AnalysisLayer analysisLayer) {
        List<String> list = new ArrayList<String>();
        String analysisId = analysisLayer.getInputAnalysisId();
        try {

            if (analysisLayer.getFields() != null) {
                for (int i = 0; i < analysisLayer.getFields().size(); i++) {
                    String field = analysisLayer.getFields().get(i);
                    if(analysisId != null && !analysisId.isEmpty()){
                        // Switch locale field name
                      field =  analysisDataService.SwitchField2OriginalField(field,  analysisId);
                    }
                    list.add(field);
                }
            }
        } catch (Exception e) {
            log.warn("Aggregate row order fetch failed ", e);

        }


        return list;
    }
    /**
     * Fix the geometry property name for WFST transform
     * Geometry property name in WPS method result is not the same as in input featurecollections
     * @param featureSet  xml featureCollection
     * @param analysisLayer
     */
    private String fixPropertyNames(String featureSet, AnalysisLayer analysisLayer) {

        try {

            AnalysisMethodParams params = analysisLayer.getAnalysisMethodParams();
            if (params.getMethod().equals(AnalysisParser.SPATIAL_JOIN_STATISTICS)){
                featureSet = featureSet.replace("feature:z_", "feature:");
                params.setGeom(((SpatialJoinStatisticsMethodParams) params).getGeom2());

            }


        } catch (Exception e) {
            log.warn("FeatureCollection property rename  failed ", e);

        }

        return featureSet;

    }


}
