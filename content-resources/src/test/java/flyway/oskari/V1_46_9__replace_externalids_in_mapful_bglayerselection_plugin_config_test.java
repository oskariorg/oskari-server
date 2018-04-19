package flyway.oskari;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import fi.nls.oskari.util.IOHelper;

public class V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config_test {

    private static final String RESOURCE_FILE_NAME = "mapfull_sample_config.json";

    private static final String SELECTED_LAYERS_SAMPLE = "{\"selectedLayers\":[{\"id\":\"base_35\",\"opacity\":100},{\"id\":99,\"opacity\":100},{\"id\":90,\"opacity\":100},{\"id\":\"myplaces_7509\",\"opacity\":100}],\"zoom\":10,\"east\":659796,\"north\":7025206}";
    
    @Test
    public void testUpdateConfig() throws JSONException, IOException {
        JSONObject config = new JSONObject(getConfig());
        
        JSONObject bgPlugin = V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config.findBGPlugin(config.getJSONArray("plugins"));
        JSONArray baseLayers = bgPlugin.getJSONObject("config").getJSONArray("baseLayers");

        assertEquals("base_2", baseLayers.getString(0));
        assertEquals("24", baseLayers.getString(1));
        assertEquals("base_35", baseLayers.getString(2));

        Map<String, Integer> externalIdToLayerId = new HashMap<>();
        externalIdToLayerId.put("base_2", 13);
        externalIdToLayerId.put("base_35", 101);

        boolean updated = V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config.updateConfig(config, externalIdToLayerId);
        assertEquals(true, updated);
        assertEquals("13", baseLayers.getString(0));
        assertEquals("24", baseLayers.getString(1));
        assertEquals("101", baseLayers.getString(2));
    }

    private String getConfig() throws IOException {
        try (InputStream in = V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config_test.class.getClassLoader().getResourceAsStream(RESOURCE_FILE_NAME)) {
            return new String(IOHelper.readBytes(in), StandardCharsets.UTF_8);
        }
    }
    
    @Test
    public void testUpdateState() throws JSONException, IOException {
        JSONObject state = new JSONObject(SELECTED_LAYERS_SAMPLE);
        
        JSONArray selectedLayers = state.getJSONArray("selectedLayers");
        assertEquals("base_35", selectedLayers.getJSONObject(0).getString("id"));
        assertEquals(99, selectedLayers.getJSONObject(1).getInt("id"));
        assertEquals(90, selectedLayers.getJSONObject(2).getInt("id"));
        assertEquals("myplaces_7509", selectedLayers.getJSONObject(3).getString("id"));
        
        Map<String, Integer> externalIdToLayerId = Collections.singletonMap("base_35", 1000);
        boolean updated = V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config.updateState(state, externalIdToLayerId);
        
        assertEquals(true, updated);
        assertEquals(1000, selectedLayers.getJSONObject(0).getInt("id"));
        assertEquals(99, selectedLayers.getJSONObject(1).getInt("id"));
        assertEquals(90, selectedLayers.getJSONObject(2).getInt("id"));
        assertEquals("myplaces_7509", selectedLayers.getJSONObject(3).getString("id"));
    }

}
