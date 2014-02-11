package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.control.view.PublishHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONObject;

@OskariViewModifier("publisher")
public class PublisherHandler extends BundleHandler {

    private static final Logger log = LogFactory.getLogger(PublisherHandler.class);

    private static JSONArray DRAW_ENABLED_ROLES = new JSONArray();
    private static final String KEY_DRAW_ROLE_IDS = "drawRoleIds";

    public void init() {
        String[] roleNames = PropertyUtil.getCommaSeparatedList(PublishHandler.PROPERTY_DRAW_TOOLS_ENABLED);
        try {
            final Role[] roles = UserService.getInstance().getRoles();
            for(Role role : roles) {
                for(String name : roleNames) {
                    if(name.equals(role.getName())) {
                        DRAW_ENABLED_ROLES.put(role.getId());
                    }
                }
            }
        } catch (ServiceException ex) {
            log.error(ex, "Error getting roles from user service");
        }
    }


    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject config = getBundleConfig(params.getConfig());

        if (config == null) {
            return false;
        }
        JSONHelper.putValue(config, KEY_DRAW_ROLE_IDS, DRAW_ENABLED_ROLES);
        return false;
    }

}
