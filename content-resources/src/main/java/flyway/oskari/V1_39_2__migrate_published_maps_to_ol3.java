package flyway.oskari;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.*;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.lang.StringUtils;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Migrates all views of type "PUBLISHED" and having a bundle with "openlayers" referenced in the startup JSON.
 * This targets all the Openlayers 2 based published maps. The views are then programmatically
 * "republished" with the current publish template which should be Openlayers 3 based, but the
 * doesn't really care about it. You could use it to migrate published maps to any new publish template with
 * some modification to the selection of views to migrate.
 */
public class V1_39_2__migrate_published_maps_to_ol3 implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_39_2__migrate_published_maps_to_ol3.class);
    private ViewService service = null;
    private BundleService bundleService = null;

    public void migrate(Connection conn)
            throws Exception {

        if (PropertyUtil.getOptional("flyway.1_39_2.skip", false)) {
            LOG.warn("You are skipping published maps migration.",
                    "All new development is happening for the Openlayers 3 based publish template",
                    "and having Openlayers 2 based published maps might not work properly anymore.",
                    "You will have to make an app specific migration since you skipped this one.");
            return;
        }
        service = new ViewServiceIbatisImpl();
        bundleService = new BundleServiceIbatisImpl();
        // 1. list view uuids that have openlayers2 and of type PUBLISHED
        List<String> uuidList = getOL2PublishedUUIDs(conn);
        for(String uuid : uuidList) {
            // load views like AppSetupHandler.get()
            final View oldView = service.getViewWithConfByUuId(uuid);
            final JSONObject json = getPayload(oldView);
            // republish -> like feed through AppSetupHandler.post() but with less user checks
            final View view = getBaseView(oldView);
            setupMapState(view, json.optJSONObject("mapfull"));

            merge(view, json);
        }

    }

    private List<String> getOL2PublishedUUIDs(Connection conn) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "select uuid from portti_view where type = 'PUBLISHED' and " +
                "id in (SELECT id FROM portti_view_bundle_seq where startup like '%openlayers%')";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    list.add(rs.getString("uuid"));
                }
            }
        }
        return list;
    }

    private JSONObject getPayload(View view) {
        try {
            return ViewHelper.getConfiguration(view);
        } catch (ViewException ex) {
            throw new RuntimeException("Couldn't restore view data for view id: " + view.getId(), ex);
        }
    }

    // ---------------------------------------------------------------
    // code copied from AppSetupHandler to do the republishing
    // ---------------------------------------------------------------
    public void merge(View view, JSONObject viewdata) {

        // check if we need to add divmanazer
        for(String bundleid : BUNDLE_REQUIRES_DIVMANAZER) {
            if(viewdata.has(bundleid)) {
                // Found bundles that require divmanazer
                // add it to the view before handling them
                LOG.info("Adding bundle", "divmanazer");
                addBundle(view, "divmanazer");
                // break so we don't add it more than once
                break;
            }
        }
        // setup all the bundles that don't need extra processing
        for(String bundleid : SIMPLE_BUNDLES) {
            if(viewdata.has(bundleid)) {
                setupBundle(view, viewdata, bundleid, ALWAYSON_BUNDLES.contains(bundleid));

                //toolbar -> add style info from metadata
                if (bundleid.equals("toolbar")) {
                    setupToolbarStyleInfo(view);
                }
            }
        }

        setupBundle(view, viewdata, "publishedmyplaces2", false);
        try {
            service.updatePublishedView(view);
        } catch (ViewException e) {
            throw new RuntimeException("Couldn't update view with id:" + view.getId());
        }
    }

    private void setupToolbarStyleInfo(final View view)  {
        final Bundle toolbarBundle = view.getBundleByName("toolbar");
        if (toolbarBundle == null) {
            return;
        }

        JSONObject toolbarConfig = toolbarBundle.getConfigJSON();
        JSONHelper.putValue(toolbarConfig, KEY_STYLE, view.getMetadata().optJSONObject(KEY_STYLE));
    }
    // Simple bundles don't require extra processing
    private static final Set<String> SIMPLE_BUNDLES = ConversionHelper.asSet(
            "infobox", "toolbar",
            "publishedgrid", "featuredata2", "coordinatetool", "statsgrid");

    // Bundles that we don't want to remove even if publisher doesn't provide config
    private static final Set<String> ALWAYSON_BUNDLES = ConversionHelper.asSet(
            "infobox", "toolbar");

    // Bundles that require divmanazer to be loaded for them to work
    private static final Set<String> BUNDLE_REQUIRES_DIVMANAZER =
            ConversionHelper.asSet("featuredata2", "coordinatetool", "statsgrid");

    // List of bundles that the user is able to publish
    // mapfull not included since it's assumed to be part of publisher template handled anyways
    private static final Set<String> BUNDLE_WHITELIST = ConversionHelper.asSet(
            "publishedmyplaces2", "divmanazer");

    static {
        // add all "simple" bundles to the whitelist
        BUNDLE_WHITELIST.addAll(SIMPLE_BUNDLES);
    }
    private Bundle setupBundle(final View view, final JSONObject inputViewData, final String bundleid, final boolean alwaysKeep) {

        // Note! Assumes value is a JSON object
        final JSONObject bundleData = inputViewData.optJSONObject(bundleid);
        if (bundleData != null) {
            LOG.info("Config found for", bundleid);
            final Bundle bundle = addBundle(view, bundleid);
            if(bundle != null) {
                mergeBundleConfiguration(bundle, bundleData.optJSONObject(KEY_CONF), bundleData.optJSONObject(KEY_STATE));
            }
            return bundle;
        } else if(!alwaysKeep) {
            // Remove bundle since it's not needed
            LOG.warn("Config not found for", bundleid, "- removing bundle.");
            view.removeBundle(bundleid);
        }
        return null;
    }

    private Bundle addBundle(final View view, final String bundleid) {
        if(!BUNDLE_WHITELIST.contains(bundleid)) {
            LOG.warn("Trying to add bundle that isn't recognized:", bundleid, "- Skipping it!");
            return null;
        }
        Bundle bundle = view.getBundleByName(bundleid);
        if (bundle == null) {
            LOG.info("Bundle with id:", bundleid, "not found in currentView - adding");
            bundle = bundleService.getBundleTemplateByName(bundleid);
            view.addBundle(bundle);
        }
        return bundle;
    }
    private static final String KEY_PLUGINS = "plugins";
    private static final String KEY_LAYERS = "layers";
    private static final String KEY_SELLAYERS = "selectedLayers";

    private static final String KEY_CROSSHAIR = "crosshair";
    private static final String KEY_STATE = "state";
    private static final String KEY_CONF = "conf";
    private static final String KEY_CONFIG = "config";
    private static final String KEY_MAPOPTIONS = "mapOptions";
    private static final String KEY_STYLE = "style";
    private static final String KEY_ID = "id";


    private void setupMapState(final View view, final JSONObject input) throws Exception {

        final Bundle mapfullBundle = view.getBundleByName("mapfull");
        if (mapfullBundle == null) {
            throw new Exception("Could not find mapfull bundle from template view: " + view.getId());
        }

        if(input == null) {
            throw new Exception("Could not get state for mapfull from publisher data");
        }
        // complete overrride of template mapfull state with the data coming from publisher!
        JSONObject mapfullState = input.optJSONObject(KEY_STATE);
        if(mapfullState == null) {
            throw new Exception("Could not get state for mapfull from publisher data");
        }
        mapfullBundle.setState(mapfullState.toString());

        // setup layers based on user rights (double check for user rights)
        final JSONArray selectedLayers = mapfullState.optJSONArray(KEY_SELLAYERS);

        // Override template layer selections
        final boolean layersUpdated = JSONHelper.putValue(mapfullBundle.getConfigJSON(), KEY_LAYERS, selectedLayers);
        final boolean selectedLayersUpdated = JSONHelper.putValue(mapfullBundle.getStateJSON(), KEY_SELLAYERS, selectedLayers);
        if (!(layersUpdated && selectedLayersUpdated)) {
            // failed to put layers correctly
            throw new Exception("Could not override layers selections");
        }

        final JSONArray plugins = mapfullBundle.getConfigJSON().optJSONArray(KEY_PLUGINS);
        if(plugins == null) {
            throw new Exception("Could not get default plugins");
        }
        final JSONObject mapfullConf = input.optJSONObject(KEY_CONF);
        if(mapfullConf == null) {
            throw new Exception("Could not get map configuration from input");
        }
        JSONObject finalConfig = mapfullBundle.getConfigJSON();

        final JSONArray userConfiguredPlugins = mapfullConf.optJSONArray(KEY_PLUGINS);
        if(userConfiguredPlugins == null) {
            throw new Exception("Could not get map plugins from input");
        }

        // merge user configs for template plugins
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.optJSONObject(i);
            //plugins
            JSONObject userPlugin = removePlugin(userConfiguredPlugins, plugin.optString(KEY_ID));
            if(userPlugin != null) {
                // same plugin from template AND user
                // merge config using users as base! and override it with template values
                // this way terms of use etc cannot be overridden by user
                JSONObject mergedConfig = JSONHelper.merge(userPlugin.optJSONObject(KEY_CONFIG), plugin.optJSONObject(KEY_CONFIG));
                JSONHelper.putValue(plugin, KEY_CONFIG, sanitizeConfigLocation(mergedConfig));
            }
        }
        // add remaining plugins user has selected on top of template plugins
        for (int i = userConfiguredPlugins.length(); --i >= 0; ) {
            JSONObject userPlugin = userConfiguredPlugins.optJSONObject(i);
            JSONHelper.putValue(userPlugin, KEY_CONFIG, sanitizeConfigLocation(userPlugin.optJSONObject(KEY_CONFIG)));
            plugins.put(userPlugin);
        }

        // replace current plugins
        JSONHelper.putValue(finalConfig, KEY_PLUGINS, plugins);

        // copy style definition from metadata to mapOptions
        JSONObject mapOptions = finalConfig.optJSONObject(KEY_MAPOPTIONS);
        if(mapOptions == null) {
            // create mapOptions if it doesn't exist
            mapOptions = new JSONObject();
            JSONHelper.putValue(finalConfig, KEY_MAPOPTIONS, mapOptions);
        }

        JSONHelper.putValue(mapOptions, KEY_CROSSHAIR, mapOptions.optBoolean(KEY_CROSSHAIR));

        // ensure consistency in mapOptions/metadata style block
        // NOTE! Need to use mapOptions from input conf to get the original one
        final JSONObject style = ensureStyleConsistency(
                view.getMetadata().optJSONObject(KEY_STYLE), mapfullConf.optJSONObject(KEY_MAPOPTIONS));
        JSONHelper.putValue(mapOptions, KEY_STYLE, style);
        view.getMetadata().put(KEY_STYLE, style);
    }

    private static final String KEY_FONT = "font";
    private static final String KEY_TOOLSTYLE = "toolStyle";
    /**
     * Some views seem to only have style saved to mapOptions and NOT metadata. Options is used for rendering, metadata
     * is used for publisher. Metadata should be consistent, but for reason or another isn't in the database. Merge
     * the values with metadata as master and mapOptions to fill in missing parts.
     * @param metadata
     * @param options
     * @return
     */
    private JSONObject ensureStyleConsistency(JSONObject metadata, JSONObject options) {
        if(options == null) {
            return metadata;
        }
        JSONObject style = options.optJSONObject(KEY_STYLE);
        if(style == null) {
            return metadata;
        }
        if(!metadata.has(KEY_FONT) && style.has(KEY_FONT)) {
            JSONHelper.putValue(metadata, KEY_FONT, style.optString(KEY_FONT));
        }
        if(!metadata.has(KEY_TOOLSTYLE) && style.has(KEY_TOOLSTYLE)) {
            JSONHelper.putValue(metadata, KEY_TOOLSTYLE, style.optString(KEY_TOOLSTYLE));
        }
        return metadata;
    }

    private static final Set<String> CLASS_WHITELIST =  ConversionHelper.asSet("center", "top", "right", "bottom", "left");
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CLASSES = "classes";
    /**
     * Removes the plugin and returns the removed value or null if not found.
     * NOTE! Modifies input list
     * @param plugins
     * @param pluginId
     * @return
     */
    public static JSONObject removePlugin(final JSONArray plugins, final String pluginId) {
        if(pluginId == null || plugins == null) {
            return null;
        }
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject pluginObj = plugins.optJSONObject(i);
            if(pluginObj != null && pluginId.equals(pluginObj.optString(KEY_ID))) {
                plugins.remove(i);
                return pluginObj;
            }
        }
        return null;
    }

    /**
     * Merges user selections to bundles default config/state.
     * @param bundle bundle to configure
     * @param userConfig overrides for default config
     * @param userState overrides for default state
     * @return root configuration object containing both config and state
     */
    public static void mergeBundleConfiguration(final Bundle bundle, final JSONObject userConfig, final JSONObject userState) {
        final JSONObject defaultConfig = bundle.getConfigJSON();
        final JSONObject defaultState = bundle.getStateJSON();
        final JSONObject mergedConfig = JSONHelper.merge(defaultConfig, userConfig);
        final JSONObject mergedState = JSONHelper.merge(defaultState, userState);
        bundle.setConfig(mergedConfig.toString());
        bundle.setState(mergedState.toString());
    }


    public static JSONObject sanitizeConfigLocation(final JSONObject config) {
        if(config == null) {
            return null;
        }

        // sanitize plugin.config.location.classes
        JSONObject location = config.optJSONObject(KEY_LOCATION);
        if (location != null) {
            String classes = location.optString(KEY_CLASSES);
            if (classes != null && classes.length() > 0) {
                String[] filteredClasses = filterClasses(classes.split(" "));
                JSONHelper.putValue(location, KEY_CLASSES, StringUtils.join(filteredClasses, " "));
            }
            // Make sure we don't have inline css set
            for(String str : CLASS_WHITELIST) {
                location.remove(str);
            }
        }

        return config;
    }

    private static String[] filterClasses(String[] classes) {
        Set<String> filteredClasses = new TreeSet<String>();
        for (int i = 0; i < classes.length; i++) {
            if (CLASS_WHITELIST.contains(classes[i])) {
                filteredClasses.add(classes[i]);
            }
        }
        return filteredClasses.toArray(new String[filteredClasses.size()]);
    }

    private View templateView;
    private View getBaseView(final View existingView) {
        if(templateView == null) {
            templateView = service.getViewWithConf(PropertyUtil.getOptional(ViewService.PROPERTY_PUBLISH_TEMPLATE, -1));
        }

        // clone a blank view based on template (so template doesn't get updated!!)
        final View view = templateView.cloneBasicInfo();

        // setup ids for updating a view
        view.setId(existingView.getId());
        view.setCreator(existingView.getCreator());
        view.setUuid(existingView.getUuid());
        view.setOldId(existingView.getOldId());
        view.setName(existingView.getName());
        view.setPubDomain(existingView.getPubDomain());
        JSONObject style = existingView.getMetadata().optJSONObject(KEY_STYLE);
        if(style != null) {
            String value = style.optString("toolStyle");
            if ("null".equalsIgnoreCase(value)) {
                // previous migration gone wrong -> replace "null" with no style selected
                style.remove("toolStyle");
            }
        }
        view.setMetadata(existingView.getMetadata());
        view.setDescription(existingView.getDescription());
        view.setIsPublic(existingView.isPublic());
        view.setOnlyForUuId(existingView.isOnlyForUuId());

        return view;
    }
}
