package fi.nls.oskari.control.statistics.plugins;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 26.9.2016.
 */
public class StatisticalIndicatorSelectorTest {

    @Test(expected = APIException.class)
    public void testSetValueInvalid()
            throws Exception {
        StatisticalIndicatorSelector sel = new StatisticalIndicatorSelector("test");
        sel.addAllowedValue("value 1");
        sel.setValue("this is not the value you're looking for");
        fail("Should not reach this");
    }

    @Test
    public void testSetValue()
            throws Exception {
        StatisticalIndicatorSelector sel = new StatisticalIndicatorSelector("test");
        sel.addAllowedValue("value 1");
        sel.setValue("value 1");
        assertEquals("Values should match", "value 1", sel.getValue());
    }
}