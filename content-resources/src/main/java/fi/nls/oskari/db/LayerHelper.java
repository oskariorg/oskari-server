package fi.nls.oskari.db;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.permission.domain.Permission;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.user.MybatisRoleService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 27.6.2014
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public class LayerHelper {

    private static Logger log = LogFactory.getLogger(LayerHelper.class);
    private static final LayerJSONFormatter LAYER_JSON_PARSER = new LayerJSONFormatter();
    private static final PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
    private static final MybatisRoleService roleService = new MybatisRoleService();

    public static int setupLayer(final String layerfile) throws IOException, JSONException {
        final String jsonStr = IOHelper.readString(DBHandler.getInputStreamFromResource("/json/layers/" + layerfile));
        final JSONObject json = JSONHelper.createJSONObject(jsonStr);
        final OskariLayer layer = LAYER_JSON_PARSER.parseLayer(json);
        final OskariLayerService service = new OskariLayerServiceIbatisImpl();
        final List<OskariLayer> dbLayers = service.findByUrlAndName(layer.getUrl(), layer.getName());
        if(!dbLayers.isEmpty()) {
            if(dbLayers.size() > 1) {
                log.warn("Found multiple layers with same url and name. Using first one. Url:", layer.getUrl(), "- name:", layer.getName());
            }
            return dbLayers.get(0).getId();
        }
        else {
            if(OskariLayer.TYPE_WFS.equals(layer.getType())) {
                // TODO: parse WFS related SLD, template_model and configuration
                // Only insert wfs layer if these are ok
            }
            // layer doesn't exist, insert it
            int id = service.insert(layer);
            layer.setId(id);
            setupLayerPermissions(json.getJSONObject("role_permissions"), layer);

            if(OskariLayer.TYPE_WFS.equals(layer.getType())) {
                // TODO: insert WFS related SLD, template_model and configuration
                // When layer has been inserted to get correct layer id
            }

            return id;
        }
    }

    /*
    "role_permissions": {
        "Guest" : ["VIEW_LAYER"],
        "User" : ["VIEW_LAYER"],
        "Administrator" : ["VIEW_LAYER"]
    }
    */
    private static void setupLayerPermissions(JSONObject permissions, OskariLayer layer) {

        // setup rights
        if(permissions == null) {
            return;
        }
        final Resource res = new OskariLayerResource(layer);

        final Iterator<String> roleNames = permissions.keys();
        while(roleNames.hasNext()) {
            final String roleName = roleNames.next();
            final Role role = roleService.findRoleByName(roleName);
            if(role == null) {
                log.warn("Couldn't find matching role in DB:", roleName, "- Skipping!");
                continue;
            }
            final JSONArray permissionTypes = permissions.optJSONArray(roleName);
            if(permissionTypes == null) {
                continue;
            }
            for (int i = 0; i < permissionTypes.length(); ++i) {
                final Permission permission = new Permission();
                permission.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
                permission.setExternalId("" + role.getId());
                final String type = permissionTypes.optString(i);
                permission.setType(type);
                res.addPermission(permission);
            }
        }
        permissionsService.saveResourcePermissions(res);
    }
}
