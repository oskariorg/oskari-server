package fi.nls.oskari.util;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Simple test for params parsing
 */
public class RequestHelperTest {

    private final String PREFIX = "name_";
    @Test
    public void testParsePrefixedParamsMap() throws Exception {

        HttpServletRequest req = mock(HttpServletRequest.class);
        Map<String, String> input = new HashMap<String, String>();
        input.put(PREFIX + "fi", "test suomi");
        input.put(PREFIX + "en", "test english");
        for(String key : input.keySet()) {
            doReturn(input.get(key)).when(req).getParameter(key);
        }
        doReturn(Collections.enumeration(input.keySet())).when(req).getParameterNames();

        final Map<String, String> map = RequestHelper.parsePrefixedParamsMap (req, PREFIX);
        assertEquals("Should have two entries", 2, map.size());
        assertEquals("Should parse finnish", "test suomi", map.get("fi"));
        assertEquals("Should parse english", "test english", map.get("en"));

    }
}
