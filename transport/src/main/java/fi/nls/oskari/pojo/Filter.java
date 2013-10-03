package fi.nls.oskari.pojo;

import fi.nls.oskari.log.LogFactory;
import org.codehaus.jackson.annotate.JsonIgnore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.log.Logger;

/**
 * Handles user's active WFS feature filtering
 * 
 * Used for storing filter in SessionStore (not saved).
 * 
 * @see SessionStore
 */
public class Filter {  
	private static final Logger log = LogFactory.getLogger(Filter.class);
			
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
	public static Filter setParamsJSON(String json) {
		Filter filter = new Filter();

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
