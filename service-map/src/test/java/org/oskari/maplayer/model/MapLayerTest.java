package org.oskari.maplayer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MapLayerTest {

    @Test
    public void testLayerJSONreadingAndWriting() throws Exception {
        final String layerJSON = IOHelper.readString(getClass().getResourceAsStream("MapLayer.json"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MapLayer layer = mapper.readValue(layerJSON, MapLayer.class);

        final String output = mapper.writeValueAsString(layer);
        final String expectedOutput = IOHelper.readString(getClass().getResourceAsStream("MapLayer-expected.json"));
        Assertions.assertTrue(JSONHelper.isEqual(new JSONObject(expectedOutput), new JSONObject(output)), "Serializing should match expected output");
    }
}