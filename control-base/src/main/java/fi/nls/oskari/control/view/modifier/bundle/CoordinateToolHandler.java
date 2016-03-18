package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

@OskariViewModifier("coordinatetool")
public class CoordinateToolHandler extends MapfullHandler {

    private static final String KEY_PROJECTIONS = "supportedProjections";

    @Override
    public void init() {
        epsgInit();
    }

    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject toolConfig = getBundleConfig(params.getConfig());
        if(!toolConfig.has(KEY_PROJECTIONS)) {
            // nothing to add
            return false;
        }
        final String[] projs = getProjections(toolConfig.opt(KEY_PROJECTIONS));
        final JSONObject mapfullConfig = getBundleConfig(params.getConfig(), BUNDLE_MAPFULL);
        if (mapfullConfig == null) {
            return false;
        }
        setProjDefsForMapConfig(mapfullConfig, projs);
        return false;
    }

    /**
     *  Projections can be like:
        supportedProjections : {
            EPSG:3035: "891fb410-099a-44db-9af1-39a49a84e59a",
            EPSG:3857: "4d0e81c4-9c60-8a6d-133c-df561a1ab8ec"
        }
        // OR
        supportedProjections : ["EPSG:3035", "EPSG:3857"]
    */
    private String[] getProjections(Object obj) {
        if(obj instanceof JSONArray) {
            return getProjections((JSONArray) obj);
        }
        if(obj instanceof JSONObject) {
            return getProjections((JSONObject) obj);
        }
        return new String[0];
    }

    private String[] getProjections(JSONArray array) {
        String[] value = new String[array.length()];
        for (int i = 0; i < array.length(); ++i) {
            value[i] = array.optString(i);
        }
        return value;
    }

    private String[] getProjections(JSONObject obj) {
        String[] value = new String[obj.length()];
        Iterator it = obj.keys();

        for (int i = 0; it.hasNext(); ++i) {

            value[i] = (String)it.next();
        }
        return value;
    }

}
