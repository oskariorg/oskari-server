package fi.nls.oskari.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author SMAKINEN
 */
public class ConversionHelperTest {

    @Test
    public void testCount() {
        Assertions.assertEquals(2, ConversionHelper.count("moo", "o"), "'moo' should match 2 o's");
        Assertions.assertEquals(1, ConversionHelper.count("moo", "oo"), "'moo' should match 1 oo");
        Assertions.assertEquals(1, ConversionHelper.count("moo", "moo"), "'moo' should match 1 moo");
        Assertions.assertEquals(1, ConversionHelper.count("moo", "mo"), "'moo' should match 1 mo");
        Assertions.assertEquals(0, ConversionHelper.count("moo", "kvaak"), "'moo' should not match 'kvaak");
        Assertions.assertEquals(3, ConversionHelper.count("wild**cards*", "*"), "'wild**cards*' should match 3 *");
    }

    @Test
    public void testDecapitalize() {
        Assertions.assertEquals("m", ConversionHelper.decapitalize("M"), "'M' should become 'm'");
        Assertions.assertEquals("http", ConversionHelper.decapitalize("HTTP"), "'HTTP' should become 'http'");
        Assertions.assertEquals("hTTP", ConversionHelper.decapitalize("HTTP", false), "'HTTP' should become 'hTTP' with second parameter false");
        Assertions.assertEquals("myAssertion", ConversionHelper.decapitalize("MyAssertion"), "'MyAssertion' should become 'myAssertion'");

        Assertions.assertEquals("", ConversionHelper.decapitalize(""), "Empty string should stay empty");
        Assertions.assertEquals(null, ConversionHelper.decapitalize(null), "<null> should stay <null>");
    }

    @Test
    public void testIsOnlyUpperCase() {
        Assertions.assertTrue(ConversionHelper.isOnlyUpperCase("M"), "'M' is uppercase only");
        Assertions.assertTrue(ConversionHelper.isOnlyUpperCase("MOO"), "'MOO' is uppercase only");

        Assertions.assertFalse(ConversionHelper.isOnlyUpperCase("Moo"), "'Moo' is NOT uppercase only");
        Assertions.assertFalse(ConversionHelper.isOnlyUpperCase(null), "<null> is NOT uppercase only");

    }

    @Test
    public void testGetString() {
        String test = "test";
        String result = ConversionHelper.getString(test, "fail");
        Assertions.assertTrue(result.equals(test), "Should get 'test'");

        test = null;
        result = ConversionHelper.getString(test, "fail");
        Assertions.assertTrue(result.equals("fail"), "Should get 'fail'");
    }

    @Test
    public void testGetLong() {
        String test = "20";
        long result = ConversionHelper.getLong(test, 0);
        Assertions.assertTrue(result == 20L, "Should get 20L");

        test = "test";
        result = ConversionHelper.getLong(test, 0);
        Assertions.assertTrue(result == 0L, "Should get 0L");
    }

    @Test
    public void testGetInt() {
        String test = "20";
        long result = ConversionHelper.getInt(test, 0);
        Assertions.assertTrue(result == 20, "Should get 20");

        test = "test";
        result = ConversionHelper.getLong(test, 0);
        Assertions.assertTrue(result == 0, "Should get 0");
    }

    @Test
    public void testGetDouble() {
        String test = "20";
        double result = ConversionHelper.getDouble(test, 0);
        Assertions.assertTrue(result == 20.0, "Should get 20.0");

        test = "test";
        result = ConversionHelper.getDouble(test, 0);
        Assertions.assertTrue(result == 0.0, "Should get 0.0");
    }

    @Test
    public void testGetBoolean() {
        String test = "true";
        boolean result = ConversionHelper.getBoolean(test, false);
        Assertions.assertTrue(result, "Should get true");

        test = "True";
        result = ConversionHelper.getBoolean(test, false);
        Assertions.assertTrue(result, "Should get true");

        test = "test";
        result = ConversionHelper.getBoolean(test, false);
        Assertions.assertTrue(!result, "Should get false");

        result = ConversionHelper.getBoolean(null, true);
        Assertions.assertTrue(result, "Null should yield default value 'true'");

        result = ConversionHelper.getBoolean(null, false);
        Assertions.assertTrue(!result, "Null should yield default value 'false'");
    }
}
