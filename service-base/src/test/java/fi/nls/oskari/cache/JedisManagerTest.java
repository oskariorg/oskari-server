package fi.nls.oskari.cache;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

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
    public void testPubSub() {
        final JedisSubscriber sub = new JedisSubscriber();
        JedisManager.subscribe(sub, "test");

        Long res = JedisManager.publish("test", "test message");
        assertTrue(res == 1);
    }
}
