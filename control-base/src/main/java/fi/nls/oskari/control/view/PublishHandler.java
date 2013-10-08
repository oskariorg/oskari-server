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
import fi.nls.oskari.view.modifier.ViewModifier;
import fi.nls.oskari.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_PLUGINS = "plugins";
    public static final String KEY_SIZE = "size";
    public static final String KEY_MAPSTATE = "mapstate";
    public static final String KEY_LAYERS = "layers";
    public static final String KEY_SELLAYERS = "selectedLayers";
    public static final String KEY_CONFIG = "config";
    public static final String KEY_STATE = "state";

    public static final String KEY_GRIDSTATE = "gridState";
    private Bundle publishedGridBundle = null;

    private static final String PREFIX_MYPLACES = "myplaces_";
    private static final String PREFIX_BASELAYER = "base_";
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

        publishedGridBundle = bundleService.getBundleTemplateByName(ViewModifier.BUNDLE_PUBLISHEDGRID);
        if(publishedGridBundle == null) {
            log.warn("Couldn't get publishedGrid bundle template from DB!");
        }        
    }

    public void handleAction(ActionParameters params) throws ActionException {
        if(PUBLISHED_VIEW_TEMPLATE_ID == -1) {
            log.error("Publish template id not configured (property: view.template.publish)!");
            throw new ActionParamsException("Trying to publish map, but template isn't configured");
        }

        final User user = params.getUser();
        long userId = user.getId();
        if (user.isGuest() || userId == -1) {
            throw new ActionDeniedException("Trying to publish map, but couldn't determine user");
        }

        // Get publisher defaults
        View currentView = viewService.getViewWithConf(PUBLISHED_VIEW_TEMPLATE_ID);
        if (currentView == null) {
            log.error("Could not get template View with id:", PUBLISHED_VIEW_TEMPLATE_ID);
            throw new ActionParamsException("Could not get template View");
        }

        Bundle mapFullBundle = currentView.getBundleByName("mapfull");
        if (mapFullBundle == null) {
            throw new ActionParamsException("Could not get current state for mapfull");
        }
        JSONObject mapfullConfig = null;
        try {
            mapfullConfig = new JSONObject(mapFullBundle.getConfig());
        } catch (JSONException e) {
            log.error("Could not create JSONs of defaults:", mapFullBundle);
            throw new ActionParamsException("Corrupted bundle data");
        }

        Bundle infoboxBundle = currentView.getBundleByName("infobox");
        JSONObject infoboxState = null;
        JSONObject infoboxConfig = null;
        try {
            infoboxState = new JSONObject(infoboxBundle.getState());
            infoboxConfig = new JSONObject(infoboxBundle.getConfig());
        } catch (JSONException e) {
            log.error("Could not create JSONs of defaults:", infoboxBundle);
            throw new ActionParamsException("Corrupted bundle data");
        }

        // Set user
        try {
            JSONObject userJson = new JSONObject();
            userJson.put(KEY_FIRSTNAME, user.getFirstname());
            userJson.put(KEY_LASTNAME, user.getLastname());
            userJson.put(KEY_NICKNAME, user.getScreenname());
            userJson.put(KEY_LOGINNAME, user.getEmail());
            mapfullConfig.put(KEY_USER, userJson);
        } catch (JSONException jsonex) {
            log.error(jsonex, "Could not create user object:", user);
            throw new ActionParamsException("User data problem");
        }

        // Parse stuff sent by JS
        final String pdStr = params.getHttpParam(KEY_PUBDATA);
        JSONObject pubdata = null;
        try {
            pubdata = new JSONObject(pdStr);
        } catch (JSONException e) {
            log.error(e, "Unable to parse publisher data:", pdStr );
            throw new ActionParamsException("Unable to parse publisher data.");
        }

        try {
            if (!pubdata.isNull("id")) {
                long updateViewId = pubdata.getLong("id");
                currentView = viewService.getViewWithConf(updateViewId);

                if (user.getId() != currentView.getCreator()) {
                    log.error("Trying to publish map, but couldnt determine user");
                    throw new ActionDeniedException("No permissions to update this id:" + updateViewId);
                }

            }
        } catch (JSONException e1) {
            log.error("Some exception when try get publish id from pubdata json");
        }

        final String domain = JSONHelper.getStringFromJSON(pubdata, KEY_DOMAIN, null);
        if(domain == null) {
            throw new ActionParamsException("Domain missing");
        }
        final String name = JSONHelper.getStringFromJSON(pubdata, KEY_NAME, "Julkaistu kartta " + System.currentTimeMillis());
        final String language = JSONHelper.getStringFromJSON(pubdata, KEY_LANGUAGE, PropertyUtil.getDefaultLanguage());

        JSONArray newPlugins;
        JSONObject size;
        JSONObject gridState = null;
        JSONObject mapfullState = null;

        try {
            newPlugins = pubdata.getJSONArray(KEY_PLUGINS);
            size = pubdata.getJSONObject(KEY_SIZE);
            if (pubdata.has(KEY_GRIDSTATE)) {
                gridState = pubdata.getJSONObject(KEY_GRIDSTATE);
            }

            mapfullState = pubdata.getJSONObject(KEY_MAPSTATE);
            final JSONObject tmpInfoboxState = pubdata.optJSONObject(ViewModifier.BUNDLE_INFOBOX);
            if (tmpInfoboxState != null) {
                infoboxState = tmpInfoboxState;
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
        final boolean layersUpdated = JSONHelper.putValue(mapfullConfig, KEY_LAYERS, selectedLayers);
        final boolean selectedLayersUpdated = JSONHelper.putValue(mapfullState, KEY_SELLAYERS, selectedLayers);
        if (!(layersUpdated && selectedLayersUpdated)) {
            // failed to put layers correctly
            throw new RuntimeException("Could not override layers selections");
        }

        // Set size
        try {
            mapfullConfig.put(KEY_SIZE, size);
        } catch (JSONException e) {
            throw new RuntimeException("Could not set size");
        }

        // Append plugins
        JSONArray plugins = null;
        try {
            plugins = mapfullConfig.getJSONArray(KEY_PLUGINS);
        } catch (JSONException e) {
            throw new RuntimeException("Could not get default plugins");
        }

        for (int i = newPlugins.length(); --i >= 0; ) {
            boolean alreadyAdded = false;
            JSONObject newPlugin = null;
            try {
                newPlugin = newPlugins.getJSONObject(i);
            } catch (JSONException e) {
                throw new RuntimeException("Could not loop new plugins");
            }
            for (int j = plugins.length(); --j >= 0; ) {
                JSONObject plugin = null;
                try {
                    plugin = plugins.getJSONObject(j);
                } catch (JSONException e) {
                    throw new RuntimeException("Could not loop"
                            + " default plugins");
                }
                try {
                    String newPluginId = newPlugin.getString(KEY_ID);
                    String pluginId = plugin.getString(KEY_ID);
                    if (newPluginId.equals(pluginId))
                        alreadyAdded = true;
                } catch (JSONException e) {
                    throw new RuntimeException("Could not compare"
                            + " plugin IDs");
                }
            }
            if (!alreadyAdded)
                plugins.put(newPlugin);
        }

        try {
            mapfullConfig.put(KEY_PLUGINS, plugins);
        } catch (JSONException e) {
            throw new RuntimeException("Could not append plugin array");
        }

        // Build viewdata
        JSONObject viewData = new JSONObject();
        try {
            final JSONObject mapfull = new JSONObject();
            mapfull.put(KEY_CONFIG, mapfullConfig);
            mapfull.put(KEY_STATE, mapfullState);

            final JSONObject infobox = new JSONObject();
            infobox.put(KEY_CONFIG, infoboxConfig);
            infobox.put(KEY_STATE, infoboxState);

            viewData.put(ViewModifier.BUNDLE_MAPFULL, mapfull);
            viewData.put(ViewModifier.BUNDLE_INFOBOX, infobox);

            if(gridState != null && publishedGridBundle != null) {
                Bundle gridBundle = currentView.getBundleByName(ViewModifier.BUNDLE_PUBLISHEDGRID);
                if(gridBundle == null) {
                    // grid bundle was not present, adding it manually
                    currentView.addBundle(publishedGridBundle);
                    gridBundle = publishedGridBundle;
                }

                final JSONObject publishedGrid = new JSONObject();
                publishedGrid.put(KEY_CONFIG, new JSONObject(gridBundle.getConfig()));
                publishedGrid.put(KEY_STATE, gridState);
                gridBundle.setState(gridState.toString());
                viewData.put(ViewModifier.BUNDLE_PUBLISHEDGRID, publishedGrid);
            }
        } catch (JSONException e) {
            throw new RuntimeException("Could not store bundle JSONs");
        }

        // Pass through the template stuff do not going to modify
        // TODO: why do we construct viewData when this overrides it for other than mapfull/infobox?
        for (Bundle s : currentView.getBundles()) {
            String bname = s.getBundleinstance();
            JSONObject bJson = new JSONObject();
            if (bname.equals(ViewModifier.BUNDLE_INFOBOX) || bname.equals(ViewModifier.BUNDLE_MAPFULL))
                continue;
            try {
                JSONObject sJson = new JSONObject(s.getState());
                JSONObject cJson = new JSONObject(s.getConfig());
                bJson.put(KEY_CONFIG, cJson);
                bJson.put(KEY_STATE, sJson);
                viewData.put(bname, bJson);
            } catch (JSONException e) {
                throw new RuntimeException("Could pass through"
                        + " template bundles as-is");
            }
        }
        // TODO: why is this added to additional params - logging purposes on exception?
        params.putAdditionalParam(KEY_VIEW_DATA, viewData.toString());

        View newView = null;

        if (!pubdata.isNull("id")) {
            newView = updateView(currentView, viewData);
        } else {
            newView = addView(currentView, viewData);
        }

        log.debug("Published a map:", newView);

        try {
            JSONObject newViewJson = new JSONObject(newView.toString());
            ResponseHelper.writeResponse(params, newViewJson);
        } catch (JSONException je) {
            log.error(je, "Could not create JSON response.");
            ResponseHelper.writeResponse(params, false);

        }
    }


    private synchronized View updateView(final View currentView, final JSONObject viewJson) throws ActionException {

        try {
            viewService.updatePublishedView(currentView, viewJson);
        } catch (ViewException e) {
            log.error("Error when trying update published view", e);
        }

        return currentView;
    }

    private synchronized View addView(final View currentView, final JSONObject viewJson) throws ActionException {

        // Create new View
        View newView = new View();

        long newViewId = 0;

        try {
            newViewId = viewService.addView(currentView, viewJson);
            newView.setId(newViewId);
        } catch (ViewException e) {
            log.error("Error when trying add published view", e);
        }

        return newView;
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
