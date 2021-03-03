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
    private static final String STRING = "string";
    private static final String NUMBER = "number";
    private static final String BOOLEAN = "boolean";
    private static final String UNKNOWN = "unknown";
    private static final String ATTRIBUTES_KEY = "attributes";
    private static final String GEOMETRY_FIELD_KEY = "geometryField";
    private static final String GEOMETRY_TYPE_KEY = "geometryType";
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
            result.put(ATTRIBUTES_KEY, attributes);
            result.put(GEOMETRY_FIELD_KEY, "geometry");
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
                String attributeType = UNKNOWN;
                if (attributeValue instanceof String) {
                    attributeType = STRING;
                } else if (attributeValue instanceof Boolean) {
                    attributeType = BOOLEAN;
                }  else if (
                    attributeValue instanceof Integer ||
                    attributeValue instanceof Long ||
                    attributeValue instanceof Float ||
                    attributeValue instanceof Double
                ) {
                    attributeType = NUMBER;
                }

                // update attribute type if its not set or UNKNOWN
                String currentAttributeType = attributes.optString(attributeName);
                if (currentAttributeType.isEmpty() || currentAttributeType.equals(UNKNOWN)) {
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
        final Set<String> geometryPropertyTypes = new HashSet<>(
            Arrays.asList(
                "GeometryPropertyType",
                "PointPropertyType",
                "LinePropertyType",
                "PolygonPropertyType",
                "MultiPointPropertyType",
                "MultiLinePropertyType",
                "MultiPolygonPropertyType",
                "SurfacePropertyType"
            )
        );
        final Set<String> stringTypes = new HashSet<>(Arrays.asList("string", "date", "time"));
        final Set<String> numericTypes = new HashSet<>(Arrays.asList("decimal", "int", "integer", "float", "double"));
        final Set<String> booleanTypes = new HashSet<>(Arrays.asList("boolean"));
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
                if (geometryPropertyTypes.contains(attributeType)) {
                    result.put(GEOMETRY_FIELD_KEY, attributeName);
                    result.put(GEOMETRY_TYPE_KEY, attributeType);
                } else if (stringTypes.contains(attributeType)) {
                    attributes.put(attributeName, STRING);
                } else if (numericTypes.contains(attributeType)) {
                    attributes.put(attributeName, NUMBER);
                } else if (booleanTypes.contains(attributeType)) {
                    attributes.put(attributeName, BOOLEAN);
                } else {
                    attributes.put(attributeName, UNKNOWN);
                }
            }
            result.put(ATTRIBUTES_KEY, attributes);
            return result;
        } catch (JSONException ex) {
            throw new ServiceException("Couldn't parse feature property types response", ex);
        }
    }
}
