package org.oskari.print.mvt;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.nls.oskari.util.JSONHelper;

public class MVTStyleResourcesTest {

    private static final String LAYERS = "["
            // A style names a font per layer: OSM Bright sets its place names
            // in bold and its water names in italic
            + "{\"id\": \"place\", \"type\": \"symbol\", \"layout\":"
            + " {\"text-field\": \"{name}\", \"text-font\": [\"Noto Sans Bold\"]}},"
            + "{\"id\": \"water\", \"type\": \"symbol\", \"layout\":"
            + " {\"text-field\": \"{name}\", \"text-font\": [\"Noto Sans Italic\"]}},"
            + "{\"id\": \"road\", \"type\": \"symbol\", \"layout\":"
            + " {\"text-field\": \"{name}\", \"text-font\": [\"Noto Sans Regular\"]}},"
            + "{\"id\": \"road2\", \"type\": \"symbol\", \"layout\":"
            + " {\"text-field\": \"{name}\", \"text-font\": [\"Noto Sans Regular\"]}}]";

    @Test
    public void eachFontstackIsAskedForSeparately() {
        List<MVTStyleLayer> layers = MVTStyleLayer.parseAll(JSONHelper.createJSONArray(LAYERS), 10);
        Map<String, List<String>> fontstacks = MVTStyleResources.getFontstacks(layers);

        Assertions.assertEquals(3, fontstacks.size(), "one entry per distinct fontstack");
        Assertions.assertEquals(List.of("Noto Sans Bold"), fontstacks.get("Noto Sans Bold"),
                "the bold layer keeps its own stack");
        Assertions.assertEquals(List.of("Noto Sans Italic"), fontstacks.get("Noto Sans Italic"),
                "the italic layer keeps its own stack");
    }
}
