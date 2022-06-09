package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

@OskariViewModifier("mydata")
public class MyDataHandler extends BundleHandler {

    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject config = getBundleConfig(params.getConfig());

        if (config == null) {
            return false;
        }
        JSONHelper.putValue(config, "showUser", PropertyUtil.getOptional("mydata.tabs.showUser", true));
        JSONHelper.putValue(config, "showViews", PropertyUtil.getOptional("mydata.tabs.showViews", true));
        return false;
    }
}
