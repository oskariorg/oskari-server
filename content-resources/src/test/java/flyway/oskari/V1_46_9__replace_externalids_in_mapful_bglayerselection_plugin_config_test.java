package flyway.oskari;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import fi.nls.oskari.util.IOHelper;
import flyway.oskari.V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config.BundleConfig;

public class V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config_test {

    private static final String RESOURCE_FILE_NAME = "mapfull_sample_config.json";

    @Test
    public void testGetBundleConfigsToUpdate() throws JSONException, IOException {
        JSONObject config = new JSONObject(getConfig());

        JSONObject bgPlugin = V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config.findBGPlugin(config.getJSONArray("plugins"));
        JSONArray baseLayers = bgPlugin.getJSONObject("config").getJSONArray("baseLayers");

        assertEquals("base_2", baseLayers.getString(0));
        assertEquals("24", baseLayers.getString(1));
        assertEquals("base_35", baseLayers.getString(2));

        BundleConfig b = new BundleConfig();
        b.viewId = 0;
        b.seqNo = 0;
        b.config = config;
        List<BundleConfig> bundleConfigs = Arrays.asList(b);

        Map<String, Integer> externalIdToLayerId = new HashMap<>();
        externalIdToLayerId.put("base_2", 13);
        externalIdToLayerId.put("base_35", 101);

        List<BundleConfig> toUpdate = V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config.getBundleConfigsToUpdate(bundleConfigs, externalIdToLayerId);
        assertEquals(1, toUpdate.size());

        assertEquals("13", baseLayers.getString(0));
        assertEquals("24", baseLayers.getString(1));
        assertEquals("101", baseLayers.getString(2));

        List<BundleConfig> second = V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config.getBundleConfigsToUpdate(toUpdate, externalIdToLayerId);
        assertEquals("Calling it second time should not change anything, since the baseLayers should now be integers", 0, second.size());
    }

    private String getConfig() throws IOException {
        try (InputStream in = V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config_test.class.getClassLoader().getResourceAsStream(RESOURCE_FILE_NAME)) {
            return new String(IOHelper.readBytes(in), StandardCharsets.UTF_8);
        }
    }

}
