package fi.nls.oskari.control.layer;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@OskariActionRoute("SaveLayerPermission")
public class SaveLayerPermissionHandler extends ActionHandler {

    private static String PARAMETER_PERMISSION_DATA = "resource";
    private static int EXTERNAL_ID_TYPE = 0;
    private static int EXTERNAL_ID = 1;
    private static int EXTERNAL_RESOURCE_NAME_TYPE = 2;
    private static int EXTERNAL_RESOURCE_NAME_NAMESPACE = 3;
    private static int EXTERNAL_RESOURCE_NAME_NAME = 4;
    private static int PERMISSION_TYPE = 5;
    private static String PARAMETER_SAVE = "save";

    private final static Logger log = LogFactory.getLogger(SaveLayerPermissionHandler.class);
    private final static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        log.debug("PERMISSION HANDLER LAYER");

        Permissions permissions = new Permissions();

        if (! params.getUser().isAdmin()){
            throw new ActionDeniedException("Denied, user not admin");
        }

        JSONArray resources = null;
        final String resourceData = params.getHttpParam(PARAMETER_PERMISSION_DATA);
        try {
            resources = new JSONArray(resourceData);
            log.debug(" jSon " + resources);

        } catch (JSONException e) {
            throw new ActionParamsException("Unable to parse param JSON:\n" + resourceData);
        }

        String whoMakesThisModification = params.getUser().getEmail();

      try{
         for (int i = 0; i < resources.length();i++) {

            JSONObject layerPermission = resources.getJSONObject(i);

            boolean isViewSelected = layerPermission.getBoolean("isViewSelected");
            log.debug(layerPermission.getString("name") + " " +isViewSelected);

            log.debug(layerPermission.getString("name") );
            log.debug("resource : " + layerPermission.getString("resourceName")+ " namespace "
                    +   layerPermission.getString("namespace") + " roleId "+   layerPermission.getString("roleId")
                    +" isSelected" + layerPermission.getBoolean("isSelected")+ " isViewSelected"
                    + layerPermission.getBoolean("isViewSelected")+ " isDownloadSelected "
                    + layerPermission.getBoolean("isDownloadSelected") + "isViewPublishedSelected "
                    + layerPermission.getString("isViewPublishedSelected"));


           permissions.setExternalIdType(Permissions.EXTERNAL_TYPE_ROLE);
           permissions.setExternalId(layerPermission.getString("roleId"));
           permissions.getUniqueResourceName().setType(Permissions.RESOURCE_TYPE_WMS_LAYER);
           permissions.getUniqueResourceName().setNamespace(layerPermission.getString("namespace"));
           permissions.getUniqueResourceName().setName(layerPermission.getString("resourceName"));

           if (layerPermission.getBoolean("isViewSelected")) {
               addPermissions(permissions,Permissions.PERMISSION_TYPE_VIEW_LAYER);
           } else {
               log.warn("Changing permissions (DELETE) by user '" + whoMakesThisModification + "': " + permissions);
               deletePermissions(permissions, Permissions.PERMISSION_TYPE_VIEW_LAYER);
           }

           if (layerPermission.getBoolean("isSelected")) {
               addPermissions(permissions,Permissions.PERMISSION_TYPE_PUBLISH);
           } else {
               log.warn("Changing permissions (DELETE) by user '" + whoMakesThisModification + "': " + permissions);
               deletePermissions(permissions, Permissions.PERMISSION_TYPE_PUBLISH);
           }

           if (layerPermission.getBoolean("isDownloadSelected")) {
               addPermissions(permissions,Permissions.PERMISSION_TYPE_DOWNLOAD);
           }  else {
               log.warn("Changing permissions (DELETE) by user '" + whoMakesThisModification + "': " + permissions);
               deletePermissions(permissions, Permissions.PERMISSION_TYPE_DOWNLOAD);
           }

           if (layerPermission.getBoolean("isViewPublishedSelected")) {
               addPermissions(permissions,Permissions.PERMISSION_TYPE_VIEW_PUBLISHED);
           } else {
               log.warn("Changing permissions (DELETE) by user '" + whoMakesThisModification + "': " + permissions);
               deletePermissions(permissions, Permissions.PERMISSION_TYPE_DOWNLOAD);
           }


                //permissionsService.insertPermissions(permissions.getUniqueResourceName(),
                //permissions.getExternalId(), permissions.getExternalIdType(), Permissions.PERMISSION_TYPE_VIEW_LAYER);

           /* else {
                log.warn("Changing permissions (DELETE) by user '" + whoMakesThisModification + "': " + permissions);
                //permissionsService.deletePermissions(permissions.getUniqueResourceName(), permissions.getExternalId(),
                // permissions.getExternalIdType(), Permissions.PERMISSION_TYPE_VIEW_LAYER);
            }*/

        }

      }catch (JSONException e) {
          e.printStackTrace();
      }

    }

    private void addPermissions(final Permissions permissions, final String permissionType) {
        permissionsService.insertPermissions(permissions.getUniqueResourceName(),
                permissions.getExternalId(), permissions.getExternalIdType(), permissionType);
    }

    private void deletePermissions(final Permissions permissions, final String permissionType) {
        permissionsService.deletePermissions(permissions.getUniqueResourceName(), permissions.getExternalId(),
                permissions.getExternalIdType(), permissionType);
    }

}



