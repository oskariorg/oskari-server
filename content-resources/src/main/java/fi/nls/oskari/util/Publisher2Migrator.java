package fi.nls.oskari.util;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by SMAKINEN on 12.10.2015.
 */
public class Publisher2Migrator {

    private static final Logger LOG = LogFactory.getLogger(Publisher2Migrator.class);
    private static final int BATCH_SIZE = 50;

    private int updatedViewCount = 0;
    private ViewService service = new ViewServiceIbatisImpl();

    public void generateViewMetadata(Connection conn) throws Exception {

        int page = 1;
        while(updateViews(page, conn)) {
            page++;
        }
        LOG.info("Updated views:", updatedViewCount);
    }

    private boolean updateViews(int page, Connection conn)
            throws Exception {
        List<View> list = service.getViews(page, BATCH_SIZE);
        LOG.info("Got", list.size(), "views on page", page);
        for(View view : list) {
            if(!ViewTypes.PUBLISHED.equals(view.getType())) {
                // only interested in published maps
                continue;
            }
            JSONObject metadata = createViewMetadata(view);
            updateMetadata(conn, view.getId(), metadata);
            updatedViewCount++;
        }
        return list.size() == BATCH_SIZE;
    }

    private void updateMetadata(Connection conn, long viewId, JSONObject metadata) throws SQLException {

        final String sql = "UPDATE portti_view SET metadata=? where id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, metadata.toString(2));
            statement.setLong(2, viewId);
            statement.execute();
        }
        catch (JSONException ignored) {}
    }

    /**
     * Generate the metadata block that publisher2 uses to return the editor state:
	"metadata": {
		"domain": "localhost",
		"name": "test",
		"language": "fi",
		"preview": "desktop",
		"toolLayout": "userlayout",
		"style": {
			"font": "georgia",
			"toolStyle": "3d-light"
		}
	}
     */
    public JSONObject createViewMetadata(View view) {
        JSONObject metadata = new JSONObject();
        JSONHelper.putValue(metadata, "domain", view.getPubDomain());
        JSONHelper.putValue(metadata, "name", view.getName());
        JSONHelper.putValue(metadata, "language", view.getLang());
        // default to desktop since we don't have this info
        JSONHelper.putValue(metadata, "preview", "desktop");

        // set all layouts to userlayout since we don't have that info directly and would need to check every
        // combination of plugin locations to be sure of anything else.
        // It's just the preselected option (lefthanded|righthanded|userlayout) and userlayout is always a safe bet.
        JSONHelper.putValue(metadata, "toolLayout", "userlayout");

        // setup "style": { "font": "georgia", "toolStyle": "3d-light" }
        // loop plugins in mapfull config, find any plugin with style config and use it as global style
        Bundle mapfull = view.getBundleByName("mapfull");
        JSONObject config = mapfull.getConfigJSON();
        JSONArray plugins = config.optJSONArray("plugins");
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.optJSONObject(i);
            JSONObject style = detectStyle(plugin.optJSONObject("config"));
            if(style != null) {
                // plugin has style -> use it in metadata (just use the first one we encounter)
                JSONHelper.putValue(metadata, "style", style);
                break;
            }
        }
        return metadata;
    }

    private JSONObject detectStyle(JSONObject pluginConfig) {
        if(pluginConfig == null) {
            return null;
        }
        if(!pluginConfig.has("font") && !pluginConfig.has("toolStyle")) {
            // this plugin has no style
            return null;
        }
        JSONObject style = new JSONObject();
        // "font": "georgia",
        JSONHelper.putValue(style, "font", pluginConfig.optString("font"));

        // try object first since optString converts objects to string
        JSONObject obj = pluginConfig.optJSONObject("toolStyle");
        if(obj != null) {
            // "toolStyle": { "val" : "sharp-dark", ... }
            JSONHelper.putValue(style, "toolStyle", obj.optString("val"));
            return style;
        }
        // "toolStyle": "sharp-dark"
        JSONHelper.putValue(style, "toolStyle", pluginConfig.optString("toolStyle"));
        return style;
    }
}
