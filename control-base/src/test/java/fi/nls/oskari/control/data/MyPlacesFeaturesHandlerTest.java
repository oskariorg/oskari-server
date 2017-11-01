
package fi.nls.oskari.control.data;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.GeoServerRequestBuilder;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MyPlacesFeaturesHandlerTest {

    @Test
    public void MyPlacesFeaturesHandler() throws Exception {
        try {

            InputStream inputStream = getClass().getResourceAsStream("insert_payload.json");
            String payload = IOUtils.toString(inputStream, "UTF-8");
            GeoServerRequestBuilder handler = new GeoServerRequestBuilder();
            handler.buildFeaturesInsert(payload);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}