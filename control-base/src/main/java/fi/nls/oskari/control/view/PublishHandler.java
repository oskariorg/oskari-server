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
import fi.nls.oskari.map.userlayer.service.UserLayerDbService;
import fi.nls.oskari.map.view.*;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import static fi.nls.oskari.control.ActionConstants.*;

/**
 * Deprecated. Replaced with fi.nls.oskari.control.view.AppSetupHandler which is used by publisher2 bundle.
 */
@Deprecated
@OskariActionRoute("Publish")
public class PublishHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(PublishHandler.class);

    public static final String PROPERTY_DRAW_TOOLS_ENABLED = "actionhandler.Publish.drawToolsRoles";
    static final String PROPERTY_VIEW_UUID = "oskari.publish.only.with.uuid";

    public static final String KEY_PUBDATA = "pubdata";
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

    private static final boolean VIEW_ACCESS_UUID = PropertyUtil.getOptional(PROPERTY_VIEW_UUID, true);
    private static final Set<String> CACHED_BUNDLE_IDS = ConversionHelper.asSet(
            ViewModifier.BUNDLE_PUBLISHEDGRID, ViewModifier.BUNDLE_TOOLBAR,
            ViewModifier.BUNDLE_PUBLISHEDMYPLACES2, ViewModifier.BUNDLE_FEATUREDATA2,
            ViewModifier.BUNDLE_DIVMANAZER);

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
            LOG.error("Publish template not available!!");
        }

        // setup roles authorized to enable drawing tools on published map
        drawToolsEnabledRoles = PropertyUtil.getCommaSeparatedList(PROPERTY_DRAW_TOOLS_ENABLED);

        for(String bundleid : CACHED_BUNDLE_IDS) {
            bundleService.forceBundleTemplateCached(bundleid);
        }
    }


    public void handleAction(ActionParameters params) throws ActionException {

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
        JSONHelper.putValue(mapFullBundle.getConfigJSON(), KEY_USER, user.toJSON());

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
        currentView.setOnlyForUuId(VIEW_ACCESS_UUID);
        LOG.debug("UUID: " + currentView.getUuid());

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
                LOG.warn("Publisher sent state for infobox, but infobox isn't available in template view! State:", tmpInfoboxState);
            }
        }

        // Setup publishedmyplaces2 bundle if user has configured it/has permission to do so
        if(user.hasAnyRoleIn(drawToolsEnabledRoles)) {
            final Bundle myplaces = setupBundle(currentView, publisherData, ViewModifier.BUNDLE_PUBLISHEDMYPLACES2);
            handleMyplacesDrawLayer(myplaces, user);
        } else {
            // check that template doesn't have it either, remove if found
            if(LOG.isDebugEnabled()) {
                Bundle drawTools = currentView.getBundleByName(ViewModifier.BUNDLE_PUBLISHEDMYPLACES2);
                if(drawTools != null) {
                    LOG.debug("Found",ViewModifier.BUNDLE_PUBLISHEDMYPLACES2, "in view, removing!");
                }
            }
            currentView.removeBundle(ViewModifier.BUNDLE_PUBLISHEDMYPLACES2);
            if(LOG.isDebugEnabled()) {
                Bundle drawTools = currentView.getBundleByName(ViewModifier.BUNDLE_PUBLISHEDMYPLACES2);
                LOG.debug(ViewModifier.BUNDLE_PUBLISHEDMYPLACES2, "should have been removed:", drawTools);
            }
        }

        // Setup toolbar bundle if user has configured it
        setupBundle(currentView, publisherData, ViewModifier.BUNDLE_TOOLBAR);

        // Setup feature data bundle if user has configured it
        final JSONObject featureData = publisherData.optJSONObject(ViewModifier.BUNDLE_FEATUREDATA2);
        if (featureData != null && featureData.names().length() > 0) {
            // Add divmanazer first since feature data uses flyout
            LOG.info("Adding bundle", ViewModifier.BUNDLE_DIVMANAZER);
            addBundle(currentView, ViewModifier.BUNDLE_DIVMANAZER);
            // then setup feature data
            final Bundle bundle = addBundle(currentView, ViewModifier.BUNDLE_FEATUREDATA2);
            PublishBundleHelper.mergeBundleConfiguration(bundle, featureData, null);
        }

        // Setup thematic map/published grid bundle
        final JSONObject gridState = publisherData.optJSONObject(KEY_GRIDSTATE);
        LOG.debug("Grid state:", gridState);
        if(gridState != null) {
            final Bundle gridBundle = addBundle(currentView, ViewModifier.BUNDLE_PUBLISHEDGRID);
            LOG.debug("Grid bundle added:", gridBundle);
            PublishBundleHelper.mergeBundleConfiguration(gridBundle, null, gridState);
        }

        final View newView = saveView(currentView);

        try {
            JSONObject newViewJson = new JSONObject(newView.toString());
            ResponseHelper.writeResponse(params, newViewJson);
        } catch (JSONException je) {
            LOG.error(je, "Could not create JSON response.");
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
        permissionHelper.setupDrawPermission(drawLayerId, user);
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
        final JSONArray selectedLayers = permissionHelper.getPublishableLayers(publisherMapState.optJSONArray(KEY_SELLAYERS), user);

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
        JSONHelper.putValue(mapFullBundle.getConfigJSON(), KEY_PLUGINS, plugins);
    }



    private JSONObject getPublisherInput(final String input) throws ActionException {
        try {
            return new JSONObject(input);
        } catch (JSONException e) {
            LOG.error(e, "Unable to parse publisher data:", input);
            throw new ActionParamsException("Unable to parse publisher data.");
        }
    }

    private View getBaseView(final JSONObject publisherInput, final User user) throws ActionException {

        if (user.isGuest()) {
            throw new ActionDeniedException("Trying to publish map, but couldn't determine user");
        }

        // Get publisher defaults
        LOG.debug("Using template to create a new view");
        final View templateView = getPublishTemplate();

        // clone a blank view based on template (so template doesn't get updated!!)
        final View view = templateView.cloneBasicInfo();
        final long viewId = publisherInput.optLong("id", -1);
        if(viewId != -1) {
            // check loaded view against user if we are updating a view
            LOG.debug("Loading view for editing:", viewId);
            final View existingView = viewService.getViewWithConf(viewId);
            if (user.getId() != existingView.getCreator()) {
                throw new ActionDeniedException("No permissions to update view with id:" + viewId);
            }
            // setup ids for updating a view
            view.setId(existingView.getId());
            view.setCreator(existingView.getCreator());
            view.setUuid(existingView.getUuid());
            view.setOldId(existingView.getOldId());
        }

        return view;
    }

    private Bundle setupBundle(final View view, final JSONObject publisherData, final String bundleid) {

        final JSONObject bundleData = publisherData.optJSONObject(bundleid);
        if (bundleData != null && bundleData.names().length() > 0) {
            LOG.info("config found for", bundleid);
            final Bundle bundle = addBundle(view, bundleid);
            PublishBundleHelper.mergeBundleConfiguration(bundle, bundleData, null);
            return bundle;
        } else {
            LOG.warn("config not found for", bundleid, "- removing bundle.");
            // We have to remove the bundle...
            // TODO: check if we really want to remove the bundle from view since it could be template view???
            view.removeBundle(bundleid);
        }
        return null;
    }

    private Bundle addBundle(final View view, final String bundleid) {
        Bundle bundle = view.getBundleByName(bundleid);
        if (bundle == null) {
            LOG.info("Bundle with id:", bundleid, "not found in currentView - adding");
            if(!CACHED_BUNDLE_IDS.contains(bundleid)) {
                LOG.warn("Trying to add bundle that isn't recognized:", bundleid, "- Skipping it!");
                return null;
            }
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
            LOG.error("Error when trying add/update published view", e);
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
