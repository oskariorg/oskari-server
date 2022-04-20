package org.oskari.helpers;

import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.OskariRuntimeException;
import org.oskari.admin.LayerCapabilitiesHelper;
import org.oskari.admin.MapLayerGroupsHelper;
import org.oskari.admin.MapLayerPermissionsHelper;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.CapabilitiesUpdateResult;
import org.oskari.maplayer.model.MapLayer;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Service-admin provides helpers for inserting layers BUT this helper has been used in multiple migrations for setting
// up initial content so keeping it as compatibility solution
public class LayerHelper {

    private static final Logger log = LogFactory.getLogger(LayerHelper.class);
    private static final OskariLayerService layerService = new OskariLayerServiceMybatisImpl();

    public static int setupLayer(final String layerfile) throws IOException {
        final String jsonStr = readLayerFile(layerfile);
        MapLayer layer = LayerAdminJSONHelper.readJSON(jsonStr);
        final List<OskariLayer> dbLayers = layerService.findByUrlAndName(layer.getUrl(), layer.getName());
        if(!dbLayers.isEmpty()) {
            if(dbLayers.size() > 1) {
                log.warn("Found multiple layers with same url and name. Using first one. Url:", layer.getUrl(), "- name:", layer.getName());
            }
            return dbLayers.get(0).getId();
        } else {
            // layer doesn't exist, insert it
            // fromJSON validates parsed layer and throws IllegalArgumentException if layer is not valid
            final OskariLayer oskariLayer = LayerAdminJSONHelper.fromJSON(layer);
            if (!oskariLayer.getUrl().startsWith("http://localhost:")) {
                // add info from capabilities if not from localhost (this is usually called when server starting == localhost doesn't work properly)
                try {
                    LayerCapabilitiesHelper.updateCapabilities(oskariLayer);
                } catch (ServiceException e) {
                    log.warn(e,"Error updating capabilities for service from", oskariLayer.getUrl());
                    if (OskariLayer.TYPE_WMTS.equals(oskariLayer.getType())) {
                        log.warn("The WMTS-layer", oskariLayer.getName(),
                                "might work slower than normal with capabilities/tilegrids not cached. Try caching the capabilities later using the admin UI.");
                    }
                }

            }
            // insert to db
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

    /**
     * So we can get updated "supported SRS" for layers after inserting a new appsetup with possibly new projection
     * @throws ServiceException
     */
    protected static void refreshLayerCapabilities(String srsForNewAppsetup) {
        Set<String> systemSRSlist;
        try {
            systemSRSlist = LayerCapabilitiesHelper.getSystemCRSs();
        } catch (Exception e) {
            throw new OskariRuntimeException("Couldn't get system crs list");
        }
        List<OskariLayer> layers = layerService.findAll().stream()
                // skip localhost servers as the service is starting when this is called and geoserver will not answer
                .filter(l -> !l.getUrl().startsWith("http://localhost:"))
                // only update layers when new projection is added
                .filter(layer -> !LayerJSONFormatter.getSRSs(layer.getAttributes(), layer.getCapabilities()).contains(srsForNewAppsetup))
                .collect(Collectors.toList());
        CapabilitiesService.updateCapabilities(layers, systemSRSlist);
        // Save updated capabilities to db
        for (OskariLayer layer : layers) {
            try {
                layerService.update(layer);
            } catch (Exception e) {
                log.warn(e,"Couldn't update capabilities for layer:", layer.getUrl(), layer.getName());
            }
        }
    }

    protected static String readLayerFile(final String file) {
        String json = getPaths(file).stream()
                .map(filename -> tryResource(filename))
                .filter(j -> j != null && !j.isEmpty())
                .findFirst()
                .orElse(null);

        if (json == null) {
            throw new OskariRuntimeException("Couldn't locate layer JSON for " + file);
        }
        return json;
    }

    private static List<String> getPaths(String filename) {
        List<String> paths = new ArrayList<>();
        if (filename == null) {
            return paths;
        } else if (filename.startsWith("/")) {
            paths.add(filename);
            return paths;
        }
        paths.add("/json/layers/" + filename);
        paths.add("/" + filename);
        return paths;
    }

    private static String tryResource(String name) {
        try {
            return IOHelper.readString(LayerHelper.class.getResourceAsStream(name));
        } catch (Exception e) {
            log.info("Tried file:", name, "- Error:", e.getMessage());
        }
        return null;
    }
}
