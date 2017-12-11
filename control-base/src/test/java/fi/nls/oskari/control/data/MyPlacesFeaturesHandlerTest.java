
package fi.nls.oskari.control.data;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.GeoServerRequestBuilder;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.axiom.om.OMElement;

public class MyPlacesFeaturesHandlerTest {

    @Test
    public void MyPlacesFeaturesHandler() throws Exception {
        try {
            InputStream inputStream = getClass().getResourceAsStream("insert_payload.json");
            String uuid = "fdsa-fdsa-fdsa-fdsa-fdsa";
            JSONObject payload = new JSONObject (IOUtils.toString(inputStream, "UTF-8"));
            JSONArray features  = payload.getJSONArray("features");
            GeoServerRequestBuilder handler = new GeoServerRequestBuilder();
            OMElement request = handler.buildFeaturesInsert(uuid, features);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}