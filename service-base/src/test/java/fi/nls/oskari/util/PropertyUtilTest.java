package fi.nls.oskari.util;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author SMAKINEN
 */
public class PropertyUtilTest {
    @BeforeClass
    public static void setUp() {
        Properties properties = new Properties();
        try {
            properties.load(PropertyUtilTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
        } catch (IOException ioe) {
            fail("Should not throw IOException:\n" + ioe.getStackTrace());
        } catch(DuplicateException de) {
            fail("Should not throw DuplicateException:\n" + de.getMessage());
        }
    }

    @Test
    public void test() {
        int workerCount = ConversionHelper.getInt(PropertyUtil.get("workerCount"), 0);
        assertTrue("Should get 10", workerCount == 10);

        String redisHostname = PropertyUtil.get("redisHostname");
        assertTrue("Should get 'localhost'", redisHostname.equals("localhost"));

        int redisPort = ConversionHelper.getInt(PropertyUtil.get("redisPort"), 0);
        assertTrue("Should get 6379", redisPort == 6379);
    }
}
