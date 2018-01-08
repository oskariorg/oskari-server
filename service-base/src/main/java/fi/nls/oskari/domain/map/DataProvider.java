package fi.nls.oskari.domain.map;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.util.Map;

public class DataProvider extends JSONLocalizedName {

	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

        return me;
    }
}
