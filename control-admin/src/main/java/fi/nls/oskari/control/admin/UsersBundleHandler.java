package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.control.view.modifier.bundle.BundleHandler;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;
import org.oskari.user.util.UserHelper;


/**
 * Injects password requirements to admin-users config

 {
     "requirements": {
        "length": 8,
        "case": true
     },
    "isExternal": false
 }
 */
@OskariViewModifier("admin-users")
public class UsersBundleHandler extends BundleHandler {

    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject config = getBundleConfig(params.getConfig());
        JSONHelper.putValue(config, "requirements", UserHelper.getPasswordRequirements());
        JSONHelper.putValue(config, "isExternal", isUsersFromExternalSource());
        return false;
    }

    /**
     * If users are managed in external source any changes to them are usually overwritten when they login.
     * So we can disable the fields that are and updates that can happen to users to make the admin UI more user-friendly.
     * @return
     */
    public static boolean isUsersFromExternalSource() {
        return PropertyUtil.getOptional("oskari.user.external", false);
    }
}
