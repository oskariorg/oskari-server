package fi.nls.oskari.control.data;

import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.oskari.log.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.XML;

import fi.mml.map.mapwindow.service.db.MapLayerService;
import fi.mml.map.mapwindow.service.db.MapLayerServiceIbatisImpl;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.wfs.WFSLayer;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AggregateMethodParams;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.domain.BufferMethodParams;
import fi.nls.oskari.map.analysis.domain.IntersectMethodParams;
import fi.nls.oskari.map.analysis.domain.UnionMethodParams;
import fi.nls.oskari.map.analysis.domain.UnionGeomMethodParams;
import fi.nls.oskari.map.analysis.domain.CollectGeometriesMethodParams;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.map.analysis.service.AnalysisWebProcessingService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

import fi.nls.oskari.wfs.WFSLayerConfiguration;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import fi.mml.portti.domain.ogc.util.WFSFilterBuilder;

@OskariActionRoute("CreateAnalysisLayer")
public class CreateAnalysisLayerHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(
            CreateAnalysisLayerHandler.class);
    private WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();

    private static final String PARAM_ANALYSE = "analyse";
    private static final String PARAM_FILTER = "filter";

    private static final String INTERNAL_FIELD_PREFIX = "__";

    private static final String DEFAULT_OUTPUT_FORMAT = "text/xml; subtype=gml/3.1.1";
    private static final int DEFAULT_OPACITY = 80;
    private static final String PARAMS_PROXY = "action_route=GetProxyRequest&serviceId=wfsquery&wfs_layer_id=";
    private static final String GEOSERVER_WPS_URL = "geoserver.wps.url";

    private static final String BUFFER = "buffer";
    private static final String INTERSECT = "intersect";
    private static final String AGGREGATE = "aggregate";
    private static final String UNION = "union";
    private static final String UNION_GEOM = "union_geom";

    private static final String JSON_KEY_METHODPARAMS = "methodParams";
    private static final String JSON_KEY_LAYERID = "layerId";
    private static final String JSON_KEY_FUNCTIONS = "functions";
    private static final String JSON_KEY_AGGRE_ATTRIBUTE = "attribute";
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
            //featureSet = this.processFeatureSet(analysisLayer);
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

        // Add data to analysis db if NOT aggregate
        if (analysisLayer.getMethod().equals(AGGREGATE)) {
            // No store to analysis db for aggregate - set results in to the
            // response
            analysisLayer.setWpsLayerId(-1);
            analysisLayer.setResult(this.parseAggregateResults(featureSet,
                    analysisLayer));
        } else if (analysisLayer.getMethod().equals(UNION_GEOM)) {
            // resultset is geometrycollection - build featurecollection for
            // wfst

            analysisLayer.setWpsLayerId(-1);

        } else {
            AnalysisDataService dataservice = new AnalysisDataService();
            Analysis analysis = dataservice.storeAnalysisData(featureSet,
                    analysisLayer, analyse, params.getUser());

            analysisLayer.setWpsLayerId(analysis.getId()); // aka. analysis_id
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
     * Parses method parameters to WPS syntax
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

        int id = json.optInt(JSON_KEY_LAYERID);
        if (id == 0) {
            throw new ActionParamsException("LayerId missing.");
        } else {
            analysisLayer.setId(id);

            // Get wfs layer configuration
            lc = layerConfigurationService.findConfiguration(id);
            // TODO: load layer
            MapLayerService mapLayerService = new MapLayerServiceIbatisImpl();
            wfsLayer = mapLayerService.findWFSLayer(id);
            log.debug("got wfs layer", wfsLayer);
            analysisLayer.setMinScale(wfsLayer.getMinScale());
            analysisLayer.setMaxScale(wfsLayer.getMaxScale());

            // Extract parameters for analysis methods from layer

        }

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
            // Remove internal fields - begins with "__"
            try {
                for (int i = 0; i < fields_in.length(); i++) {
                    if (fields_in.getString(i).indexOf(INTERNAL_FIELD_PREFIX) != 0)
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

        String analysisMethod = json.optString("method"); // "union_geom";  test 
        analysisLayer.setMethod(analysisMethod);

        analysisLayer.setAggreFunctions(null);

        if (BUFFER.equals(analysisMethod)) {
            // when analysisMethod == vec:BufferFeatureCollection

            // Set params for WPS execute

            BufferMethodParams method = this.parseBufferParams(lc, json,
                    baseUrl);

            analysisLayer.setAnalysisMethodParams(method);

            // WFS filter
            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter));

        } else if (UNION_GEOM.equals(analysisMethod)) {
            // when analysisMethod == geo:union

            // Set params for WPS execute

            CollectGeometriesMethodParams method = this.parseCollectGeometriesParams(lc, json,
                    baseUrl);

            analysisLayer.setAnalysisMethodParams(method);

            // WFS filter
            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter));

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
                throw new ActionParamsException("Intersect LayerId2 missing.");
            } else {
                // Get wfs layer configuration for union input 2
                lc2 = layerConfigurationService.findConfiguration(id2);
            }
            // Set params for WPS execute

            IntersectMethodParams method = this.parseIntersectParams(lc2, lc,
                    json, baseUrl);

            analysisLayer.setAnalysisMethodParams(method);
            // WFS filter

            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter));

        } else if (AGGREGATE.equals(analysisMethod)) {

            // 1 to n aggregate wps tasks
            String aggre_field = null;
            try {
                
                aggre_field = json.getJSONObject(
                        JSON_KEY_METHODPARAMS).optString(JSON_KEY_AGGRE_ATTRIBUTE);
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
                    baseUrl, aggre_field, analysisLayer
                            .getAggreFunctions());

            analysisLayer.setAnalysisMethodParams(method);
            // WFS filter

            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter));

        } else if (UNION.equals(analysisMethod)) {
            JSONObject params;
            try {
                params = json.getJSONObject(JSON_KEY_METHODPARAMS);
            } catch (JSONException e) {
                throw new ActionParamsException("Method parameters missing.");
            }
            WFSLayerConfiguration lc2 = null;
            int id2 = params.optInt(JSON_KEY_LAYERID);
            if (id2 == 0) {
                throw new ActionParamsException("Union LayerId2 missing.");
            } else {
                // Get wfs layer configuration for union input 2
                lc2 = layerConfigurationService.findConfiguration(id2);
            }
            // Set params for WPS execute

            UnionMethodParams method = this.parseUnionParams(lc2, lc, json,
                    baseUrl);

            analysisLayer.setAnalysisMethodParams(method);
            // WFS filter

            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter));

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
            method.setLayer_id(Integer.parseInt(lc.getLayerId()));
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
     * Parses geo:union method parameters for WPS execute variables
     * 
     * @param lc
     *            WFS layer configuration
     * @param json
     *            Method parameters and layer info from the front
     * @param baseUrl
     *            Url for Geoserver WPS reference input (input
     *            FeatureCollection)
     * @return CollectGeometriesMethodParams parameters for WPS execution
     ************************************************************************/
    private CollectGeometriesMethodParams parseCollectGeometriesParams(
            WFSLayerConfiguration lc, JSONObject json, String baseUrl)
            throws ActionParamsException {
        final CollectGeometriesMethodParams method = new CollectGeometriesMethodParams();

        method.setMethod(UNION_GEOM);
        
        try {
            // Url for Geoserver WPS execute (subprocess of execute)
          
            method.setLayer_id(Integer.parseInt(lc.getLayerId()));
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
            method.setLayer_id(Integer.parseInt(lc.getLayerId()));
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
            WFSLayerConfiguration lc2, JSONObject json, String baseUrl)
            throws ActionParamsException {
        UnionMethodParams method = new UnionMethodParams();
        // 
        method.setMethod(UNION);
        // General variable input and variable input of union input 1
        method.setLayer_id(Integer.parseInt(lc.getLayerId()));
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

        method.setLayer_id(Integer.parseInt(lc.getLayerId()));
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
     * @return String WFS filter xml
     ************************************************************************/
    private String parseFilter(WFSLayerConfiguration lc, String filter)
            throws ActionParamsException {

        JSONObject filter_js = null;
        if (filter != null)
            filter_js = JSONHelper.createJSONObject(filter);

        // Build filter
        final String[] srsCodes = lc.getSRSName().split(":");
        final String srsCode = srsCodes[srsCodes.length - 1];
        final String wfs_filter = WFSFilterBuilder.parseWfsFilter(filter_js,
                srsCode, lc.getGMLGeometryProperty());

        return wfs_filter;
    }

    private String processFeatureSet(AnalysisLayer analysisLayer)
            throws ActionParamsException, ServiceException {
        String featureSet = null;
        AnalysisWebProcessingService wps = new AnalysisWebProcessingService();
        featureSet = wps.requestFeatureSet(analysisLayer);
        // JTS:union second step
        // TODO: use other wps methods  gs:Query and buffer(0)
        if (analysisLayer.getMethod().equals(UNION_GEOM))
        {
            UnionGeomMethodParams method = new UnionGeomMethodParams();
            method.setMethod(UNION_GEOM);
            method.setGeomCollection(featureSet);
            analysisLayer.setAnalysisMethodParams(method);
            // Union geomcollection
            featureSet = wps.requestFeatureSet(analysisLayer);
        }

        return featureSet;
    }
}
