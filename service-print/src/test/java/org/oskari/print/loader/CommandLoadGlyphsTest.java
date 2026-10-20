package org.oskari.print.loader;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandLoadGlyphsTest {

    private static final String URL = "https://example.org/fonts/{fontstack}/{range}.pbf?key=k";

    @Test
    public void aFontstackIsOneRequestWithItsSpacesPercentEncoded() {
        // An endpoint asked for "Noto+Sans+Bold" may silently answer with its default font.
        // The endpoint resolves the stack, so the fallback between the fonts happens there
        String url = CommandLoadGlyphs.getGlyphsURL(URL, List.of("Noto Sans Bold", "B Regular"), 0);
        Assertions.assertEquals(
                "https://example.org/fonts/Noto%20Sans%20Bold%2CB%20Regular/0-255.pbf?key=k", url,
                "both fonts in one request, spaces as %20");
    }
}
