package fi.nls.oskari.db;

import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.util.IOHelper;
import org.oskari.admin.MapLayerGroupsHelper;
import org.oskari.admin.MapLayerPermissionsHelper;
import org.oskari.data.model.MapLayer;
import org.oskari.admin.LayerAdminJSONHelper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// Service-admin provides helpers for inserting layers BUT this helper has been used in multiple migrations for setting
// up initial content so keeping it as compatibility solution
public class LayerHelper {

    private static final Logger log = LogFactory.getLogger(LayerHelper.class);
    private static final OskariLayerService layerService = new OskariLayerServiceMybatisImpl();

    public static int setupLayer(final String layerfile) throws IOException {
        final String jsonStr = IOHelper.readString(DBHandler.getInputStreamFromResource("/json/layers/" + layerfile));
        MapLayer layer = LayerAdminJSONHelper.readJSON(jsonStr);
        // TODO: validate parsed layer?
        final List<OskariLayer> dbLayers = layerService.findByUrlAndName(layer.getUrl(), layer.getName());
        if(!dbLayers.isEmpty()) {
            if(dbLayers.size() > 1) {
                log.warn("Found multiple layers with same url and name. Using first one. Url:", layer.getUrl(), "- name:", layer.getName());
            }
            return dbLayers.get(0).getId();
        } else {
            // layer doesn't exist, insert it
            final OskariLayer oskariLayer = LayerAdminJSONHelper.fromJSON(layer);
            int id = layerService.insert(oskariLayer);
            MapLayerPermissionsHelper.setLayerPermissions(id, layer.getRole_permissions());

            if (layer.getGroups() != null) {
                List<MaplayerGroup> groups = MapLayerGroupsHelper.findGroupsForNames_dangerzone_(layer.getGroups());
                if (layer.getGroups().size() != groups.size()) {
                    log.warn("Couldn't find all the layer groups for layer.");
                }
                MapLayerGroupsHelper.setGroupsForLayer(id, groups.stream()
                        .map(g -> g.getId())
                        .collect(Collectors.toList()));
            }
            return id;
        }
    }

}
