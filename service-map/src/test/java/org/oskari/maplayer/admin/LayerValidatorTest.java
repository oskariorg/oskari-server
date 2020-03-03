package org.oskari.maplayer.admin;

import fi.nls.oskari.service.ServiceRuntimeException;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class LayerValidatorTest {

    @Test
    public void validateUrl() {
        String url = "https://api.maptiler.com/tiles/v3/{z}/{x}/{y}.pbf";
        String validated = LayerValidator.validateUrl(url);
        assertEquals("URL should be unchanged", url, validated);
    }

    @Test
    public void validateUrlNull() {
        String url = null;
        String validated = LayerValidator.validateUrl(url);
        assertNull("URL should be unchanged", validated);
    }

    @Test(expected = ServiceRuntimeException.class)
    public void validateUrlNonvalid() {
        String url = "sgashahah";
        String validated = LayerValidator.validateUrl(url);
        fail("Should have thrown exception");
    }
}