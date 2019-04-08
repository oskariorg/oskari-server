package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

public interface PluginHandler {

    public boolean modifyPlugin(final JSONObject plugin, final ModifierParams params, final String mapSRS);
}
