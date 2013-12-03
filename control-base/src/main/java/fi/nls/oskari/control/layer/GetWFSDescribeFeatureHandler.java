package fi.nls.oskari.control.layer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.GetWFSDescribeFeature;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wfs.WFSLayerConfiguration;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;

/**
 * Get WMS capabilites and return JSON
 */
@OskariActionRoute("GetWFSDescribeFeature")
public class GetWFSDescribeFeatureHandler extends ActionHandler {

    private static final Logger log = LogFactory
            .getLogger(GetWFSDescribeFeatureHandler.class);
    private final WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();
    private AnalysisDataService analysisDataService = new AnalysisDataService();

    private static final String PARM_LAYER_ID = "layer_id";
    private static final String PARM_WFSURL = "wfsurl";
    
    private static final String KEY_SEQUENCE = "xsd:sequence";
    private static final String KEY_COMPLEXTYPE = "xsd:complexType";
    private static final String KEY_ELEMENT = "xsd:element";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_PROPERTYTYPES = "propertyTypes";
    public static final String ANALYSIS_PREFIX = "analysis_";

    private static final List<String> NUMERIC_FIELD_TYPES = Arrays.asList("double",
            "byte",
            "decimal",
            "int",
            "integer",
            "long",
            "negativeInteger",
            "nonNegativeInteger",
            "nonPositiveInteger",
            "positiveInteger",
            "short",
            "unsignedLong",
            "unsignedInt",
            "unsignedShort",
            "unsignedByte"
    );

    // is needed ?? private String[] permittedRoles = new String[0];

    @Override
    public void init() {
        super.init();
        // permittedRoles =
        // PropertyUtil.getCommaSeparatedList("actionhandler.GetWFSDescribeFeatureHandler.roles");
    }

    public void handleAction(ActionParameters params) throws ActionException {

        final String layer_id = params.getHttpParam(PARM_LAYER_ID, "");
        JSONObject fea_properties = new JSONObject();

        String wfsurl = params.getHttpParam(PARM_WFSURL, "");
        String userName = "";
        String password = "";

        if (!layer_id.isEmpty()) {
            final int id = ConversionHelper.getInt(layer_id, 0);
            if (id > 0) {
                // Get WFS url in wfs layer configuration
                WFSLayerConfiguration lc = layerConfigurationService
                        .findConfiguration(id);
                wfsurl = this.parseDescribeFeatureUrl(lc);
                userName = lc.getUsername();
                password = lc.getPassword();


                if (wfsurl.isEmpty()) {
                    throw new ActionParamsException(
                            "Parameter 'wfsurl' is missing or is not in wfs configuration ");
                }
                // if (!params.getUser().hasAnyRoleIn(permittedRoles)) {
                // throw new
                // ActionDeniedException("Unauthorized user tried to get wmsservices");
                // }
                final String response = GetWFSDescribeFeature.getResponse(wfsurl,
                        userName, password);

                final JSONObject rawfea_properties = GetWFSDescribeFeature
                        .parseFeatureProperties(response);

                // Simple type match (string or numeric)
                fea_properties = populatePropertiesSimple(layer_id,
                        rawfea_properties);

                // IF NEEDED for exact wfs feature type
                //final JSONObject fea_properties = populateProperties(layer_id,
                //        rawfea_properties);
            } else {
                // Set analysis layer field types
                fea_properties = getAnalysisFeaturePropertyTypes(layer_id);

            }
        }

        ResponseHelper.writeResponse(params, fea_properties);

    }

    private JSONObject findsubJson(String mykey, JSONObject js) {
        try {
            if (js == null)
                return null;
            Iterator<?> keys = js.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (js.get(key) instanceof JSONObject) {
                    JSONObject jssub = js.getJSONObject(key);
                    if (mykey.toUpperCase().equals(key.toUpperCase())) {
                        return jssub;
                    } else {
                        JSONObject jssub2 = findsubJson(mykey, jssub);
                        if (jssub2 != null)
                            return jssub2;
                    }

                }
            }
        } catch (JSONException e) {
            log.warn(e, "JSON parse failed");
        }
        return null;

    }

    private JSONArray findsubJsonArray(String mykey, JSONObject js) {
        try {
            if (js == null)
                return null;
            Iterator<?> keys = js.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (js.get(key) instanceof JSONArray) {
                    JSONArray jssub = js.getJSONArray(key);
                    if (mykey.toUpperCase().equals(key.toUpperCase())) {
                        return jssub;
                    }

                } else if (js.get(key) instanceof JSONObject) {
                    JSONObject jssubb = js.getJSONObject(key);
                    JSONArray jssub2 = findsubJsonArray(mykey, jssubb);
                    if (jssub2 != null)
                        return jssub2;
                }
            }
        } catch (JSONException e) {
            log.warn(e, "JSON parse failed");
        }
        return null;

    }

    /**
     * @param layer_id
     *            WFS layer id in Oskari DB
     * @param js
     *            raw DescribeFeatureType response
     * @return JSONObject property names and WFS types
     */
    private JSONObject populateProperties(String layer_id, JSONObject js) {
        JSONObject js_out = new JSONObject();
        JSONObject js_props_out = new JSONObject();
        try {
            if (js == null)
                return null;
            // Find name type sequence xsd:complexType or xsd:sequence
            JSONObject js_raw = this.findXsdElement(js);
            JSONArray props_js = js_raw.getJSONArray(KEY_ELEMENT);
            // loop Array
            for (int i = 0; i < props_js.length(); i++) {
                if (props_js.get(i) instanceof JSONObject) {
                    JSONObject prop_js = props_js.getJSONObject(i);
                    if (prop_js.has(KEY_NAME) && prop_js.has(KEY_TYPE)) {
                        js_props_out.put(prop_js.getString(KEY_NAME), prop_js
                                .getString(KEY_TYPE));
                    }
                }
            }
            js_out.put(PARM_LAYER_ID, layer_id);
            js_out.put(KEY_PROPERTYTYPES, js_props_out);

        } catch (Exception e) {
            log.warn(e, "JSON parse failed");
        }
        return js_out;

    }
    /**
     * @param layer_id
     *            WFS layer id in Oskari DB
     * @param js
     *            raw DescribeFeatureType response
     * @return JSONObject property names and types (string or numeric)
     */
    private JSONObject populatePropertiesSimple(String layer_id, JSONObject js) {
        JSONObject js_out = new JSONObject();
        JSONObject js_props_out = new JSONObject();
        try {
            if (js == null)
                return null;
            // Find name type sequence xsd:complexType or xsd:sequence
            JSONObject js_raw = this.findXsdElement(js);
            JSONArray props_js = js_raw.getJSONArray(KEY_ELEMENT);
            // loop Array
            for (int i = 0; i < props_js.length(); i++) {
                if (props_js.get(i) instanceof JSONObject) {
                    JSONObject prop_js = props_js.getJSONObject(i);
                    if (prop_js.has(KEY_NAME) && prop_js.has(KEY_TYPE)) {
                        String strigOrNumeric = "string";
                        if(this.isNumericField(prop_js.getString(KEY_TYPE))) strigOrNumeric="numeric" ;
                        js_props_out.put(prop_js.getString(KEY_NAME), strigOrNumeric);
                    }
                }
            }
            js_out.put(PARM_LAYER_ID, layer_id);
            js_out.put(KEY_PROPERTYTYPES, js_props_out);

        } catch (Exception e) {
            log.warn(e, "JSON parse failed");
        }
        return js_out;

    }
    /**
     * Parses DescribeFeatureType url request
     * 
     * @param lc WFS layer configuration
     * @return
     */
    private String parseDescribeFeatureUrl(WFSLayerConfiguration lc) {
        // http://tampere.navici.com/tampere_wfs_geoserver/ows?SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=tampere_ora:KIINTEISTOT_ALUE
        String url = "";

        url = lc.getURL();
        // check params
        if (url.indexOf("?") == -1) {
            url = url + "?";
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "service=WFS";
            if (url.toLowerCase().indexOf("version=") == -1)
                url = url + "&version=" + lc.getWFSVersion();
            if (url.toLowerCase().indexOf("describefeaturetype") == -1)
                url = url + "&request=DescribeFeatureType";
            if (url.toLowerCase().indexOf("typename") == -1)
                url = url + "&TYPENAME=" + lc.getFeatureNamespace() + ":"
                        + lc.getFeatureElement();
        } else {
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "&service=WFS";
            if (url.toLowerCase().indexOf("version=") == -1)
                url = url + "&version=" + lc.getWFSVersion();
            if (url.toLowerCase().indexOf("describefeaturetype") == -1)
                url = url + "&request=DescribeFeatureType";
            if (url.toLowerCase().indexOf("typename") == -1)
                url = url + "&TYPENAME=" + lc.getFeatureNamespace() + ":"
                        + lc.getFeatureElement();

        }

        return url;
    }

    /**
     * Find 'name' 'type' sequence under xsd:complexType or xsd:sequence
     * @param js
     * @return
     */
    private JSONObject findXsdElement(JSONObject js) {
        JSONObject js_raw = this.findsubJson(KEY_SEQUENCE, js);
        try {
            if (js_raw == null) {
                JSONArray js_rawa = this
                        .findsubJsonArray(KEY_COMPLEXTYPE, js);
                if (js_rawa != null) {
                    for (int i = 0; i < js_rawa.length(); i++) {
                        if (js_rawa.get(i) instanceof JSONObject) {
                            JSONObject js_sub = js_rawa.getJSONObject(i);
                            js_raw = this.findsubJson(KEY_SEQUENCE, js_sub);
                        }
                    }

                }
            }

        } catch (Exception e) {
            log.warn(e, "JSON parse failed");
        }
        return js_raw;

    }

    /**
     * @param wfsTypeValue
     * @return true, if numeric
     */
    private boolean isNumericField(String wfsTypeValue) {
        boolean numericValue = false;
        String typeval = this.stripNamespace(wfsTypeValue);
            if (this.isNumericType(typeval)) {
                numericValue = true;
            }
        return numericValue;
    }

    /**
     *
     * @param fieldType
     * @return
     */
    private boolean isNumericType(String fieldType)
    {
        boolean isNumericValue = false;
        return NUMERIC_FIELD_TYPES.contains(fieldType);
    }
    private String stripNamespace(final String tag) {

        String splitted[] = tag.split(":");
        if (splitted.length > 1) {
            return splitted[1];
        }
        return splitted[0];
    }
    /**
     * Get property types (native fields) of analysis layer
     *
     * @param sid
     * @return analysis property names and types"
     */
    private JSONObject getAnalysisFeaturePropertyTypes(String sid) {
        JSONObject propertyTypes = new JSONObject(); // Field names and types
        JSONObject js_out = new JSONObject();
        try {
            if (sid.indexOf(ANALYSIS_PREFIX) > -1) {
                String[] values = sid.split("_");
                if (values.length > 0)
                    propertyTypes = analysisDataService
                            .getAnalysisNativeColumnTypes(values[values.length - 1]);

            }

            js_out.put(PARM_LAYER_ID, sid);
            js_out.put(KEY_PROPERTYTYPES, propertyTypes);

        } catch (Exception e) {
            log.warn("Unable to get analysis field types ",e);
        }
        return js_out;
    }
}
