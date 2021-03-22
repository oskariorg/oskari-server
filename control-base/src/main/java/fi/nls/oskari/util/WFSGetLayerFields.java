package fi.nls.oskari.util;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.service.wfs3.OskariWFS3Client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

public class WFSGetLayerFields {
    private static final String KEY_TYPES = "types";
    private static final String KEY_LOCALE = "locale";
    private static final String KEY_SELECTION = "selection";
    private static final String KEY_GEOMETRY_NAME = "geometryName";
    private static final String KEY_GEOMETRY_TYPE = "geometryType";
    private static final String CONTENT_TYPE_GEOJSON = "application/geo+json";
    /**
     * Return fields information for the WFS layer
     *
     * The result is constructed as:
     * {
     *     "attributes": {
     *         "field-1": STRING,
     *         "field-2": NUMBER,
     *         ...
     *         "field-n": BOOLEAN
     *     },
     *     "geometryField": "geometry"
     * }
     *
     * The field type can be one of the following values:
     *  - string
     *  - number
     *  - boolean
     *  - unknown
     *
     */
    public static JSONObject getLayerFields(OskariLayer layer) throws ServiceException {
        return layer.getVersion().startsWith("3") ? getCollectionFields(layer) : getFeatureTypeFields(layer);
    }

    /**
     * Return fields information for WFS 3.x layer
     *
     * The fields are collected by requesting 10 features from the
     * collection and derive the attribute type from the attribute
     * value, which is either NUMBER or STRING. If the attribute
     * type cannot be derived from any feature, e.g. all 10 features
     * have null values on the attribute, then the attribute will
     * have an "unknown" type
     *
     */
    private static JSONObject getCollectionFields(OskariLayer layer) throws ServiceException {
        try {
            final JSONArray features = getCollectionItems(layer);
            final JSONObject attributes = getFeatureAttributes(features);
            final JSONObject result = new JSONObject();
            result.put(KEY_TYPES, attributes);
            result.put(KEY_GEOMETRY_NAME, "geometry");
            return result;
        } catch (JSONException ex) {
            throw new ServiceException("Couldn't parse collection items response", ex);
        }
    }

    private static JSONArray getCollectionItems(OskariLayer layer) throws ServiceException, JSONException {
        final String path = OskariWFS3Client.getItemsPath(layer.getUrl(), layer.getName());
        final Map<String, String> queryParams = Collections.singletonMap("limit", "10");
        final Map<String, String> headers = Collections.singletonMap("Accept", CONTENT_TYPE_GEOJSON);
        try {
            final HttpURLConnection conn = IOHelper.getConnection(path, layer.getUsername(), layer.getPassword(), queryParams, headers);
            OskariWFS3Client.validateResponse(conn, CONTENT_TYPE_GEOJSON);
            final String rawResponse = IOHelper.readString(conn.getInputStream());
            final JSONObject response = new JSONObject(rawResponse);
            return response.getJSONArray("features");
        } catch (IOException ex) {
            throw new ServiceException("Cannot connect to the layer server", ex);
        }
    }

    private static JSONObject getFeatureAttributes(JSONArray features) throws JSONException {
        final JSONObject attributes = new JSONObject();
        for (int i=0; i<features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");
            Iterator<?> propertiesKeys = properties.keys();
            while (propertiesKeys.hasNext()) {
                String attributeName = (String) propertiesKeys.next();
                Object attributeValue = properties.get(attributeName);
                String attributeType = WFSConversionHelper.UNKNOWN;
                if (attributeValue instanceof String) {
                    attributeType = WFSConversionHelper.STRING;
                } else if (attributeValue instanceof Boolean) {
                    attributeType = WFSConversionHelper.BOOLEAN;
                }  else if (
                    attributeValue instanceof Integer ||
                    attributeValue instanceof Long ||
                    attributeValue instanceof Float ||
                    attributeValue instanceof Double
                ) {
                    attributeType = WFSConversionHelper.NUMBER;
                }

                // update attribute type if its not set or UNKNOWN
                String currentAttributeType = attributes.optString(attributeName);
                if (currentAttributeType.isEmpty() || currentAttributeType.equals(WFSConversionHelper.UNKNOWN)) {
                    attributes.put(attributeName, attributeType);
                }
            }
        }
        return attributes;
    }

    /**
     * Return fields information for WFS 1.x/2.x layer
     *
     * The fields are collected by parsing the DescribeFeatureType
     * response. The xml types are converted to STRING, NUMBER,
     * BOOLEAN and UNKNOWN to conform to the same set of types
     * as WFS 3.x
     *
     */
    private static JSONObject getFeatureTypeFields(OskariLayer layer) throws ServiceException {
        final JSONObject response = WFSDescribeFeatureHelper.getWFSFeaturePropertyTypes(layer, String.valueOf(layer.getId()));
        try {
            final JSONObject propertyTypes = response.getJSONObject("propertyTypes");
            final Iterator<?> attributeNames = propertyTypes.keys();
            final JSONObject attributes = new JSONObject();
            final JSONObject result = new JSONObject();
            while (attributeNames.hasNext()) {
                String attributeName = (String) attributeNames.next();
                String attributeType = propertyTypes.getString(attributeName);
                // remove xml namespace prefix if there's one
                attributeType = attributeType.contains(":") ? attributeType.split(":")[1] : attributeType;
                if (WFSConversionHelper.isGeometryType(attributeType)) {
                    result.put(KEY_GEOMETRY_NAME, attributeName);
                    result.put(KEY_GEOMETRY_TYPE, attributeType);
                } else {
                    attributes.put(attributeName, WFSConversionHelper.getSimpleType(attributeType));
                }
            }
            result.put(KEY_TYPES, attributes);
            return result;
        } catch (JSONException ex) {
            throw new ServiceException("Couldn't parse feature property types response", ex);
        }
    }
    public static void injectLayerAttributesData (OskariLayer layer, JSONObject fields) throws ServiceException {
        JSONObject data = layer.getAttributes().optJSONObject("data");
        if (data == null) {
            return;
        }
        try {
            fields.putOpt(KEY_LOCALE, data.optJSONObject("locale"));
            // selection is array or localized object of arrays
            fields.putOpt(KEY_SELECTION, data.opt("filter"));
        } catch (JSONException e) {
            throw new ServiceException("Invalid json in layer attributes, layer id: " + layer.getId(), e);
        }

    }
}
