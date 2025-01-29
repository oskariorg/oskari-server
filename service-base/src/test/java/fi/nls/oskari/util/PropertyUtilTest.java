package fi.nls.oskari.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author SMAKINEN
 */
public class PropertyUtilTest {

    @BeforeEach
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

    @AfterEach
    public  void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void test() {
        String workerCount = PropertyUtil.get("workerCount");
        assertEquals( workerCount, "10", "Should get 10");

        String redisHostname = PropertyUtil.get("redis.hostname");
        assertTrue(redisHostname.equals("localhost"), "Should get 'localhost'");

        String redisPort = PropertyUtil.get("redisPort");
        assertEquals( redisPort, "6379", "Should get 6379");
    }

    @Test
    @Disabled("Run this manually if you need to, requires illegal reflective access which might break things")
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

    @Test()
    public void testDuplicate() throws Exception {
        Assertions.assertThrows(DuplicateException.class, () -> {
            PropertyUtil.addProperty("workerCount", "30");
        });
    }


    @Test
    public void testIndonesianLanguage() throws Exception {
        try {
            PropertyUtil.addProperty("oskari.locales", "id_ID, en_US", true);
            Locale loc_ID = new Locale("id");
            // https://stackoverflow.com/questions/55955641/correct-locale-for-indonesia-id-id-vs-in-id/55965008
            assertEquals( loc_ID.getLanguage(), "id", "'id' as lang should translate to 'id' with Locale on Java 17");
            assertEquals( PropertyUtil.getDefaultLanguage(), "id", "getDefaultLanguage() doesn't use Locale");
            assertTrue(loc_ID.getLanguage().equals(PropertyUtil.getDefaultLanguage()), "Locale and Props.getDefaultLanguage() match with Java 17");

            PropertyUtil.addProperty("oskari.locales", "in_ID, en_US", true);
            Locale loc_IN = new Locale("in");
            assertEquals( loc_IN.getLanguage(), "id", "'in' as lang changes to 'id' with Locale");
            assertEquals( PropertyUtil.getDefaultLanguage(), "in", "getDefaultLanguage() doesn't use Locale");
        } finally {
            PropertyUtil.clearProperties();
        }
    }


    @Test
    public void testDuplicateWithOverwrite() throws Exception {
        PropertyUtil.addProperty("workerCount", "30", true);
        assertEquals( PropertyUtil.get("workerCount"), "30", "Should get 30");
    }

    @Test
    public void testLocales() throws Exception {
        final String propertyName = "myproperty";
        PropertyUtil.addProperty(propertyName, "for default");
        PropertyUtil.addProperty(propertyName, "for english", Locale.ENGLISH);
        PropertyUtil.addProperty(propertyName, "for germany", Locale.GERMANY);
        assertEquals( PropertyUtil.get(propertyName), "for default", "Should get 'for default'");
        assertEquals(PropertyUtil.get(Locale.ENGLISH, propertyName), "for english", "Should get 'for english'");
        assertEquals( PropertyUtil.get(Locale.GERMANY, propertyName), "for germany", "Should get 'for germany'");
        assertEquals(PropertyUtil.get(Locale.CHINA, propertyName), "for default", "Should get 'for default'");
    }

    @Test
    public void testOptional() throws Exception {
        assertEquals( PropertyUtil.getOptional("redis.hostname"), "localhost", "Should get 'localhost'");
        assertEquals(PropertyUtil.getOptional("workerCount"), "10", "Should get '10'");
        assertEquals(PropertyUtil.getOptional("non-existing-property"), (Supplier<String>) null, "Should get <null>");
    }

    @Test
    public void testCommaSeparatedProperty() throws Exception {
        String[] values1 = PropertyUtil.getCommaSeparatedList("commaseparatedNoSpaces");
        String[] values2 = PropertyUtil.getCommaSeparatedList("commaseparatedWithSpaces");
        for(int i = 0 ; i < values1.length; ++i) {
            assertEquals( values1[i], values2[i], "Values in both arrays should match");
        }
        String[] values3 = PropertyUtil.getCommaSeparatedList("non-existing-property");
        assertEquals(0, values3.length, "Non-existing list should be zero length");
    }

    @Test
    public void testEmptyMapProperty() throws Exception {
        Map<String, String> values = PropertyUtil.getMap("non-existing-property");
        assertEquals(0, values.size(), "Map should be empty");
    }


    @Test
    public void testMapProperty() throws Exception {
        Map<String, String> values1 = PropertyUtil.getMap("mapProperty1");
        assertEquals(1, values1.size(), "Map should have one key");

        Map<String, String> values2 = PropertyUtil.getMap("mapProperty2");
        assertEquals(2, values2.size(), "Map should have two keys");
    }

    @Test
    public void testMapPropertyTrimming() throws Exception {
        Map<String, String> values2 = PropertyUtil.getMap("mapProperty2");
        assertEquals(2, values2.size(), "Map should have two keys");
        Map<String, String> values3 = PropertyUtil.getMap("mapProperty3");
        assertEquals(2, values3.size(), "Map should have two keys");
        for(String key : values2.keySet()) {
            assertTrue(values3.keySet().contains(key), "PropertyUtil should trim whitespace and both maps should have same keys");
            final String val2 = values2.get(key);
            final String val3 = values3.get(key);
            assertEquals(val2, val3, "Both should have same values with the same key");
        }
    }

    @Test
    public void testLocalizableProperty() throws Exception {
        final String KEY = "my.key";
        final String value = "test value";
        PropertyUtil.addProperty(KEY, value);
        Object o = PropertyUtil.getLocalizableProperty(KEY);
        assertTrue( o instanceof String, "Single property should return String");
        assertEquals(value, o, "Value should match");
    }

    @Test
    public void testLocalizablePropertyMultipleValues() throws Exception {
        final String KEY = "my.key";
        final String value = "test value";
        PropertyUtil.addProperty(KEY + ".en", value + " en");
        PropertyUtil.addProperty(KEY + ".fi", value + " fi");
        Object o = PropertyUtil.getLocalizableProperty(KEY);
        assertTrue(o instanceof Map, "Single property should return Map");
        final Map<String, String> values = (Map<String, String>) o;
        assertEquals(2, values.size(), "Should have 2 values" );
        assertEquals( value + " en", values.get("en"), "English Value should match");
        assertEquals( value + " fi", values.get("fi"), "Finnish Value should match");
    }

    @Test
    public void testLocalizablePropertyWithModifier() throws Exception {
        final String KEY = "my.key";
        final String value = "test value";
        PropertyUtil.addProperty(KEY, value);
        PropertyUtil.addProperty(KEY + ".en", value + " en");
        PropertyUtil.addProperty(KEY + ".fi", value + " fi");
        String o = PropertyUtil.getWithOptionalModifier(KEY, "fi", "en");
        assertEquals( value + " en", PropertyUtil.getWithOptionalModifier(KEY, "en"), "English value should match");
        assertEquals( value + " fi", PropertyUtil.getWithOptionalModifier(KEY, "fi", "en"), "Finnish value should match");
        assertEquals( value + " en", PropertyUtil.getWithOptionalModifier(KEY, "sv", "en"), "Missing value should fallback to english");
        assertEquals(value, PropertyUtil.getWithOptionalModifier(KEY, "sv", "es"), "Missing value with spanish default should match default key");
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
