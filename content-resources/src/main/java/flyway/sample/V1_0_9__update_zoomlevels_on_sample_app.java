package flyway.sample;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.util.FlywayHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.util.List;

/**
 * Updates Adds statsgrid bundle to default and user views.
 */
public class V1_0_9__update_zoomlevels_on_sample_app implements JdbcMigration {


    public void migrate(Connection connection) throws Exception {
        if(PropertyUtil.getOptional("flyway.sample.1_0_9.skip", false)) {
            return;
        }
        final List<Long> views = FlywayHelper.getViewIdsForTypes(connection, ViewTypes.DEFAULT, ViewTypes.USER, ViewTypes.PUBLISH_TEMPLATE);
        for(Long viewId : views){
            Bundle mapfull = FlywayHelper.getBundleFromView(connection, "mapfull", viewId);
            if (mapfull == null) {
                continue;
            }
            try {

                JSONObject config = mapfull.getConfigJSON();
                JSONObject opts = config.optJSONObject("mapOptions");
                String srs = opts.optString("srsName");
                if(!"EPSG:4326".equalsIgnoreCase(srs)) {
                    continue;
                }
                final String newResolutions = "[0.3515625,0.17578125,0.087890625,0.0439453125,0.02197265625,0.010986328125,0.0054931640625,0.00274658203125,0.001373291015625,0.0006866455078125,0.00034332275390625,0.000171661376953125,0.0000858306884765625,0.00004291534423828125,0.000021457672119140625,0.000010728836059570312,0.000005364418029785156,0.000002682209014892578]";
                opts.put("resolutions", new JSONArray(newResolutions));
                FlywayHelper.updateBundleInView(connection, mapfull, viewId);
            } catch(Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }
}
