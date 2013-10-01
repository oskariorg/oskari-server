package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.service.db.LayerClassService;
import fi.mml.map.mapwindow.service.db.LayerClassServiceIbatisImpl;
import fi.mml.map.mapwindow.service.db.MapLayerService;
import fi.mml.map.mapwindow.service.db.MapLayerServiceIbatisImpl;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;


/**
 * @author EVAARASMAKI
 */
@OskariActionRoute("GetPermissionsLayerHandlers")
public class GetPermissionsLayerHandlers extends ActionHandler {

    private static MapLayerService mapLayerService = new MapLayerServiceIbatisImpl();
    private static LayerClassService mapClassService = new LayerClassServiceIbatisImpl();
    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
    private static String JSON_ID = "id";
    private static String JSON_NAME = "name";
    private static String JSON_RESOURCE_TYPE = "resourcetype";
    private static String JSON_EXTERNAL = "external";
    private static String JSON_NAMES_SPACE = "namespace";
    private static String JSON_RESOURCE_NAME = "resourceName";
    private static String JSON_RESOURCE = "resource";
    private static String JSON_IS_SELECTED = "isSelected";
    private static String JSON_IS_VIEW_SELECTED = "isViewSelected";
    private static String JSON_IS_VIEW_PUBLISHED_SELECTED = "isViewPublishedSelected";
    private static String JSON_IS_DOWNLOAD_SELECTED = "isDownloadSelected";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {


        String externalId = params.getHttpParam("externalId", "");
        String externalType = params.getHttpParam("externalType", "");

        List<Layer> layers = mapLayerService.findAll();
        Collections.sort(layers);
        List<String> resources = permissionsService.getResourcesWithGrantedPermissions(

                Permissions.RESOURCE_TYPE_WMS_LAYER, externalId, externalType,Permissions.PERMISSION_TYPE_PUBLISH);
        List<String> resourcesview = permissionsService.getResourcesWithGrantedPermissions(
                Permissions.RESOURCE_TYPE_WMS_LAYER, externalId, externalType,Permissions.PERMISSION_TYPE_VIEW_LAYER);
        List<String> resourcesviewPublished = permissionsService.getResourcesWithGrantedPermissions(
                Permissions.RESOURCE_TYPE_WMS_LAYER, externalId, externalType,Permissions.PERMISSION_TYPE_VIEW_PUBLISHED);

        List<String> resourcesdownload = permissionsService.getResourcesWithGrantedPermissions(
                Permissions.RESOURCE_TYPE_WMS_LAYER, externalId, externalType,Permissions.PERMISSION_TYPE_DOWNLOAD);

        JSONObject root = new JSONObject();

        for (Layer layer : layers) {

            try {
                JSONObject realJson = new JSONObject();
                realJson.put(JSON_NAME, layer.getName(PropertyUtil.getDefaultLanguage()));
                realJson.put(JSON_NAMES_SPACE, layer.getWmsUrl());
                realJson.put(JSON_RESOURCE_NAME, layer.getWmsName());

                if (resources.contains(layer.getWmsUrl() + "+" + layer.getWmsName())) {
                    realJson.put(JSON_IS_SELECTED, true);
                } else {
                    realJson.put(JSON_IS_SELECTED, false);
                }

                if (resourcesview != null && resourcesview.contains(layer.getWmsUrl() + "+" + layer.getWmsName())) {
                    realJson.put(JSON_IS_VIEW_SELECTED, true);
                } else {
                    realJson.put(JSON_IS_VIEW_SELECTED, false);
                }
                if (resourcesviewPublished != null && resourcesviewPublished.contains(layer.getWmsUrl() + "+" + layer.getWmsName())) {
                    realJson.put(JSON_IS_VIEW_PUBLISHED_SELECTED, true);
                } else {
                    realJson.put(JSON_IS_VIEW_PUBLISHED_SELECTED, false);
                }

                if (resourcesdownload != null && resourcesdownload.contains(layer.getWmsUrl() + "+" + layer.getWmsName())) {
                    realJson.put(JSON_IS_DOWNLOAD_SELECTED, true);
                } else {
                    realJson.put(JSON_IS_DOWNLOAD_SELECTED, false);
                }

                root.append(JSON_RESOURCE, realJson);
            } catch (JSONException e) {
                throw new ActionException("Something is wrong with doPermissionResourcesJson ajax reguest", e);
            }

        }

        ResponseHelper.writeResponse(params, root.toString());
    }
}
