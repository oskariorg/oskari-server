package fi.nls.oskari.control.statistics.data;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatisticalIndicatorTest {

    @BeforeClass
    public static void init() throws Exception {
        PropertyUtil.addProperty("oskari.locales", "fi_FI,en_US,sv_SE", true);
    }

    @AfterClass
    public static void teardown() throws Exception {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testSetValueInvalid() {
        StatisticalIndicator ind = new StatisticalIndicator();
        ind.addName("sv", "testing");
        assertEquals("Should return sv as only value with en request", "testing", ind.getName("en"));
        ind.addName("fi", "testing fi");
        assertEquals("Should return fi (default locale) with en request", "testing fi", ind.getName("en"));
        ind.addName("en", "testing en");
        assertEquals("Should return direct match", "testing en", ind.getName("en"));
    }
}
