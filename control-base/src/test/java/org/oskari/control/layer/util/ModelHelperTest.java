package org.oskari.control.layer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;
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
        User user = new GuestUser();
        user.addRole(1, "Guest");
        List<OskariLayer> layers = OskariLayerWorker.getLayersForUser(user, false);
        JSONArray oldJSON = new JSONArray(getOldJSON(layers, user));
        JSONArray newJSON = new JSONArray(getNewJSON(layers));
        for (int i = 0; i < oldJSON.length(); i++) {
            JSONObject old = oldJSON.optJSONObject(i);
            JSONObject describe = newJSON.optJSONObject(i);
            assertEquals(old.optString("id"), describe.optString("id"));
            assertEquals(MAPPER.readTree(old.toString()), MAPPER.readTree(describe.toString()));
        }
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
}