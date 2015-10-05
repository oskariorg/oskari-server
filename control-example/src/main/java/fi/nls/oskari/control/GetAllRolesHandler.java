package fi.nls.oskari.control;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Example route returning all roles in the system.
 * @author SMAKINEN
 */
@OskariActionRoute("GetAllRoles")
public class GetAllRolesHandler extends ActionHandler {

    private Logger log = LogFactory.getLogger(GetAllRolesHandler.class);
    private UserService userService = null;

    private final static String JSKEY_ID = "id";
    private final static String JSKEY_NAME = "name";
    private final static String JSKEY_EXTERNAL = "external";

    @Override
    public void init() {
        try {
            userService = UserService.getInstance();
        } catch (Exception ex) {
            log.error(ex, "Unable to initialize User service!");
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        if (userService == null) {
            throw new ActionParamsException("User service not initialized");
        }
        try {
            final Role[] roles = userService.getRoles();

            final JSONArray rolesJSON = new JSONArray();

            for (Role role : roles) {
                JSONObject external = new JSONObject();
                external.put(JSKEY_ID, role.getId());
                external.put(JSKEY_NAME, role.getName());
                rolesJSON.put(external);
            }

            final JSONObject root = new JSONObject();
            root.put(JSKEY_EXTERNAL, rolesJSON);
            ResponseHelper.writeResponse(params, root);

        } catch (Exception e) {
            throw new ActionException("Something went wrong getting roles from the platform", e);
        }
    }
}
