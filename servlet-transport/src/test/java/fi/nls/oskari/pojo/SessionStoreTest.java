package fi.nls.oskari.pojo;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.TestHelper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class SessionStoreTest {

    @Test
    public void testJSON() throws IOException {
        // check that we have redis connectivity (redis server running)
        String testJSON = IOHelper.readString(getClass().getResourceAsStream("sessionstore-valid.json"));
        assumeTrue(TestHelper.redisAvailable());
        final SessionStore store = SessionStore.setJSON(testJSON);
        final String jsonResult = store.getAsJSON();
        assertTrue("the logical JSON structure should be identical",
                JSONHelper.isEqual(
                        JSONHelper.createJSONObject(testJSON),
                        JSONHelper.createJSONObject(jsonResult)));
    }

    @Test(expected = IOException.class)
    public void testJSONIOException() throws IOException {
        String testJSON = IOHelper.readString(getClass().getResourceAsStream("sessionstore-fail.json"));
        SessionStore.setJSON(testJSON);
    }

}
