package fi.nls.oskari.utils;

import fi.nls.oskari.cache.JedisManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper for checking if we have correct proxy settings available. Tests can use
 * assume(TestHelper.canDoHttp()) to ignore a test if http is not available.
 */
public class TestHelper {

    public static final String TEST_URL = "http://httpbin.org/ip";
    private static enum STATUS {
        NONE,
        ENABLED,
        DISABLED;

        public static STATUS getEnabled(boolean enabled) {
            return enabled ? ENABLED : DISABLED;
        }
    };
    private static STATUS networkingStatus = STATUS.NONE;
    private static STATUS redisStatus = STATUS.NONE;

    /**
     * Tests network connectivity by polling #TEST_URL with 3 second timeout.
     * @return
     */
    public static boolean canDoHttp() {
        if(networkingStatus.equals(STATUS.NONE)) {
            // check status if not yet checked
            try {
                URL url = new URL(TEST_URL);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setReadTimeout(3000);
                urlConn.connect();

                networkingStatus = STATUS.getEnabled(HttpURLConnection.HTTP_OK == urlConn.getResponseCode());
            } catch (IOException e) {
                System.err.println("Couldn't connect to test URL (" + TEST_URL + "). Skipping http tests.");
                networkingStatus = STATUS.DISABLED;
            }
        }
        return networkingStatus.equals(STATUS.ENABLED);
    }

    public static boolean redisAvailable() {
        if(redisStatus.equals(STATUS.NONE)) {
            final String testKey = "testing";
            final String testValue = "availability";

            final String msg = JedisManager.setex(testKey, 10, testValue);
            if(msg == null) {
                redisStatus = STATUS.DISABLED;
            }

            redisStatus = STATUS.getEnabled(testValue.equals(JedisManager.get(testKey)));
        }
        return redisStatus.equals(STATUS.ENABLED);
    }
}
