package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 26.9.2016.
 */
public class StatisticalIndicatorDataDimensionTest {

    @Test(expected = APIException.class)
    public void testSetValueInvalid()
            throws Exception {
        StatisticalIndicatorDataDimension sel = new StatisticalIndicatorDataDimension("test");
        sel.addAllowedValue("value 1");
        sel.setValue("this is not the value you're looking for");
        fail("Should not reach this");
    }

    @Test
    public void testSetValue()
            throws Exception {
        StatisticalIndicatorDataDimension sel = new StatisticalIndicatorDataDimension("test");
        sel.addAllowedValue("value 1");
        sel.setValue("value 1");
        assertEquals("Values should match", "value 1", sel.getValue());
    }

    private StatisticalIndicatorDataDimension createDimension() {
        StatisticalIndicatorDataDimension sel = new StatisticalIndicatorDataDimension("test");
        sel.addAllowedValue("value 3");
        sel.addAllowedValue("value 1");
        sel.addAllowedValue("value 2");
        return sel;

    }

    @Test
    public void testDefaultValue()
            throws Exception {
        StatisticalIndicatorDataDimension sel = createDimension();
        final String defaultValue = "value 2";
        sel.useDefaultValue(defaultValue);

        assertEquals("Size should remain", 3, sel.getAllowedValues().size());
        assertEquals("Default value should be first", defaultValue, sel.getAllowedValues().get(0).getKey());
        assertEquals("3 should be second", "value 3", sel.getAllowedValues().get(1).getKey());
        assertEquals("1 should be last", "value 1", sel.getAllowedValues().get(2).getKey());
    }

    @Test
    public void testSorting()
            throws Exception {

        StatisticalIndicatorDataDimension sel = createDimension();
        sel.sort(false);
        assertEquals("Size should remain", 3, sel.getAllowedValues().size());
        assertEquals("1 should be first", "value 1", sel.getAllowedValues().get(0).getKey());
        assertEquals("2 should be second", "value 2", sel.getAllowedValues().get(1).getKey());
        assertEquals("3 should be last", "value 3", sel.getAllowedValues().get(2).getKey());

        sel.sort(true);
        assertEquals("Size should remain", 3, sel.getAllowedValues().size());
        assertEquals("3 should be first", "value 3", sel.getAllowedValues().get(0).getKey());
        assertEquals("2 should be second", "value 2", sel.getAllowedValues().get(1).getKey());
        assertEquals("1 should be last", "value 1", sel.getAllowedValues().get(2).getKey());
    }
}