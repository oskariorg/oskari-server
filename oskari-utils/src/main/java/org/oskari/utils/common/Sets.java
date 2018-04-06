package org.oskari.utils.common;

import java.util.HashSet;
import java.util.Set;

public class Sets {

    public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        return intersection;
    }

}
