package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

/**
 * Created by TMIKKOLAINEN on 22.10.2014.
 */
@OskariViewModifier("rpc")
public class RpcHandler extends BundleHandler {

    private static final String KEY_DOMAIN = "domain";

    @Override
    public boolean modifyBundle(ModifierParams params) throws ModifierException {
        // Add published map's domain to rpc config
        final JSONObject config = getBundleConfig(params.getConfig());
        JSONHelper.putValue(config, KEY_DOMAIN, params.getView().getPubDomain());
        return false;
    }
}
