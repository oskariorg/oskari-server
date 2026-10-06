package org.oskari.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class LayerUrlHelperTest {

    @Test
    public void getSanitizedUrl() {
        assertEquals("https://jee.org/?apikey=woot&proxy=not", LayerUrlHelper.getSanitizedUrl("https://jee.org/?apikey=woot&request=GetWheel&proxy=not"));
        // doesn't work:
        // assertEquals("resources://jee.org/?apikey=woot&proxy=not", LayerUrlHelper.getSanitizedUrl("resources://jee.org/?apikey=woot&request=GetWheel&proxy=not"));
    }
}