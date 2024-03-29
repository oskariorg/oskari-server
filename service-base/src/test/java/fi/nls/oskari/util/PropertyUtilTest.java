package fi.nls.oskari.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author SMAKINEN
 */
public class PropertyUtilTest {

    @Before
    public void setUp() {
        PropertyUtil.clearProperties();
        Properties properties = new Properties();
        try {
            properties.load(PropertyUtilTest.class.getResourceAsStream("PropertyUtilTest.properties"));
            PropertyUtil.addProperties(properties);
        } catch (IOException ioe) {
            fail("Should not throw IOException:\n" + ioe.getStackTrace());
        } catch(DuplicateException de) {
            fail("Should not throw DuplicateException:\n" + de.getMessage());
        }
    }

    @After
    public  void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void test() {
        String workerCount = PropertyUtil.get("workerCount");
        assertEquals("Should get 10", workerCount, "10");

        String redisHostname = PropertyUtil.get("redis.hostname");
        assertTrue("Should get 'localhost'", redisHostname.equals("localhost"));

        String redisPort = PropertyUtil.get("redisPort");
        assertEquals("Should get 6379", redisPort, "6379");
    }

    @Test
    @Ignore("Run this manually if you need to, requires illegal reflective access which might break things")
    public void testEnv() throws Exception {
        String key = "redis.hostname";
        String env = "OSKARI_REDIS_HOSTNAME";
        String localhost = "localhost";
        String localipv4 = "127.0.0.1";

        // redis.hostname=localhost
        assertEquals(localhost, PropertyUtil.get(key));

        setenvHack(env, localipv4);
        assertEquals(localipv4, PropertyUtil.get(key));

        setenvHack(env, null);
        assertEquals(localhost, PropertyUtil.get(key));
    }

    @Test(expected = DuplicateException.class)
    public void testDuplicate() throws Exception {
        PropertyUtil.addProperty("workerCount", "30");
        throw new IllegalStateException("Should not get this far");
    }


    @Test
    public void testIndonesianLanguage() throws Exception {
        try {
            PropertyUtil.addProperty("oskari.locales", "id_ID, en_US", true);
            Locale loc_ID = new Locale("id");
            // https://stackoverflow.com/questions/55955641/correct-locale-for-indonesia-id-id-vs-in-id/55965008
            assertEquals("'id' as lang should translate to 'in' with Locale", loc_ID.getLanguage(), "in");
            assertEquals("getDefaultLanguage() doesn't use Locale", PropertyUtil.getDefaultLanguage(), "id");
            assertFalse("The problem is locale and Props.getDefaultLanguage() don't match", loc_ID.getLanguage().equals(PropertyUtil.getDefaultLanguage()));

            PropertyUtil.addProperty("oskari.locales", "in_ID, en_US", true);
            Locale loc_IN = new Locale("in");
            assertEquals("'in' as lang should remain 'in' with Locale", loc_IN.getLanguage(), "in");
            assertEquals("getDefaultLanguage() doesn't use Locale", PropertyUtil.getDefaultLanguage(), "in");
            assertEquals("Using 'in_ID' for Indonesian works as expected", loc_IN.getLanguage(), PropertyUtil.getDefaultLanguage());
        } finally {
            PropertyUtil.clearProperties();
        }
    }


    @Test
    public void testDuplicateWithOverwrite() throws Exception {
        PropertyUtil.addProperty("workerCount", "30", true);
        assertEquals("Should get 30", PropertyUtil.get("workerCount"), "30");
    }

    @Test
    public void testLocales() throws Exception {
        final String propertyName = "myproperty";
        PropertyUtil.addProperty(propertyName, "for default");
        PropertyUtil.addProperty(propertyName, "for english", Locale.ENGLISH);
        PropertyUtil.addProperty(propertyName, "for germany", Locale.GERMANY);
        assertEquals("Should get 'for default'", PropertyUtil.get(propertyName), "for default");
        assertEquals("Should get 'for english'", PropertyUtil.get(Locale.ENGLISH, propertyName), "for english");
        assertEquals("Should get 'for germany'", PropertyUtil.get(Locale.GERMANY, propertyName), "for germany");
        assertEquals("Should get 'for default'", PropertyUtil.get(Locale.CHINA, propertyName), "for default");
    }

    @Test
    public void testOptional() throws Exception {
        assertEquals("Should get 'localhost'", PropertyUtil.getOptional("redis.hostname"), "localhost");
        assertEquals("Should get '10'", PropertyUtil.getOptional("workerCount"), "10");
        assertEquals("Should get <null>", PropertyUtil.getOptional("non-existing-property"), null);
    }

    @Test
    public void testCommaSeparatedProperty() throws Exception {
        String[] values1 = PropertyUtil.getCommaSeparatedList("commaseparatedNoSpaces");
        String[] values2 = PropertyUtil.getCommaSeparatedList("commaseparatedWithSpaces");
        for(int i = 0 ; i < values1.length; ++i) {
            assertEquals("Values in both arrays should match", values1[i], values2[i]);
        }
        String[] values3 = PropertyUtil.getCommaSeparatedList("non-existing-property");
        assertEquals("Non-existing list should be zero length", values3.length, 0);
    }

    @Test
    public void testEmptyMapProperty() throws Exception {
        Map<String, String> values = PropertyUtil.getMap("non-existing-property");
        assertEquals("Map should be empty", 0, values.size());
    }


    @Test
    public void testMapProperty() throws Exception {
        Map<String, String> values1 = PropertyUtil.getMap("mapProperty1");
        assertEquals("Map should have one key", 1, values1.size());

        Map<String, String> values2 = PropertyUtil.getMap("mapProperty2");
        assertEquals("Map should have two keys", 2, values2.size());
    }

    @Test
    public void testMapPropertyTrimming() throws Exception {
        Map<String, String> values2 = PropertyUtil.getMap("mapProperty2");
        assertEquals("Map should have two keys", 2, values2.size());
        Map<String, String> values3 = PropertyUtil.getMap("mapProperty3");
        assertEquals("Map should have two keys", 2, values3.size());
        for(String key : values2.keySet()) {
            assertTrue("PropertyUtil should trim whitespace and both maps should have same keys", values3.keySet().contains(key));
            final String val2 = values2.get(key);
            final String val3 = values3.get(key);
            assertEquals("Both should have same values with the same key", val2, val3);
        }
    }

    @Test
    public void testLocalizableProperty() throws Exception {
        final String KEY = "my.key";
        final String value = "test value";
        PropertyUtil.addProperty(KEY, value);
        Object o = PropertyUtil.getLocalizableProperty(KEY);
        assertTrue("Single property should return String", o instanceof String);
        assertEquals("Value should match", value, o);
    }

    @Test
    public void testLocalizablePropertyMultipleValues() throws Exception {
        final String KEY = "my.key";
        final String value = "test value";
        PropertyUtil.addProperty(KEY + ".en", value + " en");
        PropertyUtil.addProperty(KEY + ".fi", value + " fi");
        Object o = PropertyUtil.getLocalizableProperty(KEY);
        assertTrue("Single property should return Map", o instanceof Map);
        final Map<String, String> values = (Map<String, String>) o;
        assertEquals("Should have 2 values", 2, values.size());
        assertEquals("English Value should match", value + " en", values.get("en"));
        assertEquals("Finnish Value should match", value + " fi", values.get("fi"));
    }

    @Test
    public void testLocalizablePropertyWithModifier() throws Exception {
        final String KEY = "my.key";
        final String value = "test value";
        PropertyUtil.addProperty(KEY, value);
        PropertyUtil.addProperty(KEY + ".en", value + " en");
        PropertyUtil.addProperty(KEY + ".fi", value + " fi");
        String o = PropertyUtil.getWithOptionalModifier(KEY, "fi", "en");
        assertEquals("English value should match", value + " en", PropertyUtil.getWithOptionalModifier(KEY, "en"));
        assertEquals("Finnish value should match", value + " fi", PropertyUtil.getWithOptionalModifier(KEY, "fi", "en"));
        assertEquals("Missing value should fallback to english", value + " en", PropertyUtil.getWithOptionalModifier(KEY, "sv", "en"));
        assertEquals("Missing value with spanish default should match default key", value, PropertyUtil.getWithOptionalModifier(KEY, "sv", "es"));
    }

    private static void setenvHack(String key, String value) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            if (value == null) {
                env.remove(key);
            } else {
                env.put(key, value);
            }
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            if (value == null) {
                cienv.remove(key);
            } else {
                cienv.put(key, value);
            }
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    if (value == null) {
                        map.remove(key);
                    } else {
                        map.put(key, value);
                    }
                }
            }
        }
    }

}
