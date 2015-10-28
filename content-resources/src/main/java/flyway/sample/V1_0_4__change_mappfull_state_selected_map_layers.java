package flyway.sample;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
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

/**
 * Created by Marko Kuosmanen on 25.9.2015.
 */
public class V1_0_4__change_mappfull_state_selected_map_layers implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_4__change_mappfull_state_selected_map_layers.class);
    private static final ViewService VIEW_SERVICE = new ViewServiceIbatisImpl();
    private static final OskariLayerService LAYER_SERVICE = new OskariLayerServiceIbatisImpl();

    private static final String OSM_WMS_WORLDWIDE_URL = "http://129.206.228.72/cached/osm";
    private static final String OSM_WMS_WORLDWIDE_NAME = "osm_auto:all";
    private static final String MAPFULL = "mapfull";
    private static final String SELECTED_LAYERS = "selectedLayers";
    private static final String ID = "id";
    private static final String OPACITY = "opacity";

    public void migrate(Connection connection)
            throws Exception {
        View view = VIEW_SERVICE.getViewWithConf(VIEW_SERVICE.getDefaultViewId());
        final Bundle mapfull = view.getBundleByName(MAPFULL);
        List<OskariLayer> layers = LAYER_SERVICE.findByUrlAndName(OSM_WMS_WORLDWIDE_URL, OSM_WMS_WORLDWIDE_NAME);
        if(layers.size()>0){
            int layerId = layers.get(0).getId();
            boolean changed = changeState(mapfull, layerId);
            if(changed){
                VIEW_SERVICE.updateBundleSettingsForView(view.getId(), mapfull);
            }
        }
    }

    private boolean changeState(final Bundle mapfull, int layerId) throws JSONException {
        final JSONObject state = mapfull.getStateJSON();
        final JSONArray selectedLayers = state.optJSONArray(SELECTED_LAYERS);
        if(selectedLayers == null) {
            throw new RuntimeException("No selectedLayers" + selectedLayers.toString(2));
        }

        for(int i=0;i<selectedLayers.length();i++){
            final JSONObject layer = selectedLayers.getJSONObject(i);
            final int selectedLayerId = layer.getInt(ID);
            if(layerId!=selectedLayerId){
                selectedLayers.remove(i);
            }
        }

        // check at if there is no selected layers
        if(selectedLayers.length()==0) {
            JSONObject layer = new JSONObject();
            layer.put(ID, layerId);
            layer.put(OPACITY, 100);
            selectedLayers.put(layer);
        }

        LOG.debug("SelectedLayers: ", selectedLayers.toString(2));

        return true;
    }
}
