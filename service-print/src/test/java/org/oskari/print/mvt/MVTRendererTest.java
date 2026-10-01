package org.oskari.print.mvt;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.oskari.print.mvt.glyph.BlockGlyphSource;

import fi.nls.oskari.util.JSONHelper;
import no.ecc.vectortile.VectorTileDecoder;

public class MVTRendererTest {

    private static final double DELTA = 0.0001;
    private static final int EXTENT = 4096;

    private static Coordinate transform(AffineTransformation t, double x, double y) {
        Coordinate result = new Coordinate();
        t.transform(new Coordinate(x, y), result);
        return result;
    }

    @Test
    public void tileLocalCoordinatesAreMappedToProjectionThenThroughTheMapTransform() {
        // Identity map transform to check the tile local part alone
        double[] tileExtent = { 1000, 2000, 2000, 3000 };
        AffineTransformation t = MVTRenderer.getTransform(tileExtent, EXTENT,
                new AffineTransformation());

        Coordinate topLeft = transform(t, 0, 0);
        Assertions.assertEquals(1000, topLeft.x, DELTA, "origin x is the tile's min x");
        Assertions.assertEquals(3000, topLeft.y, DELTA, "MVT y grows down, origin is the tile's max y");

        Coordinate bottomRight = transform(t, EXTENT, EXTENT);
        Assertions.assertEquals(2000, bottomRight.x, DELTA, "max x");
        Assertions.assertEquals(2000, bottomRight.y, DELTA, "max y");

        Coordinate center = transform(t, EXTENT / 2, EXTENT / 2);
        Assertions.assertEquals(1500, center.x, DELTA, "center x");
        Assertions.assertEquals(2500, center.y, DELTA, "center y");

        // Map transform that shifts everything by 10 and flips y like PDF does
        AffineTransformation toPDF = new AffineTransformation(1, 0, 10, 0, -1, 0);
        Coordinate onPage = transform(MVTRenderer.getTransform(tileExtent, EXTENT, toPDF), 0, 0);
        Assertions.assertEquals(1010, onPage.x, DELTA, "translated x");
        Assertions.assertEquals(-3000, onPage.y, DELTA, "tile transform runs first, then the map transform");
    }

    @Test
    public void haloWidthIsRelativeToTheTextSize() {
        float large = MVTRenderer.getHaloWidth(2, 24);
        float small = MVTRenderer.getHaloWidth(2, 12);
        Assertions.assertEquals(2, large, DELTA, "at the field's own em size the width is unchanged");
        Assertions.assertEquals(2 * large, small, DELTA, "a smaller label gets a wider halo in the field");
    }

    @Test
    public void aPointInTheBufferIsPlacedOnlyByItsOwnTile() {
        // The same point, inside the right tile and in the left tile's buffer
        GeometryFactory gf = new GeometryFactory();
        List<MVTTile> tiles = List.of(
                new MVTTile(new double[] { 0, 0, 100, 100 }, List.of(symbolFeature(
                        gf.createPoint(new Coordinate(EXTENT + 64, EXTENT / 2))))),
                new MVTTile(new double[] { 100, 0, 200, 100 }, List.of(symbolFeature(
                        gf.createPoint(new Coordinate(64, EXTENT / 2))))));

        List<LabelCandidate> candidates = MVTRenderer.collectSymbols(tiles,
                symbolStyle("point"), new AffineTransformation(), resources());
        Assertions.assertEquals(1, candidates.size(), "one label, not one per tile");
    }

    @Test
    public void roadPiecesOfNeighbouringTilesAreJoinedAtTheEdge() {
        // A road across the edge from one tile to the other,
        // both pieces running on into the other tile's buffer
        GeometryFactory gf = new GeometryFactory();
        List<MVTTile> tiles = List.of(
                new MVTTile(new double[] { 0, 0, 100, 100 }, List.of(symbolFeature(
                        line(gf, EXTENT / 8, EXTENT + 256)))),
                new MVTTile(new double[] { 100, 0, 200, 100 }, List.of(symbolFeature(
                        line(gf, -256, EXTENT * 7 / 8)))));

        List<LabelCandidate> candidates = MVTRenderer.collectSymbols(tiles,
                symbolStyle("line-center"), new AffineTransformation(), resources());
        Assertions.assertEquals(1, candidates.size(), "the label is placed on the joined road");
    }

    private static Geometry line(GeometryFactory gf, double fromX, double toX) {
        return gf.createLineString(new Coordinate[] {
                new Coordinate(fromX, EXTENT / 2), new Coordinate(toX, EXTENT / 2) });
    }

    private static VectorTileDecoder.Feature symbolFeature(Geometry geometry) {
        return new VectorTileDecoder.Feature("places", EXTENT, geometry,
                Map.of("name", "Long Road Name"), 1);
    }

    /** 14 characters of about 8 pt: 110 pt, a piece is 94 and the road 175 */
    private static List<MVTStyleLayer> symbolStyle(String placement) {
        return MVTStyleLayer.parseAll(JSONHelper.createJSONArray(
                "[{\"id\": \"labels\", \"type\": \"symbol\", \"source-layer\": \"places\","
                + "\"layout\": {\"text-field\": \"{name}\", \"text-size\": 20,"
                + " \"symbol-placement\": \"" + placement + "\", \"text-allow-overlap\": true},"
                + "\"paint\": {\"text-color\": \"#000000\"}}]"), 5);
    }

    private static MVTStyleResources resources() {
        return new MVTStyleResources(Map.of("Open Sans Regular", new BlockGlyphSource()), null);
    }
}
