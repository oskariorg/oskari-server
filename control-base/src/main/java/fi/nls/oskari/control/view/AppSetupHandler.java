package fi.nls.oskari.control.view;

import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.view.*;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.map.userlayer.service.UserLayerDbService;

import java.util.Set;

import static fi.nls.oskari.control.ActionConstants.*;

/**
 * This replaces the fi.nls.oskari.control.view.PublishHandler. Usable with publisher2 bundle
 *
 * actionhandler.AppSetup.bundles.simple=[bundleid],[another bundleid]
 */
@OskariActionRoute("AppSetup")
public class AppSetupHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(AppSetupHandler.class);

    public static final String PROPERTY_DRAW_TOOLS_ENABLED = "actionhandler.Publish.drawToolsRoles";
    static final String PROPERTY_VIEW_UUID = "oskari.publish.only.with.uuid";

    static final String KEY_PUBDATA = "pubdata";
    static final String KEY_METADATA = "metadata";
    static final String KEY_VIEWCONFIG = "configuration";
    static final String PARAM_PUBLISHER_VIEW_UUID = "publishedFrom";

    public static final String KEY_DOMAIN = "domain";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_PLUGINS = "plugins";
    public static final String KEY_LAYERS = "layers";
    public static final String KEY_SELLAYERS = "selectedLayers";

    public static final String KEY_CROSSHAIR = "crosshair";

    private static final boolean VIEW_ACCESS_UUID = PropertyUtil.getOptional(PROPERTY_VIEW_UUID, true);
    // Simple bundles don't require extra processing
    private static final Set<String> SIMPLE_BUNDLES = ConversionHelper.asSet(
            ViewModifier.BUNDLE_INFOBOX, ViewModifier.BUNDLE_TOOLBAR,
            ViewModifier.BUNDLE_PUBLISHEDGRID, ViewModifier.BUNDLE_FEATUREDATA2,
            ViewModifier.BUNDLE_COORDINATETOOL, ViewModifier.BUNDLE_STATSGRID, ViewModifier.BUNDLE_FEEDBACKSERVICE);

    // Bundles that we don't want to remove even if publisher doesn't provide config
    private static final Set<String> ALWAYSON_BUNDLES = ConversionHelper.asSet(
            ViewModifier.BUNDLE_INFOBOX, ViewModifier.BUNDLE_TOOLBAR);

    // Bundles that require divmanazer to be loaded for them to work
    private static final Set<String> BUNDLE_REQUIRES_DIVMANAZER =
            ConversionHelper.asSet(ViewModifier.BUNDLE_FEATUREDATA2, ViewModifier.BUNDLE_COORDINATETOOL, ViewModifier.BUNDLE_STATSGRID);

    // List of bundles that the user is able to publish
    // mapfull not included since it's assumed to be part of publisher template handled anyways
    private static final Set<String> BUNDLE_WHITELIST = ConversionHelper.asSet(
            ViewModifier.BUNDLE_PUBLISHEDMYPLACES2,ViewModifier.BUNDLE_DIVMANAZER);

    private static long PUBLISHED_VIEW_TEMPLATE_ID = -1;

    private ViewService viewService = null;
    private BundleService bundleService = null;
    private PublishPermissionHelper permissionHelper = new PublishPermissionHelper();

    private String[] drawToolsEnabledRoles = new String[0];


    public void setMyPlacesService(final MyPlacesService service) {
        permissionHelper.setMyPlacesService(service);
    }

    public void setAnalysisService(final AnalysisDbService service) {
        permissionHelper.setAnalysisService(service);
    }

    public void setUserLayerService(final UserLayerDbService service) {
        permissionHelper.setUserLayerService(service);
    }


    public void setPermissionsService(final PermissionsService service) {
        permissionHelper.setPermissionsService(service);
    }
    public void setOskariLayerService(final OskariLayerService service) {
        permissionHelper.setOskariLayerService(service);
    }

    public void setViewService(final ViewService service) {
        viewService = service;
    }

    public void setBundleService(final BundleService service) {
        bundleService = service;
    }


    public void init() {
        // setup services if it hasn't been initialized
        permissionHelper.init();

        if (viewService == null) {
            setViewService(new ViewServiceIbatisImpl());
        }

        if (bundleService == null) {
            setBundleService(new BundleServiceIbatisImpl());
        }
        try {
            getPublishTemplate();
        } catch (ActionException ex) {
            LOG.error(ex, "Publish template not available!!");
        }

        // setup roles authorized to enable drawing tools on published map
        drawToolsEnabledRoles = PropertyUtil.getCommaSeparatedList(PROPERTY_DRAW_TOOLS_ENABLED);

        final String[] configBundles = PropertyUtil.getCommaSeparatedList("actionhandler.AppSetup.bundles.simple");
        if(configBundles.length > 0) {
            LOG.info("Whitelisting more bundles due to configuration configured ", configBundles);
        }
        for(String bundleId : configBundles) {
            SIMPLE_BUNDLES.add(bundleId);
        }

        // add all "simple" bundles to the whitelist
        BUNDLE_WHITELIST.addAll(SIMPLE_BUNDLES);

        // preload regularly used bundles to cache
        for(String bundleid : BUNDLE_WHITELIST) {
            bundleService.forceBundleTemplateCached(bundleid);
        }
    }

    public void preProcess(ActionParameters params) throws ActionException {
        // published maps are user content so only available for logged in users
        params.requireLoggedInUser();

        // check permission if modifying existing view
        final String viewUuid = params.getHttpParam(PARAM_UUID);
        if(viewUuid == null) {
            return;
        }
        // verify this is users own view
        final View view = viewService.getViewWithConfByUuId(viewUuid);
        final boolean hasPermission = viewService.hasPermissionToAlterView(view, params.getUser());
        if(!hasPermission) {
            throw new ActionDeniedException("Not allowed to modify view with uuid: " + viewUuid);
        }
    }

    public void handleGet(ActionParameters params) throws ActionException {
        final String viewUuid = params.getRequiredParam(PARAM_UUID);
        final View view = viewService.getViewWithConfByUuId(viewUuid);
        final JSONObject response = new JSONObject();
        try {
            JSONHelper.putValue(response, KEY_VIEWCONFIG, ViewHelper.getConfiguration(view));
        } catch (ViewException ex) {
            throw new ActionException("Couldn't restore view data", ex);
        }
        JSONHelper.putValue(response, KEY_METADATA, view.getMetadata());

        ResponseHelper.writeResponse(params, response);
    }

    public void handlePut(ActionParameters params) throws ActionException {
        handlePost(params);
    }
    public void handlePost(ActionParameters params) throws ActionException {

        final View publishedMap = buildPublishedView(params);
        try {
            JSONObject newViewJson = new JSONObject(publishedMap.toString());
            ResponseHelper.writeResponse(params, newViewJson);
        } catch (JSONException je) {
            LOG.error(je, "Could not create JSON response.");
            ResponseHelper.writeResponse(params, false);
        }
    }

    protected View buildPublishedView(ActionParameters params) throws ActionException {

        final User user = params.getUser();

        final String viewUuid = params.getHttpParam(PARAM_UUID);
        final View view = getBaseView(viewUuid, user);
        LOG.debug("Processing view with UUID: " + view.getUuid());

        // Parse stuff sent by JS
        final JSONObject publisherData = getPublisherInput(params.getRequiredParam(KEY_PUBDATA));

        // setup metadata for publisher
        view.setMetadata(publisherData.optJSONObject(KEY_METADATA));
        parseMetadata(view, user);

        // setup view modifications
        final JSONObject viewdata = publisherData.optJSONObject(KEY_VIEWCONFIG);
        if(viewdata == null) {
            throw new ActionParamsException("Missing configuration for the view to be saved");
        }

        // setup map state
        setupMapState(view, user, viewdata.optJSONObject(ViewModifier.BUNDLE_MAPFULL),
                params.getRequiredParam(PARAM_PUBLISHER_VIEW_UUID));

        // check if we need to add divmanazer
        for(String bundleid : BUNDLE_REQUIRES_DIVMANAZER) {
            if(viewdata.has(bundleid)) {
                // Found bundles that require divmanazer
                // add it to the view before handling them
                LOG.info("Adding bundle", ViewModifier.BUNDLE_DIVMANAZER);
                addBundle(view, ViewModifier.BUNDLE_DIVMANAZER);
                // break so we don't add it more than once
                break;
            }
        }
        // setup all the bundles that don't need extra processing
        for(String bundleid : SIMPLE_BUNDLES) {
            if(viewdata.has(bundleid)) {
                setupBundle(view, viewdata, bundleid, ALWAYSON_BUNDLES.contains(bundleid));

                //toolbar -> add style info from metadata
                if (bundleid.equals(ViewModifier.BUNDLE_TOOLBAR)) {
                    setupToolbarStyleInfo(view);
                }
            }
        }

        // reorder bundles - rpc bundle must have highest segment number
        view.pushBundleLast(ViewModifier.BUNDLE_RPC);

        // Setup publishedmyplaces2 bundle if user has configured it/has permission to do so
        if(!user.hasAnyRoleIn(drawToolsEnabledRoles)) {
            // remove myplaces functionality if user doesn't have permission to add them
            Object drawTools = viewdata.remove(ViewModifier.BUNDLE_PUBLISHEDMYPLACES2);
            if(drawTools != null) {
                LOG.warn("User tried to add draw tools, but doesn't have any of the permitted roles. Removing draw tools!");
            }
        }

        final Bundle myplaces = setupBundle(view, viewdata, ViewModifier.BUNDLE_PUBLISHEDMYPLACES2, false);
        handleMyplacesDrawLayer(myplaces, user);

        return saveView(view);
    }

    private void setupToolbarStyleInfo(final View view) throws ActionParamsException {
        final Bundle toolbarBundle = view.getBundleByName(ViewModifier.BUNDLE_TOOLBAR);
        if (toolbarBundle == null) {
            throw new ActionParamsException("Could not find toolbar bundle from template view: " + view.getId());
        }

        JSONObject toolbarConfig = toolbarBundle.getConfigJSON();
        JSONHelper.putValue(toolbarConfig, KEY_STYLE, view.getMetadata().optJSONObject(KEY_STYLE));
    }

    protected void parseMetadata(final View view, final User user) throws ActionException {
        if(view.getMetadata() == null) {
            throw new ActionParamsException("Missing metadata for the view to be saved");
        }

        // setup basic info about view
        final String domain = JSONHelper.getStringFromJSON(view.getMetadata(), KEY_DOMAIN, null);
        if(domain == null || domain.trim().isEmpty()) {
            throw new ActionParamsException("Domain missing from metadata");
        }
        final String name = JSONHelper.getStringFromJSON(view.getMetadata(), KEY_NAME, "Published map " + System.currentTimeMillis());
        final String language = JSONHelper.getStringFromJSON(view.getMetadata(), KEY_LANGUAGE, PropertyUtil.getDefaultLanguage());

        view.setPubDomain(domain.trim());
        view.setName(name);
        view.setType(ViewTypes.PUBLISHED);
        view.setCreator(user.getId());
        view.setIsPublic(true);
        // application/page/developmentPath should be configured to publish template view
        view.setLang(language);
        view.setOnlyForUuId(VIEW_ACCESS_UUID);
    }

    private void setupMapState(final View view, final User user, final JSONObject input, final String publisherUUID) throws ActionException {

        final Bundle mapfullBundle = view.getBundleByName(ViewModifier.BUNDLE_MAPFULL);
        if (mapfullBundle == null) {
            throw new ActionParamsException("Could not find mapfull bundle from template view: " + view.getId());
        }

        if(input == null) {
            throw new ActionParamsException("Could not get state for mapfull from publisher data");
        }
        // complete overrride of template mapfull state with the data coming from publisher!
        JSONObject mapfullState = input.optJSONObject(KEY_STATE);
        if(mapfullState == null) {
            throw new ActionParamsException("Could not get state for mapfull from publisher data");
        }
        mapfullBundle.setState(mapfullState.toString());

        // setup layers based on user rights (double check for user rights)
        final JSONArray selectedLayers = permissionHelper.getPublishableLayers(mapfullState.optJSONArray(KEY_SELLAYERS), user);

        // Override template layer selections
        final boolean layersUpdated = JSONHelper.putValue(mapfullBundle.getConfigJSON(), KEY_LAYERS, selectedLayers);
        final boolean selectedLayersUpdated = JSONHelper.putValue(mapfullBundle.getStateJSON(), KEY_SELLAYERS, selectedLayers);
        if (!(layersUpdated && selectedLayersUpdated)) {
            // failed to put layers correctly
            throw new ActionParamsException("Could not override layers selections");
        }

        final JSONArray plugins = mapfullBundle.getConfigJSON().optJSONArray(KEY_PLUGINS);
        if(plugins == null) {
            throw new ActionParamsException("Could not get default plugins");
        }
        final JSONObject mapfullConf = input.optJSONObject(KEY_CONF);
        if(mapfullConf == null) {
            throw new ActionParamsException("Could not get map configuration from input");
        }
        JSONObject finalConfig = mapfullBundle.getConfigJSON();

        final JSONArray userConfiguredPlugins = mapfullConf.optJSONArray(KEY_PLUGINS);
        if(userConfiguredPlugins == null) {
            throw new ActionParamsException("Could not get map plugins from input");
        }

        // merge user configs for template plugins
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.optJSONObject(i);
            //plugins
            JSONObject userPlugin = PublishBundleHelper.removePlugin(userConfiguredPlugins, plugin.optString(KEY_ID));
            if(userPlugin != null) {
                // same plugin from template AND user
                // merge config using users as base! and override it with template values
                // this way terms of use etc cannot be overridden by user
                JSONObject mergedConfig = JSONHelper.merge(userPlugin.optJSONObject(KEY_CONFIG), plugin.optJSONObject(KEY_CONFIG));
                JSONHelper.putValue(plugin, KEY_CONFIG, PublishBundleHelper.sanitizeConfigLocation(mergedConfig));
            }
        }
        // add remaining plugins user has selected on top of template plugins
        for (int i = userConfiguredPlugins.length(); --i >= 0; ) {
            JSONObject userPlugin = userConfiguredPlugins.optJSONObject(i);
            JSONHelper.putValue(userPlugin, KEY_CONFIG, PublishBundleHelper.sanitizeConfigLocation(userPlugin.optJSONObject(KEY_CONFIG)));
            plugins.put(userPlugin);
        }

        // replace current plugins
        JSONHelper.putValue(finalConfig, KEY_PLUGINS, plugins);

        // get mapOptions from publisher view, copy style definition from metadata to mapOptions
        JSONHelper.putValue(finalConfig, KEY_MAPOPTIONS,
                getMapOptions(publisherUUID, input, view.getMetadata().optJSONObject(KEY_STYLE)));
    }

    /**
     * Get mapOptions for the requested view. UUID should be of the view that was used to publish the map.
     * This overrides any mapOptions on the publish template so we can have published maps on different projections.
     * @param publisherUUID view uuid - usually of the view that had the publisher
     * @return mapOptions - part of the conf of mapfull bundle
     */
    private JSONObject getMapOptions(String publisherUUID, final JSONObject input, JSONObject style) throws ActionException {
        // uuid is a request parameter (PARAM_PUBLISHER_VIEW_UUID)
        View publisherView = viewService.getViewWithConfByUuId(publisherUUID);
        if (publisherView == null) {
            throw new ActionParamsException("Could not get the parent appsetup for uuid " + publisherUUID);
        }
        JSONObject mapOptions = publisherView.getMapOptions();
        if (mapOptions == null) {
            LOG.info("Could not get the mapOptions from appsetup for uuid", publisherUUID,
                    "the embedded maps will use defaults from frontend code");
            mapOptions = new JSONObject();
        }
        JSONHelper.putValue(mapOptions, KEY_CROSSHAIR, crosshairEnabled(input));
        JSONHelper.putValue(mapOptions, KEY_STYLE, style);
        return mapOptions;
    }

    private boolean crosshairEnabled(JSONObject input) {
        if (input == null) {
            return false;
        }
        JSONObject conf = input.optJSONObject(KEY_CONF);
        if (conf == null) {
            return false;
        }

        JSONObject confMapOptions = conf.optJSONObject(KEY_MAPOPTIONS);
        if (confMapOptions == null) {
            return false;
        }

        return confMapOptions.optBoolean(KEY_CROSSHAIR);
    }
    private void handleMyplacesDrawLayer(final Bundle myplaces, final User user) throws ActionException {

        if(myplaces == null) {
            // nothing to handle, bundle not added
            return;
        }
        final JSONObject config = myplaces.getConfigJSON();
        final String drawLayerId = config.optString("layer");
        permissionHelper.setupDrawPermission(drawLayerId, user);
        // NOTE! allowing guests to draw features on the layer
        JSONHelper.putValue(config, "allowGuest", true);
    }

    private JSONObject getPublisherInput(final String input) throws ActionException {
        try {
            return new JSONObject(input);
        } catch (JSONException e) {
            LOG.error(e, "Unable to parse publisher data:", input);
            throw new ActionParamsException("Unable to parse publisher data.");
        }
    }

    private View getBaseView(final String viewUuid, final User user) throws ActionException {

        if (user.isGuest()) {
            throw new ActionDeniedException("Trying to publish map, but couldn't determine user");
        }

        // Get publisher defaults
        final View templateView = getPublishTemplate();

        // clone a blank view based on template (so template doesn't get updated!!)
        final View view = templateView.cloneBasicInfo();
        if(viewUuid != null) {
            // check loaded view against user if we are updating a view
            LOG.debug("Loading view for editing:", viewUuid);
            final View existingView = viewService.getViewWithConfByUuId(viewUuid);
            // double check
            if(!viewService.hasPermissionToAlterView(existingView, user)) {
                throw new ActionDeniedException("No permissions to update view with uuid:" + viewUuid);
            }
            // setup ids for updating a view
            view.setId(existingView.getId());
            view.setCreator(existingView.getCreator());
            view.setUuid(existingView.getUuid());
            view.setOldId(existingView.getOldId());
        }

        return view;
    }

    private Bundle setupBundle(final View view, final JSONObject inputViewData, final String bundleid, final boolean alwaysKeep) {

        // Note! Assumes value is a JSON object
        final JSONObject bundleData = inputViewData.optJSONObject(bundleid);
        if (bundleData != null) {
            LOG.info("Config found for", bundleid);
            final Bundle bundle = addBundle(view, bundleid);
            if(bundle != null) {
                PublishBundleHelper.mergeBundleConfiguration(bundle, bundleData.optJSONObject(KEY_CONF), bundleData.optJSONObject(KEY_STATE));
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

    private View saveView(final View view) {
        try {
            if (view.getId() != -1) {
                viewService.updatePublishedView(view);
            } else {
                long viewId = viewService.addView(view);
                view.setId(viewId);
            }
        } catch (ViewException e) {
            LOG.error(e, "Error when trying add/update published view");
        }
        return view;
    }


    private View getPublishTemplate()
            throws ActionException {
        if (PUBLISHED_VIEW_TEMPLATE_ID == -1) {
            PUBLISHED_VIEW_TEMPLATE_ID = PropertyUtil.getOptional(ViewService.PROPERTY_PUBLISH_TEMPLATE, -1);
            if (PUBLISHED_VIEW_TEMPLATE_ID == -1) {
                // TODO: maybe try checking for view of type PUBLISH from DB?
                LOG.warn("Publish template id not configured (property:", ViewService.PROPERTY_PUBLISH_TEMPLATE, ")!");
            } else {
                LOG.info("Using publish template id: ", PUBLISHED_VIEW_TEMPLATE_ID);
            }
        }

        if (PUBLISHED_VIEW_TEMPLATE_ID == -1) {
            LOG.error("Publish template id not configured (property: view.template.publish)!");
            throw new ActionParamsException("Trying to publish map, but template isn't configured");
        }
        LOG.debug("Using template to create a new view");
        // Get publisher defaults
        View templateView = viewService.getViewWithConf(PUBLISHED_VIEW_TEMPLATE_ID);
        if (templateView == null) {
            LOG.error("Could not get template View with id:", PUBLISHED_VIEW_TEMPLATE_ID);
            throw new ActionParamsException("Could not get template View");
        }
        return templateView;
    }

}
