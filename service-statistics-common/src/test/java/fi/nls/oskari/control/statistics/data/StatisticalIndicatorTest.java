package fi.nls.oskari.control.statistics.data;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class StatisticalIndicatorTest {

    @BeforeAll
    public static void init() throws Exception {
        PropertyUtil.addProperty("oskari.locales", "fi_FI,en_US,sv_SE", true);
    }

    @AfterAll
    public static void teardown() throws Exception {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testSetValueInvalid() {
        StatisticalIndicator ind = new StatisticalIndicator();
        ind.addName("sv", "testing");
        Assertions.assertEquals("testing", ind.getName("en"), "Should return sv as only value with en request");
        ind.addName("fi", "testing fi");
        Assertions.assertEquals("testing fi", ind.getName("en"), "Should return fi (default locale) with en request");
        ind.addName("en", "testing en");
        Assertions.assertEquals("testing en", ind.getName("en"), "Should return direct match");
    }
}
