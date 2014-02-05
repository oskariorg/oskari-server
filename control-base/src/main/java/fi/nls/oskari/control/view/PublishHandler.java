package fi.nls.oskari.control.view;

import fi.mml.map.mapwindow.service.db.MyPlacesService;
import fi.mml.map.mapwindow.service.db.MyPlacesServiceIbatisImpl;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.*;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

@OskariActionRoute("Publish")
public class PublishHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(PublishHandler.class);
    
    public static final String KEY_PUBDATA = "pubdata";
    public static final String KEY_VIEW_DATA = "viewData";

    public static final String KEY_FIRSTNAME = "firstName";
    public static final String KEY_LASTNAME = "lastName";
    public static final String KEY_NICKNAME = "nickName";
    public static final String KEY_LOGINNAME = "loginName";
    public static final String KEY_USER = "user";
    public static final String KEY_ID = "id";
    public static final String KEY_DOMAIN = "domain";
    public static final String KEY_NAME = "name";
    public static final String KEY_LAYOUT = "layout";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_PLUGINS = "plugins";
    public static final String KEY_SIZE = "size";
    public static final String KEY_MAPSTATE = "mapstate";
    public static final String KEY_LAYERS = "layers";
    public static final String KEY_SELLAYERS = "selectedLayers";
    public static final String KEY_CONFIG = "config";
    public static final String KEY_STATE = "state";

    public static final String KEY_GRIDSTATE = "gridState";
    private static final String[] CACHED_BUNDLE_IDS = {
            ViewModifier.BUNDLE_PUBLISHEDGRID, ViewModifier.BUNDLE_TOOLBAR, ViewModifier.BUNDLE_PUBLISHEDMYPLACES2};
    private Map<String, Bundle> bundleCache = new HashMap<String, Bundle>(CACHED_BUNDLE_IDS.length);

    private static final String PREFIX_MYPLACES = "myplaces_";
    private static final String PREFIX_BASELAYER = "base_";
    private static final String LOGO_PLUGIN_ID = "Oskari.mapframework.bundle.mapmodule.plugin.LogoPlugin";
    private static final Set<String> CLASS_WHITELIST;
    static {
        CLASS_WHITELIST = new TreeSet<String>();
        CLASS_WHITELIST.add("center");
        CLASS_WHITELIST.add("top");
        CLASS_WHITELIST.add("right");
        CLASS_WHITELIST.add("bottom");
        CLASS_WHITELIST.add("left");
    }
    private static long PUBLISHED_VIEW_TEMPLATE_ID = -1;

    private ViewService viewService = null;
    private MyPlacesService myPlaceService = null;
    private PermissionsService permissionsService = null;
    private BundleService bundleService = null;
    

    public void setViewService(final ViewService service) {
        viewService = service;
    }

    public void setMyPlacesService(final MyPlacesService service) {
    	myPlaceService = service;
    }

    public void setPermissionsService(final PermissionsService service) {
    	permissionsService = service;
    }

    public void setBundleService(final BundleService service) {
    	bundleService = service;
    }

    public void init() {
        // setup service if it hasn't been initialized
        if (viewService == null) {
            setViewService(new ViewServiceIbatisImpl());
        }

        if (myPlaceService == null) {
        	setMyPlacesService(new MyPlacesServiceIbatisImpl());
        }

        if (permissionsService == null) {
        	setPermissionsService(new PermissionsServiceIbatisImpl());
        }

        if (bundleService == null) {
        	setBundleService(new BundleServiceIbatisImpl());
        }
        final String publishTemplateIdProperty = PropertyUtil.getOptional("view.template.publish");
        PUBLISHED_VIEW_TEMPLATE_ID = ConversionHelper.getLong(publishTemplateIdProperty, PUBLISHED_VIEW_TEMPLATE_ID);
        if(publishTemplateIdProperty == null) {
            log.warn("Publish template id not configured (property: view.template.publish)!");
        }
        else {
            log.info("Using publish template id: ", PUBLISHED_VIEW_TEMPLATE_ID);
        }


        for(String bundleid : CACHED_BUNDLE_IDS) {
            final Bundle bundle = bundleService.getBundleTemplateByName(bundleid);
            if(bundle == null) {
                log.warn("Couldn't get", bundleid, "bundle template from DB!");
                continue;
            }
            bundleCache.put(bundleid, bundle);
        }
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


    public void handleAction(ActionParameters params) throws ActionException {

        final User user = params.getUser();

        // Parse stuff sent by JS
        final JSONObject publishedData = getPublisherInput(params.getRequiredParam(KEY_PUBDATA));
        final View currentView = getBaseView(publishedData, user);

        Bundle mapFullTemplateBundle = currentView.getBundleByName(ViewModifier.BUNDLE_MAPFULL);
        if (mapFullTemplateBundle == null) {
            throw new ActionParamsException("Could not get current state for mapfull");
        }
        JSONObject mapfullTemplateConfig = null;
        try {
            mapfullTemplateConfig = new JSONObject(mapFullTemplateBundle.getConfig());
        } catch (JSONException e) {
            log.error("Could not create JSONs of defaults:", mapFullTemplateBundle);
            throw new ActionParamsException("Corrupted bundle data");
        }

        Bundle infoboxTemplateBundle = currentView.getBundleByName(ViewModifier.BUNDLE_INFOBOX);
        JSONObject infoboxTemplateState = null;
        JSONObject infoboxTemplateConfig = null;
        try {
            infoboxTemplateState = new JSONObject(infoboxTemplateBundle.getState());
            infoboxTemplateConfig = new JSONObject(infoboxTemplateBundle.getConfig());
        } catch (JSONException e) {
            log.error("Couldn't create JSONs of defaults:", infoboxTemplateBundle);
            throw new ActionParamsException("Corrupted bundle data");
        }

        // Set user
        try {
            JSONObject userJson = new JSONObject();
            userJson.put(KEY_FIRSTNAME, user.getFirstname());
            userJson.put(KEY_LASTNAME, user.getLastname());
            userJson.put(KEY_NICKNAME, user.getScreenname());
            userJson.put(KEY_LOGINNAME, user.getEmail());
            mapfullTemplateConfig.put(KEY_USER, userJson);
        } catch (JSONException jsonex) {
            log.error(jsonex, "Could not create user object:", user);
            throw new ActionParamsException("User data problem");
        }


        final String domain = JSONHelper.getStringFromJSON(publishedData, KEY_DOMAIN, null);
        if(domain == null) {
            throw new ActionParamsException("Domain missing");
        }
        final String name = JSONHelper.getStringFromJSON(publishedData, KEY_NAME, "Published map " + System.currentTimeMillis());
        final String language = JSONHelper.getStringFromJSON(publishedData, KEY_LANGUAGE, PropertyUtil.getDefaultLanguage());
        final String layout = JSONHelper.getStringFromJSON(publishedData, KEY_LAYOUT, "lefthanded");

        JSONHelper.putValue(mapfullTemplateConfig, KEY_LAYOUT, layout);

        JSONArray newPlugins;
        JSONObject size;
        JSONObject mapfullState = null;
        try {
            newPlugins = publishedData.getJSONArray(KEY_PLUGINS);
            size = publishedData.getJSONObject(KEY_SIZE);

            mapfullState = publishedData.getJSONObject(KEY_MAPSTATE);
            final JSONObject tmpInfoboxState = publishedData.optJSONObject(ViewModifier.BUNDLE_INFOBOX);
            if (tmpInfoboxState != null) {
                infoboxTemplateState = tmpInfoboxState;
            }

        } catch (JSONException jsonex) {
            throw new RuntimeException("[PublishHandler] Unable to parse "
                    + "params for new published map!\n" + "Param string is:\n"
                    + params.getRequest().getParameter(KEY_PUBDATA) + "\n" + "Error was "
                    + jsonex.getMessage());
        }

        currentView.setPubDomain(domain);
        currentView.setName(name);
        currentView.setType(params.getHttpParam(ViewTypes.VIEW_TYPE, ViewTypes.PUBLISHED));
        currentView.setCreator(user.getId());
        currentView.setIsPublic(true);
        // application/page/developmentPath should be configured to publish template view
        currentView.setLang(language);

        JSONArray selectedLayers = null;
        try {
            selectedLayers = getPublishableLayers(mapfullState.getJSONArray(KEY_SELLAYERS), user);
        } catch (JSONException e) {
            throw new RuntimeException("Could not get selected layers");
        }

        // Override layer selections
        final boolean layersUpdated = JSONHelper.putValue(mapfullTemplateConfig, KEY_LAYERS, selectedLayers);
        final boolean selectedLayersUpdated = JSONHelper.putValue(mapfullState, KEY_SELLAYERS, selectedLayers);
        if (!(layersUpdated && selectedLayersUpdated)) {
            // failed to put layers correctly
            throw new RuntimeException("Could not override layers selections");
        }

        // Set size
        try {
            mapfullTemplateConfig.put(KEY_SIZE, size);
        } catch (JSONException e) {
            throw new RuntimeException("Could not set size");
        }

        // Append plugins
        JSONArray plugins = null;
        try {
            plugins = mapfullTemplateConfig.getJSONArray(KEY_PLUGINS);
        } catch (JSONException e) {
            throw new RuntimeException("Could not get default plugins");
        }

        // see if the plugin is already in the template
        for (int i = newPlugins.length(); --i >= 0; ) {
            boolean alreadyAdded = false;
            JSONObject newPlugin = null;
            JSONObject location = null;
            try {
                newPlugin = newPlugins.getJSONObject(i);
                // sanitize plugin.config.location.classes
                location = null;
                if (newPlugin.has("config") && newPlugin.getJSONObject("config").has("location")) {
                    location = newPlugin.getJSONObject("config").getJSONObject("location");
                    if (location.has("classes")) {
                        String classes = location.getString("classes");
                        if (classes != null && classes.length() > 0) {
                            String[] filteredClasses = filterClasses(classes.split(" "));
                            location.put("classes", StringUtils.join(filteredClasses, " "));
                        }
                    }
                    // Make sure we don't have inline css set
                    if (location.has("top")) {
                        location.remove("top");
                    }
                    if (location.has("right")) {
                        location.remove("right");
                    }
                    if (location.has("bottom")) {
                        location.remove("bottom");
                    }
                    if (location.has("left")) {
                        location.remove("left");
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException("Could not loop new plugins", e);
            }
            for (int j = plugins.length(); --j >= 0; ) {
                JSONObject plugin = null;
                try {
                    plugin = plugins.getJSONObject(j);
                } catch (JSONException e) {
                    throw new RuntimeException("Could not loop"
                            + " default plugins", e);
                }
                try {
                    String newPluginId = newPlugin.getString(KEY_ID);
                    String pluginId = plugin.getString(KEY_ID);
                    if (newPluginId.equals(pluginId)) {
                        alreadyAdded = true;
                        // copy plugin classes
                        if (location != null) {
                            if (!plugin.has("config")) {
                                plugin.put("config", new JSONObject());
                            }
                            plugin.getJSONObject("config").put("location", location);
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException("Could not compare"
                            + " plugin IDs", e);
                }
            }
            if (!alreadyAdded) {
                plugins.put(newPlugin);
            }
        }

        try {
            mapfullTemplateConfig.put(KEY_PLUGINS, plugins);
        } catch (JSONException e) {
            throw new RuntimeException("Could not append plugin array");
        }

        // Build viewdata
        JSONObject viewData = new JSONObject();
        try {
            final JSONObject mapfull = new JSONObject();
            mapfull.put(KEY_CONFIG, mapfullTemplateConfig);
            mapfull.put(KEY_STATE, mapfullState);

            final JSONObject infobox = new JSONObject();
            infobox.put(KEY_CONFIG, infoboxTemplateConfig);
            infobox.put(KEY_STATE, infoboxTemplateState);

            viewData.put(ViewModifier.BUNDLE_MAPFULL, mapfull);
            viewData.put(ViewModifier.BUNDLE_INFOBOX, infobox);

            // Setup publishedmyplaces2 bundle if user has configured it
            setupBundle(currentView, publishedData, ViewModifier.BUNDLE_PUBLISHEDMYPLACES2, viewData);

            // Setup toolbar bundle if user has configured it
            setupBundle(currentView, publishedData, ViewModifier.BUNDLE_TOOLBAR, viewData);

            final JSONObject gridState = publishedData.optJSONObject(KEY_GRIDSTATE);
            log.debug("Grid state:", gridState);
            if(gridState != null) {
                final Bundle gridBundle = addBundle(currentView, ViewModifier.BUNDLE_PUBLISHEDGRID);
                log.debug("Grid bundle added:", gridBundle);
                final JSONObject conf = getBundleConfiguration(gridBundle, null, gridState);
                log.debug("Grid bundle conf:", conf);
                JSONHelper.putValue(viewData, ViewModifier.BUNDLE_PUBLISHEDGRID, conf);
            }
        } catch (JSONException e) {
            throw new RuntimeException("Could not store bundle JSONs", e);
        }

        log.debug("View loop");
        // Pass through the template stuff do not going to modify
        // TODO: why do we construct viewData when this overrides it for other than mapfull/infobox?
        for (Bundle s : currentView.getBundles()) {
            String bundleName = s.getBundleinstance();
            if (bundleName.equals(ViewModifier.BUNDLE_INFOBOX) || bundleName.equals(ViewModifier.BUNDLE_MAPFULL) || bundleName.equals(ViewModifier.BUNDLE_TOOLBAR) || bundleName.equals(ViewModifier.BUNDLE_PUBLISHEDMYPLACES2)) {
                continue;
            }
            try {
                JSONObject stateJson = new JSONObject(s.getState());
                JSONObject configJson = new JSONObject(s.getConfig());
                JSONObject bJson = new JSONObject();
                bJson.put(KEY_CONFIG, configJson);
                bJson.put(KEY_STATE, stateJson);
                viewData.put(bundleName, bJson);
            } catch (JSONException e) {
                throw new RuntimeException("Could pass through"
                        + " template bundles as-is");
            }
        }
        log.debug("View loop finished");
        // TODO: why is this added to additional params - logging purposes on exception?
        params.putAdditionalParam(KEY_VIEW_DATA, viewData.toString());

        log.debug("Save view:", viewData);
        View newView = saveView(currentView, viewData);
        log.debug("Published a map:", newView);

        try {
            JSONObject newViewJson = new JSONObject(newView.toString());
            ResponseHelper.writeResponse(params, newViewJson);
        } catch (JSONException je) {
            log.error(je, "Could not create JSON response.");
            ResponseHelper.writeResponse(params, false);
        }
    }


    private JSONObject getPublisherInput(final String input) throws ActionException {
        try {
            return new JSONObject(input);
        } catch (JSONException e) {
            log.error(e, "Unable to parse publisher data:", input );
            throw new ActionParamsException("Unable to parse publisher data.");
        }
    }

    private View getBaseView(final JSONObject publisherInput, final User user) throws ActionException {

        if (user.isGuest()) {
            throw new ActionDeniedException("Trying to publish map, but couldn't determine user");
        }

        // check if we are updating a view
        long viewId = publisherInput.optLong("id", -1);
        if(viewId != -1) {
            log.debug("Loading view for editing:", viewId);
            final View view = viewService.getViewWithConf(viewId);
            if (user.getId() != view.getCreator()) {
                throw new ActionDeniedException("No permissions to update view with id:" + viewId);
            }
            return view;
        }

        // not editing, use template view
        if(PUBLISHED_VIEW_TEMPLATE_ID == -1) {
            log.error("Publish template id not configured (property: view.template.publish)!");
            throw new ActionParamsException("Trying to publish map, but template isn't configured");
        }
        log.debug("Using template to create a new view");
        // Get publisher defaults
        View view = viewService.getViewWithConf(PUBLISHED_VIEW_TEMPLATE_ID);
        if (view == null) {
            log.error("Could not get template View with id:", PUBLISHED_VIEW_TEMPLATE_ID);
            throw new ActionParamsException("Could not get template View");
        }
        // reset id so template doesn't get updated!!
        // NOT NEEDED WITH CLONÌNG view.setId(-1);

        // clone a blank view based on template
        return view.cloneBasicInfo();
    }

    private void setupBundle(final View view, final JSONObject publisherData, final String bundleid, final JSONObject viewData) {

        final JSONObject bundleData = publisherData.optJSONObject(bundleid);
        if (bundleData != null && bundleData.names().length() > 0) {
            log.info("config found for", bundleid);
            final Bundle bundle = addBundle(view, bundleid);
            final JSONObject conf = getBundleConfiguration(bundle, bundleData, null);
            JSONHelper.putValue(viewData, bundleid, conf);
        } else {
            log.warn("config not found for", bundleid, "- removing bundle.");
            // We have to remove the bundle...
            // TODO: check if we really want to remove the bundle from view since it could be template view???
            view.removeBundle(bundleid);
        }
    }
    private Bundle addBundle(final View view, final String bundleid) {
        Bundle bundle = view.getBundleByName(bundleid);
        if (bundle == null) {
            log.info("Bundle with id:", bundleid, "not found in currentView - adding");
            if(!bundleCache.containsKey(bundleid)) {
                log.warn("Trying to add bundle that isn't loaded:", bundleid, "- Skipping it!");
                return null;
            }
            bundle = bundleCache.get(bundleid);
            view.addBundle(bundle);
        }
        return bundle;
    }

    /**
     * Merges user selections to bundles default config/state.
     * @param bundle bundle to configure
     * @param userConfig overrides for default config
     * @param userState overrides for default state
     * @return root configuration object containing both config and state
     */
    private JSONObject getBundleConfiguration(final Bundle bundle, final JSONObject userConfig, final JSONObject userState) {
        final JSONObject rootConf = new JSONObject();
        final JSONObject defaultConfig = JSONHelper.createJSONObject(bundle.getConfig());
        final JSONObject defaultState = JSONHelper.createJSONObject(bundle.getState());

        JSONHelper.putValue(rootConf, KEY_CONFIG, JSONHelper.merge(defaultConfig, userConfig));
        JSONHelper.putValue(rootConf, KEY_STATE, JSONHelper.merge(defaultState, userState));
        return rootConf;
    }

    private View saveView(final View view, final JSONObject viewJson) {
        try {
            if (view.getId() != -1) {
                viewService.updatePublishedView(view, viewJson);
            } else {
                long viewId = viewService.addView(view, viewJson);
                view.setId(viewId);
            }
        } catch (ViewException e) {
            log.error("Error when trying add/update published view", e);
        }
        return view;
    }

    private JSONArray getPublishableLayers(final JSONArray selectedLayers, final User user) {
        final JSONArray filteredList = new JSONArray();
        log.debug("Selected layers:", selectedLayers);

        String userUuid = user.getUuid();
        try {
            for (int i = 0; i < selectedLayers.length(); ++i) {
                JSONObject layer = selectedLayers.getJSONObject(i);
                String layerId = layer.getString("id");
                if (layerId.startsWith(PREFIX_MYPLACES)) {
                    // check publish right for published myplaces layer
                    if (hasRightToPublishMyPlaceLayer(layerId, userUuid, user.getScreenname())) {
                        filteredList.put(layer);
                    }
                } else if (layerId.startsWith(PREFIX_BASELAYER)) {
                    // check publish right for base layer
                    if (hasRightToPublishBaseLayer(layerId, user)) {
                        filteredList.put(layer);
                    }
                }
                // check publish right for normal layer
                else if (hasRightToPublishLayer(layerId, user)) {
                    filteredList.put(layer);
                }
            }
        } catch (Exception e) {
            log.error(e, "Error parsing myplaces layers from published layers", selectedLayers);
        }
        log.debug("Filtered layers:", filteredList);
        return filteredList;
    }

    private boolean hasRightToPublishMyPlaceLayer(final String layerId, final String userUuid, final String publisherName) {
        final long categoryId = ConversionHelper.getLong(layerId.substring(PREFIX_MYPLACES.length()), -1);
        if (categoryId == -1) {
            log.warn("Error parsing layerId:", layerId);
            return false;
        }
        final List<Long> publishedMyPlaces = new ArrayList<Long>();
        publishedMyPlaces.add(categoryId);
        final List<MyPlaceCategory> myPlacesLayers = myPlaceService.getMyPlaceLayersById(publishedMyPlaces);
        for (MyPlaceCategory place : myPlacesLayers) {
            if (place.getUuid().equals(userUuid)) {
                myPlaceService.updatePublisherName(categoryId, userUuid, publisherName); // make it public
                return true;
            }
        }
        log.warn("Found my places layer in selected that isn't users own or isnt published any more! LayerId:", layerId, "User UUID:", userUuid);
        return false;
    }


    private boolean hasRightToPublishBaseLayer(final String layerId, final User user) {

        final long id = ConversionHelper.getLong(layerId.substring(PREFIX_BASELAYER.length()), -1);
        if (id == -1) {
            log.warn("Error parsing layerId:", layerId);
            return false;
        }
        List<Long> list = new ArrayList<Long>();
        list.add(id);
        Map<Long, List<Permissions>> map =
                permissionsService.getPermissionsForBaseLayers(list, Permissions.PERMISSION_TYPE_PUBLISH);
        List<Permissions> permissions = map.get(id);

        boolean hasPermission = false;
        hasPermission = permissionsService.permissionGrantedForRolesOrUser(
                user, permissions, Permissions.PERMISSION_TYPE_PUBLISH);
        if (!hasPermission) {
            log.warn("User tried to publish layer with no publish permission. LayerID:", layerId, "- User:", user);
        }
        return hasPermission;
    }


    private boolean hasRightToPublishLayer(final String layerId, final User user) {
        final long id = ConversionHelper.getLong(layerId, -1);
        if (id == -1) {
            log.warn("Error parsing layerId:", layerId);
            return false;
        }
        List<Long> list = new ArrayList<Long>();
        list.add(id);
        Map<Long, List<Permissions>> map = permissionsService.getPermissionsForLayers(list, Permissions.PERMISSION_TYPE_PUBLISH);
        List<Permissions> permissions = map.get(id);
        boolean hasPermission = false;
        hasPermission = permissionsService.permissionGrantedForRolesOrUser(
                user, permissions, Permissions.PERMISSION_TYPE_PUBLISH);
        if (!hasPermission) {
            log.warn("User tried to publish layer with no publish permission. LayerID:", layerId, "- User:", user);
        }
        return hasPermission;
    }


}
