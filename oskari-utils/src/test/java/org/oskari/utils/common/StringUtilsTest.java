package org.oskari.utils.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class StringUtilsTest {
    
    @Test
    public void testJoin() {
        assertNull(StringUtils.join(null, ','));
        assertEquals("", StringUtils.join(new String[0], ','));
        assertEquals("foo", StringUtils.join(new String[] { "foo" }, ','));
        assertEquals("foo,bar",
                StringUtils.join(new String[] { "foo", "bar" }, ','));
        assertEquals("foo,bar,baz",
                StringUtils.join(new String[] { "foo", "bar", "baz" }, ','));
        assertEquals("foo,null,baz",
                StringUtils.join(new String[] { "foo", null, "baz" }, ','));
        assertEquals("foo,,baz",
                StringUtils.join(new String[] { "foo", "", "baz" }, ','));
    }

    @Test
    public void testParseDoubleArray() {
        assertFalse(StringUtils.parseDoubleArray(null, ',').isPresent());
        assertFalse(StringUtils.parseDoubleArray("", ',').isPresent());
        assertFalse(StringUtils.parseDoubleArray("foo", ',').isPresent());
        assertFalse(StringUtils.parseDoubleArray(",123.0,333.0,652.0", ',').isPresent());
        assertFalse(StringUtils.parseDoubleArray("123.0,333.0,652.0,", ',').isPresent());
        assertArrayEquals(new double[] { 123.0, -333.0, 652.0 },
                StringUtils.parseDoubleArray("123.0,-333.0,652", ',').get(), 0.0);
        double[] arr = new double[17];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            double factor = i % 2 == 0 ? -2.0 : 2.0;
            double d = factor * i;
            arr[i] = d;
            if (i > 0) {
                sb.append(',');
            }
            sb.append(d);
        }
        assertArrayEquals(arr, StringUtils.parseDoubleArray(sb.toString(), ',').get(), 0.0);
    }

    @Test
    public void testParseIntArray() {
        assertFalse(StringUtils.parseIntArray(null, ',').isPresent());
        assertFalse(StringUtils.parseIntArray("", ',').isPresent());
        assertFalse(StringUtils.parseIntArray("foo", ',').isPresent());
        assertFalse(StringUtils.parseIntArray(",123,333,652", ',').isPresent());
        assertFalse(StringUtils.parseIntArray("123,333,652,", ',').isPresent());
        assertArrayEquals(new int[] { 123, -333, 652 },
                StringUtils.parseIntArray("123,-333,652", ',').get());
        int[] arr = new int[17];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
        }
        assertArrayEquals(arr, StringUtils.parseIntArray(sb.toString(), ',').get());
    }

}
