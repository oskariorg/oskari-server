package fi.nls.oskari.control.data;

import fi.nls.oskari.analysis.AnalysisParser;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.map.analysis.service.AnalysisWebProcessingService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

@OskariActionRoute("CreateAnalysisLayer")
public class CreateAnalysisLayerHandler extends ActionHandler {

    private static final Logger log = LogFactory
            .getLogger(CreateAnalysisLayerHandler.class);
    private AnalysisDataService analysisDataService = new AnalysisDataService();
    private AnalysisWebProcessingService wpsService = new AnalysisWebProcessingService();
    private AnalysisParser analysisParser = new AnalysisParser();

    private static final String PARAM_ANALYSE = "analyse";
    private static final String PARAM_FILTER = "filter";

    private static final String PARAMS_PROXY = "action_route=GetProxyRequest&serviceId=wfsquery&wfs_layer_id=";

    private static final String INTERSECT = "intersect";
    private static final String AGGREGATE = "aggregate";
    private static final String UNION = "union";
    private static final String LAYER_UNION = "layer_union";


    final private static String GEOSERVER_PROXY_BASE_URL = PropertyUtil.getOptional("analysis.baseproxy.url");

    /**
     * Handles action_route CreateAnalysisLayer
     *
     * @param params
     *            Ajax request parameters
     ************************************************************************/
    public void handleAction(ActionParameters params) throws ActionException {

       final String analyse = params.getHttpParam(PARAM_ANALYSE);
        // Test
        //final String analyse = "{\"name\":\"layerUnionTest_\",\"method\":\"layer_union\",\"fields\":[],\"fieldTypes\":{\"t3\":\"string\",\"t2\":\"string\",\"t1\":\"string\",\"t4\":\"string\",\"t5\":\"string\",\"t6\":\"string\",\"t7\":\"string\",\"t8\":\"string\"},\"layerId\":\"analysis_778_799\",\"layerType\":\"analysis\",\"methodParams\":{\"layers\":[\"analysis_778_799\",\"analysis_778_800\",\"analysis_778_801\"]},\"style\":{\"dot\":{\"size\":\"4\",\"color\":\"#CC9900\"},\"line\":{\"size\":\"2\",\"color\":\"#CC9900\"},\"area\":{\"size\":\"2\",\"lineColor\":\"#CC9900\",\"fillColor\":\"#FFDC00\"}}}";

        if (analyse == null) {
            throw new ActionParamsException("analyse parameter missing");
        }

        // filter conf data
        final String filter = params.getHttpParam(PARAM_FILTER);

        // Get baseProxyUrl
        final String baseUrl = getBaseProxyUrl(params);
        AnalysisLayer analysisLayer = null;
        try {
            analysisLayer = analysisParser.parseAnalysisLayer(analyse, filter, baseUrl);
        } catch (ServiceException e) {
            throw new ActionException("Unable to parse analysis", e);
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
                throw new ActionException("Unable to get WPS Feature Set", e);
            }
            // Check, if exception result set
            if (featureSet.indexOf("ows:Exception") > -1)
                throw new ActionParamsException(
                        "WPS-execute returns ows:Exception: " + featureSet);

            // Check, if any data in result set
            if (featureSet.indexOf("numberOfFeatures=\"0\"") > -1)
                throw new ActionParamsException("WPS-execute returns 0 features");
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
                    throw new ActionException("Unable to parse analysis for aggregate union", e);
                }
                try {
                    featureSet = wpsService.requestFeatureSet(analysisLayer);
                    // Harmonize namespaces and element names
                    featureSet = analysisParser.harmonizeElementNames(featureSet, analysisLayer);
                    featureSet = analysisParser.mergeAggregateResults2FeatureSet(featureSet, analysisLayer);
                } catch (ServiceException e) {
                    throw new ActionException("Unable to get WPS Feature Set for union", e);
                }

            }

            analysis = analysisDataService.storeAnalysisData(
                    featureSet, analysisLayer, analyse, params.getUser());
        }

            if (analysis == null) throw new ActionException("Unable to store Analysis data");

            analysisLayer.setWpsLayerId(analysis.getId()); // aka. analysis_id
            // Analysis field mapping
            analysisLayer.setLocaleFields(analysis);
            analysisLayer.setNativeFields(analysis);



        // Get analysisLayer JSON for response to front
        try {
            JSONObject analysisLayerJSON = analysisLayer.getJSON();
            ResponseHelper.writeResponse(params, analysisLayerJSON);
        } catch (JSONException e) {
            throw new ActionException("Unable to get AnalysisLayer JSON", e);
        }
    }


    /**
     * Parses WPS Proxy url via Oskari action route
     *
     * @param params
     *            Action parameters
     * @return String baseurl for Geoserver WPS reference WFS data input
     ************************************************************************/
    public String getBaseProxyUrl(ActionParameters params) {
        String baseurl = GEOSERVER_PROXY_BASE_URL;
        if(baseurl == null) {
            try {
                final URL url = new URL(params.getRequest().getRequestURL().toString());
                baseurl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
            }
            catch (Exception ignored) { }
        }

        final String baseAjaxUrl = PropertyUtil.get(params.getLocale(),
                GetAppSetupHandler.PROPERTY_AJAXURL);
        baseurl = baseurl + baseAjaxUrl + PARAMS_PROXY;
        log.debug("Analysis baseURL:", baseurl);
        return baseurl;
    }
}
