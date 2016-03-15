package fi.nls.oskari.control.rating;

import fi.nls.oskari.rating.Rating;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by MHURME on 16.9.2015.
 */
public class FeedbackJSONFormatter {

    public static JSONObject getAverageJSON(String categoryItem, String[] average) throws JSONException {
        JSONObject item = new JSONObject();
        item.put("id", categoryItem);
        item.put("score", average[0]);
        item.put("amount", average[1]);
        return item;
    }

    public static JSONObject getRatingsJSON(Rating rating) throws JSONException {
        JSONObject item = new JSONObject();
        item.put("id", rating.getId());
        item.put("score", rating.getRating());
        item.put("category", rating.getCategory());
        item.put("categoryItem", rating.getCategoryItem());
        item.put("comment", rating.getComment());
        item.put("user", rating.getUserId());
        item.put("userRole", rating.getUserRole());
        return item;
    }
}
