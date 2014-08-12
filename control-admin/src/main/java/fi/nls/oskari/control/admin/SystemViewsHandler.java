package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.user.IbatisRoleService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

@OskariActionRoute("SystemViews")
public class SystemViewsHandler extends RestActionHandler {

    private Logger log = LogFactory.getLogger(SystemViewsHandler.class);
    private static ViewService viewService;
    private static IbatisRoleService roleService;

    public void init() {
        viewService = new ViewServiceIbatisImpl();
        roleService = new IbatisRoleService();
    }

    /**
     * Responds with json containing:
     * - global default viewId
     * - list of roles in the system
     * - roles will have viewId as role-based default view id if it's configured and is not the same as global default.
     * @param params
     * @throws ActionException
     */
    @Override
    public void handleGet(ActionParameters params) throws ActionException {

        final JSONObject response = new JSONObject();
        final JSONArray list = new JSONArray();

        final long globalDefaultViewId = viewService.getDefaultViewId();
        JSONHelper.putValue(response, "viewId", globalDefaultViewId);

        final List<Role> roles = roleService.findAll();
        for (Role role : roles) {
            final JSONObject json = new JSONObject();
            JSONHelper.putValue(json, "id", role.getId());
            JSONHelper.putValue(json, "name", role.getName());

            final long viewId = viewService.getDefaultViewIdForRole(role.getName());
            if (viewId != globalDefaultViewId) {
                JSONHelper.putValue(json, "viewId", viewId);
            }
            list.put(json);
        }

        JSONHelper.putValue(response, "roles", list);
        JSONHelper.putValue(response, "timestamp", new Date());
        ResponseHelper.writeResponse(params, response);
    }


    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

}