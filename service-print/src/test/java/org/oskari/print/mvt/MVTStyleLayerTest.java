package org.oskari.print.mvt;

import java.awt.Color;
import java.util.Collections;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.nls.oskari.util.JSONHelper;

public class MVTStyleLayerTest {

    private static MVTStyleLayer parse(String json, double zoom) {
        return MVTStyleLayer.parse(JSONHelper.createJSONObject(json), zoom);
    }

    @Test
    public void fillOpacityBecomesTheColorsAlpha() {
        MVTStyleLayer layer = parse("{\"id\": \"water\", \"type\": \"fill\","
                + "\"paint\": {\"fill-color\": \"#0000ff\", \"fill-opacity\": 0.5}}", 10);
        Assertions.assertEquals(128, layer.getFillColor().getAlpha(), "opacity becomes alpha");
    }

    @Test
    public void layerOutsideItsZoomRangeIsSkipped() {
        String json = "{\"id\": \"road\", \"type\": \"line\", \"minzoom\": 10, \"maxzoom\": 14,"
                + "\"paint\": {\"line-color\": \"#000000\"}}";
        Assertions.assertNull(parse(json, 9), "below minzoom");
        Assertions.assertNotNull(parse(json, 10), "minzoom is inclusive");
        Assertions.assertNotNull(parse(json, 13.9), "inside the range");
        Assertions.assertNull(parse(json, 14), "maxzoom is exclusive");
    }

    @Test
    public void patternFillIsSkippedRatherThanPaintedBlack() {
        // OSM Bright draws a "wave" pattern over the water it has just filled;
        // drawing it with the default colour would black the water out
        MVTStyleLayer layer = parse("{\"id\": \"water-pattern\", \"type\": \"fill\","
                + "\"paint\": {\"fill-pattern\": \"wave\"}}", 10);
        Assertions.assertNull(layer, "a layer that only has a pattern is not drawn");
    }

    @Test
    public void matchPicksTheFillColorPerFeature() {
        // MTK colours its fields by kohdeluokka and leaves the rest transparent
        MVTStyleLayer layer = parse("{\"id\": \"fields\", \"type\": \"fill\","
                + "\"paint\": {\"fill-color\": [\"match\", [\"get\", \"kohdeluokka\"],"
                + " 32611, \"#fffcd6\", [32200, 32612], \"#eef6e0\", \"hsla(360, 100%, 100%, 0)\"]}}",
                10);
        Assertions.assertNotNull(layer, "a layer coloured per feature is drawn");
        Assertions.assertEquals(new Color(0xff, 0xfc, 0xd6),
                layer.getFillColor(Collections.singletonMap("kohdeluokka", 32611L)), "single label");
        Assertions.assertEquals(new Color(0xee, 0xf6, 0xe0),
                layer.getFillColor(Collections.singletonMap("kohdeluokka", 32612L)), "label list");
        Assertions.assertNull(layer.getFillColor(Collections.singletonMap("kohdeluokka", 1L)),
                "a transparent fallback leaves the feature undrawn");
    }

    @Test
    public void unsupportedFillColorIsSkippedRatherThanPaintedBlack() {
        MVTStyleLayer layer = parse("{\"id\": \"fields\", \"type\": \"fill\","
                + "\"paint\": {\"fill-color\": [\"case\", [\"has\", \"x\"], \"#ff0000\", \"#00ff00\"]}}",
                10);
        Assertions.assertNull(layer.getFillColor(), "no default black in its place");
        Assertions.assertFalse(layer.isDrawable(), "a fill colour that can't be resolved is not drawn");
    }

    @Test
    public void multiGeometriesMatchTheirSingleTypeInAFilter() {
        // OSM Bright draws its paths behind ["==", "$type", "LineString"], and
        // the tiles carry them as MultiLineStrings
        MVTStyleLayer layer = parse("{\"id\": \"path\", \"type\": \"line\","
                + "\"filter\": [\"==\", \"$type\", \"LineString\"],"
                + "\"paint\": {\"line-color\": \"#000000\"}}", 10);
        GeometryFactory gf = new GeometryFactory();
        LineString line = gf.createLineString(new Coordinate[] {
                new Coordinate(0, 0), new Coordinate(1, 1) });

        Assertions.assertTrue(layer.matches(Collections.emptyMap(), line), "a line");
        Assertions.assertTrue(layer.matches(Collections.emptyMap(),
                gf.createMultiLineString(new LineString[] { line })), "a multi line");
        Assertions.assertFalse(layer.matches(Collections.emptyMap(),
                gf.createPoint(new Coordinate(0, 0))), "a point is not a line");
    }

    @Test
    public void geometryTypeExpressionFiltersLikeTypeKey() {
        // MTK draws its roads behind ["==", ["geometry-type"], "LineString"]; left
        // unsupported the whole filter matches, and every road gets every road style
        MVTStyleLayer layer = parse("{\"id\": \"road\", \"type\": \"line\","
                + "\"filter\": [\"all\", [\"==\", [\"geometry-type\"], \"LineString\"],"
                + " [\"match\", [\"get\", \"kohdeluokka\"], [12141], true, false]],"
                + "\"paint\": {\"line-color\": \"#000000\"}}", 10);
        GeometryFactory gf = new GeometryFactory();
        LineString line = gf.createLineString(new Coordinate[] {
                new Coordinate(0, 0), new Coordinate(1, 1) });

        Assertions.assertTrue(layer.matches(Collections.singletonMap("kohdeluokka", 12141L), line),
                "a line of the class");
        Assertions.assertFalse(layer.matches(Collections.singletonMap("kohdeluokka", 12111L), line),
                "a line of another class");
        Assertions.assertFalse(layer.matches(Collections.singletonMap("kohdeluokka", 12141L),
                gf.createPoint(new Coordinate(0, 0))), "a point is not a line");
    }
}
