package fi.nls.oskari.cache;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore
public class JedisManagerTest {
    private static String key;
    private static String value;
    private static String hKey;

    @BeforeClass
    public static void setUp() {
        JedisManager.connect(10, "localhost", 6379);

        key = "lol";
        value = "lollol";
        hKey = "hlol";

        JedisManager.setex(key, 86400, value);
        JedisManager.setex(key.getBytes(), 86400, value.getBytes());
        JedisManager.hset(hKey, key, value);
    }

    @Test
    public void testGet() {
        String test = JedisManager.get(key);
        assertTrue("Should get 'lollol'", test.equals(value));
    }

    @Test
    public void testByteGet() {
        byte[] testBytes = JedisManager.get(key.getBytes());
        assertTrue("Should get 'lollol'", Arrays.equals(testBytes, value.getBytes()));
    }

    @Test
    public void testSet() {
        Set<String> hSet = JedisManager.hkeys(hKey);
        assertTrue("Should contain key 'lol'", hSet.contains(key));

        String test = JedisManager.hget(hKey, key);
        assertTrue("Should get 'lollol'", test.equals(value));
    }

    @Test
    public void testKeys() {
        Set<String> keys = JedisManager.keys("lo*");
        assertTrue("Should contain key 'lol'", keys.contains(key));
    }

    @Test
    public void testCluster() {
        assertFalse("Null input returns false ", JedisManager.hasClusterProfile(null));
        assertFalse("Empty profile list returns false", JedisManager.hasClusterProfile(new String[]{}));
        assertFalse("Profiles without Redis session returns false", JedisManager.hasClusterProfile(new String[]{"some", "other"}));
        assertTrue("Redis session profile as single returns true", JedisManager.hasClusterProfile(new String[]{JedisManager.CLUSTERED_ENV_PROFILE}));
        assertTrue("Redis session profile in list returns true", JedisManager.hasClusterProfile(new String[]{"some", JedisManager.CLUSTERED_ENV_PROFILE, "other"}));
    }
}
