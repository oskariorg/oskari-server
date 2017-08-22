package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.control.view.AppSetupHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

@OskariViewModifier("publisher")
public class PublisherHandler extends BundleHandler {

    private static final Logger log = LogFactory.getLogger(PublisherHandler.class);

    private static JSONArray DRAW_ENABLED_ROLES = new JSONArray();
    private static final String KEY_DRAW_ROLE_IDS = "drawRoleIds";
    private static final String KEY_TERMS_OF_USE_URL = "termsOfUseUrl";
    public static final String PROPERTY_PUBLISH_TERMS_OF_USE_URL = "oskari.map.publish.terms.url";
    public static final String PROPERTY_TERMS_OF_USE_URL = "oskari.map.terms.url";

    public void init() {
        String[] roleNames = PropertyUtil.getCommaSeparatedList(AppSetupHandler.PROPERTY_DRAW_TOOLS_ENABLED);
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
        setupTerms(config);
        return false;
    }
    private void setupTerms(final JSONObject config) {
        if(config.has(KEY_TERMS_OF_USE_URL)) {
            return;
        }

        Object termsObj = PropertyUtil.getLocalizableProperty(PROPERTY_PUBLISH_TERMS_OF_USE_URL);
        if(termsObj == null) {
            termsObj = PropertyUtil.getLocalizableProperty(PROPERTY_TERMS_OF_USE_URL);
        }
        if(termsObj instanceof String) {
            JSONHelper.putValue(config, KEY_TERMS_OF_USE_URL, termsObj);
        } else if(termsObj instanceof Map) {
            Map<String, String> values = (Map<String, String>) termsObj;
            JSONHelper.putValue(config, KEY_TERMS_OF_USE_URL, new JSONObject(values));
        }
    }

}
