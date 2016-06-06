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

    private static final String[] XSD_XMLNS = {"xsd:","xs:",""};
    private static final String KEY_SEQUENCE = "sequence";
    private static final String KEY_COMPLEXTYPE = "complexType";
    private static final String KEY_ELEMENT = "element";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";

    private final static String ENCODE_ATTRIBUTE = "encoding=\"";

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
     *  Parses DescribeFeatureType url request for WFS request
     * @param url  Wfs service Url
     * @param version  Wfs service version
     * @param xmlns  Feature type namespace
     * @param featureTypeName  Feature type name
     * @return Wfs request url
     * e.g. http://tampere.navici.com/tampere_wfs_geoserver/ows?SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=tampere_ora:KIINTEISTOT_ALUE
     */
    public static String parseDescribeFeatureUrl(String url, String version, String xmlns, String featureTypeName) {

        // check params
        if (url.indexOf("?") == -1) {
            url = url + "?";
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "service=WFS";
            if (url.toLowerCase().indexOf("version=") == -1)
                url = url + "&version=" + version;
            if (url.toLowerCase().indexOf("describefeaturetype") == -1)
                url = url + "&request=DescribeFeatureType";
            if (url.toLowerCase().indexOf("typename") == -1)
                url = url + "&TYPENAME=" + xmlns + ":"
                        + featureTypeName;
        } else {
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "&service=WFS";
            if (url.toLowerCase().indexOf("version=") == -1)
                url = url + "&version=" + version;
            if (url.toLowerCase().indexOf("describefeaturetype") == -1)
                url = url + "&request=DescribeFeatureType";
            if (url.toLowerCase().indexOf("typename") == -1)
                url = url + "&TYPENAME=" + xmlns + ":"
                        + featureTypeName;

        }

        return url;
    }

    public static String getGetFeatureUrl(String url, String version, String xmlns, String featureTypeName, String maxfea) {

        // check params
        if (url.indexOf("?") == -1) {
            url = url + "?";
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "service=WFS";
        } else {
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "&service=WFS";
        }
        if (url.toLowerCase().indexOf("version=") == -1)
            url = url + "&version=" + version;
        if (url.toLowerCase().indexOf("getfeature") == -1)
            url = url + "&request=GetFeature";
        if (url.toLowerCase().indexOf("typename") == -1)
            url = url + "&TYPENAME=" + xmlns + ":"
                    + featureTypeName;
        if (url.toLowerCase().indexOf("maxfeatures") == -1)
            url = url + "&maxFeatures=" + maxfea;

        return url;
    }
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

    /**
     * Xml  to JSONObject
     * @param xml
     * @return
     * @throws ServiceException
     */

    public static JSONObject xml2JSON(String xml)
            throws ServiceException {
        try {
            // convert xml String to JSON
            return XML.toJSONObject(xml);

        } catch (Exception e) {
            throw new ServiceException("XML to JSON failed", e);
        }
    }

    /**
     * Check is WFS property (field) type is numeric
     * @param wfsTypeValue
     * @return true, if numeric
     */
    private static boolean isNumericField(String wfsTypeValue) {
        boolean numericValue = false;
        String typeval = stripNamespace(wfsTypeValue);
        if (NUMERIC_FIELD_TYPES.contains(typeval)) {
            numericValue = true;
        }
        return numericValue;
    }

    /**
     * Strip namspace out of featureTypeName, if exists
     * @param tag featureTypeName
     * @return
     */
    private static String stripNamespace(final String tag) {

        String splitted[] = tag.split(":");
        if (splitted.length > 1) {
            return splitted[1];
        }
        return splitted[0];
    }
    /**
     * **** Use only for analysis layer
     *
     * Get property types (native fields) of analysis layer
     * Analysis layer/feature has fixed field names for any kind of feature data
     * t1,t2,t3,t4,... are for text type fields
     * n1,n2,n3,n4, ... are for numeric fields
     * There is in each analysis a field mapping to original feature type fields
     *
     * @param sid  Analysis layer id
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
     * Pick up property names and types out of DescribeFeatureType response
     * works with WFS version 1.0.0 and 1.1.0
     *
     * @param layer_id
     *            WFS layer id in Oskari DB
     * @param js
     *            full DescribeFeatureType response
     * @return JSONObject WFS feature property names and WFS types
     */
    public static JSONObject getWFSFeaturePropertyTypes(String layer_id, JSONObject js) {
        JSONObject js_out = new JSONObject();
        JSONObject js_props_out = new JSONObject();
        try {
            if (js == null)
                return null;
            // Find recursively name type sequence xsd:complexType or xsd:sequence
            // This is parent json for KEY_NAMEs (feature property name)
            // 'parent' json is not in fixed position (branch) in response json
            JSONObject js_raw = null;
            int index = 0;
            for (index = 0; index < XSD_XMLNS.length; index++) {
                js_raw = digXsdElement(XSD_XMLNS[index], js);
                if (js_raw != null) {
                    break;
                }
            }

            if (js_raw == null) {
                return js_out;
            }
            JSONArray props_js = js_raw.getJSONArray(XSD_XMLNS[index]+KEY_ELEMENT);
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
     * Pick up property names and types out of DescribeFeatureType response
     * works with WFS version 1.0.0 and 1.1.0
     * WFSlayerconfiguration ala Oskari
     * @param lc WFSlayerconfiguration ala Oskari
     * @param layer_id
     * @return
     * @throws ServiceException
     */
    public static JSONObject getWFSFeaturePropertyTypes(WFSLayerConfiguration lc, String layer_id) throws ServiceException {
        final String wfsurl = parseDescribeFeatureUrl(lc.getURL(), lc.getWFSVersion(), lc.getFeatureNamespace(), lc.getFeatureElement());
        final String response = getResponse(wfsurl, lc.getUsername(), lc.getPassword());
        JSONObject props = WFSDescribeFeatureHelper.xml2JSON(response);
        return getWFSFeaturePropertyTypes(layer_id, props);
    }
    /**
     * Pick up property names and harmonized types (text or numeric) out of DescribeFeatureType response
     * works with WFS version 1.0.0 and 1.1.0
     * @param layer_id
     *            WFS layer id in Oskari DB
     * @param js
     *            raw WFS DescribeFeatureType response
     * @return JSONObject property names and types (string or numeric)
     */
    public static JSONObject getFeatureTypesTextOrNumeric(String layer_id, JSONObject js) {
        JSONObject js_out = new JSONObject();
        JSONObject js_props_out = new JSONObject();
        try {
            if (js == null)
                return null;
            // Find recursively name type sequence xsd:complexType or xsd:sequence
            // This is parent json for KEY_NAMEs (feature property name)
            JSONObject js_raw = null;
            int index = 0;
            for (index = 0; index < XSD_XMLNS.length; index++) {
                js_raw = digXsdElement(XSD_XMLNS[index], js);
                if (js_raw != null) {
                    break;
                }
            }

            if (js_raw == null) {
                return js_out;
            }
            JSONArray props_js = js_raw.getJSONArray(XSD_XMLNS[index]+KEY_ELEMENT);
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
     * Pick up property names and harmonized types (text or numeric) out of DescribeFeatureType response
     * works with WFS version 1.0.0 and 1.1.0
     * WFSlayerconfiguration ala Oskari
     * @param lc WFSlayerconfiguration ala Oskari
     * @param layer_id
     * @return
     * @throws ServiceException
     */
    public static JSONObject getFeatureTypesTextOrNumeric(WFSLayerConfiguration lc, String layer_id) throws ServiceException {
        final String wfsurl = parseDescribeFeatureUrl(lc.getURL(), lc.getWFSVersion(), lc.getFeatureNamespace(), lc.getFeatureElement());
        final String response = getResponse(wfsurl, lc.getUsername(), lc.getPassword());
        JSONObject props = WFSDescribeFeatureHelper.xml2JSON(response);
        return getFeatureTypesTextOrNumeric(layer_id, props);
    }

    /**
     * Find 'name' 'type' sequence under xsd:complexType or xsd:sequence recursively
     * Target could be under JSONArray or JSONObject set
     * @param xmlns String
     * @param js JSON
     * @return
     */
    private static JSONObject digXsdElement(String xmlns, JSONObject js) {

        if (xmlns == null) {
            xmlns = "";
        }
        JSONObject js_raw = findChildJson(xmlns+KEY_SEQUENCE, js);
        try {
            if (js_raw == null) {
                JSONArray js_rawa = findChildJsonArray(xmlns+KEY_COMPLEXTYPE, js);
                if (js_rawa != null) {
                    for (int i = 0; i < js_rawa.length(); i++) {
                        if (js_rawa.get(i) instanceof JSONObject) {
                            JSONObject js_sub = js_rawa.getJSONObject(i);
                            js_raw = findChildJson(xmlns+KEY_SEQUENCE, js_sub);
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
     * Find 1st JSONObject for search in JSONObject
     * @param mykey  key to find
     * @param js json to search
     * @return  json object of search key
     */
    private static JSONObject findChildJson(String mykey, JSONObject js) {
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
                        JSONObject jssub2 = findChildJson(mykey, jssub);
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
    /**
     * Find 1st JSONArray for search key in JSONObject
     * @param mykey  key to find
     * @param js json to search
     * @return  json object of search key
     */

    private static JSONArray findChildJsonArray(String mykey, JSONObject js) {
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
                    JSONArray jssub2 = findChildJsonArray(mykey, jssubb);
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
