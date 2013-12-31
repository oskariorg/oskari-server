package fi.nls.oskari.control.layer;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.LayerGroup;
import fi.nls.oskari.domain.map.wms.LayerClass;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.LayerGroupService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.ServiceFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Admin insert/update of class layer or class sub layer
 */
@OskariActionRoute("SaveOrganization")
public class SaveOrganizationHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(SaveOrganizationHandler.class);

    private final LayerGroupService layerGroupService = ServiceFactory.getLayerGroupService();
    private final PermissionsService permissionsService = ServiceFactory.getPermissionsService();

    private static final String PARAM_GROUP_ID = "id";
    private static final String NAME_PREFIX = "name_";


    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final HttpServletRequest request = params.getRequest();
        try {
            final int groupId = params.getHttpParam(PARAM_GROUP_ID, -1);
            final LayerGroup group = new LayerGroup();
            group.setId(groupId);
            handleLocalizations(group, NAME_PREFIX, request);

            // ************** UPDATE ************************
            if (groupId != -1) {
                if (!layerGroupService.hasPermissionToUpdate(params.getUser(), groupId)) {
                    throw new ActionDeniedException("Unauthorized user tried to update layer group - id=" + groupId);
                }
                layerGroupService.update(group);
                ResponseHelper.writeResponse(params, group.getAsJSON());
            }
            // ************** INSERT ************************
            else if(params.getUser().isAdmin()) {
                final int id = layerGroupService.insert(group);
                group.setId(id);
                ResponseHelper.writeResponse(params, group.getAsJSON());

                //final String[] externalIds = params.getHttpParam("viewPermissions", "").split(",");
                //addPermissionsForAdmin(lc, params.getUser(), externalIds);
            }
            else {
                throw new ActionDeniedException("Unauthorized user tried to update layer group - id=" + groupId);
            }

        } catch (Exception e) {
            throw new ActionException("Couldn't update/insert map layer class", e);
        }
    }

    private void handleLocalizations(final LayerGroup lc, final String nameprefix, final HttpServletRequest request) {
        final int prefixLength = nameprefix.length();
        final Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final String nextName = paramNames.nextElement();
            if (nextName.indexOf(nameprefix) == 0) {
                lc.setName(nextName.substring(prefixLength), request.getParameter(nextName));
            }
        }
    }

    private void addPermissionsForAdmin(final LayerClass lc, final User user, final String[] externalIds) {
        Permissions permissions = new Permissions();
        permissions.getUniqueResourceName().setType(Permissions.RESOURCE_TYPE_LAYER_GROUP);
        permissions.getUniqueResourceName().setNamespace("BASE");
        permissions.getUniqueResourceName().setName(String.valueOf(lc.getId()));

        // insert permissions
        for (String externalId : externalIds) {
            if (user.hasRoleWithId(ConversionHelper.getLong(externalId, -1))) {
                permissionsService.insertPermissions(permissions.getUniqueResourceName(), externalId, Permissions.EXTERNAL_TYPE_ROLE, Permissions.PERMISSION_TYPE_VIEW_LAYER);
                permissionsService.insertPermissions(permissions.getUniqueResourceName(), externalId, Permissions.EXTERNAL_TYPE_ROLE, Permissions.PERMISSION_TYPE_EDIT_LAYER);
            }
        }
    }
}
