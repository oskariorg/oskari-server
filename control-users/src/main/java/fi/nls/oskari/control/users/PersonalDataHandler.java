package fi.nls.oskari.control.users;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.control.view.modifier.bundle.BundleHandler;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

@OskariViewModifier("personaldata")
public class PersonalDataHandler extends BundleHandler {

    private static final String PROFILE_URL = "changeInfoUrl";

    @Override
    public boolean modifyBundle(ModifierParams params) throws ModifierException {
        if(!RegistrationUtil.isEnabled()) {
            return false;
        }
        final JSONObject config = getBundleConfig(params.getConfig());
        JSONHelper.putValue(config, PROFILE_URL, PropertyUtil.get("auth.register.url", "/user"));
        return false;
    }
}
