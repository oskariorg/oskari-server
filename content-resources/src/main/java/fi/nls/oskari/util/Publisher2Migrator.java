package fi.nls.oskari.util;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMAKINEN on 12.10.2015.
 */
public class Publisher2Migrator {

    private static final Logger LOG = LogFactory.getLogger(Publisher2Migrator.class);
    private static final int BATCH_SIZE = 50;
    private static final String BUNDLE_PUBLISHER_OLD = "publisher";
    private static final String BUNDLE_PUBLISHER_NEW = "publisher2";


    private int updatedViewCount = 0;
    private ViewService service = null;
    public Publisher2Migrator(ViewService service) {
        this.service = service;
    }

    /**
     * The main method to call for generating metadata for all published views/appsetups
     * @param conn
     * @throws Exception
     */
    public void migratePublishedAppsetups(Connection conn) throws Exception {

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

        // some plugins might have font, some might have style. Need to loop all of them to get the whole style...
        String toolStyle = null;
        String font = null;
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.optJSONObject(i);
            JSONObject pluginConfig = plugin.optJSONObject("config");
            if(toolStyle == null) {
                toolStyle = getToolStyle(pluginConfig);
            }
            if(font == null) {
                font = getFont(pluginConfig);
            }
        }
        JSONObject style = new JSONObject();
        if(font != null) {
            JSONHelper.putValue(style, "font", font);
        }
        if(toolStyle != null) {
            JSONHelper.putValue(style, "toolStyle", toolStyle);
        }
        JSONHelper.putValue(metadata, "style", style);
        return metadata;
    }

    private String getToolStyle(JSONObject pluginConfig) {
        if(pluginConfig == null) {
            return null;
        }

        if(!pluginConfig.has("toolStyle")) {
            // this plugin has no style
            return null;
        }
        // try object first since optString converts objects to string
        JSONObject obj = pluginConfig.optJSONObject("toolStyle");
        if(obj != null) {
            // "toolStyle": { "val" : "sharp-dark", ... }
            return obj.optString("val", null);
        }
        // "toolStyle": "sharp-dark"
        return pluginConfig.optString("toolStyle", null);
    }

    private String getFont(JSONObject pluginConfig) {
        if(pluginConfig == null) {
            return null;
        }
        if(!pluginConfig.has("font")) {
            // this plugin has no font def
            return null;
        }
        // "font": "georgia",
        return pluginConfig.optString("font", null);
    }

    /**
     * Returns view id listing for views having the old publisher.
     * @param conn
     * @return
     * @throws SQLException
     */
    public List<Long> getViewsWithOldPublisher(Connection conn) throws SQLException {
        List<Long> idList = new ArrayList<>();

        final PreparedStatement statement =
                conn.prepareStatement("SELECT view_id FROM portti_view_bundle_seq " +
                        "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name=?)");
        statement.setString(1, BUNDLE_PUBLISHER_OLD);
        try (ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                idList.add(rs.getLong("view_id"));
            }
        } finally {
            statement.close();
        }
        return idList;
    }

    public void switchPublisherBundles(final long viewId, Connection conn) throws SQLException {
        Bundle oldBundle = BundleHelper.getRegisteredBundle(BUNDLE_PUBLISHER_OLD, conn);
        Bundle newBundle = BundleHelper.getRegisteredBundle(BUNDLE_PUBLISHER_NEW, conn);
        final String sql = "UPDATE portti_view_bundle_seq " +
                "SET " +
                "    bundle_id=?, " +
                "    startup=?, " +
                "    config=?, " +
                "    state=?, " +
                "    bundleinstance=?" +
                "WHERE bundle_id = ? and view_id=?";

        try (PreparedStatement statement =
                     conn.prepareStatement(sql)){
            statement.setLong(1, newBundle.getBundleId());
            statement.setString(2, newBundle.getStartup());
            statement.setString(3, newBundle.getConfig());
            statement.setString(4, newBundle.getState());
            statement.setString(5, newBundle.getName());
            statement.setLong(6, oldBundle.getBundleId());
            statement.setLong(7, viewId);
            statement.execute();
        }
    }
}
