package flyway.oskari;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;

public class V2_7_3__migrate_statsgrid_colors extends BaseJavaMigration {
    private static final String BUNDLE = "statsgrid";
    private static final List<String> COLORS = Arrays.asList("#00ff01", "#26bf4b", "#3233ff", "#652d90", "#cccccc", "#000000", "#bf2652", "#ff3334", "#f8931f", "#ffde00", "#666666", "#ffffff");

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<Long> ids = AppSetupHelper.getSetupsForType(connection, ViewTypes.PUBLISHED, ViewTypes.USER);
        for (Long id: ids) {
            migrateColor(connection, id);
        }
    }
    private void migrateColor (Connection connection, Long id) throws Exception {
        Bundle bundle = AppSetupHelper.getAppBundle(connection, id, BUNDLE);
        if (bundle == null) {
            return;
        }
        JSONArray indicators = JSONHelper.getJSONArray(bundle.getStateJSON(), "indicators");
        if (indicators == null || indicators.length() == 0) {
            return;
        }
        for(int i=0; i < indicators.length(); i++) {
            JSONObject indicator = JSONHelper.getJSONObject(indicators, i);
            JSONObject classification = JSONHelper.getJSONObject(indicator, "classification");
            if (classification == null) {
                continue;
            }
            if ("points".equals(classification.optString("mapStyle"))) {
                // default string values to first color
                int index = classification.optInt("name");
                classification.put("color", COLORS.get(index));
            } else {
                String name = classification.optString("name", "Blues");
                classification.put("color", name);
            }
            classification.remove("name");
        }
        AppSetupHelper.updateAppBundle(connection, id, bundle);
    }
}
