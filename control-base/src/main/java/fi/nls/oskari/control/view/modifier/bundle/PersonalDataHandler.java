package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

@OskariViewModifier("personaldata")
public class PersonalDataHandler extends BundleHandler {

    private static final String PROFILE_URL = "changeInfoUrl";
    private String defaultValue = "";

    @Override
    public void init() {
        super.init();
        if(PropertyUtil.getOptional("allow.registration", false)) {
            // empty url as default if user registration is NOT allowed
            defaultValue = "/user";
        }
    }

    @Override
    public boolean modifyBundle(ModifierParams params) throws ModifierException {
        final JSONObject config = getBundleConfig(params.getConfig());
        if(!config.has(PROFILE_URL)) {
            // only write if not configured in db
            JSONHelper.putValue(config, PROFILE_URL, PropertyUtil.getLocalizableProperty("auth.profile.url", defaultValue));
            return true;
        }
        return false;
    }
}
