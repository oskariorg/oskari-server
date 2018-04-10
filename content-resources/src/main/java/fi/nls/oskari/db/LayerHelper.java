package fi.nls.oskari.db;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.DataProviderServiceIbatisImpl;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkServiceMybatisImpl;
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

public class LayerHelper {

    private static final Logger log = LogFactory.getLogger(LayerHelper.class);
    private static final OskariMapLayerGroupService groupService = new OskariMapLayerGroupServiceIbatisImpl();
    private static final OskariLayerGroupLinkService linkService = new OskariLayerGroupLinkServiceMybatisImpl();
    private static final DataProviderService dataProviderService = new DataProviderServiceIbatisImpl();
    private static final PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
    private static final MybatisRoleService roleService = new MybatisRoleService();

    public static int setupLayer(final String layerfile) throws IOException, JSONException {
        final String jsonStr = IOHelper.readString(DBHandler.getInputStreamFromResource("/json/layers/" + layerfile));
        final JSONObject json = JSONHelper.createJSONObject(jsonStr);
        final OskariLayer layer = parseLayer(json);

        final OskariLayerService service = new OskariLayerServiceIbatisImpl();
        final List<OskariLayer> dbLayers = service.findByUrlAndName(layer.getUrl(), layer.getName());
        if(!dbLayers.isEmpty()) {
            if(dbLayers.size() > 1) {
                log.warn("Found multiple layers with same url and name. Using first one. Url:", layer.getUrl(), "- name:", layer.getName());
            }
            return dbLayers.get(0).getId();
        } else {
            // layer doesn't exist, insert it
            int id = service.insert(layer);
            layer.setId(id);
            setupLayerPermissions(json.getJSONObject("role_permissions"), layer);

            final String groupName = json.getString("inspiretheme");
            // handle inspiretheme
            final MaplayerGroup group = groupService.findByName(groupName);
            if (group == null) {
                log.warn("Didn't find match for theme:", groupName);
            } else {
                linkService.insert(new OskariLayerGroupLink(id, group.getId()));
            }
            return id;
        }
    }


    /**
     * Minimal implementation for parsing layer in json format.
     * @param json
     * @return
     */
    private static OskariLayer parseLayer(final JSONObject json) throws JSONException {
        OskariLayer layer = new OskariLayer();

        // read mandatory values, an JSONException is thrown if these are missing
        layer.setType(json.getString("type"));
        layer.setUrl(json.getString("url"));
        layer.setName(json.getString("name"));
        final String orgName = json.getString("organization");

        layer.setLocale(json.getJSONObject("locale"));

        // read optional values
        layer.setBaseMap(json.optBoolean("base_map", layer.isBaseMap()));
        layer.setOpacity(json.optInt("opacity", layer.getOpacity()));
        layer.setStyle(json.optString("style", layer.getStyle()));
        layer.setMinScale(json.optDouble("minscale", layer.getMinScale()));
        layer.setMaxScale(json.optDouble("maxscale", layer.getMaxScale()));
        layer.setLegendImage(json.optString("legend_image", layer.getLegendImage()));
        layer.setMetadataId(json.optString("metadataid", layer.getMetadataId()));
        layer.setGfiType(json.optString("gfi_type", layer.getGfiType()));
        layer.setGfiXslt(json.optString("gfi_xslt", layer.getGfiXslt()));
        layer.setGfiContent(json.optString("gfi_content", layer.getGfiContent()));
        layer.setGeometry(json.optString("geometry", layer.getGeometry()));
        layer.setRealtime(json.optBoolean("realtime", layer.getRealtime()));
        layer.setRefreshRate(json.optInt("refresh_rate", layer.getRefreshRate()));
        layer.setSrs_name(json.optString("srs_name", layer.getSrs_name()));
        layer.setVersion(json.optString("version", layer.getVersion()));
        layer.setUsername(json.optString("username", layer.getUsername()));
        layer.setPassword(json.optString("password", layer.getPassword()));
        // omit permissions, these are handled by LayerHelper

        // handle params, check for null to avoid overwriting empty JS Object Literal
        final JSONObject params = json.optJSONObject("params");
        if (params != null) {
            layer.setParams(params);
        }

        // handle options, check for null to avoid overwriting empty JS Object Literal
        final JSONObject options = json.optJSONObject("options");
        if (options != null) {
            layer.setOptions(options);
        }

        // setup data producer/layergroup
        final DataProvider dataProvider = dataProviderService.findByName(orgName);
        if(dataProvider == null) {
            log.warn("Didn't find match for layergroup:", orgName);
        } else {
            layer.addDataprovider(dataProvider);
        }

        return layer;
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
