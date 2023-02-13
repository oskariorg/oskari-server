package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

@OskariViewModifier("layerlist")
public class LayerlistHandler extends BundleHandler {

    private static final String BACKEND_STATUS_BUNDLE = "backendstatus";
    private static final String LAYER_GROUP_TOGGLE_LIMIT = "layerGroupToggleLimit";
    private static final String BACKEND_STATUS_AVAILABLE = "backendStatusAvailable";

    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject config = getBundleConfig(params.getConfig());
        int toggleLimit = PropertyUtil.getOptional("layerlist.groupToggleLimit", 0);
        if(!config.has(LAYER_GROUP_TOGGLE_LIMIT) && toggleLimit != 0) {
            JSONHelper.putValue(config, LAYER_GROUP_TOGGLE_LIMIT, toggleLimit);
        }
        boolean backendstatus = params.getView().getBundleByName(BACKEND_STATUS_BUNDLE) != null;
        JSONHelper.putValue(config, BACKEND_STATUS_AVAILABLE, backendstatus);
        return false;
    }
}
