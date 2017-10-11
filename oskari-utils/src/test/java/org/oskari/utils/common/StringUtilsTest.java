package org.oskari.utils.common;

import static org.junit.Assert.assertEquals;
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

}
