package fi.nls.oskari.domain.map;

import fi.nls.oskari.util.JSONHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class MaplayerGroup extends JSONLocalizedName {

	private int id;
	
	private JSONArray layers;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public JSONArray getLayers() {
		return layers;
	}

	public void setLayers(JSONArray layers) {
		this.layers = layers;
	}

    public JSONObject getAsJSON() {
        final JSONObject me = new JSONObject();
        if(id > 0) {
            JSONHelper.putValue(me, "id", id);
        }

        final JSONObject names = new JSONObject();
        for (Map.Entry<String, String> localization : getNames().entrySet()) {
            JSONHelper.putValue(names, localization.getKey(), localization.getValue());
        }
        JSONHelper.putValue(me, "name", names);
        JSONHelper.putValue(me, "layers", this.getLayers());
        return me;
    }
}
