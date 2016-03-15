package fi.nls.oskari.pojo;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handles user's active WFS feature filtering
 * 
 * Used for storing filter in SessionStore (not saved).
 * 
 * @see SessionStore
 */
public class GeoJSONFilter {
	private static final Logger log = LogFactory.getLogger(GeoJSONFilter.class);
			
	private JSONObject geojson;
	private JSONArray features;

	/**
	 * Gets geojson
	 * 
	 * @return geojson
	 */
	@JsonIgnore
	public JSONObject getGeoJSON() {
		return geojson;
	}

	/**
	 * Sets geojson
	 * 
	 * @param geojson
	 */
	public void setGeoJSON(JSONObject geojson) {
		this.geojson = geojson;
	}

	/**
	 * Gets features
	 * 
	 * @return features
	 */
	@JsonIgnore
	public JSONArray getFeatures() {
		return features;
	}
	
	/**
	 * Sets features
	 * 
	 * @param features
	 */
	public void setFeatures(JSONArray features) {
		this.features = features;
	}
	
	/**
	 * Transforms parameters JSON String to object
	 * 
	 * @param json
	 * @return object
	 */
	@JsonIgnore
	public static GeoJSONFilter setParamsJSON(String json) {
        GeoJSONFilter filter = new GeoJSONFilter();

        JSONObject data = null;
		try {
			data = new JSONObject(json);
		} catch (JSONException e) {
			log.error(e, "Reading JSON data failed");
		}
		
        if(data != null && data.has("data")) {
        	try {
        		JSONObject dataJSON = (JSONObject) data.get("data");
        		JSONObject filterJSON = (JSONObject) dataJSON.get("filter");
        		filter.setGeoJSON((JSONObject)filterJSON.get("geojson"));
				filter.setFeatures((JSONArray)filter.getGeoJSON().get("features"));
			} catch (Exception e) {
				log.error(e, "Reading JSON data data failed");
				return null;
			}
        }

		return filter;
	}
}
