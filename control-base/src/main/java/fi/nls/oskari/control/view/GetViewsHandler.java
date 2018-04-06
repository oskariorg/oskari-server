package fi.nls.oskari.control.view;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.nls.oskari.control.ActionConstants.*;

@OskariActionRoute("GetViews")
public class GetViewsHandler extends ActionHandler {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_ISPUBLIC = "isPublic";
    public static final String KEY_ISDEFAULT = "isDefault";
    public static final String KEY_PUBDOMAIN = "pubDomain";
    public static final String KEY_VIEWS = "views";
    public static final String KEY_METADATA = "metadata";
    public static final String KEY_SRSNAME = "srsName";

    private static final Logger log = LogFactory.getLogger(GetViewsHandler.class);

    private ViewService viewService;

    public void setViewService(final ViewService service) {
        viewService = service;
    }

    public void init() {
        // setup service only if it hasn't been set via the setter
        if (viewService == null) {
            setViewService(new ViewServiceIbatisImpl());
        }
    }

    public void handleAction(final ActionParameters params) throws ActionException {
        // require a logged in user when requesting views
        params.requireLoggedInUser();
        final long userId = params.getUser().getId();

        final String type = params.getHttpParam(ViewTypes.VIEW_TYPE, ViewTypes.USER);
        final List<View> views = viewService.getViewsForUser(userId);
        for (View view : views) {
            if (view.getType() == null) {
                view.setType(ViewTypes.USER);
            }
        }
        final List<JSONObject> viewsAsJsonObjects = views.stream()
                .filter(v -> type.equalsIgnoreCase(v.getType()))
                .map(v -> viewToJSONObject(v))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        final JSONArray viewArray = new JSONArray(viewsAsJsonObjects);
        final JSONObject ret = JSONHelper.createJSONObject(KEY_VIEWS, viewArray);
        ResponseHelper.writeResponse(params, ret);
    }

    private Optional<JSONObject> viewToJSONObject(View view) {
        try {
            final JSONObject viewJson = new JSONObject();
            viewJson.put(KEY_NAME, view.getName());
            viewJson.put(KEY_DESCRIPTION, view.getDescription());
            viewJson.put(KEY_LANG, view.getLang());
            viewJson.put(KEY_ID, view.getId());
            viewJson.put(KEY_UUID, view.getUuid());
            viewJson.put(KEY_ISPUBLIC, view.isPublic());
            viewJson.put(KEY_ISDEFAULT, view.isDefault());
            viewJson.put(KEY_PUBDOMAIN, view.getPubDomain());
            viewJson.put(KEY_URL, view.getUrl());
            viewJson.put(KEY_METADATA, view.getMetadata());
            viewJson.put(KEY_SRSNAME, view.getSrsName());
            viewJson.put(KEY_STATE, bundlesToJSONObject(view.getBundles()));
            return Optional.of(viewJson);
        } catch (Exception ex) {
            log.error("Failed to parse states for view:", view);
            return Optional.empty();
        }
    }

    private JSONObject bundlesToJSONObject(List<Bundle> bundles) {
        // publisher 2 doesn't need the view info since it loads it using id
        // The old publisher and normal view listing need them.
        final JSONObject state = new JSONObject();
        for (Bundle bundle : bundles) {
            JSONObject bundleNode = bundleToJSONObjet(bundle);
            // If bundleNode is null putValue will actually eventually call remove, which is fine here
            JSONHelper.putValue(state, bundle.getBundleinstance(), bundleNode);
        }
        return state;
    }

    private JSONObject bundleToJSONObjet(Bundle bundle) {
        JSONObject state = JSONHelper.createJSONObject(bundle.getState());
        if (state == null) {
            return null;
        }
        JSONObject config = JSONHelper.createJSONObject(bundle.getConfig());
        if (config == null) {
            return null;
        }
        JSONObject bundleNode = new JSONObject();
        JSONHelper.putValue(bundleNode, KEY_STATE, state);
        JSONHelper.putValue(bundleNode, KEY_CONFIG, config);
        return bundleNode;
    }

}
