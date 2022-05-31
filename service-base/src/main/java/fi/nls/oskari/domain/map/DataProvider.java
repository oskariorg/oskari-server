package fi.nls.oskari.domain.map;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;


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
        JSONHelper.putValue(me, "locale", getLocale());
        return me;
    }

    public String getDescription(String language) {
        return getLocalizedValue(language, LOCALE_DESCRIPTION);
    }
}
