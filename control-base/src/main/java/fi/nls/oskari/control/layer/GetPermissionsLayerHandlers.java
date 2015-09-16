package fi.nls.oskari.control.layer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

/**
 * @author EVAARASMAKI
 */
@OskariActionRoute("GetPermissionsLayerHandlers")
public class GetPermissionsLayerHandlers extends ActionHandler {

    private static OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
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
    	
    	// require admin user
        params.requireAdminUser();

        String externalId = params.getHttpParam("externalId", "");
        String externalType = params.getHttpParam("externalType", "");
    	
    	List<String> permissionTypes = Arrays.asList(PropertyUtil.get("permission.types")	.replaceAll("\\s+","").split(","));
    	
    	List<Map<String, String>> permissionsData = new ArrayList<Map<String, String>>();
    	Map<String, List<String>> resourcesMap = new HashMap<String, List<String>>();
    	for (String permissionType : permissionTypes)
    	{
    		String id = PropertyUtil.get("permission." + permissionType + ".id");
    		HashMap<String, String> permissionData = new HashMap<String, String>();
    		permissionData.put("id", id);
    		permissionData.put("name", PropertyUtil.get("permission." + permissionType + ".name." + params.getLocale()));
    		permissionsData.add(permissionData);
    		List<String> val = permissionsService.getResourcesWithGrantedPermissions(Permissions.RESOURCE_TYPE_MAP_LAYER, externalId, externalType, id);
        	resourcesMap.put(id, val);
    	}

        List<OskariLayer> layers = mapLayerService.findAll();
        Collections.sort(layers);
        
        JSONObject root = new JSONObject();
        for (OskariLayer layer : layers) {
            try {
                final OskariLayerResource res = new OskariLayerResource(layer);
                JSONObject realJson = new JSONObject();
                realJson.put(JSON_ID, layer.getId());
                realJson.put(JSON_NAME, layer.getName(PropertyUtil.getDefaultLanguage()));
                realJson.put(JSON_NAMES_SPACE, res.getNamespace());
                realJson.put(JSON_RESOURCE_NAME, res.getName());
                final String permissionKey = res.getMapping();

                List<Map<String, String>> jsonResults = new ArrayList<Map<String,String>>();
                for (Entry<String, List<String>> resource : resourcesMap.entrySet())
                {
                	Map<String, String> layerJson = new HashMap<String, String>();
                	
                	if (resource.getValue().contains(permissionKey))
                	{
                		layerJson.put("allow", "true");
                	}
                	else
                	{
                		layerJson.put("allow", "false");
                	}
                	layerJson.put("name", GetPermissionLocaleName(permissionsData, resource.getKey()));
                	layerJson.put("id", resource.getKey());
                	jsonResults.add(layerJson);
                }
                realJson.put("permissions", jsonResults);

                root.append(JSON_RESOURCE, realJson);
            } catch (JSONException e) {
                throw new ActionException("Something is wrong with doPermissionResourcesJson ajax reguest", e);
            }

        }

        ResponseHelper.writeResponse(params, root.toString());
    }
    
    private String GetPermissionLocaleName(List<Map<String, String>> permissions, String permissionId)
    {
    	for (Map<String, String> permission : permissions)
    	{
    		if (permission.get("id") == permissionId) {
    			return permission.get("name");
    		}
    	}
    	return "";
    }
}
