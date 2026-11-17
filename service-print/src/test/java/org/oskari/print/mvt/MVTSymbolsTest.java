package org.oskari.print.mvt;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.oskari.print.mvt.glyph.BlockGlyphSource;
import org.oskari.print.mvt.glyph.GlyphSource;
import org.oskari.print.mvt.sprite.SpriteAtlas;

import fi.nls.oskari.util.JSONHelper;
import org.junit.jupiter.api.Test;

public class MVTSymbolsTest {

    private static final GlyphSource GLYPHS = new BlockGlyphSource();

    @Test
    public void aPieceIsReversedWhenThatIsHowItContinues() {
        List<double[]> joined = MVTSymbols.joinLines(Arrays.asList(
                new double[] { 0, 0, 10, 0 },
                new double[] { 20, 0, 10, 0 }));
        Assertions.assertEquals(1, joined.size(), "direction doesn't keep them apart");
        Assertions.assertArrayEquals(new double[] { 0, 0, 10, 0, 20, 0 }, joined.get(0), 0.0001,
                "the reversed piece continues the run");
    }

    @Test
    public void aRunGrowsFromBothEnds() {
        List<double[]> joined = MVTSymbols.joinLines(Arrays.asList(
                new double[] { 10, 0, 20, 0 },
                new double[] { 20, 0, 30, 0 },
                new double[] { 0, 0, 10, 0 }));
        Assertions.assertEquals(1, joined.size(), "all three make one run");
        Assertions.assertEquals(8, joined.get(0).length, "four points");
    }

    @Test
    public void aJunctionEndsTheRun() {
        // Three lines meet at (10, 0): which way the road goes on is not known
        List<double[]> joined = MVTSymbols.joinLines(Arrays.asList(
                new double[] { 0, 0, 10, 0 },
                new double[] { 10, 0, 20, 0 },
                new double[] { 10, 0, 10, 10 }));
        Assertions.assertEquals(3, joined.size(), "a fork is left alone");
    }

    @Test
    public void aLongLineRepeatsTheLabel() {
        int drawn = MVTSymbols.placeAlongLines(roadNames("line"), straightLine(1000), "Road",
                GLYPHS, 10, 1).size();
        Assertions.assertTrue(drawn > 1, "repeated, was " + drawn);
        Assertions.assertTrue(drawn < 1000 / 100, "no closer than symbol-spacing, was " + drawn);
    }

    @Test
    public void lineCenterPlacesTheLabelOnceInTheMiddle() {
        // Four characters of half an em at 10 pt: 20 long
        List<LabelCandidate> placed = MVTSymbols.placeAlongLines(roadNames("line-center"),
                straightLine(1000), "Road", GLYPHS, 10, 1);
        Assertions.assertEquals(1, placed.size(), "placed once");
        Assertions.assertEquals(490, placed.get(0).getChunks().get(0).getX(), 0.001,
                "centred on the line");
    }

    @Test
    public void aLabelLongerThanTheLineIsNotPlaced() {
        Assertions.assertTrue(MVTSymbols.placeAlongLines(roadNames("line-center"), straightLine(15),
                "Road", GLYPHS, 10, 1).isEmpty(), "the label doesn't run past the line");
    }

    private static MVTStyleLayer roadNames(String placement) {
        return MVTStyleLayer.parse(JSONHelper.createJSONObject(
                "{\"id\": \"road-name\", \"type\": \"symbol\","
                + "\"layout\": {\"text-field\": \"{name}\", \"symbol-placement\": \"" + placement + "\","
                + " \"symbol-spacing\": 100, \"text-size\": 10},"
                + "\"paint\": {\"text-color\": \"#000000\"}}"), 14);
    }

    private static List<Geometry> straightLine(double length) {
        return List.of(new GeometryFactory().createLineString(new Coordinate[] {
                new Coordinate(0, 0), new Coordinate(length, 0) }));
    }

    @Test
    public void withoutGlyphsTheIconIsPlacedAlone() {
        MVTStyleLayer layer = MVTStyleLayer.parse(JSONHelper.createJSONObject(
                "{\"id\": \"poi\", \"type\": \"symbol\","
                + "\"layout\": {\"text-field\": \"{name}\", \"icon-image\": \"marker\"},"
                + "\"paint\": {\"text-color\": \"#000000\"}}"), 14);
        SpriteAtlas sprites = new SpriteAtlas(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB),
                Map.of("marker", new SpriteAtlas.Icon(0, 0, 10, 10, 1)));
        Geometry point = new GeometryFactory().createPoint(new Coordinate(100, 100));

        List<LabelCandidate> placed = MVTSymbols.place(layer, point, Map.of("name", "Cafe"),
                null, sprites, 10, 1);
        Assertions.assertEquals(1, placed.size(), "only the icon is placed");
        Assertions.assertTrue(placed.get(0).isIcon(), "the candidate is the icon");
    }

    @Test
    public void withoutGlyphsALineLabelIsNotPlaced() {
        MVTStyleLayer layer = MVTStyleLayer.parse(JSONHelper.createJSONObject(
                "{\"id\": \"road-name\", \"type\": \"symbol\","
                + "\"layout\": {\"text-field\": \"{name}\", \"symbol-placement\": \"line\"},"
                + "\"paint\": {\"text-color\": \"#000000\"}}"), 14);
        List<Geometry> line = List.of(new GeometryFactory().createLineString(new Coordinate[] {
                new Coordinate(0, 0), new Coordinate(1000, 0) }));

        Assertions.assertTrue(MVTSymbols.placeAlongLines(layer, line, "Road", null, 10, 1).isEmpty(),
                "labels are skipped when the glyphs are missing");
    }
}
