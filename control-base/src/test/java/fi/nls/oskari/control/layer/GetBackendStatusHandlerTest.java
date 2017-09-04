package fi.nls.oskari.control.layer;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.domain.map.BackendStatus;

public class GetBackendStatusHandlerTest {

    @Test
    public void testJSONSerialization() throws JsonProcessingException {
        BackendStatus foo = new BackendStatus(1, "OK", null, "http://foo.bar");
        BackendStatus bar = new BackendStatus(2, "ERROR", "Unknown service", null);

        List<BackendStatus> arr = new ArrayList<>();
        arr.add(foo);
        arr.add(bar);

        ObjectMapper om = new ObjectMapper();
        byte[] jsonB = GetBackendStatusHandler.serialize(om, arr);
        String json = new String(jsonB, StandardCharsets.UTF_8);

        String expected = "{'backendstatus':["
                + "{'maplayer_id':1,'status':'OK','statusjson':null,'infourl':'http://foo.bar','ts':null},"
                + "{'maplayer_id':2,'status':'ERROR','statusjson':'Unknown service','infourl':null,'ts':null}"
                + "]}";
        expected = expected.replace('\'', '"');
        assertEquals(expected, json);

    }

}
