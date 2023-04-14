package org.oskari.capabilities.ogc.api;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.WFSConversionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.wfs.FeaturePropertyType;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

import static fi.nls.oskari.util.IOHelper.CONTENT_TYPE_GEOJSON;

public class OGCAPIFeatureItemsDescriber {

    public String getItemsSample(ServiceConnectInfo src, String featureType) throws IOException {
        final String path = OGCAPIFeaturesService.getItemsPath(src.getUrl(), featureType);
        final Map<String, String> queryParams = Collections.singletonMap("limit", "10");
        final Map<String, String> headers = Collections.singletonMap("Accept", CONTENT_TYPE_GEOJSON);
        final HttpURLConnection conn = IOHelper.getConnection(path, src.getUser(), src.getPass(), queryParams, headers);
        IOHelper.validateResponse(conn, CONTENT_TYPE_GEOJSON);
        return IOHelper.readString(conn.getInputStream());
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
    public Collection<FeaturePropertyType> getFeatureProperties(String geojson) throws ServiceException {
        if (geojson == null) {
            return Collections.emptyList();
        }
        try {
            final JSONObject response = new JSONObject(geojson);
            return getFeatureAttributes(response.getJSONArray("features"));
        } catch (JSONException ex) {
            throw new ServiceException("Cannot connect to the layer server", ex);
        }

    }

    private static Collection<FeaturePropertyType> getFeatureAttributes(JSONArray features) throws JSONException {
        final Map<String, FeaturePropertyType> attributes = new HashMap<>();
        final String GEOM_FIELD = "geometry";
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");
            // check geometry field
            JSONObject geometry = feature.getJSONObject(GEOM_FIELD);
            FeaturePropertyType geomAttribute = attributes.get(GEOM_FIELD);
            if (geometry != null && geomAttribute == null) {
                FeaturePropertyType prop = new FeaturePropertyType();
                prop.name = GEOM_FIELD;
                prop.type = geometry.optString("type");
                attributes.put(GEOM_FIELD, prop);
            }

            Iterator<?> propertiesKeys = properties.keys();
            while (propertiesKeys.hasNext()) {
                String attributeName = (String) propertiesKeys.next();
                Object attributeValue = properties.get(attributeName);
                String attributeType = WFSConversionHelper.UNKNOWN;
                if (attributeValue instanceof String) {
                    attributeType = WFSConversionHelper.STRING;
                } else if (attributeValue instanceof Boolean) {
                    attributeType = WFSConversionHelper.BOOLEAN;
                } else if (
                        attributeValue instanceof Integer ||
                                attributeValue instanceof Long ||
                                attributeValue instanceof Float ||
                                attributeValue instanceof Double
                ) {
                    attributeType = WFSConversionHelper.NUMBER;
                }

                // update attribute type if its not set or UNKNOWN
                FeaturePropertyType currentAttributeType = attributes.get(attributeName);
                if (currentAttributeType == null || currentAttributeType.equals(WFSConversionHelper.UNKNOWN)) {
                    FeaturePropertyType prop = new FeaturePropertyType();
                    prop.name = attributeName;
                    prop.type = attributeType;
                    attributes.put(attributeName, prop);
                }
            }
        }
        return attributes.values();
    }
}
