package fi.nls.oskari.myplaces;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public static MyPlaceCategory parseFromGeoJSON(JSONObject properties)
            throws JSONException {
        MyPlaceCategory category = new MyPlaceCategory();

        category.setUuid(JSONHelper.optString(properties, "uuid"));
        category.setPublisher_name(JSONHelper.optString(properties, "publisher_name"));

        // Everything is optional except category_name
        category.setCategory_name(JSONHelper.getString(properties, "category_name"));
        category.setDefault(properties.optBoolean("default"));

        category.setStroke_width(properties.optInt("stroke_width"));
        category.setStroke_color(JSONHelper.optString(properties, "stroke_color"));
        category.setStroke_linejoin(JSONHelper.optString(properties, "stroke_linejoin"));
        category.setStroke_linecap(JSONHelper.optString(properties, "stroke_linecap"));
        category.setStroke_dasharray(JSONHelper.optString(properties, "stroke_dasharray"));

        category.setFill_color(JSONHelper.optString(properties, "fill_color"));
        category.setFill_pattern(properties.optInt("fill_pattern"));

        category.setDot_color(JSONHelper.optString(properties, "dot_color"));
        category.setDot_size(properties.optInt("dot_size"));
        category.setDot_shape(JSONHelper.optString(properties, "dot_shape"));

        category.setBorder_width(properties.optInt("border_width"));
        category.setBorder_color(JSONHelper.optString(properties, "border_color"));
        category.setBorder_linejoin(JSONHelper.optString(properties, "border_linejoin"));
        category.setBorder_dasharray(JSONHelper.optString(properties, "border_dasharray"));

        return category;
    }

    public static ByteArrayOutputStream toGeoJSONFeatureCollection(List<MyPlaceCategory> categories)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonFactory factory = new JsonFactory();
        JsonGenerator json = factory.createGenerator(baos);
        json.writeStartObject();
        json.writeStringField("type", "FeatureCollection");
        json.writeFieldName("features");
        json.writeStartArray();
        for (MyPlaceCategory category : categories) {
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
        json.writeStringField("category_name", category.getCategory_name());
        json.writeStringField("publisher_name", category.getPublisher_name());
        json.writeBooleanField("default", category.isDefault());

        json.writeStringField("stroke_color", category.getStroke_color());
        json.writeStringField("stroke_dasharray", category.getStroke_dasharray());
        json.writeStringField("stroke_linecap", category.getStroke_linecap());
        json.writeStringField("stroke_linejoin", category.getStroke_linejoin());
        json.writeNumberField("stroke_width", category.getStroke_width());

        json.writeStringField("fill_color", category.getFill_color());
        json.writeNumberField("fill_pattern", category.getFill_pattern());

        json.writeStringField("dot_color", category.getDot_color());
        json.writeStringField("dot_shape", category.getDot_shape());
        json.writeNumberField("dot_size", category.getDot_size());

        json.writeStringField("border_color", category.getBorder_color());
        json.writeStringField("border_dasharray", category.getBorder_dasharray());
        json.writeStringField("border_linejoin", category.getBorder_linejoin());
        json.writeNumberField("border_width", category.getBorder_width());
        json.writeEndObject();

        json.writeEndObject();
    }

}
