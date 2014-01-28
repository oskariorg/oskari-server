package fi.nls.oskari.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper for checking if we have correct proxy settings available. Tests can use
 * assume(TestHelper.canDoHttp()) to ignore a test if http is not available.
 *
 * TODO: add similar isRedisAvailable() method.
 */
public class TestHelper {

    public static final String TEST_URL = "http://httpbin.org/ip";
    private static enum NETWORKING_STATUS {
        NONE,
        ENABLED,
        DISABLED;

        public static NETWORKING_STATUS getEnabled(boolean enabled) {
            return enabled ? ENABLED : DISABLED;
        }
    };
    private static NETWORKING_STATUS status = NETWORKING_STATUS.NONE;

    /**
     * Tests network connectivity by polling #TEST_URL with 3 second timeout.
     * @return
     */
    public static boolean canDoHttp() {
        if(status.equals(NETWORKING_STATUS.NONE)) {
            // check status if not yet checked
            try {
                URL url = new URL(TEST_URL);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setReadTimeout(3000);
                urlConn.connect();

                status = NETWORKING_STATUS.getEnabled(HttpURLConnection.HTTP_OK == urlConn.getResponseCode());
            } catch (IOException e) {
                System.err.println("Couldn't connect to test URL (" + TEST_URL + "). Skipping http tests.");
                status = NETWORKING_STATUS.DISABLED;
            }
        }
        return status.equals(NETWORKING_STATUS.ENABLED);
    }
}
