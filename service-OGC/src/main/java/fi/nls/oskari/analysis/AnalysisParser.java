package fi.nls.oskari.analysis;

import fi.mml.map.mapwindow.service.db.MapLayerService;
import fi.mml.map.mapwindow.service.db.MapLayerServiceIbatisImpl;
import fi.mml.portti.domain.ogc.util.WFSFilterBuilder;
import fi.nls.oskari.domain.map.wfs.WFSLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.*;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.map.analysis.service.AnalysisWebProcessingService;
import fi.nls.oskari.map.analysis.service.TransformationService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSLayerConfiguration;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.util.*;


public class AnalysisParser {

    private static final Logger log = LogFactory
            .getLogger(AnalysisParser.class);
    private WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();
    private AnalysisDataService analysisDataService = new AnalysisDataService();
    private static final TransformationService transformationService = new TransformationService();

    private MapLayerService mapLayerService = new MapLayerServiceIbatisImpl();


    private static final List<String> HIDDEN_FIELDS = Arrays.asList("ID",
            "__fid", "metaDataProperty", "description", "boundedBy", "name",
            "location", "__centerX", "__centerY", "geometry", "geom", "the_geom", "uuid");


    private static final String LAYER_PREFIX = "analysis_";
    private static final String MYPLACES_LAYER_PREFIX = "myplaces_";

    private static final String DEFAULT_OUTPUT_FORMAT = "text/xml; subtype=gml/3.1.1";
    private static final int DEFAULT_OPACITY = 80;
    private static final String PARAMS_PROXY = "action_route=GetProxyRequest&serviceId=wfsquery&wfs_layer_id=";
    private static final String FILTER_ID_TEMPLATE1 = "{\"filters\":[{\"caseSensitive\":false,\"attribute\":\"{propertyName}\",\"operator\":\"=\",\"value\":\"{propertyValue}\"}]}";
    private static final String FILTER_ID_TEMPLATE2 = "{\"caseSensitive\":false,\"attribute\":\"{propertyName}\",\"operator\":\"=\",\"value\":\"{propertyValue}}\"}";
    private static final String FILTER_ID_TEMPLATE3 = "[{\"caseSensitive\":false,\"attribute\":\"{propertyName}\",\"operator\":\"=\",\"value\":\"{propertyValue}\"}]";

    private static final String ANALYSIS_INPUT_TYPE_WFS = "wfs";
    private static final String ANALYSIS_INPUT_TYPE_GS_VECTOR = "gs_vector";
    private static final String ANALYSIS_BASELAYER_ID = "analysis.baselayer.id";
    private static final String ANALYSIS_RENDERING_URL = "analysis.rendering.url";
    private static final String ANALYSIS_RENDERING_ELEMENT = "analysis.rendering.element";
    private static final String ANALYSIS_WPS_ELEMENT_LOCALNAME = "analysis_data";
    private static final String ANALYSIS_PROPERTY_NAME = "analysis_id";
    private static final String WPS_INPUT_TYPE = "input_type";

    private static final String MYPLACES_BASELAYER_ID = "myplaces.baselayer.id";
    private static final String MYPLACES_PROPERTY_NAME = "category_id";

    private static final String ANALYSIS_WFST_GEOMETRY = "feature:geometry>";
    private static final String ANALYSIS_WPS_UNION_GEOM = "gml:geom>";
    private static final String ANALYSIS_GML_PREFIX = "gml:";

    private static final String ANALYSIS_WFST_PREFIX = "feature:";

    private static final String BUFFER = "buffer";
    private static final String INTERSECT = "intersect";
    private static final String AGGREGATE = "aggregate";
    private static final String UNION = "union";
    private static final String LAYER_UNION = "layer_union";

    private static final String JSON_KEY_METHODPARAMS = "methodParams";
    private static final String JSON_KEY_LAYERID = "layerId";
    private static final String JSON_KEY_FUNCTIONS = "functions";
    private static final String JSON_KEY_AGGRE_ATTRIBUTE = "attribute";
    private static final String JSON_KEY_FILTERS = "filters";
    private static final String JSON_KEY_LAYERS = "layers";
    private static final String JSON_KEY_FIELDTYPES = "fieldTypes";

    final String analysisBaseLayerId = PropertyUtil.get(ANALYSIS_BASELAYER_ID);
    final String myplacesBaseLayerId = PropertyUtil.get(MYPLACES_BASELAYER_ID);
    final String analysisRenderingUrl = PropertyUtil.get(ANALYSIS_RENDERING_URL);
    final String analysisRenderingElement = PropertyUtil.get(ANALYSIS_RENDERING_ELEMENT);


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
    public AnalysisLayer parseAnalysisLayer(String layerJSON, String filter,
                                             String baseUrl) throws ServiceException {
        AnalysisLayer analysisLayer = new AnalysisLayer();

        WFSLayer wfsLayer;
        JSONObject json = JSONHelper.createJSONObject(layerJSON);
        WFSLayerConfiguration lc = null;

        // analysis input data type - default is WFS layer
        analysisLayer.setInputType(ANALYSIS_INPUT_TYPE_WFS);
        // analysis rendering url
        analysisLayer.setWpsUrl(analysisRenderingUrl);
        // analysis element name
        analysisLayer.setWpsName(analysisRenderingElement);


        analysisLayer.setInputAnalysisId(null);
        int id = 0;
        try {
            // Analysis input property types
            this.prepareFieldtypeMap(analysisLayer, json);

            String sid = json.getString(JSON_KEY_LAYERID);

            // Input is wfs layer or analaysis layer or my places
            if (sid.indexOf(LAYER_PREFIX) == 0 ) {
                // Analysislayer is input
                if (!this.prepareAnalysis4Analysis(analysisLayer, json))
                    throw new ServiceException(
                            "AnalysisInAnalysis parameters are invalid");
                id = analysisLayer.getId();
            }
            else if (sid.indexOf(MYPLACES_LAYER_PREFIX) == 0) {
                // myplaces is input
                if (!this.prepareAnalysis4Myplaces(analysisLayer, json))
                    throw new ServiceException(
                            "AnalysisInMyPlaces parameters are invalid");
                id = analysisLayer.getId();
            } else {
                // Wfs layer id
                id = ConversionHelper.getInt(sid, -1);
            }
        } catch (JSONException e) {
            throw new ServiceException(
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

        // Set WFS input type, other than analysis_ and myplaces_- default is REFERENCE
        this.setWpsInputLayerType(lc.getWps_params(), analysisLayer);

        // Extract parameters for analysis methods from layer

        String name = json.optString("name");
        if (name.isEmpty()) {
            throw new ServiceException("Analysis name missing.");
        } else {
            analysisLayer.setName(name);
        }

        JSONArray fields_in = json.optJSONArray("fields");
        List<String> fields = new ArrayList<String>();

        if (fields_in == null) {
            throw new ServiceException("Fields missing.");
        } else {
            // Add fields of WFS service, if empty and all fields mode on
            if(fields_in.length() == 0) fields_in = this.getWfsFields(analysisLayer);
            // Remove internal fields
            try {
                for (int i = 0; i < fields_in.length(); i++) {
                    if (!HIDDEN_FIELDS.contains(fields_in.getString(i)))
                        fields.add(fields_in.getString(i));
                }
            } catch (JSONException e) {
                throw new ServiceException(
                        "Method fields parameters missing.");
            }
            analysisLayer.setFields(fields);

        }

        String style = json.optString("style");
        if (style.isEmpty()) {
            throw new ServiceException("Style missing.");
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
        analysisLayer.setMergeAnalysisLayers(null);

        //------------------LAYER_UNION -----------------------
        if (LAYER_UNION.equals(analysisMethod)) {
         JSONObject params;
            try {
           params = json.getJSONObject(JSON_KEY_METHODPARAMS);
        } catch (JSONException e) {
            throw new ServiceException("Method parameters missing.");
        }
                JSONArray sids = params.optJSONArray(JSON_KEY_LAYERS);
                // Loop merge layers - get analysis ids
                List<Long> ids = new ArrayList<Long>();
                List<String> mergelays = new ArrayList<String>();
                if (sids == null) {
                    throw new ServiceException("merge layers missing");
                } else {
                    try {
                        for (int i = 0; i < sids.length(); i++) {
                            Long aid = this.getAnalysisId(sids.getString(i));
                            if (aid > 0)
                            {
                                ids.add(aid);
                                mergelays.add(sids.getString(i));
                            }
                        }
                    } catch (JSONException e) {
                        throw new ServiceException("Merge layers missing.");
                    }
                    // Merge analysis Ids
                    analysisLayer.setMergeAnalysisIds(ids);
                    // Merge analysis Layers
                    analysisLayer.setMergeAnalysisLayers(mergelays);
                }
        }
        //------------------ BUFFER -----------------------
        else if (BUFFER.equals(analysisMethod)) {
            // when analysisMethod == vec:BufferFeatureCollection

            // Set params for WPS execute

            BufferMethodParams method = this.parseBufferParams(lc, json,
                    baseUrl);

            method.setWps_reference_type(analysisLayer.getInputType());
            analysisLayer.setAnalysisMethodParams(method);

            // WFS filter
            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter, analysisLayer
                            .getInputAnalysisId(), analysisLayer.getInputCategoryId()));
            // WFS Query properties
            analysisLayer.getAnalysisMethodParams().setProperties(
                    this
                            .parseProperties(analysisLayer.getFields(), lc
                                    .getFeatureNamespace(), lc
                                    .getGMLGeometryProperty()));
            //------------------ INTERSECT -----------------------
        } else if (INTERSECT.equals(analysisMethod)) {
            JSONObject params;
            try {
                params = json.getJSONObject(JSON_KEY_METHODPARAMS);
            } catch (JSONException e) {
                throw new ServiceException("Method parameters missing.");
            }
            WFSLayerConfiguration lc2 = null;
            int id2 = 0;
            String sid = "";
            try {
                sid = params.getString(JSON_KEY_LAYERID);
                // Input is wfs layer or analaysis layer
                if (sid.indexOf(LAYER_PREFIX) == 0) {
                    // Analysislayer is input
                    // eg. analyse_216_340
                    id2 = ConversionHelper.getInt(analysisBaseLayerId, 0);

                }else if (sid.indexOf(MYPLACES_LAYER_PREFIX) == 0) {
                    // Myplaces is input
                    id2 = ConversionHelper.getInt(myplacesBaseLayerId, 0);

                } else {
                    // Wfs layer id
                    id2 = ConversionHelper.getInt(sid, -1);
                }
            } catch (JSONException e) {
                throw new ServiceException(
                        "AnalysisInAnalysis parameters are invalid");
            }

            // Get wfs layer configuration for union input 2
            lc2 = layerConfigurationService.findConfiguration(id2);

            // Set params for WPS execute

            IntersectMethodParams method = this.parseIntersectParams(lc, lc2,
                    json, baseUrl);

            method.setWps_reference_type(analysisLayer.getInputType());
            if (sid.indexOf(LAYER_PREFIX) == 0 || sid.indexOf(MYPLACES_LAYER_PREFIX) == 0) {
                method.setWps_reference_type2(ANALYSIS_INPUT_TYPE_GS_VECTOR);
            } else {
                method.setWps_reference_type2(ANALYSIS_INPUT_TYPE_WFS);
            }
            // Set WFS input type, other than analysis_ and myplaces_- default is REFERENCE
            this.setWpsInputLayerType(lc.getWps_params(), analysisLayer);

            // WFS filter

            method.setFilter(this.parseFilter(lc, filter, analysisLayer
                    .getInputAnalysisId(), analysisLayer.getInputCategoryId()));

            if (sid.indexOf(MYPLACES_LAYER_PREFIX) == 0) {
                method.setFilter2(this.parseFilter(lc2, null, null, this
                        .getAnalysisInputId(params)));
            }
            else {
                method.setFilter2(this.parseFilter(lc2, null, this
                        .getAnalysisInputId(params), null));
            }
            // WFS Query properties
            method.setProperties(this.parseProperties(
                    analysisLayer.getFields(), lc.getFeatureNamespace(), lc
                    .getGMLGeometryProperty()));

            analysisLayer.setAnalysisMethodParams(method);
            //------------------ AGGREGATE -----------------------
        } else if (AGGREGATE.equals(analysisMethod)) {

            // 1 to n aggregate wps tasks
            String aggre_field = null;
            try {

                aggre_field = json.getJSONObject(JSON_KEY_METHODPARAMS)
                        .optString(JSON_KEY_AGGRE_ATTRIBUTE);
                if (analysisLayer.getInputType().equals(
                        ANALYSIS_INPUT_TYPE_GS_VECTOR))
                {
                    if(analysisLayer.getInputAnalysisId() != null)
                    {
                        aggre_field = analysisDataService
                                .SwitchField2AnalysisField(aggre_field,
                                        analysisLayer.getInputAnalysisId());
                    }
                }
                JSONArray aggre_func_in = json.getJSONObject(
                        JSON_KEY_METHODPARAMS).optJSONArray(JSON_KEY_FUNCTIONS);
                List<String> aggre_funcs = new ArrayList<String>();
                if (aggre_func_in == null) {
                    throw new ServiceException(
                            "Aggregate functions missing.");
                } else {
                    try {
                        for (int i = 0; i < aggre_func_in.length(); i++) {
                            aggre_funcs.add(aggre_func_in.getString(i));
                        }
                    } catch (JSONException e) {
                        throw new ServiceException(
                                "Aggregate functions missing.");
                    }
                    analysisLayer.setAggreFunctions(aggre_funcs);
                }

            } catch (JSONException e) {
                throw new ServiceException("Method parameters missing.");
            }

            // Set params for WPS execute
            if (aggre_field == null)
                throw new ServiceException(
                        "Aggregate field parameter missing.");
            AggregateMethodParams method = this.parseAggregateParams(lc, json,
                    baseUrl, aggre_field, analysisLayer.getAggreFunctions());

            method.setWps_reference_type(analysisLayer.getInputType());
            analysisLayer.setAnalysisMethodParams(method);
            // WFS filter

            analysisLayer.getAnalysisMethodParams().setFilter(
                    this.parseFilter(lc, filter, analysisLayer
                            .getInputAnalysisId(), analysisLayer.getInputCategoryId()));
            //------------------ UNION -----------------------
        } else if (UNION.equals(analysisMethod)) {
            JSONObject params;
            try {
                params = json.getJSONObject(JSON_KEY_METHODPARAMS);
            } catch (JSONException e) {
                throw new ServiceException("Method parameters missing.");
            }

            // Set params for WPS execute

            UnionMethodParams method = this.parseUnionParams(lc, json, baseUrl);
            method.setWps_reference_type(analysisLayer.getInputType());

            // WFS filter

            method.setFilter(this.parseFilter(lc, filter, analysisLayer
                    .getInputAnalysisId(), analysisLayer.getInputCategoryId()));

            analysisLayer.setAnalysisMethodParams(method);

        } else {
            throw new ServiceException("Method parameters missing.");
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
                                                 JSONObject json, String baseUrl) throws ServiceException {
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
            throw new ServiceException("Method parameters missing.");
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
            throws ServiceException {
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
            throw new ServiceException("Method parameters missing.");
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
                                               JSONObject json, String baseUrl) throws ServiceException {
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
            throw new ServiceException("Bbox parameters missing.");
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
            JSONObject json, String baseUrl) throws ServiceException {
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
            throw new ServiceException("Bbox parameters missing.");
        }

        return method;
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
    public String parseAggregateResults(String response,
                                         AnalysisLayer analysisLayer) {

        try {

            // convert xml/text String to JSON

            final JSONObject json = XML.toJSONObject(response); // all
            // Add field name
            final AggregateMethodParams aggreParams = (AggregateMethodParams) analysisLayer
                    .getAnalysisMethodParams();
            String fieldName = aggreParams.getAggreField1();
            if(analysisLayer.getInputAnalysisId() != null)
            {
                fieldName = analysisDataService
                        .SwitchField2OriginalField(fieldName,
                                analysisLayer.getInputAnalysisId());
            }
            json.put("fieldName", fieldName);
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
     * @throws fi.nls.oskari.service.ServiceException
     ************************************************************************/
    private String parseFilter(WFSLayerConfiguration lc, String filter,
                               String analysisId, String categoryId) throws ServiceException {

        JSONObject filter_js = null;
        try {
            if (filter == null) {
                String idfilter = null;
                if (analysisId != null) {
                    // Add analysis id filter when analysis in analysis
                    idfilter = FILTER_ID_TEMPLATE1.replace("{propertyName}", ANALYSIS_PROPERTY_NAME);
                    idfilter = idfilter.replace("{propertyValue}", analysisId);
                } else if (categoryId != null) {
                    // Add category id filter when myplaces in analysis
                    idfilter = FILTER_ID_TEMPLATE1.replace("{propertyName}", MYPLACES_PROPERTY_NAME);
                    idfilter = idfilter.replace("{propertyValue}", categoryId);
                }
                if (idfilter != null) filter_js = JSONHelper.createJSONObject(idfilter);

            } else {
                filter_js = JSONHelper.createJSONObject(filter);
                // Add analysis id filter when analysis in analysis
                if (filter_js.has(JSON_KEY_FILTERS)) {
                    String idfilter = null;
                    if (analysisId != null) {
                        // Add analysis id filter when analysis in analysis
                        idfilter = FILTER_ID_TEMPLATE2.replace("{propertyName}", ANALYSIS_PROPERTY_NAME);
                        idfilter = idfilter.replace("{propertyValue}", analysisId);
                    } else if (categoryId != null) {
                        // Add category id filter when myplaces in analysis
                        idfilter = FILTER_ID_TEMPLATE2.replace("{propertyName}", MYPLACES_PROPERTY_NAME);
                        idfilter = idfilter.replace("{propertyValue}", categoryId);
                    }
                    if (idfilter != null) {
                        JSONObject analysis_id_filter = JSONHelper
                                .createJSONObject(idfilter);
                        filter_js.getJSONArray(JSON_KEY_FILTERS).put(
                                analysis_id_filter);
                    }

                } else {
                    String idfilter = null;
                    if (analysisId != null) {
                        // Add analysis id filter when analysis in analysis
                        idfilter = FILTER_ID_TEMPLATE3.replace("{propertyName}", ANALYSIS_PROPERTY_NAME);
                        idfilter = idfilter.replace("{propertyValue}", analysisId);
                    } else if (categoryId != null) {
                        // Add category id filter when myplaces in analysis
                        idfilter = FILTER_ID_TEMPLATE3.replace("{propertyName}", MYPLACES_PROPERTY_NAME);
                        idfilter = idfilter.replace("{propertyValue}", categoryId);
                    }
                    if (idfilter != null) {
                        JSONArray idfilter_js = JSONHelper
                                .createJSONArray(idfilter);
                        filter_js.put(JSON_KEY_FILTERS, idfilter_js);
                    }

                }
            }
        } catch (JSONException e) {
            log.warn(e, "JSON parse failed");
        }

        // Build filter
        final String wfs_filter = WFSFilterBuilder.parseWfsFilter(filter_js,
                lc.getSRSName(), lc.getGMLGeometryProperty());

        return wfs_filter;
    }

    private String parseProperties(List<String> props, String ns,
                                   String geom_prop) throws ServiceException {

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

            String sid = this.getAnalysisInputId(json);
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
     * Setup extra data for analysis layer when input is myplaces
     *
     * @param analysisLayer
     *            analysis input layer data
     * @param json
     *            wps analysis parameters
     * @return false, if no id found
     */
    private boolean prepareAnalysis4Myplaces(AnalysisLayer analysisLayer,
                                             JSONObject json) {

        try {

            String sid = this.getAnalysisInputId(json);
            if (sid != null) {

                analysisLayer.setId(ConversionHelper.getInt(
                        myplacesBaseLayerId, 0));
                analysisLayer.setInputType(ANALYSIS_INPUT_TYPE_GS_VECTOR);
                analysisLayer.setInputCategoryId(sid);
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * Use gs_vector input type, when wfs input layer is in the same server as WPS service
     * @param wps_params
     * @param analysisLayer
     */
    private void setWpsInputLayerType(String wps_params, AnalysisLayer analysisLayer) {

        try {

            if (!wps_params.equals("{}")) {
                JSONObject json = JSONHelper.createJSONObject(wps_params);
                if(json.has(WPS_INPUT_TYPE))
                {
                    if(json.getString(WPS_INPUT_TYPE).equals(ANALYSIS_INPUT_TYPE_GS_VECTOR))analysisLayer.setInputType(ANALYSIS_INPUT_TYPE_GS_VECTOR);
                }
            }
        } catch (Exception e) {

        }

    }
    /**
     * Set analysis field types
     *
     * @param analysisLayer
     *            analysis input layer data
     * @param json
     *            wps analysis parameters
     * @return false, if no id found
     */
    private boolean prepareFieldtypeMap(AnalysisLayer analysisLayer,
                                        JSONObject json) {

        try {
            if (json.has(JSON_KEY_FIELDTYPES)) {
                JSONObject ftypes = json.getJSONObject(JSON_KEY_FIELDTYPES);
                Iterator<?> keys = ftypes.keys();

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    final String value = ftypes.getString(key);
                    analysisLayer.getFieldtypeMap().put(key, value);
                }
            }

        } catch (Exception e) {

        }
        return false;
    }
    /**
     * Get WFS service field names
     *
     * @param analysisLayer
     *            analysis input layer data
     *
     * @return field names
     */
    private JSONArray getWfsFields(AnalysisLayer analysisLayer) {
        JSONArray fields = new JSONArray();
        try {
            Map<String,String> map = analysisLayer.getFieldtypeMap();
            if (map != null)
            {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    fields.put( entry.getKey());
                }
            }


        } catch (Exception e) {

        }
        return fields;
    }
    /**
     * @param json
     *            wps analysis parameters
     * @return analysis id
     */
    private String getAnalysisInputId(JSONObject json) {

        try {

            String sid = json.optString(JSON_KEY_LAYERID);
            if (sid.indexOf(LAYER_PREFIX) == -1 && sid.indexOf(MYPLACES_LAYER_PREFIX) == -1)
                return null;
            String sids[] = sid.split("_");
            if (sids.length > 1) {
                // Old analysis is input for analysis or myplaces

                return sids[sids.length-1];
            }
        } catch (Exception e) {
            log.debug("Decoding analysis layer id failed: ", e);
        }
        return null;
    }
    private Long getAnalysisId(String sid) {

        long id = 0;
        try {
            String sids[] = sid.split("_");
            if (sids.length > 1) {

               id= Long.parseLong( sids[sids.length-1]);
            }
        } catch (Exception e) {
           id=0;
        }
        return id;
    }

    /**
     * Reform the featureset after WPS response for WFS-T
     * (fix prefixes, propertynames, etc)
     * @param featureSet
     * @param analysisLayer
     * @return
     */
    public String harmonizeElementNames(String featureSet,
                                         final AnalysisLayer analysisLayer) {

        try {

            final AnalysisMethodParams params = analysisLayer
                    .getAnalysisMethodParams();
            String[] enames = params.getTypeName().split(":");
            String ename = enames[0];
            if (enames.length > 1)
                ename = enames[1];
            String extraFrom = "gml:" + ename + "_";

            // Mixed perfixes to feature: prefix etc
            featureSet = featureSet.replace(extraFrom, ANALYSIS_WFST_PREFIX);

            extraFrom = ANALYSIS_GML_PREFIX + ename;
            String extraTo = ANALYSIS_WFST_PREFIX + ename;
            featureSet = featureSet.replace(extraFrom, extraTo);
            String[] geoms = params.getGeom().split(":");
            String geom = geoms[0];
            if (geoms.length > 1)
                geom = geoms[1];
            extraFrom = ANALYSIS_GML_PREFIX + geom + ">";
            featureSet = featureSet.replace(extraFrom, ANALYSIS_WFST_GEOMETRY);
            featureSet = featureSet.replace(ANALYSIS_WPS_UNION_GEOM,
                    ANALYSIS_WFST_GEOMETRY);
            featureSet = featureSet.replace(ANALYSIS_GML_PREFIX
                    + ANALYSIS_WPS_ELEMENT_LOCALNAME, ANALYSIS_WFST_PREFIX
                    + ANALYSIS_WPS_ELEMENT_LOCALNAME);
            featureSet = featureSet.replace(" NaN", "");
            featureSet = featureSet.replace("srsDimension=\"3\"",
                    "srsDimension=\"2\"");

        } catch (Exception e) {
            log.debug("Harmonizing element names failed: ", e);
        }
        return featureSet;
    }
    public AnalysisLayer parseSwitch2UnionLayer(AnalysisLayer analysisLayer, String layerJSON, String filter,
                                            String baseUrl) throws ServiceException {
        // Switch to UNION method
        layerJSON = layerJSON.replace("\"method\":\"aggregate\"","\"method\":\"union\"");

        AnalysisLayer al2 = this.parseAnalysisLayer(layerJSON, filter, baseUrl);
        al2.setResult(analysisLayer.getResult());
        return al2;

    }
    public String mergeAggregateResults2FeatureSet(String featureSet, AnalysisLayer analysisLayer){
        try {
            // Add aggregate results to FeatureCollection ( only to one feature)
            featureSet = transformationService.addPropertiesTo1stFeature(featureSet, analysisLayer.getResult());
        } catch (ServiceException e) {
            log.debug("Feature property insert to FeatureCollection failed: ", e);
        }
        return featureSet;
    }

    /**
     * Remove prefix in xml element
     * @param tag
     * @return element name without prefix
     */
    private String stripNamespace(final String tag) {

        String splitted[] = tag.split(":");
        if (splitted.length > 1) {
            return splitted[1];
        }
        return splitted[0];
    }

}
