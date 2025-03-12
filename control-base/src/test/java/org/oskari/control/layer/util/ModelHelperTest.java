package org.oskari.control.layer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oskari.control.layer.model.LayerOutput;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ModelHelperTest {

    private final static String LANG = "en";
    private final static String SRS = "EPSG:3857";
    private final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeClass
    public static void init() throws Exception {
        PropertyUtil.addProperty("db.username", "oskari");
        PropertyUtil.addProperty("db.password", "oskari");
    }
    @AfterClass
    public static void teardown() throws Exception {
        PropertyUtil.clearProperties();
    }

    /**
     * Testing how the proposal to render layer JSON differs.
     * Initial testing shows that:
     * orgName -> _dataproviderName (the name is only written for mapfull to get the name for data sources popup (prefixed with _ to emphasize temp nature)
     * created -> created type changed for value from string "2021-01-11T17:37Z" -> long 1610379427259
     * layerName -> layer
     *
     * these are left out if the value is an empty object or matches the default value
     * "realtime": false,
     * "refreshRate": 0,
     * "baseLayerId": -1,
     * "permissions": {},
     * "params": {},
     * "subtitle": "",
     * "styles": [],
     * "metadataUuid": "",
     * "opacity": 100,
     *
     * These are left out because they aren't used by frontend(?)
     * "srs_name": "EPSG:3857", // should be used only when requesting/writing data by the backend
     * "updated": "2021-01-11T18:05Z" // might be used instead of created -> need to check
     *
     *
     * @throws Exception
     */
    @Test
    public void getLayerDetails() throws Exception {
        //OskariLayerService service = new OskariLayerServiceMybatisImpl();
        //List<OskariLayer> layers = service.findAll();
        TestHelper.dbAvailable();
        User user = new GuestUser();
        user.addRole(1, "Guest");
        List<OskariLayer> layers = OskariLayerWorker.getLayersForUser(user, false);
        JSONArray oldJSON = new JSONArray(getOldJSON(layers, user));
        // these are the model changes
        reproduceModelChanges(oldJSON);
        // new model doesn't have empty values or keys with default values to reduce network payload
        removeEmptyAndDefaultValues(oldJSON);
        JSONArray newJSON = new JSONArray(getNewJSON(layers));
        for (int i = 0; i < oldJSON.length(); i++) {
            JSONObject old = oldJSON.optJSONObject(i);
            JSONObject describe = newJSON.optJSONObject(i);
            assertEquals(old.optString("id"), describe.optString("id"));
            removeDates(old);
            removeDates(describe);
            assertTrue("Should match", JSONHelper.isEqual(old, describe));
            //assertEquals(MAPPER.readTree(old.toString()), MAPPER.readTree(describe.toString()));
        }
    }

    private void removeDates(JSONObject layer) {
        layer.remove("created");
        layer.remove("updated");
    }

    private String getOldJSON(List<OskariLayer> layers, User user) throws Exception {
        JSONArray oldJson = OskariLayerWorker.getListOfMapLayers(layers, user, LANG, SRS, false, false).optJSONArray("layers");
        return oldJson.toString(2);
    }

    private String getNewJSON(List<OskariLayer> layers) throws Exception {
        List<LayerOutput> models = layers.stream()
                .map(layer -> ModelHelper.getLayerDetails(layer, LANG, SRS))
                .collect(Collectors.toList());
        return MAPPER.writeValueAsString(models);
    }

    // TODO: These need to be defaulted on client side
    private void removeEmptyAndDefaultValues(JSONArray list) {
        for (int i = 0; i < list.length(); i++) {
            JSONObject layer = list.optJSONObject(i);
            if (layer.optInt("opacity") == 100) {
                layer.remove("opacity");
            }
            if (layer.optInt("minScale") == -1) {
                layer.remove("minScale");
            }
            if (layer.optInt("maxScale") == -1) {
                layer.remove("maxScale");
            }
            if (layer.optInt("refreshRate") == 0) {
                layer.remove("refreshRate");
            }
            if (layer.optInt("baseLayerId") == -1) {
                layer.remove("baseLayerId");
            }
            if (!layer.optBoolean("realtime")) {
                layer.remove("realtime");
            }
            if (!layer.optBoolean("isQueryable")) {
                layer.remove("isQueryable");
            }

            removeEmptyObj(layer, "permissions");
            removeEmptyObj(layer, "params");
            removeEmptyObj(layer, "options");
            removeEmptyObj(layer, "attributes");
            removeEmptyArray(layer, "styles");
            removeEmptyString(layer, "metadataUuid");
            removeEmptyString(layer, "desc");
        }
    }
    // FIXME: these need to be dealt with on server and/or client side
    //  and/or shoveled in other parts of the model JSON (options object perhaps for wmts?)
    private void reproduceModelChanges(JSONArray list) {
        for (int i = 0; i < list.length(); i++) {
            JSONObject layer = list.optJSONObject(i);
            if (layer.optString("tileUrl", null) != null) {
                // tileUrl is available in options.urlTemplate
                layer.remove("tileUrl");
                System.out.println(layer);
                // WMTS layers seem to get {"legend":"","name":"default","title":"default"} in styles array by default that are NOT in the new one
                // also srs is empty but srs_name has the projection as "EPSG:3857"
                // "style":"default"
                // so for WMTS these are added: {srs},{style},{styles},{tileUrl}
            }
            layer.remove("srs_name");
            renameField(layer, "orgName", "_dataproviderName");
            renameField(layer, "layerName", "layer");
            renameField(layer, "subtitle", "desc");
            renameField(layer, "dataproviderId", "dataprovider");
            // created  value "2021-01-11T17:37Z" -> 1610379427259
            // ^ same with updated
        }
    }

    private void renameField(JSONObject layer, String key, String newKey) {

        if (layer.has(key)) {
            Object value = layer.remove(key);
            try {
                layer.put(newKey, value);
            } catch (Exception ignored) {}
        }
    }

    private void removeEmptyObj(JSONObject layer, String key) {

        if (layer.optJSONObject(key) != null && layer.optJSONObject(key).length() == 0) {
            layer.remove(key);
        }
    }
    private void removeEmptyArray(JSONObject layer, String key) {

        if (layer.optJSONArray(key) != null && layer.optJSONArray(key).length() == 0) {
            layer.remove(key);
        }
    }
    private void removeEmptyString(JSONObject layer, String key) {
        if (layer.has(key) && layer.optString(key).isEmpty()) {
            layer.remove(key);
        }
    }
}