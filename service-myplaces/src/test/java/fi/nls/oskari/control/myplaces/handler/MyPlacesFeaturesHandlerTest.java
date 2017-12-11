package fi.nls.oskari.control.myplaces.handler;

import fi.nls.oskari.util.IOHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MyPlacesFeaturesHandlerTest {

    private static final String INSERT = "insert_payload.json";

    @Test
    public void MyPlacesFeaturesHandler() throws Exception {
        String payload = getInput(INSERT);
        JSONObject request = new JSONObject(payload);
        JSONArray features = request.getJSONArray("features");
    }

    private String getInput(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            return new String(IOHelper.readBytes(in), StandardCharsets.UTF_8);
        }
    }

}