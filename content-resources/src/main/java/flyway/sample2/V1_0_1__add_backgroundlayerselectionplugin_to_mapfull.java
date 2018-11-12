package flyway.sample2;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Marko Kuosmanen on 23.9.2015.
 */
public class V1_0_1__add_backgroundlayerselectionplugin_to_mapfull implements JdbcMigration {
    private static final ViewService VIEW_SERVICE = new ViewServiceIbatisImpl();
    private static final OskariLayerService LAYER_SERVICE = new OskariLayerServiceIbatisImpl();
    private static final String PLUGIN_NAME = "Oskari.mapframework.bundle.mapmodule.plugin.BackgroundLayerSelectionPlugin";
    private static final String MAPFULL = "mapfull";

    private static final String OSM_WMS_WORLDWIDE_URL = "http://129.206.228.72/cached/osm";
    private static final String OSM_WMS_WORLDWIDE_NAME = "osm_auto:all";
    private static final String OSM_WMS_LANDSAT_URL = "http://irs.gis-lab.info/";
    private static final String OSM_WMS_LANDSAT_NAME = "landsat";

   // private static final String OPENMAPTILES_URL = "http://demo.oskari.org/cached/openmaptiles";
    private static final String OPENMAPTILES_URL = "https://maps.tilehosting.com/data/v3/{z}/{x}/{y}.pbf?key=qrAN6vh4XeHdA2eCyKFa";

    private static final String OPENMAPTILES_NAME = "openmaptiles";


    public void migrate(Connection connection)
            throws Exception {
        View view = VIEW_SERVICE.getViewWithConf(VIEW_SERVICE.getDefaultViewId());
        final Bundle mapfull = view.getBundleByName(MAPFULL);
        boolean addedPlugin = addPlugin(mapfull);
        if(addedPlugin) {
            VIEW_SERVICE.updateBundleSettingsForView(view.getId(), mapfull);
        }
    }

    private boolean addPlugin(final Bundle mapfull) throws JSONException {
        final JSONObject config = mapfull.getConfigJSON();
        final JSONArray plugins = config.optJSONArray("plugins");
        if(plugins == null) {
            throw new RuntimeException("No plugins" + config.toString(2));
        }
        boolean found = false;
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.getJSONObject(i);
            if(PLUGIN_NAME.equals(plugin.optString("id"))) {
                found = true;
                break;
            }
        }
        // add plugin if not there yet
        if(!found) {
            JSONObject plugin = new JSONObject();
            plugin.put("id", PLUGIN_NAME);
            JSONObject pluginConfig = new JSONObject();
            JSONArray baseLayers = new JSONArray();
            LAYER_SERVICE.findAll().stream()
                    .filter(layer ->
                        (layer.getName().equals(OSM_WMS_WORLDWIDE_NAME) && layer.getUrl().equals(OSM_WMS_WORLDWIDE_URL)) ||
                        (layer.getName().equals(OSM_WMS_LANDSAT_NAME) && layer.getUrl().equals(OSM_WMS_LANDSAT_URL)) ||
                        (layer.getName().equals(OPENMAPTILES_NAME) && layer.getUrl().equals(OPENMAPTILES_URL)))
                    .forEach(layer -> baseLayers.put(Integer.toString(layer.getId())));

            pluginConfig.put("baseLayers", baseLayers);
            pluginConfig.put("showAsDropdown", false);
            plugin.put("config", pluginConfig);
            plugins.put(plugin);
        }
        return true;
    }

}
