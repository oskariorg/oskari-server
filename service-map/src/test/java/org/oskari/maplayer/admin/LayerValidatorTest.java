package org.oskari.maplayer.admin;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.junit.Test;
import org.oskari.maplayer.model.MapLayer;

import java.util.HashMap;

import static org.junit.Assert.*;

public class LayerValidatorTest {

    @Test
    public void validateUrl() {
        String url = "https://api.maptiler.com/tiles/v3/{z}/{x}/{y}.pbf";
        String validated = LayerValidator.sanitizeUrl(url);
        assertEquals("URL should be unchanged", url, validated);
    }

    @Test
    public void validateUrlNull() {
        String url = null;
        String validated = LayerValidator.sanitizeUrl(url);
        assertNull("URL should be unchanged", validated);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateUrlNonvalid() {
        String url = "sgashahah";
        String validated = LayerValidator.sanitizeUrl(url);
        fail("Should have thrown exception");
    }

    @Test
    public void validateWMSLayer() {
        MapLayer input = new MapLayer();
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            assertEquals("Required field missing 'type'", e.getMessage());
        }

        input.setType(OskariLayer.TYPE_WMS);
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            assertEquals("Required field missing 'name'", e.getMessage());
        }
        input.setName("testing");
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            assertEquals("Required field missing 'url'", e.getMessage());
        }

        input.setUrl("http://oskari.org/testing");
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            assertEquals("Required field missing 'version'", e.getMessage());
        }

        input.setVersion("1.1.0");
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            //
            assertEquals("Localization for layer names missing", e.getMessage());
        }
        input.setLocale(new HashMap<>());
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            // Localization for layer names missing
            assertEquals("Name missing for default language: en", e.getMessage());
        }

        HashMap locale = new HashMap<>();
        HashMap en = new HashMap<>();
        en.put("name", "testing");
        locale.put("en", en);
        input.setLocale(locale);
        LayerValidator.validateAndSanitizeLayerInput(input);
    }
}