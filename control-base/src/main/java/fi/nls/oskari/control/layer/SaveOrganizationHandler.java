package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.service.db.LayerClassService;
import fi.mml.map.mapwindow.service.db.LayerClassServiceIbatisImpl;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.wms.LayerClass;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Admin insert/update of class layer or class sub layer
 * 
 */
@OskariActionRoute("SaveOrganization")
public class SaveOrganizationHandler extends ActionHandler {

    private LayerClassService layerClassService = new LayerClassServiceIbatisImpl();
    private static final Logger log = LogFactory.getLogger(SaveOrganizationHandler.class);
    private static final String PARM_LAYERCLASS_ID = "layerclass_id";
    private static final String PARM_PARENT_ID = "parent_id";

    private static final String SUB_NAME_PREFIX = "sub_name_";
    private static final String NAME_PREFIX = "name_";


    private PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        HttpServletRequest request = params.getRequest();

        try {

            final String layercl_id = params.getHttpParam(PARM_LAYERCLASS_ID, "");
            final String parentId = params.getHttpParam(PARM_PARENT_ID, "");
            final int layclId = ConversionHelper.getInt(layercl_id, 0);
            
            if(!layerClassService.hasPermissionToUpdate(params.getUser(), layclId)) {
                throw new ActionDeniedException("Unauthorized user tried to update layer - id=" + layercl_id);
            }

            // ************** UPDATE ************************
            if (!layercl_id.isEmpty()) {
                if (parentId.isEmpty()) {

                    int layerClassId = ConversionHelper.getInt(layercl_id, 0); // id
                    LayerClass lc = new LayerClass();
                    lc.setId(layerClassId);
                    handleLocalizations(lc, NAME_PREFIX,  request);

                    layerClassService.update(lc);

                } else {
                    int layerClassId = ConversionHelper.getInt(layercl_id, 0); // sub_id

                    LayerClass lc = new LayerClass();
                    lc.setId(layerClassId);
                    handleLocalizations(lc, SUB_NAME_PREFIX, request);

                    lc.setMapLayersSelectable(request.getParameterMap()
                            .containsKey("sub_maplayers_selectable"));
                    lc.setLegendImage(request.getParameter("sub_legend_image"));
                    lc.setDataUrl(request.getParameter("sub_data_url"));
                    lc.setParent(ConversionHelper.getInt(parentId, 0));
                    lc.setGroupMap(request.getParameterMap().containsKey(
                            "group_map"));
                    layerClassService.update(lc);
                }
            }

            // ************** INSERT ************************
            else {
                if (parentId.isEmpty()) {

                    LayerClass lc = new LayerClass();

                    handleLocalizations(lc, NAME_PREFIX, request);

                    lc.setMapLayersSelectable(request.getParameterMap()
                            .containsKey("sub_maplayers_selectable"));
                    lc.setGroupMap(request.getParameterMap().containsKey(
                            "group_map"));
                    lc.setLegendImage(request.getParameter("sub_legend_image"));
                    lc.setDataUrl(request.getParameter("sub_data_url"));
                    layerClassService.insert(lc);

                } else { // New sub layer class

                    LayerClass lc = new LayerClass();

                    handleLocalizations(lc, SUB_NAME_PREFIX, request);
                    
                    lc.setMapLayersSelectable(request.getParameterMap()
                            .containsKey("sub_maplayers_selectable"));
                    lc.setGroupMap(request.getParameterMap().containsKey(
                            "group_map"));
                    lc.setLegendImage(request.getParameter("sub_legend_image"));
                    lc.setDataUrl(request.getParameter("sub_data_url"));
                    lc.setParent(Integer.parseInt(parentId));

                    int id = layerClassService.insert(lc);
                    lc.setId(id);

                    final String[] externalIds = params.getHttpParam("viewPermissions", "").split(",");
                    addPermissionsForAdmin(lc, params.getUser(), externalIds);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ActionException("Couldn't update/insert map layer class",
                    e);
        }
    }


    private void handleLocalizations(final LayerClass lc, final String nameprefix, final HttpServletRequest request) {
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String nextName = paramNames.nextElement();
            if (nextName.indexOf(nameprefix) == 0) {
                lc.setName(nextName.substring(nameprefix.length()), request.getParameter(nextName));
            }
        }
    }

    private void addPermissionsForAdmin(final LayerClass lc, final User user, final String[] externalIds) {
        Permissions permissions = new Permissions();
        permissions.getUniqueResourceName().setType(Permissions.RESOURCE_TYPE_LAYER_GROUP);
        permissions.getUniqueResourceName().setNamespace("");
        permissions.getUniqueResourceName().setName(String.valueOf(lc.getId()));

        // insert permissions
        for (String externalId : externalIds) {
            if(user.hasRoleWithId(ConversionHelper.getLong(externalId, -1))) {
                permissionsService.insertPermissions(permissions.getUniqueResourceName(), externalId, Permissions.EXTERNAL_TYPE_ROLE, Permissions.PERMISSION_TYPE_VIEW_LAYER);
                permissionsService.insertPermissions(permissions.getUniqueResourceName(), externalId, Permissions.EXTERNAL_TYPE_ROLE, Permissions.PERMISSION_TYPE_EDIT_LAYER);
            }
        }
    }
}
