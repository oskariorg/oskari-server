package fi.nls.oskari.myplaces;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.UserDataStyle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.myplaces.service.wfst.CategoriesWFSTRequestBuilder;
import fi.nls.oskari.util.JSONHelper;

public class MyPlaceCategoryHelper {

    public static List<MyPlaceCategory> parseFromGeoJSON(String geojson,
            boolean checkId) throws IOException, JSONException {
        JSONObject featureCollection = new JSONObject(geojson);
        JSONArray featuresArray = featureCollection.getJSONArray("features");
        final int n = featuresArray.length();
        List<MyPlaceCategory> categories = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            JSONObject feature = featuresArray.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");
            MyPlaceCategory category = parseFromGeoJSON(properties);
            if (checkId) {
                Object id = feature.get("id");
                category.setId(getLongFromIdObject(id));
            }
            categories.add(category);
        }
        return categories;
    }

    private static long getLongFromIdObject(Object id)
            throws IllegalArgumentException {
        if (id instanceof String) {
            return CategoriesWFSTRequestBuilder.removePrefixFromId((String) id);
        } else if (id instanceof Integer) {
            return ((Integer) id).longValue();
        } else if (id instanceof Long) {
            return ((Long) id).longValue();
        } else {
            throw new IllegalArgumentException("Invalid id");
        }
    }

    public static MyPlaceCategory parseFromGeoJSON(JSONObject properties) throws JSONException {
        MyPlaceCategory category = new MyPlaceCategory();

        category.setUuid(JSONHelper.optString(properties, "uuid"));
        category.setPublisher_name(JSONHelper.optString(properties, "publisher_name"));

        // Everything is optional except category_name
        category.setName(JSONHelper.getString(properties, "category_name"));
        category.setDefault(properties.optBoolean("default"));
        // GeoServer adds String options property
        String options = JSONHelper.optString(properties,"options", "{}");
        category.setOptions(JSONHelper.createJSONObject(options));
        // Frontend adds JSONObject style property
        JSONObject style = JSONHelper.getJSONObject(properties,"style");
        if (style != null) {
            category.getWFSLayerOptions().setDefaultFeatureStyle(style);
        }
        return category;
    }

    public static ByteArrayOutputStream toGeoJSONFeatureCollection(List<MyPlaceCategory> categories, OskariLayer baselayer)
            throws IOException {
        JSONObject baseOptions = baselayer.getOptions();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonFactory factory = new JsonFactory();
        JsonGenerator json = factory.createGenerator(baos);
        json.writeStartObject();
        json.writeStringField("type", "FeatureCollection");
        json.writeFieldName("features");
        json.writeStartArray();
        for (MyPlaceCategory category : categories) {
            category.getWFSLayerOptions().injectBaseLayerOptions(baseOptions);
            toGeoJSONFeature(json, category);
        }
        json.writeEndArray();
        json.writeEndObject();
        json.close();
        return baos;
    }
    private static void toGeoJSONFeature(JsonGenerator json, MyPlaceCategory category)
            throws IOException {
        json.writeStartObject();
        json.writeStringField("type", "Feature");
        json.writeNumberField("id", category.getId());
        json.writeNullField("geometry");
        json.writeFieldName("properties");

        json.writeStartObject();
        json.writeStringField("uuid", category.getUuid());
        json.writeStringField("name", category.getName());
        json.writeStringField("publisher_name", category.getPublisher_name());
        json.writeBooleanField("default", category.isDefault());
        json.writeFieldName("options");
        json.writeRawValue(category.getOptions().toString());
        json.writeEndObject();

        json.writeEndObject();
    }

}
