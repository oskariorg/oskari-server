package flyway.oskari;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import fi.nls.oskari.util.IOHelper;

public class V1_46_10__replace_externalids_in_mapful_layerselection_plugin_config_test {

    private static final String RESOURCE_FILE_NAME = "mapfull_sample_config_layer_selection_plugin.json";

    @Test
    public void testUpdateConfig() throws JSONException, IOException {
        JSONObject config = new JSONObject(getConfig());
        JSONArray plugins = config.getJSONArray("plugins");
        JSONObject plugin = V1_46_10__replace_externalids_in_mapful_layerselection_plugin_config.findPlugin(plugins);
        JSONObject pluginConfig = plugin.getJSONObject("config");

        String defaultBaseLayer = pluginConfig.getString("defaultBaseLayer");
        assertEquals("base_35", defaultBaseLayer);

        JSONArray baseLayers = pluginConfig.getJSONArray("baseLayers");
        assertEquals(2, baseLayers.length());
        assertEquals("base_35", baseLayers.getString(0));
        assertEquals("24", baseLayers.getString(1));

        Map<String, Integer> externalIdToLayerId = new HashMap<>();
        externalIdToLayerId.put("base_35", 1337);

        boolean updated = V1_46_10__replace_externalids_in_mapful_layerselection_plugin_config.updateConfig(config, externalIdToLayerId);

        assertEquals(true, updated);

        defaultBaseLayer = pluginConfig.getString("defaultBaseLayer");
        assertEquals("1337", defaultBaseLayer);

        assertEquals(2, baseLayers.length());
        assertEquals("1337", baseLayers.getString(0));
        assertEquals("24", baseLayers.getString(1));
    }

    private String getConfig() throws IOException {
        try (InputStream in = V1_46_10__replace_externalids_in_mapful_layerselection_plugin_config_test.class
                .getClassLoader()
                .getResourceAsStream(RESOURCE_FILE_NAME)) {
            return new String(IOHelper.readBytes(in), StandardCharsets.UTF_8);
        }
    }

}
