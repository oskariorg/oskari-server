package org.oskari.utils.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

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
        Assertions.assertEquals(2, intersection.size());
        Assertions.assertTrue(intersection.contains("bar"));
        Assertions.assertTrue(intersection.contains("baz"));
    }

}
