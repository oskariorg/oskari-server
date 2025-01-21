package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by SMAKINEN on 26.9.2016.
 */
public class StatisticalIndicatorDataDimensionTest {

    @Test
    public void testSetValueInvalid()
            throws Exception {

        assertThrows(APIException.class, () -> {
            StatisticalIndicatorDataDimension sel = new StatisticalIndicatorDataDimension("test");
            sel.addAllowedValue("value 1");
            sel.setValue("this is not the value you're looking for");
        });
    }

    @Test
    public void testSetValue()
            throws Exception {
        StatisticalIndicatorDataDimension sel = new StatisticalIndicatorDataDimension("test");
        sel.addAllowedValue("value 1");
        sel.setValue("value 1");
        assertEquals( "value 1", sel.getValue(), "Values should match");
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

        assertEquals(3, sel.getAllowedValues().size(), "Size should remain");
        assertEquals( defaultValue, sel.getAllowedValues().get(0).getKey(), "Default value should be first");
        assertEquals( "value 3", sel.getAllowedValues().get(1).getKey(), "3 should be second");
        assertEquals("value 1", sel.getAllowedValues().get(2).getKey(), "1 should be last");
    }

    @Test
    public void testSorting()
            throws Exception {

        StatisticalIndicatorDataDimension sel = createDimension();
        sel.sort(false);
        assertEquals(3, sel.getAllowedValues().size(), "Size should remain");
        assertEquals("value 1", sel.getAllowedValues().get(0).getKey(), "1 should be first");
        assertEquals("value 2", sel.getAllowedValues().get(1).getKey(), "2 should be second");
        assertEquals("value 3", sel.getAllowedValues().get(2).getKey(), "3 should be last");

        sel.sort(true);
        assertEquals(3, sel.getAllowedValues().size(), "Size should remain");
        assertEquals("value 3", sel.getAllowedValues().get(0).getKey(), "3 should be first");
        assertEquals("value 2", sel.getAllowedValues().get(1).getKey(), "2 should be second");
        assertEquals("value 1", sel.getAllowedValues().get(2).getKey(), "1 should be last");
    }
}