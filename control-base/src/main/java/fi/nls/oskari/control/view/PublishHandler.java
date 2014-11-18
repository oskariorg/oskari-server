package fi.nls.oskari.control.view;

import fi.mml.map.mapwindow.service.db.MyPlacesService;
import fi.mml.map.mapwindow.service.db.MyPlacesServiceIbatisImpl;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.analysis.service.AnalysisDbServiceIbatisImpl;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.userlayer.service.UserLayerDbService;
import fi.nls.oskari.map.userlayer.service.UserLayerDbServiceIbatisImpl;
import fi.nls.oskari.map.view.*;
import fi.nls.oskari.permission.domain.Permission;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.*;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.apache.commons.lang.StringUtils;
import static fi.nls.oskari.control.ActionConstants.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

@OskariActionRoute("Publish")
public class PublishHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(PublishHandler.class);

    public static final String PROPERTY_DRAW_TOOLS_ENABLED = "actionhandler.Publish.drawToolsRoles";

    public static final String KEY_PUBDATA = "pubdata";
    public static final String KEY_VIEW_DATA = "viewData";

    public static final String KEY_FIRSTNAME = "firstName";
    public static final String KEY_LASTNAME = "lastName";
    public static final String KEY_NICKNAME = "nickName";
    public static final String KEY_LOGINNAME = "loginName";
    public static final String KEY_DOMAIN = "domain";
    public static final String KEY_LAYOUT = "layout";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_PLUGINS = "plugins";
    public static final String KEY_SIZE = "size";
    public static final String KEY_MAPSTATE = "mapstate";
    public static final String KEY_LAYERS = "layers";
    public static final String KEY_SELLAYERS = "selectedLayers";
    public static final String KEY_RESPONSIVE = "responsive";
    public static final String VIEW_RESPONSIVE = "responsive";
    public static final String APP_RESPONSIVE = "responsive-published-map";

    public static final String KEY_GRIDSTATE = "gridState";
    private static final String[] CACHED_BUNDLE_IDS = {
            ViewModifier.BUNDLE_PUBLISHEDGRID, ViewModifier.BUNDLE_TOOLBAR,
            ViewModifier.BUNDLE_PUBLISHEDMYPLACES2, ViewModifier.BUNDLE_FEATUREDATA2,
            ViewModifier.BUNDLE_DIVMANAZER};
    private Map<String, Bundle> bundleCache = new HashMap<String, Bundle>(CACHED_BUNDLE_IDS.length);

    private static final String PREFIX_MYPLACES = "myplaces_";
    private static final String PREFIX_ANALYSIS = "analysis_";
    private static final String PREFIX_USERLAYER = "userlayer_";
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
    private AnalysisDbService analysisService = null;
    private UserLayerDbService userLayerService = null;
    private PermissionsService permissionsService = null;
    private BundleService bundleService = null;
    private OskariLayerService layerService = null;

    private String[] drawToolsEnabledRoles = new String[0];
    

    public void setViewService(final ViewService service) {
        viewService = service;
    }

    public void setMyPlacesService(final MyPlacesService service) {
    	myPlaceService = service;
    }

    public void setAnalysisService(final AnalysisDbService service) {
    	analysisService = service;
    }

    public void setUserLayerService(final UserLayerDbService service) { userLayerService = service; }

    public void setPermissionsService(final PermissionsService service) {
    	permissionsService = service;
    }

    public void setBundleService(final BundleService service) {
    	bundleService = service;
    }

    public void setOskariLayerService(final OskariLayerService service) {
        layerService = service;
    }

    public void init() {
        // setup service if it hasn't been initialized
        if (myPlaceService == null) {
        	setMyPlacesService(new MyPlacesServiceIbatisImpl());
        }

        if (analysisService == null) {
            setAnalysisService(new AnalysisDbServiceIbatisImpl());
        }

        if (userLayerService == null) {
            setUserLayerService(new UserLayerDbServiceIbatisImpl());
        }

        if (permissionsService == null) {
        	setPermissionsService(ServiceFactory.getPermissionsService());
        }

        if (viewService == null) {
            setViewService(new ViewServiceIbatisImpl());
        }

        if (bundleService == null) {
        	setBundleService(new BundleServiceIbatisImpl());
        }

        if (layerService == null) {
            setOskariLayerService(ServiceFactory.getMapLayerService());
        }
        final String publishTemplateIdProperty = PropertyUtil.getOptional("view.template.publish");
        PUBLISHED_VIEW_TEMPLATE_ID = ConversionHelper.getLong(publishTemplateIdProperty, PUBLISHED_VIEW_TEMPLATE_ID);
        if(publishTemplateIdProperty == null) {
            log.warn("Publish template id not configured (property: view.template.publish)!");
        }
        else {
            log.info("Using publish template id: ", PUBLISHED_VIEW_TEMPLATE_ID);
        }

        // setup roles authorized to enable drawing tools on published map
        drawToolsEnabledRoles = PropertyUtil.getCommaSeparatedList(PROPERTY_DRAW_TOOLS_ENABLED);

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


    	final String useUuid = PropertyUtil.get("oskari.publish.only.with.uuid");    	
    	final User user = params.getUser();

        
        // Parse stuff sent by JS
        final JSONObject publisherData = getPublisherInput(params.getRequiredParam(KEY_PUBDATA));
        final View currentView = getBaseView(publisherData, user);

        final Bundle mapFullBundle = currentView.getBundleByName(ViewModifier.BUNDLE_MAPFULL);
        if (mapFullBundle == null) {
            throw new ActionParamsException("Could not find mapfull bundle from view: " + currentView.getId());
        }

        // Add responsive boolean to mapfull if it's true
        // this _could_ be done by checking view.getPage() in a viewmodifier,
        // but this messes up the code less IMHO
        final Boolean responsive =  JSONHelper.getBooleanFromJSON(
                publisherData,
                KEY_RESPONSIVE,
                false
        );

        if (responsive) {
            JSONHelper.putValue(
                    mapFullBundle.getConfigJSON(),
                    KEY_RESPONSIVE,
                    responsive
            );
            currentView.setPage(VIEW_RESPONSIVE);
            currentView.setApplication(APP_RESPONSIVE);
        }

        // Setup user
        try {
            JSONObject userJson = new JSONObject();
            userJson.put(KEY_FIRSTNAME, user.getFirstname());
            userJson.put(KEY_LASTNAME, user.getLastname());
            userJson.put(KEY_NICKNAME, user.getScreenname());
            userJson.put(KEY_LOGINNAME, user.getEmail());
            JSONHelper.putValue(mapFullBundle.getConfigJSON(), KEY_USER, userJson);
            //mapfullTemplateConfig.put(KEY_USER, userJson);
        } catch (JSONException jsonex) {
            log.error("Could not create user object:", user, "- Error:", jsonex.getMessage());
            throw new ActionParamsException("User data problem");
        }

        // setup basic info about view
        final String domain = JSONHelper.getStringFromJSON(publisherData, KEY_DOMAIN, null);
        if(domain == null || domain.trim().isEmpty()) {
            throw new ActionParamsException("Domain missing");
        }
        final String name = JSONHelper.getStringFromJSON(publisherData, KEY_NAME, "Published map " + System.currentTimeMillis());
        final String language = JSONHelper.getStringFromJSON(publisherData, KEY_LANGUAGE, PropertyUtil.getDefaultLanguage());

        currentView.setPubDomain(domain);
        currentView.setName(name);
        currentView.setType(params.getHttpParam(ViewTypes.VIEW_TYPE, ViewTypes.PUBLISHED));
        currentView.setCreator(user.getId());
        currentView.setIsPublic(true);
        // application/page/developmentPath should be configured to publish template view
        currentView.setLang(language);
        
        currentView.setUuid(UUID.randomUUID().toString());
        
        if(useUuid != null && useUuid.equalsIgnoreCase("true")){
        	currentView.setOnlyForUuId(true);
        }else{
        	currentView.setOnlyForUuId(false);
        }
        log.debug("UUID: " + currentView.getUuid());

        // setup map state
        setupMapState(mapFullBundle, publisherData, user);

        // setup infobox
        final JSONObject tmpInfoboxState = publisherData.optJSONObject(ViewModifier.BUNDLE_INFOBOX);
        if (tmpInfoboxState != null) {
            final Bundle infoboxTemplateBundle = currentView.getBundleByName(ViewModifier.BUNDLE_INFOBOX);
            if(infoboxTemplateBundle != null) {
                infoboxTemplateBundle.setState(tmpInfoboxState.toString());
            }
            else {
                log.warn("Publisher sent state for infobox, but infobox isn't available in template view! State:", tmpInfoboxState);
            }
        }

        // Setup publishedmyplaces2 bundle if user has configured it/has permission to do so
        if(user.hasAnyRoleIn(drawToolsEnabledRoles)) {
            final Bundle myplaces = setupBundle(currentView, publisherData, ViewModifier.BUNDLE_PUBLISHEDMYPLACES2);
            handleMyplacesDrawLayer(myplaces, user);
        } else {
            // check that template doesn't have it either, remove if found
            if(log.isDebugEnabled()) {
                Bundle drawTools = currentView.getBundleByName(ViewModifier.BUNDLE_PUBLISHEDMYPLACES2);
                if(drawTools != null) {
                    log.debug("Found",ViewModifier.BUNDLE_PUBLISHEDMYPLACES2, "in view, removing!");
                }
            }
            currentView.removeBundle(ViewModifier.BUNDLE_PUBLISHEDMYPLACES2);
            if(log.isDebugEnabled()) {
                Bundle drawTools = currentView.getBundleByName(ViewModifier.BUNDLE_PUBLISHEDMYPLACES2);
                log.debug(ViewModifier.BUNDLE_PUBLISHEDMYPLACES2, "should have been removed:", drawTools);
            }
        }

        // Setup toolbar bundle if user has configured it
        setupBundle(currentView, publisherData, ViewModifier.BUNDLE_TOOLBAR);

        // Setup feature data bundle if user has configured it
        final JSONObject featureData = publisherData.optJSONObject(ViewModifier.BUNDLE_FEATUREDATA2);
        if (featureData != null && featureData.names().length() > 0) {
            // Add divmanazer first since feature data uses flyout
            log.info("Adding bundle", ViewModifier.BUNDLE_DIVMANAZER);
            addBundle(currentView, ViewModifier.BUNDLE_DIVMANAZER);
            // then setup feature data
            final Bundle bundle = addBundle(currentView, ViewModifier.BUNDLE_FEATUREDATA2);
            mergeBundleConfiguration(bundle, featureData, null);
        }

        // Setup thematic map/published grid bundle
        final JSONObject gridState = publisherData.optJSONObject(KEY_GRIDSTATE);
        log.debug("Grid state:", gridState);
        if(gridState != null) {
            final Bundle gridBundle = addBundle(currentView, ViewModifier.BUNDLE_PUBLISHEDGRID);
            log.debug("Grid bundle added:", gridBundle);
            mergeBundleConfiguration(gridBundle, null, gridState);
        }

        final View newView = saveView(currentView);

        try {
            JSONObject newViewJson = new JSONObject(newView.toString());
            ResponseHelper.writeResponse(params, newViewJson);
        } catch (JSONException je) {
            log.error(je, "Could not create JSON response.");
            ResponseHelper.writeResponse(params, false);
        }
    }

    private void handleMyplacesDrawLayer(final Bundle myplaces, final User user) throws ActionException {

        if(myplaces == null) {
            // nothing to handle, bundle not added
            return;
        }
        final JSONObject config = myplaces.getConfigJSON();
        final String drawLayerId = config.optString("layer");
        if(!myPlaceService.canModifyCategory(user, drawLayerId)) {
            throw new ActionDeniedException("Trying to publish another users layer as drawlayer!");
        }
        Resource resource = myPlaceService.getResource(drawLayerId);
        if(resource.hasPermission(user, myPlaceService.PERMISSION_TYPE_DRAW)) {
            // clear up any previous DRAW permissions
            resource.removePermissionsOfType(myPlaceService.PERMISSION_TYPE_DRAW);
        }
        try {
            // add DRAW permission for all roles currently in the system
            for(Role role: UserService.getInstance().getRoles()) {
                final Permission perm = new Permission();
                perm.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
                perm.setExternalId("" + role.getId());
                perm.setType(myPlaceService.PERMISSION_TYPE_DRAW);
                resource.addPermission(perm);
            }
        } catch (Exception e) {
            log.error("Something went wrong when generating DRAW permissions for myplaces layer");
        }

        permissionsService.saveResourcePermissions(resource);
        // NOTE! allowing guests to draw features on the layer
        JSONHelper.putValue(config, "allowGuest", true);
    }

    private void setupMapState(final Bundle mapFullBundle, final JSONObject publisherData, final User user) throws ActionException {

        // setup map state
        final JSONObject publisherMapState = publisherData.optJSONObject(KEY_MAPSTATE);
        if(publisherMapState == null) {
            throw new ActionParamsException("Could not get state for mapfull from publisher data");
        }
        // complete overrride of template mapfull state with the data coming from publisher!
        mapFullBundle.setState(publisherMapState.toString());

        // setup layers based on user rights (double check for user rights)
        final JSONArray selectedLayers = getPublishableLayers(publisherMapState.optJSONArray(KEY_SELLAYERS), user);

        // Override template layer selections
        final boolean layersUpdated = JSONHelper.putValue(mapFullBundle.getConfigJSON(), KEY_LAYERS, selectedLayers);
        final boolean selectedLayersUpdated = JSONHelper.putValue(mapFullBundle.getStateJSON(), KEY_SELLAYERS, selectedLayers);
        if (!(layersUpdated && selectedLayersUpdated)) {
            // failed to put layers correctly
            throw new ActionParamsException("Could not override layers selections");
        }

        // Set layout
        final String layout = JSONHelper.getStringFromJSON(publisherData, KEY_LAYOUT, "lefthanded");
        JSONHelper.putValue(mapFullBundle.getConfigJSON(), KEY_LAYOUT, layout);

        // Set size
        final JSONObject size = publisherData.optJSONObject(KEY_SIZE);
        if(!JSONHelper.putValue(mapFullBundle.getConfigJSON(), KEY_SIZE, size)) {
            throw new ActionParamsException("Could not set size for map");
        }

        final JSONArray plugins = mapFullBundle.getConfigJSON().optJSONArray(KEY_PLUGINS);
        if(plugins == null) {
            throw new ActionParamsException("Could not get default plugins");
        }
        final JSONArray userConfiguredPlugins = publisherData.optJSONArray(KEY_PLUGINS);

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
        JSONHelper.putValue(mapFullBundle.getConfigJSON(), KEY_PLUGINS, plugins);
    }

    /**
     * Removes the plugin and returns the removed value or null if not found.
     * NOTE! Modifies input list
     * @param plugins
     * @param pluginId
     * @return
     */
    private JSONObject removePlugin(final JSONArray plugins, final String pluginId) {
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

    private JSONObject sanitizeConfigLocation(final JSONObject config) {
        if(config == null) {
            return null;
        }

        // sanitize plugin.config.location.classes
        JSONObject location = config.optJSONObject("location");
        if (location != null) {
            String classes = location.optString("classes");
            if (classes != null && classes.length() > 0) {
                String[] filteredClasses = filterClasses(classes.split(" "));
                JSONHelper.putValue(location, "classes", StringUtils.join(filteredClasses, " "));
            }
            // Make sure we don't have inline css set
            location.remove("top");
            location.remove("right");
            location.remove("bottom");
            location.remove("left");
        }

        return config;
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

        // not editing, use template view
        if(PUBLISHED_VIEW_TEMPLATE_ID == -1) {
            log.error("Publish template id not configured (property: view.template.publish)!");
            throw new ActionParamsException("Trying to publish map, but template isn't configured");
        }
        log.debug("Using template to create a new view");
        // Get publisher defaults
        View templateView = viewService.getViewWithConf(PUBLISHED_VIEW_TEMPLATE_ID);
        if (templateView == null) {
            log.error("Could not get template View with id:", PUBLISHED_VIEW_TEMPLATE_ID);
            throw new ActionParamsException("Could not get template View");
        }

        // clone a blank view based on template (so template doesn't get updated!!)
        final View view = templateView.cloneBasicInfo();
        final long viewId = publisherInput.optLong("id", -1);
        if(viewId != -1) {
            // check loaded view against user if we are updating a view
            log.debug("Loading view for editing:", viewId);
            final View existingView = viewService.getViewWithConf(viewId);
            if (user.getId() != existingView.getCreator()) {
                throw new ActionDeniedException("No permissions to update view with id:" + viewId);
            }
            // setup ids for updating a view
            view.setId(existingView.getId());
            view.setSupplementId(existingView.getSupplementId());
            view.setUuid(existingView.getUuid());
            view.setOldId(existingView.getOldId());
        }

        return view;
    }

    private Bundle setupBundle(final View view, final JSONObject publisherData, final String bundleid) {

        final JSONObject bundleData = publisherData.optJSONObject(bundleid);
        if (bundleData != null && bundleData.names().length() > 0) {
            log.info("config found for", bundleid);
            final Bundle bundle = addBundle(view, bundleid);
            mergeBundleConfiguration(bundle, bundleData, null);
            return bundle;
        } else {
            log.warn("config not found for", bundleid, "- removing bundle.");
            // We have to remove the bundle...
            // TODO: check if we really want to remove the bundle from view since it could be template view???
            view.removeBundle(bundleid);
        }
        return null;
    }
    private Bundle addBundle(final View view, final String bundleid) {
        Bundle bundle = view.getBundleByName(bundleid);
        if (bundle == null) {
            log.info("Bundle with id:", bundleid, "not found in currentView - adding");
            if(!bundleCache.containsKey(bundleid)) {
                log.warn("Trying to add bundle that isn't loaded:", bundleid, "- Skipping it!");
                return null;
            }
            bundle = bundleCache.get(bundleid).clone();
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
    private void mergeBundleConfiguration(final Bundle bundle, final JSONObject userConfig, final JSONObject userState) {
        final JSONObject defaultConfig = bundle.getConfigJSON();
        final JSONObject defaultState = bundle.getStateJSON();
        final JSONObject mergedConfig = JSONHelper.merge(defaultConfig, userConfig);
        final JSONObject mergedState = JSONHelper.merge(defaultState, userState);
        bundle.setConfig(mergedConfig.toString());
        bundle.setState(mergedState.toString());
    }

    private View saveView(final View view) {
        try {
            if (view.getId() != -1) {
                viewService.updatePublishedView(view);
            } else {
                long viewId = viewService.addView(view);
                view.setId(viewId);
            }
        } catch (ViewException e) {
            log.error("Error when trying add/update published view", e);
        }
        return view;
    }

    private JSONArray getPublishableLayers(final JSONArray selectedLayers, final User user) throws ActionException {
        if(selectedLayers == null || user == null) {
            throw new ActionParamsException("Could not get selected layers");
        }
        final JSONArray filteredList = new JSONArray();
        log.debug("Selected layers:", selectedLayers);

        String userUuid = user.getUuid();
        try {
            for (int i = 0; i < selectedLayers.length(); ++i) {
                JSONObject layer = selectedLayers.getJSONObject(i);
                final String layerId = layer.getString("id");
                if (layerId.startsWith(PREFIX_MYPLACES)) {
                    // check publish right for published myplaces layer
                    if (hasRightToPublishMyPlaceLayer(layerId, userUuid, user.getScreenname())) {
                        filteredList.put(layer);
                    }
                } else if (layerId.startsWith(PREFIX_ANALYSIS)) {
                    // check publish right for published analysis layer
                    if (hasRightToPublishAnalysisLayer(layerId, user)) {
                        filteredList.put(layer);
                    }
                } else if (layerId.startsWith(PREFIX_USERLAYER)) {
                    // check publish rights for user layer
                    if (hasRightToPublishUserLayer(layerId, user)) {
                        filteredList.put(layer);
                    }
                } else if (hasRightToPublishLayer(layerId, user)) {
                    // check publish right for normal layer
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
            if (place.isOwnedBy(userUuid)) {
                myPlaceService.updatePublisherName(categoryId, userUuid, publisherName); // make it public
                // IMPORTANT! delete layer data from redis so transport will get updated layer data
                JedisManager.del(WFSLayerConfiguration.KEY + layerId);
                return true;
            }
        }
        log.warn("Found my places layer in selected that isn't users own or isn't published any more! LayerId:", layerId, "User UUID:", userUuid);
        return false;
    }


    private boolean hasRightToPublishAnalysisLayer(final String layerId, final User user) {
        final long analysisId = AnalysisHelper.getAnalysisIdFromLayerId(layerId);
        if(analysisId == -1) {
            return false;
        }
        final Analysis analysis = analysisService.getAnalysisById(analysisId);
        if (!analysis.getUuid().equals(user.getUuid())) {
            log.warn("Found analysis layer in selected that isn't users own! LayerId:", layerId, "User UUID:", user.getUuid(), "Analysis UUID:", analysis.getUuid());
            return false;
        }

        final List<String> permissionsList = permissionsService.getResourcesWithGrantedPermissions(
                AnalysisLayer.TYPE, user, Permissions.PERMISSION_TYPE_PUBLISH);
        log.debug("Analysis layer publish permissions", permissionsList);
        final String permissionKey = "analysis+"+analysis.getId();

        log.debug("PublishPermissions:", permissionsList);
        boolean hasPermission = permissionsList.contains(permissionKey);
        if (hasPermission) {
            // write publisher name for analysis
            analysisService.updatePublisherName(analysisId, user.getUuid(), user.getScreenname());
            // IMPORTANT! delete layer data from redis so transport will get updated layer data
            JedisManager.del(WFSLayerConfiguration.KEY + layerId);
        }
        else {
            log.warn("Found analysis layer in selected that isn't publishable any more! Permissionkey:", permissionKey, "User:", user);
        }
        return hasPermission;
    }

    private boolean hasRightToPublishUserLayer(final String layerId, final User user) {
        final long id = ConversionHelper.getLong(layerId.substring(PREFIX_USERLAYER.length()), -1);
        if (id == -1) {
            log.warn("Error parsing layerId:", layerId);
            return false;
        }
        final UserLayer userLayer = userLayerService.getUserLayerById(id);
        if (userLayer.isOwnedBy(user.getUuid())) {
            userLayerService.updatePublisherName(id, user.getUuid(), user.getScreenname());
            // IMPORTANT! delete layer data from redis so transport will get updated layer data
            JedisManager.del(WFSLayerConfiguration.KEY + layerId);
            return true;
        } else {
            return false;
        }
    }

    private boolean hasRightToPublishLayer(final String layerId, final User user) {
        // layerId might be external so don't use it straight up
        final OskariLayer layer = layerService.find(layerId);
        if (layer == null) {
            log.warn("Couldn't find layer with id:", layerId);
            return false;
        }
        final Long id = new Long(layer.getId());
        final List<Long> list = new ArrayList<Long>();
        list.add(id);
        final Map<Long, List<Permissions>> map = permissionsService.getPermissionsForLayers(list, Permissions.PERMISSION_TYPE_PUBLISH);
        List<Permissions> permissions = map.get(id);
        boolean hasPermission = permissionsService.permissionGrantedForRolesOrUser(
                user, permissions, Permissions.PERMISSION_TYPE_PUBLISH);
        if (!hasPermission) {
            log.warn("User tried to publish layer with no publish permission. LayerID:", layerId, "- User:", user);
        }
        return hasPermission;
    }


}
