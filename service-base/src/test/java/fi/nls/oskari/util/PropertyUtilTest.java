package fi.nls.oskari.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
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

        String redisHostname = PropertyUtil.get("redisHostname");
        assertTrue("Should get 'localhost'", redisHostname.equals("localhost"));

        String redisPort = PropertyUtil.get("redisPort");
        assertEquals("Should get 6379", redisPort, "6379");
    }

    @Test(expected = DuplicateException.class)
    public void testDuplicate() throws Exception {
        PropertyUtil.addProperty("workerCount", "30");
        throw new IllegalStateException("Should not get this far");
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
        assertEquals("Should get 'localhost'", PropertyUtil.getOptional("redisHostname"), "localhost");
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

}
