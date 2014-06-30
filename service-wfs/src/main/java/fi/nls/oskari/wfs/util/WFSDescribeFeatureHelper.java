package fi.nls.oskari.wfs.util;

import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Moved WFSDescribeFeature response parsing code from
 * control-base/GetWFSDescribeFeatureHandler so it can be used elsewhere.
 *
 * FIXME: Terrible code, please rewrite
 */
public class WFSDescribeFeatureHelper {

    private static final Logger log = LogFactory.getLogger(WFSDescribeFeatureHelper.class);

    private static final AnalysisDataService analysisDataService = new AnalysisDataService();
    private static final String KEY_LAYER_ID = "layer_id";
    private static final String KEY_PROPERTYTYPES = "propertyTypes";
    public static final String ANALYSIS_PREFIX = "analysis_";
    private static final String KEY_SEQUENCE = "xsd:sequence";
    private static final String KEY_COMPLEXTYPE = "xsd:complexType";
    private static final String KEY_ELEMENT = "xsd:element";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";

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
    /**
     * Parses DescribeFeatureType url request
     *
     * @param lc WFS layer configuration
     * @return
     */
    public static String parseDescribeFeatureUrl(WFSLayerConfiguration lc) {
        // http://tampere.navici.com/tampere_wfs_geoserver/ows?SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=tampere_ora:KIINTEISTOT_ALUE
        String url = lc.getURL();
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

    final static String ENCODE_ATTRIBUTE = "encoding=\"";

    public static String getResponse(final String url, final String userName,
                                     final String password) throws ServiceException {
        try {
            String rawResponse = IOHelper.getURL(url, userName, password);
            final String response = fixEncode(rawResponse, url);

            if (response == null) {
                throw new ServiceException("Response was <null>");
            } else if (response.toUpperCase().indexOf("EXCEPTIONREPORT") > -1) {
                throw new ServiceException(response);
            }
            return response;
        } catch (IOException ex) {
            throw new ServiceException("Couldnt read server response from url.",
                    ex);
        }
    }

    // Check if not UTF-8
    private static String fixEncode(String response, String pUrl)
            throws ServiceException {
        String encodedResponse = response;
        String[] processingSplit = response.split("\\?>");

        if (processingSplit != null && processingSplit.length > 0) {

            int encodeAttributeStart = processingSplit[0]
                    .indexOf(ENCODE_ATTRIBUTE);

            if (encodeAttributeStart > 0) {
                encodeAttributeStart = encodeAttributeStart
                        + ENCODE_ATTRIBUTE.length();
                String charset = processingSplit[0].substring(
                        encodeAttributeStart, processingSplit[0].indexOf("\"",
                        encodeAttributeStart));
                try {
                    if (!IOHelper.DEFAULT_CHARSET.equals(charset)) {
                        encodedResponse = IOHelper.getURL(pUrl,
                                Collections.EMPTY_MAP, charset);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw new ServiceException("UnsupportedEncodingException",
                            ex);
                } catch (IOException ex) {
                    throw new ServiceException(
                            "Couldnt read server response from url.", ex);
                }
            }
        }

        return encodedResponse;
    }

    public static JSONObject parseFeatureProperties(String response)
            throws ServiceException {
        try {
            // convert xml String to JSON
            return XML.toJSONObject(response);

        } catch (Exception e) {
            throw new ServiceException("XML to JSON failed", e);
        }
    }

    /**
     * @param wfsTypeValue
     * @return true, if numeric
     */
    private static boolean isNumericField(String wfsTypeValue) {
        boolean numericValue = false;
        String typeval = stripNamespace(wfsTypeValue);
        if (isNumericType(typeval)) {
            numericValue = true;
        }
        return numericValue;
    }
    /**
     *
     * @param fieldType
     * @return
     */
    private static boolean isNumericType(String fieldType)
    {
        return NUMERIC_FIELD_TYPES.contains(fieldType);
    }
    private static String stripNamespace(final String tag) {

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
    public static JSONObject getAnalysisFeaturePropertyTypes(String sid) {
        JSONObject propertyTypes = new JSONObject(); // Field names and types
        JSONObject js_out = new JSONObject();
        try {
            if (sid.indexOf(ANALYSIS_PREFIX) > -1) {
                String[] values = sid.split("_");
                if (values.length > 0)
                    propertyTypes = analysisDataService
                            .getAnalysisNativeColumnTypes(values[values.length - 1]);

            }

            js_out.put(KEY_LAYER_ID, sid);
            js_out.put(KEY_PROPERTYTYPES, propertyTypes);

        } catch (Exception e) {
            log.warn("Unable to get analysis field types ",e);
        }
        return js_out;
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
            js_out.put(KEY_LAYER_ID, layer_id);
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
    public static JSONObject populatePropertiesSimple(String layer_id, JSONObject js) {
        JSONObject js_out = new JSONObject();
        JSONObject js_props_out = new JSONObject();
        try {
            if (js == null)
                return null;
            // Find name type sequence xsd:complexType or xsd:sequence
            JSONObject js_raw = findXsdElement(js);
            JSONArray props_js = js_raw.getJSONArray(KEY_ELEMENT);
            // loop Array
            for (int i = 0; i < props_js.length(); i++) {
                if (props_js.get(i) instanceof JSONObject) {
                    JSONObject prop_js = props_js.getJSONObject(i);
                    if (prop_js.has(KEY_NAME) && prop_js.has(KEY_TYPE)) {
                        String strigOrNumeric = "string";
                        if(isNumericField(prop_js.getString(KEY_TYPE))) strigOrNumeric="numeric" ;
                        js_props_out.put(prop_js.getString(KEY_NAME), strigOrNumeric);
                    }
                }
            }
            js_out.put(KEY_LAYER_ID, layer_id);
            js_out.put(KEY_PROPERTYTYPES, js_props_out);

        } catch (Exception e) {
            log.warn(e, "JSON parse failed");
        }
        return js_out;

    }

    /**
     * Find 'name' 'type' sequence under xsd:complexType or xsd:sequence
     * @param js
     * @return
     */
    private static JSONObject findXsdElement(JSONObject js) {
        JSONObject js_raw = findsubJson(KEY_SEQUENCE, js);
        try {
            if (js_raw == null) {
                JSONArray js_rawa = findsubJsonArray(KEY_COMPLEXTYPE, js);
                if (js_rawa != null) {
                    for (int i = 0; i < js_rawa.length(); i++) {
                        if (js_rawa.get(i) instanceof JSONObject) {
                            JSONObject js_sub = js_rawa.getJSONObject(i);
                            js_raw = findsubJson(KEY_SEQUENCE, js_sub);
                        }
                    }

                }
            }

        } catch (Exception e) {
            log.warn(e, "JSON parse failed");
        }
        return js_raw;

    }


    private static JSONObject findsubJson(String mykey, JSONObject js) {
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

    private static JSONArray findsubJsonArray(String mykey, JSONObject js) {
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
}
