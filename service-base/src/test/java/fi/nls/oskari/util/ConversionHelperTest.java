package fi.nls.oskari.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author SMAKINEN
 */
public class ConversionHelperTest {

    @Test
    public void testGetString() {
        String test = "test";
        String result = ConversionHelper.getString(test, "fail");
        assertTrue("Should get 'test'", result.equals(test));

        test = null;
        result = ConversionHelper.getString(test, "fail");
        assertTrue("Should get 'fail'", result.equals("fail"));
    }

    @Test
    public void testGetLong() {
        String test = "20";
        long result = ConversionHelper.getLong(test, 0);
        assertTrue("Should get 20L", result == 20L);

        test = "test";
        result = ConversionHelper.getLong(test, 0);
        assertTrue("Should get 0L", result == 0L);
    }

    @Test
    public void testGetInt() {
        String test = "20";
        long result = ConversionHelper.getInt(test, 0);
        assertTrue("Should get 20", result == 20);

        test = "test";
        result = ConversionHelper.getLong(test, 0);
        assertTrue("Should get 0", result == 0);
    }

    @Test
    public void testGetDouble() {
        String test = "20";
        double result = ConversionHelper.getDouble(test, 0);
        assertTrue("Should get 20.0", result == 20.0);

        test = "test";
        result = ConversionHelper.getDouble(test, 0);
        assertTrue("Should get 0.0", result == 0.0);
    }

    @Test
    public void testGetBoolean() {
        String test = "true";
        boolean result = ConversionHelper.getBoolean(test, false);
        assertTrue("Should get true", result);

        test = "True";
        result = ConversionHelper.getBoolean(test, false);
        assertTrue("Should get true", result);

        test = "test";
        result = ConversionHelper.getBoolean(test, false);
        assertTrue("Should get false", !result);
    }
}
