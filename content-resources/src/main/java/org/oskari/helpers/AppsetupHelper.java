package org.oskari.helpers;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Helpers for flyway scripts. Be very careful when making changes as previous versions of Oskari are using this
 * to migrate database.
 */
public class AppsetupHelper {

    private static final ViewService viewService = new AppSetupServiceMybatisImpl();
    private static Logger log = LogFactory.getLogger(AppsetupHelper.class);

    public static long create(Connection conn, final String viewfile)
            throws IOException, SQLException {
        try {
            JSONObject viewJSON = readViewFile(viewfile);
            final Set<Integer> selectedLayerIds = setupLayers(viewJSON);

            final View view = createView(conn, viewJSON);
            Bundle bundle = view.getBundleByName("mapfull");
            replaceSelectedLayers(bundle, selectedLayerIds);

            final long viewId = viewService.addView(view);
            log.info("Added view from file:", viewfile, "/viewId is:", viewId, "/uuid is:", view.getUuid());
            // update supported SRS for layers after possibly new projection on appsetup/view
            LayerHelper.refreshLayerCapabilities();
            return viewId;
        } catch (Exception ex) {
            log.error(ex, "Unable to insert appsetup! ");
            throw new ServiceRuntimeException("Unable to insert appsetup", ex);
        }
    }

    private static Set<Integer> setupLayers(JSONObject viewJSON)
            throws Exception {

        final JSONArray layers = viewJSON.optJSONArray("selectedLayers");
        final Set<Integer> selectedLayerIds = new HashSet<Integer>();
        if (layers != null) {
            for (int i = 0; i < layers.length(); ++i) {
                final String layerfile = layers.getString(i);
                try {
                    selectedLayerIds.add(LayerHelper.setupLayer(layerfile));
                } catch (Exception ex) {
                    log.warn("Unable to setup layers from:", layerfile);
                    throw ex;
                }
            }
        }
        return selectedLayerIds;
    }

    private static JSONObject readViewFile(final String viewfile)
            throws Exception {
        log.info("/ - /json/views/" + viewfile);
        String json = IOHelper.readString(AppsetupHelper.class.getResourceAsStream("/json/views/" + viewfile));
        JSONObject viewJSON = JSONHelper.createJSONObject(json);
        log.debug(viewJSON);
        return viewJSON;
    }

    private static View createView(Connection conn, final JSONObject viewJSON)
            throws Exception {
        try {
            final View view = new View();
            view.setCreator(ConversionHelper.getLong(viewJSON.optString("creator"), -1));
            view.setIsPublic(viewJSON.optBoolean("public", false));
            view.setOnlyForUuId(viewJSON.optBoolean("onlyUuid", true));
            view.setType(viewJSON.getString("type"));
            view.setName(viewJSON.getString("name"));
            view.setIsDefault(viewJSON.optBoolean("default"));
            final JSONObject oskari = JSONHelper.getJSONObject(viewJSON, "oskari");
            view.setPage(oskari.getString("page"));
            view.setDevelopmentPath(oskari.getString("development_prefix"));
            view.setApplication(oskari.getString("application"));

            setupLayers(viewJSON);

            final JSONArray bundles = viewJSON.getJSONArray("bundles");
            for (int i = 0; i < bundles.length(); ++i) {
                final JSONObject bJSON = bundles.getJSONObject(i);
                final Bundle bundle = BundleHelper.getRegisteredBundle(bJSON.getString("id"), conn);
                if (bundle == null) {
                    throw new Exception("Bundle not registered - id:" + bJSON.getString("id"));
                }
                if (bJSON.has("instance")) {
                    bundle.setBundleinstance(bJSON.getString("instance"));
                }
                if (bJSON.has("config")) {
                    bundle.setConfig(bJSON.getJSONObject("config").toString());
                }
                if (bJSON.has("state")) {
                    bundle.setState(bJSON.getJSONObject("state").toString());
                }

                // set up seq number
                view.addBundle(bundle);
            }
            return view;
        } catch (Exception ex) {
            log.error(ex, "Unable to insert view! ");
        }
        return null;
    }

    private static void replaceSelectedLayers(final Bundle mapfull, final Set<Integer> idSet) {
        if (idSet == null || idSet.isEmpty()) {
            // nothing to setup
            return;
        }
        JSONArray layers = mapfull.getStateJSON().optJSONArray("selectedLayers");
        if (layers == null) {
            layers = new JSONArray();
            JSONHelper.putValue(mapfull.getStateJSON(), "selectedLayers", layers);
        }
        for (Integer id : idSet) {
            layers.put(JSONHelper.createJSONObject("id", id));
        }
    }

}
