package fi.nls.oskari.routing;

import org.json.JSONObject;

/**
 * Created by HVELLONEN on 1.7.2015.
 */
public class RouteResponse {

    private JSONObject geoJson;
    private JSONObject instructions;

    public JSONObject getGeoJson() {
        return geoJson;
    }

    public void setGeoJson(JSONObject geoJson) {
        this.geoJson = geoJson;
    }


    public JSONObject getInstructions() {
        return instructions;
    }

    public void setInstructions(JSONObject instructions) {
        this.instructions = instructions;
    }


}
