package flyway.sample;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.util.List;

/**
 * Created by Marko Kuosmanen on 25.9.2015.
 */
public class V1_0_3__change_osm_layer_names implements JdbcMigration {
    private static final ViewService VIEW_SERVICE = new ViewServiceIbatisImpl();
    private static final OskariLayerService LAYER_SERVICE = new OskariLayerServiceIbatisImpl();

    private static final String OSM_WMS_WORLDWIDE_URL = "http://129.206.228.72/cached/osm";
    private static final String OSM_WMS_WORLDWIDE_NAME = "osm_auto:all";
    private static final String OSM_WMS_WORLDWIDE_UI_NAME = "OSM Wordwide";
    private static final String OSM_WMS_LANDSAT_URL = "http://irs.gis-lab.info/";
    private static final String OSM_WMS_LANDSAT_NAME = "landsat";
    private static final String OSM_WMS_LANDSAT_UI_NAME = "OSM Landsat";

    private static final String PARAM_NAME = "name";

    public void migrate(Connection connection)
            throws Exception {
        List<OskariLayer> layers = LAYER_SERVICE.findAll();

        for (int i = 0; i < layers.size(); i++) {
            OskariLayer layer = layers.get(i);

            if((OSM_WMS_WORLDWIDE_NAME.equals(layer.getName()) && OSM_WMS_WORLDWIDE_URL.equals(layer.getUrl()))){
                updateLayer(layer, OSM_WMS_WORLDWIDE_UI_NAME);
            }
            if(OSM_WMS_LANDSAT_NAME.equals(layer.getName()) && OSM_WMS_LANDSAT_URL.equals(layer.getUrl())){
                updateLayer(layer, OSM_WMS_LANDSAT_UI_NAME);
            }
        }
    }

    private void updateLayer(OskariLayer layer, String uiName) throws JSONException{
        String[] locales = PropertyUtil.getSupportedLanguages();
        JSONObject locale = new JSONObject();
        JSONObject layerName = new JSONObject();
        layerName.put(PARAM_NAME, uiName);

        for(int i=0;i<locales.length;i++){
            String l = locales[i];
            locale.put(l,layerName);
        }
        layer.setLocale(locale);
        LAYER_SERVICE.update(layer);
    }
}
