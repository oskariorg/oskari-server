package org.oskari.helpers;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LayerHelperTest {

    @Test
    public void readLayerFile() throws Exception {
        // direct from root as the new way with no expectation
        Assertions.assertEquals("layers", readFileAsJSON("/json/layers/layer.json").optString("name"));
        Assertions.assertEquals("root", readFileAsJSON("/test.json").optString("name"));

        // for older "assumed path" /json/layers
        JSONObject layer = readFileAsJSON("layer.json");
        Assertions.assertEquals("layers", layer.optString("name"));
    }

    private JSONObject readFileAsJSON(String file) throws Exception {
        return new JSONObject(LayerHelper.readLayerFile(file));
    }
}