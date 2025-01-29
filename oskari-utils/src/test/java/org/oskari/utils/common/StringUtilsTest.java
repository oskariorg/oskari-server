package org.oskari.utils.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {
    
    @Test
    public void testJoin() {
        Assertions.assertNull(StringUtils.join(null, ','));
        Assertions.assertEquals("", StringUtils.join(new String[0], ','));
        Assertions.assertEquals("foo", StringUtils.join(new String[] { "foo" }, ','));
        Assertions.assertEquals("foo,bar", StringUtils.join(new String[] { "foo", "bar" }, ','));
        Assertions.assertEquals("foo,bar,baz", StringUtils.join(new String[] { "foo", "bar", "baz" }, ','));
        Assertions.assertEquals("foo,null,baz", StringUtils.join(new String[] { "foo", null, "baz" }, ','));
        Assertions.assertEquals("foo,,baz", StringUtils.join(new String[] { "foo", "", "baz" }, ','));
    }

    @Test
    public void testParseDoubleArray() {
        Assertions.assertFalse(StringUtils.parseDoubleArray(null, ',').isPresent());
        Assertions.assertFalse(StringUtils.parseDoubleArray("", ',').isPresent());
        Assertions.assertFalse(StringUtils.parseDoubleArray("foo", ',').isPresent());
        Assertions.assertFalse(StringUtils.parseDoubleArray(",123.0,333.0,652.0", ',').isPresent());
        Assertions.assertFalse(StringUtils.parseDoubleArray("123.0,333.0,652.0,", ',').isPresent());
        Assertions.assertArrayEquals(new double[] { 123.0, -333.0, 652.0 }, StringUtils.parseDoubleArray("123.0,-333.0,652", ',').get(), 0.0);
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
        Assertions.assertArrayEquals(arr, StringUtils.parseDoubleArray(sb.toString(), ',').get(), 0.0);
    }

    @Test
    public void testParseIntArray() {
        Assertions.assertFalse(StringUtils.parseIntArray(null, ',').isPresent());
        Assertions.assertFalse(StringUtils.parseIntArray("", ',').isPresent());
        Assertions.assertFalse(StringUtils.parseIntArray("foo", ',').isPresent());
        Assertions.assertFalse(StringUtils.parseIntArray(",123,333,652", ',').isPresent());
        Assertions.assertFalse(StringUtils.parseIntArray("123,333,652,", ',').isPresent());
        Assertions.assertArrayEquals(new int[] { 123, -333, 652 }, StringUtils.parseIntArray("123,-333,652", ',').get());
        int[] arr = new int[17];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
        }
        Assertions.assertArrayEquals(arr, StringUtils.parseIntArray(sb.toString(), ',').get());
    }

}
