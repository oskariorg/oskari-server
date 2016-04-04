package fi.nls.oskari.control.layer;

import java.util.*;
import java.util.Map.Entry;

import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
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

import static fi.nls.oskari.control.ActionConstants.*;
/**

Configuring additional permission types in oskari-ext.properties

permission.types = EDIT_LAYER_CONTENT
permission.EDIT_LAYER_CONTENT.name.fi=Muokkaa tasoa
permission.EDIT_LAYER_CONTENT.name.en=Edit layer

 */
@OskariActionRoute("GetPermissionsLayerHandlers")
public class GetPermissionsLayerHandlers extends ActionHandler {

    private static OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
    private static String JSON_NAMES_SPACE = "namespace";
    private static String JSON_RESOURCE_NAME = "resourceName";
    private static String JSON_RESOURCE = "resource";

    private static final Set<String> PERMISSIONS =
            ConversionHelper.asSet(Permissions.PERMISSION_TYPE_VIEW_LAYER, Permissions.PERMISSION_TYPE_VIEW_PUBLISHED,
                    Permissions.PERMISSION_TYPE_PUBLISH, Permissions.PERMISSION_TYPE_DOWNLOAD);

    @Override
    public void init() {
        super.init();

        // add any additional permissions
        PERMISSIONS.addAll(permissionsService.getAdditionalPermissions());
    }


    @Override
    public void handleAction(ActionParameters params) throws ActionException {
    	
    	// require admin user
        params.requireAdminUser();

        final String externalId = params.getRequiredParam("externalId");
        final String externalType = params.getRequiredParam("externalType");

    	Map<String, List<String>> resourcesMap = new HashMap<String, List<String>>();

        final JSONArray permissionNames = new JSONArray();
    	for (String id : PERMISSIONS)
    	{
            JSONObject perm = new JSONObject();
            JSONHelper.putValue(perm, KEY_ID, id);
            JSONHelper.putValue(perm, KEY_NAME, permissionsService.getPermissionName(id, params.getLocale().getLanguage()));
            permissionNames.put(perm);
            // list resources having the permission
    		List<String> val = permissionsService.getResourcesWithGrantedPermissions(Permissions.RESOURCE_TYPE_MAP_LAYER, externalId, externalType, id);
        	resourcesMap.put(id, val);
    	}
        final JSONObject root = new JSONObject();
        JSONHelper.putValue(root, "names", permissionNames);

        final List<OskariLayer> layers = mapLayerService.findAll();
        Collections.sort(layers);


        for (OskariLayer layer : layers) {
            try {
                final OskariLayerResource res = new OskariLayerResource(layer);
                JSONObject realJson = new JSONObject();
                realJson.put(KEY_ID, layer.getId());
                realJson.put(KEY_NAME, layer.getName(PropertyUtil.getDefaultLanguage()));
                realJson.put(JSON_NAMES_SPACE, res.getNamespace());
                realJson.put(JSON_RESOURCE_NAME, res.getName());
                final String permissionMapping = res.getMapping();

                JSONArray jsonResults = new JSONArray();
                for (Entry<String, List<String>> resource : resourcesMap.entrySet())
                {
                	JSONObject layerJson = new JSONObject();
                    layerJson.put(KEY_ID, resource.getKey());
                    layerJson.put("allow", resource.getValue().contains(permissionMapping));
                	jsonResults.put(layerJson);
                }
                realJson.put("permissions", jsonResults);

                root.append(JSON_RESOURCE, realJson);
            } catch (JSONException e) {
                throw new ActionException("Something is wrong with doPermissionResourcesJson ajax reguest", e);
            }

        }

        ResponseHelper.writeResponse(params, root.toString());
    }
}
