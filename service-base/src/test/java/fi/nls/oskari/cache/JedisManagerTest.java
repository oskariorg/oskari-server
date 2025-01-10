package fi.nls.oskari.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Disabled
public class JedisManagerTest {
    private static String key;
    private static String value;
    private static String hKey;

    @BeforeAll
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
        Assertions.assertTrue(test.equals(value), "Should get 'lollol'");
    }

    @Test
    public void testByteGet() {
        byte[] testBytes = JedisManager.get(key.getBytes());
        Assertions.assertTrue(Arrays.equals(testBytes, value.getBytes()), "Should get 'lollol'");
    }

    @Test
    public void testSet() {
        Set<String> hSet = JedisManager.hkeys(hKey);
        Assertions.assertTrue(hSet.contains(key), "Should contain key 'lol'");

        String test = JedisManager.hget(hKey, key);
        Assertions.assertTrue(test.equals(value), "Should get 'lollol'");
    }

    @Test
    public void testKeys() {
        Set<String> keys = JedisManager.keys("lo*");
        Assertions.assertTrue(keys.contains(key), "Should contain key 'lol'");
    }

    @Test
    public void testCluster() {
        Assertions.assertFalse(JedisManager.hasClusterProfile(null), "Null input returns false ");
        Assertions.assertFalse(JedisManager.hasClusterProfile(new String[]{}), "Empty profile list returns false");
        Assertions.assertFalse(JedisManager.hasClusterProfile(new String[]{"some", "other"}), "Profiles without Redis session returns false");
        Assertions.assertTrue(JedisManager.hasClusterProfile(new String[]{JedisManager.CLUSTERED_ENV_PROFILE}), "Redis session profile as single returns true");
        Assertions.assertTrue(JedisManager.hasClusterProfile(new String[]{"some", JedisManager.CLUSTERED_ENV_PROFILE, "other"}), "Redis session profile in list returns true");
    }
}
