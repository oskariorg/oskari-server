package org.oskari.print.mvt;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.print.mvt.glyph.BlockGlyphSource;
import org.oskari.print.mvt.glyph.GlyphSource;
import org.oskari.print.mvt.sprite.SpriteAtlas;
import org.locationtech.jts.geom.util.AffineTransformation;

import fi.nls.oskari.util.JSONHelper;
import no.ecc.vectortile.VectorTileDecoder;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;

/**
 * Draws tile features the way the decoder hands them over and checks that the
 * pixels land where the style and the tile extent say they should.
 */
public class MVTSmokeTest {

    private static final GlyphSource GLYPHS = new BlockGlyphSource();

    private static final int EXTENT = 4096;

    private static VectorTileDecoder.Feature feature(String layer, Map<String, Object> attrs,
            double... xy) {
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] ring = new Coordinate[xy.length / 2 + 1];
        for (int i = 0; i < xy.length / 2; i++) {
            ring[i] = new Coordinate(xy[i * 2], xy[i * 2 + 1]);
        }
        ring[ring.length - 1] = ring[0];
        return new VectorTileDecoder.Feature(layer, EXTENT, gf.createPolygon(ring), attrs, 1);
    }

    private static MVTLayerData data(List<MVTStyleLayer> style, MVTTile... tiles) {
        return new MVTLayerData(Arrays.asList(tiles), style,
                new MVTStyleResources(Map.of("Open Sans Regular", GLYPHS), null));
    }

    private static Map<String, Object> attributes(String key, Object value) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(key, value);
        return attrs;
    }

    @Test
    public void featureIsPlacedAccordingToTheTileExtent() throws Exception {
        // Same tile local geometry, but the tile covers the right half of the map
        VectorTileDecoder.Feature f = feature("land", attributes("class", "water"),
                0, 0, EXTENT, 0, EXTENT, EXTENT, 0, EXTENT);
        MVTTile tile = new MVTTile(new double[] { 50, 0, 100, 100 }, Arrays.asList(f));

        List<MVTStyleLayer> style = MVTStyleLayer.parseAll(JSONHelper.createJSONArray(
                "[{\"id\": \"land\", \"type\": \"fill\", \"source-layer\": \"land\","
                + "\"paint\": {\"fill-color\": \"#ff0000\"}}]"), 5);

        BufferedImage img = render(tile, style, 100);
        Assertions.assertNotEquals(0xffff0000, img.getRGB(25, 50),
                "nothing outside the tile's extent");
        Assertions.assertEquals(0xffff0000, img.getRGB(75, 50),
                "the tile is drawn where its extent says");
    }

    @Test
    public void lineWidthIsInPointsWhateverTheTileUnits() throws Exception {
        // Paths are written in tile units, 41 to a point here, the width must not follow
        GeometryFactory gf = new GeometryFactory();
        VectorTileDecoder.Feature f = new VectorTileDecoder.Feature("roads", EXTENT,
                gf.createLineString(new Coordinate[] {
                        new Coordinate(0, EXTENT / 2), new Coordinate(EXTENT, EXTENT / 2) }),
                attributes("class", "road"), 1);
        MVTTile tile = new MVTTile(new double[] { 0, 0, 100, 100 }, Arrays.asList(f));
        // 12.6 px is 10 pt
        List<MVTStyleLayer> style = MVTStyleLayer.parseAll(JSONHelper.createJSONArray(
                "[{\"id\": \"road\", \"type\": \"line\", \"source-layer\": \"roads\","
                + "\"paint\": {\"line-color\": \"#ff0000\", \"line-width\": 12.6}}]"), 5);

        BufferedImage img = render(tile, style, 100);
        int red = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            if (img.getRGB(50, y) == 0xffff0000) {
                red++;
            }
        }
        // Give or take the antialiased edge
        Assertions.assertTrue(red >= 9 && red <= 11, "the line is 10 pt wide, was " + red);
    }

    @Test
    public void featureOfAnotherExtentIsScaledToTheTileUnits() throws Exception {
        // The first feature drawn sets the units, the second covers the right
        // half in an extent of its own
        GeometryFactory gf = new GeometryFactory();
        VectorTileDecoder.Feature other = new VectorTileDecoder.Feature("land", 512,
                gf.createPolygon(new Coordinate[] { new Coordinate(256, 0), new Coordinate(512, 0),
                        new Coordinate(512, 512), new Coordinate(256, 512), new Coordinate(256, 0) }),
                attributes("class", "water"), 2);
        MVTTile tile = new MVTTile(new double[] { 0, 0, 100, 100 }, Arrays.asList(
                feature("land", attributes("class", "water"),
                        0, 0, 1024, 0, 1024, EXTENT, 0, EXTENT),
                other));
        List<MVTStyleLayer> style = MVTStyleLayer.parseAll(JSONHelper.createJSONArray(
                "[{\"id\": \"land\", \"type\": \"fill\", \"source-layer\": \"land\","
                + "\"paint\": {\"fill-color\": \"#ff0000\"}}]"), 5);

        BufferedImage img = render(tile, style, 100);
        Assertions.assertEquals(0xffff0000, img.getRGB(75, 50), "right half is filled");
        Assertions.assertNotEquals(0xffff0000, img.getRGB(40, 50), "the gap between is not");
    }

    private static VectorTileDecoder.Feature point(String layer, Map<String, Object> attrs,
            double x, double y) {
        GeometryFactory gf = new GeometryFactory();
        return new VectorTileDecoder.Feature(layer, EXTENT,
                gf.createPoint(new Coordinate(x, y)), attrs, 1);
    }

    private static List<MVTStyleLayer> labelStyle(String layout) {
        return MVTStyleLayer.parseAll(JSONHelper.createJSONArray(
                "[{\"id\": \"labels\", \"type\": \"symbol\", \"source-layer\": \"places\","
                + "\"layout\": " + layout + ","
                + "\"paint\": {\"text-color\": \"#ff0000\"}}]"), 5);
    }

    /**
     * @return bounding box of the drawn label as minX, minY, maxX, maxY,
     *         null when nothing was drawn
     */
    private static int[] labelBounds(BufferedImage img) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (img.getRGB(x, y) == 0xffff0000) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return maxX < 0 ? null : new int[] { minX, minY, maxX, maxY };
    }

    @Test
    public void textOffsetMovesTheLabelAwayFromThePoint() throws Exception {
        MVTTile tile = new MVTTile(new double[] { 0, 0, 100, 100 },
                Arrays.asList(point("places", attributes("name", "III"), EXTENT / 2, EXTENT / 2)));

        int[] centered = labelBounds(render(tile, labelStyle(
                "{\"text-field\": \"{name}\", \"text-size\": 40}"), 100));
        // Positive y in text-offset points down, which is down on the image too
        int[] offset = labelBounds(render(tile, labelStyle(
                "{\"text-field\": \"{name}\", \"text-size\": 40, \"text-offset\": [0, 1]}"), 100));

        Assertions.assertNotNull(offset, "label was drawn");
        Assertions.assertTrue(offset[1] > centered[1],
                "a positive y offset moves the label down the page");
        Assertions.assertEquals(centered[0], offset[0], 2, "x is unchanged");
    }

    @Test
    public void leftAnchoredLabelStartsAtThePointOnItsCentreLine() throws Exception {
        MVTTile tile = new MVTTile(new double[] { 0, 0, 100, 100 },
                Arrays.asList(point("places", attributes("name", "III"), EXTENT / 2, EXTENT / 2)));

        int[] bounds = labelBounds(render(tile, labelStyle(
                "{\"text-field\": \"{name}\", \"text-size\": 40, \"text-anchor\": \"left\"}"), 100));
        Assertions.assertNotNull(bounds, "label was drawn");
        Assertions.assertTrue(bounds[0] >= 48, "label starts at the point, was " + bounds[0]);
        int centerY = (bounds[1] + bounds[3]) / 2;
        Assertions.assertTrue(Math.abs(centerY - 50) <= 3,
                "label sits on the point's centre line, was " + centerY);
    }

    @Test
    public void neighbouringTilesShowNoSeamWithATranslucentFill() throws Exception {
        // From a print where the tile edges came to 121.8330 and 121.8331 and overlapped
        int b = 64;
        MVTTile lower = new MVTTile(new double[] { 0, 0, 1, 1 }, Arrays.asList(
                feature("land", attributes("class", "town"),
                        -b, -b, EXTENT + b, -b, EXTENT + b, EXTENT + b, -b, EXTENT + b)));
        MVTTile upper = new MVTTile(new double[] { 0, 1, 1, 2 }, Arrays.asList(
                feature("land", attributes("class", "town"),
                        -b, -b, EXTENT + b, -b, EXTENT + b, EXTENT + b, -b, EXTENT + b)));
        List<MVTStyleLayer> style = MVTStyleLayer.parseAll(JSONHelper.createJSONArray(
                "[{\"id\": \"town\", \"type\": \"fill\", \"source-layer\": \"land\","
                + "\"paint\": {\"fill-color\": \"rgba(255, 0, 0, 0.4)\"}}]"), 5);
        AffineTransformation transform = new AffineTransformation(
                812.74963, 0, -448.83784, 0, 812.74963, -690.91664);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(1000, 1000));
            doc.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                MVTRenderer.draw(doc, stream, data(style, lower, upper), transform,
                        new PDRectangle(-1000, -1000, 3000, 3000), 1f);
            }
            for (float scale : new float[] { 1f, 2f, 300f / 72 }) {
                BufferedImage img = new PDFRenderer(doc).renderImage(0, scale, ImageType.RGB);
                // The tiles span x -449..364 on the page
                int x = Math.round(100 * scale);
                int edge = Math.round((1000 - 121.833f) * scale);
                int inside = img.getRGB(x, edge + 10) & 0xFFFFFF;
                for (int y = edge - 2; y <= edge + 2; y++) {
                    Assertions.assertEquals(Integer.toHexString(inside),
                            Integer.toHexString(img.getRGB(x, y) & 0xFFFFFF),
                            "row " + y + " at scale " + scale);
                }
            }
        }
    }

    @Test
    public void anIconPlacedManyTimesIsEmbeddedOnce() throws Exception {
        MVTTile tile = new MVTTile(new double[] { 0, 0, 100, 100 }, Arrays.asList(
                point("places", attributes("name", "a"), 512, EXTENT / 2),
                point("places", attributes("name", "b"), 2048, EXTENT / 2),
                point("places", attributes("name", "c"), 3584, EXTENT / 2)));
        List<MVTStyleLayer> style = MVTStyleLayer.parseAll(JSONHelper.createJSONArray(
                "[{\"id\": \"pois\", \"type\": \"symbol\", \"source-layer\": \"places\","
                + "\"layout\": {\"icon-image\": \"dot\"}}]"), 5);
        Map<String, SpriteAtlas.Icon> icons = new HashMap<>();
        icons.put("dot", new SpriteAtlas.Icon(0, 0, 4, 4, 1));
        SpriteAtlas sprites = new SpriteAtlas(new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB), icons);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(100, 100));
            doc.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                MVTLayerData data = new MVTLayerData(Arrays.asList(tile), style,
                        new MVTStyleResources(Map.of("Open Sans Regular", GLYPHS), sprites));
                MVTRenderer.draw(doc, stream, data, new AffineTransformation(),
                        page.getMediaBox(), 1f);
            }
            String content;
            try (InputStream in = page.getContents()) {
                content = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
            }
            int images = 0;
            for (COSName name : page.getResources().getXObjectNames()) {
                if (page.getResources().getXObject(name) instanceof PDImageXObject) {
                    images++;
                }
            }
            Assertions.assertEquals(3, count(content, " Do\n"), "every icon is drawn");
            Assertions.assertEquals(1, images, "the icon's image is embedded once");
        }
    }

    private static int count(String s, String part) {
        return s.split(java.util.regex.Pattern.quote(part), -1).length - 1;
    }

    private static BufferedImage render(MVTTile tile, List<MVTStyleLayer> style, int size)
            throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(size, size));
            doc.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                MVTRenderer.draw(doc, stream, data(style, tile), new AffineTransformation(),
                        page.getMediaBox(), 1f);
            }
            return new PDFRenderer(doc).renderImage(0, 1, ImageType.ARGB);
        }
    }
}
