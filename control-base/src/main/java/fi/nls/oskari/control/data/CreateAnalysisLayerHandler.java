package fi.nls.oskari.control.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import fi.mml.map.mapwindow.service.db.MapLayerService;
import fi.mml.map.mapwindow.service.db.MapLayerServiceIbatisImpl;
import fi.mml.portti.domain.ogc.util.WFSFilterBuilder;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.wfs.WFSLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AggregateMethodParams;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.domain.AnalysisMethodParams;
import fi.nls.oskari.map.analysis.domain.BufferMethodParams;
import fi.nls.oskari.map.analysis.domain.CollectGeometriesMethodParams;
import fi.nls.oskari.map.analysis.domain.IntersectMethodParams;
import fi.nls.oskari.map.analysis.domain.UnionGeomMethodParams;
import fi.nls.oskari.map.analysis.domain.UnionMethodParams;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.map.analysis.service.AnalysisWebProcessingService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wfs.WFSLayerConfiguration;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;

@OskariActionRoute("CreateAnalysisLayer")
public class CreateAnalysisLayerHandler extends ActionHandler {

    private static final Logger log = LogFactory
            .getLogger(CreateAnalysisLayerHandler.class);
    private WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();
    private AnalysisDataService analysisDataService = new AnalysisDataService();
    private MapLayerService mapLayerService = new MapLayerServiceIbatisImpl();

    private static final String PARAM_ANALYSE = "analyse";
    private static final String PARAM_FILTER = "filter";
    private static final List<String> HIDDEN_FIELDS = Arrays.asList("ID",
            "__fid", "metaDataProperty", "description", "name", "boundedBy",
            "location", "__centerX", "__centerY");

    private static final String INTERNAL_FIELD_PREFIX = "__";
    private static final String LAYER_PREFIX = "analysis_";

    private static final String DEFAULT_OUTPUT_FORMAT = "text/xml; subtype=gml/3.1.1";
    private static final int DEFAULT_OPACITY = 80;
    private static final String PARAMS_PROXY = "action_route=GetProxyRequest&serviceId=wfsquery&wfs_layer_id=";
    private static final String FILTER_ID_TEMPLATE1 = "{\"filters\":[{\"caseSensitive\":false,\"attribute\":\"analysis_id\",\"operator\":\"=\",\"value\":\"{analysisId}\"}]}";
    private static final String FILTER_ID_TEMPLATE2 = "{\"caseSensitive\":false,\"attribute\":\"analysis_id\",\"operator\":\"=\",\"value\":\"{analysisId}\"}";
    private static final String GEOSERVER_WPS_URL = "geoserver.wps.url";

    private static final String ANALYSIS_INPUT_TYPE_WFS = "wfs";
    private static final String ANALYSIS_INPUT_TYPE_GS_VECTOR = "gs_vector";
    private static final String ANALYSIS_BASELAYER_ID = "analysis.baselayer.id";
    private static final String ANALYSIS_RENDERING_URL = "analysis.rendering.url";
    private static final String ANALYSIS_WPS_ELEMENT_NAME = "ana:analysis_data";
    private static final String ANALYSIS_WFST_ELEMENT_NAME = "feature:analysis_data";
    private static final String ANALYSIS_WPS_ELEMENT_BUG = "gml:analysis_data";
    private static final String ANALYSIS_WFST_GEOMETRY = "feature:geometry";
    private static final String ANALYSIS_WPS_UNION_GEOM = "gml:geom";

    private static final String BUFFER = "buffer";
    private static final String INTERSECT = "intersect";
    private static final String AGGREGATE = "aggregate";
    private static final String UNION = "union";
    private static final String UNION_GEOM = "union_geom";

    private static final String JSON_KEY_METHODPARAMS = "methodParams";
    private static final String JSON_KEY_LAYERID = "layerId";
    private static final String JSON_KEY_FUNCTIONS = "functions";
    private static final String JSON_KEY_AGGRE_ATTRIBUTE = "attribute";
    private static final String JSON_KEY_FILTERS = "filters";

    final String analysisBaseLayerId = PropertyUtil.get(ANALYSIS_BASELAYER_ID);
    final String analysisRenderingUrl = PropertyUtil
            .get(ANALYSIS_RENDERING_URL);

    /**
     * Handles action_route CreateAnalysisLayer
     * 
     * @param params
     *            Ajax request parameters
     ************************************************************************/
    public void handleAction(ActionParameters params) throws ActionException {

        final String analyse = params.getHttpParam(PARAM_ANALYSE);
        if (analyse == null) {
            throw new ActionParamsException("analyse parameter missing");
        }
        // filter conf data
        final String filter = params.getHttpParam(PARAM_FILTER);

        // Get baseProxyUrl
        final String baseUrl = getBaseProxyUrl(params);
        final AnalysisLayer analysisLayer = this.parseAnalysisLayer(analyse,
                filter, baseUrl);

        // Generate WPS XML
        String featureSet;
        try {
            AnalysisWebProcessingService wps = new AnalysisWebProcessingService();
            featureSet = wps.requestFeatureSet(analysisLayer);
            // featureSet = this.processFeatureSet(analysisLayer);
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
        if (analysisLayer.getMethod().equals(UNION)) {
            // Harmonize namespaces and element names
            featureSet = this.harmonizeElementNames(featureSet, analysisLayer);
        }

        // Add data to analysis db if NOT aggregate
        if (analysisLayer.getMethod().equals(AGGREGATE)) {
            // No store to analysis db for aggregate - set results in to the
            // response
            analysisLayer.setWpsLayerId(-1);
            analysisLayer.setResult(this.parseAggregateResults(featureSet,
                    analysisLayer));
        } else {

            Analysis analysis = analysisDataService.storeAnalysisData(
                    featureSet, analysisLayer, analyse, params.getUser());

            analysisLayer.setWpsLayerId(analysis.getId()); // aka. analysis_id
            // Analysis field mapping
            analysisLayer.setLocaleFields(analysis);
            analysisLayer.setNativeFields(analysis);
        }

        // Get analysisLayer JSON for response to front
        try {
            JSONObject analysisLayerJSON = analysisLayer.getJSON();
            ResponseHelper.writeResponse(params, analysisLayerJSON);
        } catch (JSONException e) {
            throw new ActionException("Unable to get AnalysisLayer JSON", e);
        }
    }

    /**
     * Parses method parameters to WPS execute xml syntax
     * definition
     * 
     * @param layerJSON
     *            method parameters and layer info from the front
     * @param baseUrl
     *            Url for Geoserver WPS reference input (input
     *            FeatureCollection)
     * @return AnalysisLayer parameters for WPS execution
     ************************************************************************/
    private AnalysisLayer parseAnalysisLayer(String layerJSON, String filter,
            String baseUrl) throws ActionParamsException {
        AnalysisLayer analysisLayer = new AnalysisLayer();

        WFSLayer wfsLayer;
        JSONObject json = JSONHelper.createJSONObject(layerJSON);
        WFSLayerConfiguration lc = null;

        // analysis input data type - default is WFS layer
        analysisLayer.setInputType(ANALYSIS_INPUT_TYPE_WFS);
        // analysis rendering url
        analysisLayer.setWpsUrl(analysisRenderingUrl);
        // analysis element name
        analysisLayer.setWpsName(ANALYSIS_WPS_ELEMENT_NAME);

        analysisLayer.setInputAnalysisId(null);
        int id = 0;
        try {
            String sid = json.getString(JSON_KEY_LAYERID);

            // Input is wfs layer or analaysis layer
            if (sid.indexOf(LAYER_PREFIX) > -1) {
                // Analysislayer is input
                // eg. analyse_216_340
                if (!this.prepareAnalysis4Analysis(analysisLayer, json))
                    throw new ActionParamsException(
                            "AnalysisInAnalysis parameters are invalid");
                id = analysisLayer.getId();
            } else {
                // Wfs layer id
                id = ConversionHelper.getInt(sid, -1);
            }
        } catch (JSONException e) {
            throw new ActionParamsException(
                    "AnalysisInAnalysis parameters are invalid");
        }
        // --- WFS layer is analysis input
        analysisLayer.setId(id);

        // Get wfs layer configuration
        lc = layerConfigurationService.findConfiguration(id);

        wfsLayer = mapLayerService.findWFSLayer(id);
        log.debug("got wfs layer", wfsLayer);
        analysisLayer.setMinScale(wfsLayer.getMinScale());
        analysisLayer.setMaxScale(wfsLayer.getMaxScale());

        // Extract parameters for analysis methods from layer

        String name = json.optString("name");
        if (name.isEmpty()) {
            throw new ActionParamsException("Fields missing.");
        } else {
            analysisLayer.setName(name);
        }

        JSONArray fields_in = json.optJSONArray("fields");
        List<String> fields = new ArrayList<String>();
        if (fields_in == null) {
            throw new ActionParamsException("Fields missing.");
        } else {
            // Remove internal fields
            try {
                for (int i = 0; i < fields_in.length(); i++) {
                    if (!HIDDEN_FIELDS.contains(fields_in.getString(i)))
                        fields.add(fields_in.getString(i));
                }
            } catch (JSONException e) {
                throw new ActionParamsException(
                        "Method fields parameters missing.");
            }
            analysisLayer.setFields(fields);

        }

        String style = json.optString("style");
        if (style.isEmpty()) {
            throw new ActionParamsException("Style missing.");
        } else {
            analysisLayer.setStyle(style);
        }

        Integer opacity = json.optInt("opacity");
        if (opacity == 0)
            opacity = DEFAULT_OPACITY;
        analysisLayer.setOpacity(opacity);

        String analysisMethod = json.optString("method"); // "union_geom"; test
        analysisLayer.setMethod(analysisMethod);

        analysisLayer.setAggreFunctions(null);

        if (BUFFER.equals(analysisMethod)) {
            // when analysisMethod == vec:BufferFeatureCollection

            // Set params for WPS execute

            BufferMethodParams method = this.parseBufferParams(lc, json,
                    baseUrl);

            method.setWps_reference_type(analysisLayer.getInputType());
            analysisLayer.setAnalysisMethodParams(method);

            // WFS filter
            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter, analysisLayer
                            .getInputAnalysisId()));
            // WFS Query properties
            analysisLayer.getAnalysisMethodParams().setProperties(
                    this.parseProperties(analysisLayer.getFields(), lc
                            .getFeatureNamespace(),lc.getGMLGeometryProperty()));
        } else if (INTERSECT.equals(analysisMethod)) {
            JSONObject params;
            try {
                params = json.getJSONObject(JSON_KEY_METHODPARAMS);
            } catch (JSONException e) {
                throw new ActionParamsException("Method parameters missing.");
            }
            WFSLayerConfiguration lc2 = null;
            int id2 = params.optInt(JSON_KEY_LAYERID);
            if (id2 == 0) {

                // ---- Analysis in analysis layer
                if (getAnalysisInputId(params) != null) {
                    id2 = ConversionHelper.getInt(analysisBaseLayerId, 0);
                } else {
                    throw new ActionParamsException("Layer 2 id is invalid");
                }

            }
            // Get wfs layer configuration for union input 2
            lc2 = layerConfigurationService.findConfiguration(id2);

            // Set params for WPS execute

            IntersectMethodParams method = this.parseIntersectParams(lc2, lc,
                    json, baseUrl);

            method.setWps_reference_type(analysisLayer.getInputType());

            analysisLayer.setAnalysisMethodParams(method);
            // WFS filter

            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter, analysisLayer
                            .getInputAnalysisId()));

            analysisLayer.getAnalysisMethodParams()
                    .setFilter2(
                            this.parseFilter(lc, null, this
                                    .getAnalysisInputId(params)));

        } else if (AGGREGATE.equals(analysisMethod)) {

            // 1 to n aggregate wps tasks
            String aggre_field = null;
            try {

                aggre_field = json.getJSONObject(JSON_KEY_METHODPARAMS)
                        .optString(JSON_KEY_AGGRE_ATTRIBUTE);
                if (analysisLayer.getInputType().equals(
                        ANALYSIS_INPUT_TYPE_GS_VECTOR))
                    aggre_field = analysisDataService
                            .SwitchField2AnalysisField(aggre_field,
                                    analysisLayer.getInputAnalysisId());
                JSONArray aggre_func_in = json.getJSONObject(
                        JSON_KEY_METHODPARAMS).optJSONArray(JSON_KEY_FUNCTIONS);
                List<String> aggre_funcs = new ArrayList<String>();
                if (aggre_func_in == null) {
                    throw new ActionParamsException(
                            "Aggregate functions missing.");
                } else {
                    try {
                        for (int i = 0; i < aggre_func_in.length(); i++) {
                            aggre_funcs.add(aggre_func_in.getString(i));
                        }
                    } catch (JSONException e) {
                        throw new ActionParamsException(
                                "Aggregate functions missing.");
                    }
                    analysisLayer.setAggreFunctions(aggre_funcs);
                }

            } catch (JSONException e) {
                throw new ActionParamsException("Method parameters missing.");
            }

            // Set params for WPS execute
            if (aggre_field == null)
                throw new ActionParamsException(
                        "Aggregate field parameter missing.");
            AggregateMethodParams method = this.parseAggregateParams(lc, json,
                    baseUrl, aggre_field, analysisLayer.getAggreFunctions());

            method.setWps_reference_type(analysisLayer.getInputType());
            analysisLayer.setAnalysisMethodParams(method);
            // WFS filter

            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter, analysisLayer
                            .getInputAnalysisId()));

        } else if (UNION.equals(analysisMethod)) {
            JSONObject params;
            try {
                params = json.getJSONObject(JSON_KEY_METHODPARAMS);
            } catch (JSONException e) {
                throw new ActionParamsException("Method parameters missing.");
            }

            // Set params for WPS execute

            UnionMethodParams method = this.parseUnionParams(lc, json, baseUrl);
            method.setWps_reference_type(analysisLayer.getInputType());

            analysisLayer.setAnalysisMethodParams(method);
            // WFS filter

            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter, analysisLayer
                            .getInputAnalysisId()));

            analysisLayer.getAnalysisMethodParams()
                    .setFilter2(
                            this.parseFilter(lc, null, this
                                    .getAnalysisInputId(params)));

        } else {
            throw new ActionParamsException("Method parameters missing.");
        }

        return analysisLayer;
    }

    /**
     * Parses BUFFER method parameters for WPS execute xml variables
     * 
     * @param lc
     *            WFS layer configuration
     * @param json
     *            Method parameters and layer info from the front
     * @param baseUrl
     *            Url for Geoserver WPS reference input (input
     *            FeatureCollection)
     * @return BufferMethodParams parameters for WPS execution
     ************************************************************************/
    private BufferMethodParams parseBufferParams(WFSLayerConfiguration lc,
            JSONObject json, String baseUrl) throws ActionParamsException {
        final BufferMethodParams method = new BufferMethodParams();
        method.setMethod(BUFFER);
        // 
        try {
            method.setLayer_id(ConversionHelper.getInt(lc.getLayerId(), 0));
            method.setServiceUrl(lc.getURL());
            baseUrl = baseUrl.replace("&", "&amp;");
            method.setHref(baseUrl + String.valueOf(lc.getLayerId()));
            method.setTypeName(lc.getFeatureNamespace() + ":"
                    + lc.getFeatureElement());
            method.setMaxFeatures(String.valueOf(lc.getMaxFeatures()));
            method.setSrsName(lc.getSRSName());
            method.setOutputFormat(DEFAULT_OUTPUT_FORMAT);
            method.setVersion(lc.getWFSVersion());
            method.setXmlns("xmlns:" + lc.getFeatureNamespace() + "=\""
                    + lc.getFeatureNamespaceURI() + "\"");

            method.setGeom(lc.getGMLGeometryProperty());

            final JSONObject params = json.getJSONObject(JSON_KEY_METHODPARAMS);
            final JSONObject bbox = json.getJSONObject("bbox");
            method.setX_lower(bbox.optString("left"));
            method.setY_lower(bbox.optString("bottom"));
            method.setX_upper(bbox.optString("right"));
            method.setY_upper(bbox.optString("top"));

            method.setDistance(params.optString("distance"));

        } catch (JSONException e) {
            throw new ActionParamsException("Method parameters missing.");
        }

        return method;
    }

    /**
     * Parses AGGREGATE method parameters for WPS execute xml variables
     * 
     * @param lc
     *            WFS layer configuration
     * @param json
     *            Method parameters and layer info from the front
     * @param baseUrl
     *            Url for Geoserver WPS reference input (input
     *@param aggre_field
     *            Field name for aggregate function
     * @return AggregateMethodParams parameters for WPS execution
     ************************************************************************/
    private AggregateMethodParams parseAggregateParams(
            WFSLayerConfiguration lc, JSONObject json, String baseUrl,
            String aggre_field, List<String> aggre_funcs)
            throws ActionParamsException {
        AggregateMethodParams method = new AggregateMethodParams();
        // 
        method.setMethod(AGGREGATE);
        try {
            method.setLayer_id(ConversionHelper.getInt(lc.getLayerId(), 0));
            method.setServiceUrl(lc.getURL());
            baseUrl = baseUrl.replace("&", "&amp;");
            method.setHref(baseUrl + String.valueOf(lc.getLayerId()));
            method.setTypeName(lc.getFeatureNamespace() + ":"
                    + lc.getFeatureElement());
            method.setMaxFeatures(String.valueOf(lc.getMaxFeatures()));
            method.setSrsName(lc.getSRSName());
            method.setOutputFormat(DEFAULT_OUTPUT_FORMAT);
            method.setVersion(lc.getWFSVersion());
            method.setXmlns("xmlns:" + lc.getFeatureNamespace() + "=\""
                    + lc.getFeatureNamespaceURI() + "\"");

            method.setGeom(lc.getGMLGeometryProperty());

            JSONObject bbox = null;

            bbox = json.getJSONObject("bbox");
            method.setX_lower(bbox.optString("left"));
            method.setY_lower(bbox.optString("bottom"));
            method.setX_upper(bbox.optString("right"));
            method.setY_upper(bbox.optString("top"));

            // TODO: loop fields - current solution only for 1st field
            method.setAggreField1(aggre_field);
            method.setAggreFunctions(aggre_funcs);

        } catch (JSONException e) {
            throw new ActionParamsException("Method parameters missing.");
        }

        return method;
    }

    /**
     * Parses UNION method parameters for WPS execute xml variables
     * Originally vec:UnionFeatureCollection
     * Changed to geom union (gs:feature + subprocess gs:CollectGeometries
     * 
     * @param lc
     *            WFS layer configuration
     * @param json
     *            Method parameters and layer info from the front
     * @param baseUrl
     *            Url for Geoserver WPS reference input (input
     *            FeatureCollection)
     * @return UnionMethodParams parameters for WPS execution
     ************************************************************************/
    private UnionMethodParams parseUnionParams(WFSLayerConfiguration lc,
            JSONObject json, String baseUrl) throws ActionParamsException {
        UnionMethodParams method = new UnionMethodParams();
        // 
        method.setMethod(UNION);
        // General variable input and variable input of union input 1
        method.setLayer_id(ConversionHelper.getInt(lc.getLayerId(), 0));
        method.setServiceUrl(lc.getURL());
        baseUrl = baseUrl.replace("&", "&amp;");
        method.setHref(baseUrl + String.valueOf(lc.getLayerId()));
        method.setTypeName(lc.getFeatureNamespace() + ":"
                + lc.getFeatureElement());
        method.setLocalTypeName(lc.getFeatureElement());
        method.setMaxFeatures(String.valueOf(lc.getMaxFeatures()));
        method.setSrsName(lc.getSRSName());
        method.setOutputFormat(DEFAULT_OUTPUT_FORMAT);
        method.setVersion(lc.getWFSVersion());
        method.setXmlns("xmlns:" + lc.getFeatureNamespace() + "=\""
                + lc.getFeatureNamespaceURI() + "\"");
        method.setGeom(lc.getGMLGeometryProperty());

        JSONObject bbox = null;

        try {
            bbox = json.getJSONObject("bbox");
            method.setX_lower(bbox.optString("left"));
            method.setY_lower(bbox.optString("bottom"));
            method.setX_upper(bbox.optString("right"));
            method.setY_upper(bbox.optString("top"));
        } catch (JSONException e) {
            throw new ActionParamsException("Bbox parameters missing.");
        }

        return method;
    }

    /**
     * Parses INTERSECT method parameters for WPS execute xml variables
     * 
     * @param lc
     *            WFS layer configuration
     * @param json
     *            Method parameters and layer info from the front
     * @param baseUrl
     *            Url for Geoserver WPS reference input (input
     *            FeatureCollection)
     * @return IntersectMethodParams parameters for WPS execution
     ************************************************************************/
    private IntersectMethodParams parseIntersectParams(
            WFSLayerConfiguration lc, WFSLayerConfiguration lc2,
            JSONObject json, String baseUrl) throws ActionParamsException {
        IntersectMethodParams method = new IntersectMethodParams();
        //
        method.setMethod(INTERSECT);
        // General variable input and variable input of union input 1

        method.setLayer_id(ConversionHelper.getInt(lc.getLayerId(), 0));
        method.setServiceUrl(lc.getURL());
        baseUrl = baseUrl.replace("&", "&amp;");
        method.setHref(baseUrl + String.valueOf(lc.getLayerId()));
        method.setTypeName(lc.getFeatureNamespace() + ":"
                + lc.getFeatureElement());

        method.setMaxFeatures(String.valueOf(lc.getMaxFeatures()));
        method.setSrsName(lc.getSRSName());
        method.setOutputFormat(DEFAULT_OUTPUT_FORMAT);
        method.setVersion(lc.getWFSVersion());
        method.setXmlns("xmlns:" + lc.getFeatureNamespace() + "=\""
                + lc.getFeatureNamespaceURI() + "\"");
        method.setGeom(lc.getGMLGeometryProperty());

        // Variable values of Union input 2
        method.setHref2(baseUrl + String.valueOf(lc2.getLayerId()));
        method.setTypeName2(lc2.getFeatureNamespace() + ":"
                + lc2.getFeatureElement());
        method.setXmlns2("xmlns:" + lc2.getFeatureNamespace() + "=\""
                + lc2.getFeatureNamespaceURI() + "\"");
        method.setGeom2(lc2.getGMLGeometryProperty());

        JSONObject bbox = null;

        try {

            bbox = json.getJSONObject("bbox");
            method.setX_lower(bbox.optString("left"));
            method.setY_lower(bbox.optString("bottom"));
            method.setX_upper(bbox.optString("right"));
            method.setY_upper(bbox.optString("top"));

            // TODO: Intersect retain columns
            // A layer
            // method.setFieldA1(fieldA1);
            // B layer
            // method.setFieldA1(fieldB1);

        } catch (JSONException e) {
            throw new ActionParamsException("Bbox parameters missing.");
        }

        return method;
    }

    /**
     * Parses WPS Proxy url via Oskari action route
     * 
     * @param params
     *            Action parameters
     * @return String baseurl for Geoserver WPS reference WFS data input
     ************************************************************************/
    public String getBaseProxyUrl(ActionParameters params) {
        // TODO: baseurl setup to properties
        String baseurl = params.getRequest().getRequestURL().toString().split(
                "/portti2")[0];

        final String baseAjaxUrl = PropertyUtil.get(params.getLocale(),
                GetAppSetupHandler.PROPERTY_AJAXURL);
        baseurl = baseurl + baseAjaxUrl + PARAMS_PROXY;
        return baseurl;
    }

    /**
     * Parses AGGREGATE results for Oskari front
     * 
     * @param response
     *            WPS vec:aggregate execute results
     * @param analysisLayer
     *            analysis layer params (field/columns info)
     * @return JSON.toSting() eg. aggregate WPS results
     ************************************************************************/
    private String parseAggregateResults(String response,
            AnalysisLayer analysisLayer) {

        try {

            // convert xml/text String to JSON

            final JSONObject json = XML.toJSONObject(response); // all
            // Add field name
            final AggregateMethodParams aggreParams = (AggregateMethodParams) analysisLayer
                    .getAnalysisMethodParams();
            json.put("fieldName", aggreParams.getAggreField1());
            return json.toString();

        } catch (JSONException e) {
            log.error(e, "XML to JSON failed", response);
        }

        return "{}";
    }

    /**
     * Parses WFS filter
     * 
     * @param lc
     *            WFS layer configuration
     * @param filter
     *            WFS filter params
     * @param analysisId
     *            Analysis id when input is analysislayer, in other case null
     * @return String WFS filter xml
     * @throws ActionParamsException
     ************************************************************************/
    private String parseFilter(WFSLayerConfiguration lc, String filter,
            String analysisId) throws ActionParamsException {

        JSONObject filter_js = null;
        try {
            if (filter == null) {
                if (analysisId != null) {
                    // Add analysis id filter when analysis in analysis
                    filter_js = JSONHelper.createJSONObject(FILTER_ID_TEMPLATE1
                            .replace("{analysisId}", analysisId));
                }
            } else {
                filter_js = JSONHelper.createJSONObject(filter);
                // Add analysis id filter when analysis in analysis
                if (analysisId != null) {
                    JSONObject analysis_id_filter = JSONHelper
                            .createJSONObject(FILTER_ID_TEMPLATE2.replace(
                                    "{analysisId}", analysisId));

                    filter_js.getJSONArray(JSON_KEY_FILTERS).put(
                            analysis_id_filter);

                }
            }
        } catch (JSONException e) {
            log.warn(e, "JSON parse failed");
        }

        // Build filter
        final String[] srsCodes = lc.getSRSName().split(":");
        final String srsCode = srsCodes[srsCodes.length - 1];
        final String wfs_filter = WFSFilterBuilder.parseWfsFilter(filter_js,
                srsCode, lc.getGMLGeometryProperty());

        return wfs_filter;
    }

    private String parseProperties(List<String> props, String ns, String geom_prop)
            throws ActionParamsException {

        try {
            return WFSFilterBuilder.parseProperties(props, ns, geom_prop);

        } catch (Exception e) {
            log.warn(e, "Properties parse failed");
        }

        return null;
    }

    /**
     * Setup extra data for analysis layer when input is analysislayer
     * 
     * @param analysisLayer
     *            analysis input layer data
     * @param json
     *            wps analysis parameters
     * @return false, if no id found
     */
    private boolean prepareAnalysis4Analysis(AnalysisLayer analysisLayer,
            JSONObject json) {

        try {

            String sid = getAnalysisInputId(json);
            if (sid != null) {

                analysisLayer.setId(ConversionHelper.getInt(
                        analysisBaseLayerId, 0));
                analysisLayer.setInputType(ANALYSIS_INPUT_TYPE_GS_VECTOR);
                analysisLayer.setInputAnalysisId(sid);
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * @param json
     *            wps analysis parameters
     * @return analysis id
     */
    private String getAnalysisInputId(JSONObject json) {

        try {

            String sid = json.optString(JSON_KEY_LAYERID);
            if (sid.indexOf(LAYER_PREFIX) == -1)
                return null;
            String sids[] = sid.split("_");
            if (sids.length > 2) {
                // Old analysis is input for analysis (layerid:
                // analysis_216_340)
                return sids[2];
            }
        } catch (Exception e) {
            log.debug("Decoding analysis layer id failed: ", e);
        }
        return null;
    }

    private String harmonizeElementNames(String featureSet,
            final AnalysisLayer analysisLayer) {

        try {

            final AnalysisMethodParams params = analysisLayer
                    .getAnalysisMethodParams();
            featureSet = featureSet.replace(ANALYSIS_WPS_ELEMENT_BUG,
                    ANALYSIS_WFST_ELEMENT_NAME);
            featureSet = featureSet.replace(ANALYSIS_WPS_UNION_GEOM, ANALYSIS_WFST_GEOMETRY);
            featureSet = featureSet.replace(" NaN", "");
            featureSet = featureSet.replace("srsDimension=\"3\"",  "srsDimension=\"2\"");

        } catch (Exception e) {
            log.debug("Harmonizing element names failed: ", e);
        }
        return featureSet;
    }

    private String stripNamespace(final String tag) {

        String splitted[] = tag.split(":");
        if (splitted.length > 1) {
            return splitted[1];
        }
        return splitted[0];
    }

}
