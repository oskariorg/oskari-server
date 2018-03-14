package org.oskari.utils.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class SetsTest {

    @Test
    public void testIntersection() {
        Set<String> a = new HashSet<>();
        a.add("foo");
        a.add("bar");
        a.add("baz");
        a.add("qux");

        Set<String> b = new HashSet<>();
        b.add("bar");
        b.add("baz");

        Set<String> intersection = Sets.intersection(a, b);
        assertEquals(2, intersection.size());
        assertTrue(intersection.contains("bar"));
        assertTrue(intersection.contains("baz"));
    }

}
