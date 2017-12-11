package fi.nls.oskari.control.myplaces.handler;

import fi.nls.oskari.myplaces.util.GeoServerRequestBuilder;
import fi.nls.oskari.util.IOHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.axiom.om.OMElement;

public class MyPlacesFeaturesHandlerTest {

    private static final String INSERT = "insert_payload.json";

    @Test
    public void MyPlacesFeaturesHandler() throws Exception {
        String payload = getInput(INSERT);
        String uuid = "fdsa-fdsa-fdsa-fdsa-fdsa";
        JSONObject request = new JSONObject(payload);
        JSONArray features = request.getJSONArray("features");
        GeoServerRequestBuilder handler = new GeoServerRequestBuilder();
        OMElement insertRequest = handler.insertFeatures(uuid, features);
    }

    private String getInput(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            return new String(IOHelper.readBytes(in), StandardCharsets.UTF_8);
        }
    }

}