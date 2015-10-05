package fi.nls.oskari.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author SMAKINEN
 */
public class ConversionHelperTest {

    @Test
    public void testCount() {
        assertEquals("'moo' should match 2 o's", 2, ConversionHelper.count("moo", "o"));
        assertEquals("'moo' should match 1 oo", 1, ConversionHelper.count("moo", "oo"));
        assertEquals("'moo' should match 1 moo", 1, ConversionHelper.count("moo", "moo"));
        assertEquals("'moo' should match 1 mo", 1, ConversionHelper.count("moo", "mo"));
        assertEquals("'moo' should not match 'kvaak", 0, ConversionHelper.count("moo", "kvaak"));
        assertEquals("'wild**cards*' should match 3 *", 3, ConversionHelper.count("wild**cards*", "*"));
    }

    @Test
    public void testDecapitalize() {
        assertEquals("'M' should become 'm'", "m", ConversionHelper.decapitalize("M"));
        assertEquals("'HTTP' should become 'http'", "http", ConversionHelper.decapitalize("HTTP"));
        assertEquals("'HTTP' should become 'hTTP' with second parameter false", "hTTP", ConversionHelper.decapitalize("HTTP", false));
        assertEquals("'MyAssertion' should become 'myAssertion'", "myAssertion", ConversionHelper.decapitalize("MyAssertion"));

        assertEquals("Empty string should stay empty", "", ConversionHelper.decapitalize(""));
        assertEquals("<null> should stay <null>", null, ConversionHelper.decapitalize(null));
    }

    @Test
    public void testIsOnlyUpperCase() {
        assertTrue("'M' is uppercase only", ConversionHelper.isOnlyUpperCase("M"));
        assertTrue("'MOO' is uppercase only", ConversionHelper.isOnlyUpperCase("MOO"));

        assertFalse("'Moo' is NOT uppercase only", ConversionHelper.isOnlyUpperCase("Moo"));
        assertFalse("<null> is NOT uppercase only", ConversionHelper.isOnlyUpperCase(null));

    }

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

        result = ConversionHelper.getBoolean(null, true);
        assertTrue("Null should yield default value 'true'", result);

        result = ConversionHelper.getBoolean(null, false);
        assertTrue("Null should yield default value 'false'", !result);
    }
}
