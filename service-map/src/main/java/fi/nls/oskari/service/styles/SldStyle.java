package fi.nls.oskari.service.styles;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Oskari team
 */
public class SldStyle {
    private int id;
    private String name;
    private String sld_style;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSld_style() {
        return sld_style;
    }

    public void setSld_style(String sld_style) {
        this.sld_style = sld_style;
    }

    public JSONObject getAsJSON() {
        final JSONObject me = new JSONObject();
        if(id > 0) {
            JSONHelper.putValue(me, "id", id);
        }
        JSONHelper.putValue(me, "name", name);

        return me;
    }
}
