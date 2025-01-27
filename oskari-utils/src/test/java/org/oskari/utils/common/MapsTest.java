package org.oskari.utils.common;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapsTest {

    Map<String, String> map;
    Iterator<String> keys;
    Iterator<String> values;

    @Test
    public void testOneKeyValue() {
        map = Maps.of("foo", "bar");
        assertEquals(1, map.size());
        assertEquals("foo", map.keySet().iterator().next());
        assertEquals("bar", map.values().iterator().next());
    }

    @Test
    public void testTwoKeyValues() {
        map = Maps.of("foo", "bar", "baz", "qux");
        assertEquals(2, map.size());
        keys = map.keySet().iterator();
        assertEquals("foo", keys.next());
        assertEquals("baz", keys.next());
        values = map.values().iterator();
        assertEquals("bar", values.next());
        assertEquals("qux", values.next());
    }

    @Test()
    public void oddNumberOfParamsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            map = Maps.of("foo", "bar", "baz");
        }, "fuu");
    }

    @Test
    public void evenNumberOfParamsWorks() {
        map = Maps.of("a", "b", "c", "d", "e", "f");
        assertEquals(3, map.size());
        keys = map.keySet().iterator();
        assertEquals("a", keys.next());
        assertEquals("c", keys.next());
        assertEquals("e", keys.next());
        values = map.values().iterator();
        assertEquals("b", values.next());
        assertEquals("d", values.next());
        assertEquals("f", values.next());
    }

}
