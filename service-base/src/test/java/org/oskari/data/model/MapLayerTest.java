package org.oskari.data.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.util.IOHelper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class MapLayerTest {

    @Test
    public void setupLayer() throws IOException {
        final String layerJSON = IOHelper.readString(getClass().getResourceAsStream("MapLayer.json"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MapLayer layer = mapper.readValue(layerJSON, MapLayer.class);
        System.out.println(mapper.writeValueAsString(layer));
    }
}