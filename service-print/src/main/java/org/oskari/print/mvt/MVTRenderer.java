package org.oskari.print.mvt;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.oskari.print.mvt.glyph.GlyphForms;
import org.oskari.print.mvt.glyph.GlyphSource;
import org.oskari.print.mvt.glyph.SDFGlyph;
import org.oskari.print.mvt.sprite.SpriteAtlas;
import org.oskari.print.request.PDPrintStyle;
import org.oskari.print.util.Units;

import no.ecc.vectortile.VectorTileDecoder;

/**
 * Draws decoded vector tile features with a Mapbox GL style.
 *
 * Each tile is drawn as a Form XObject in its own units, clipped to the tile.
 * Symbols are laid out across all tiles after them.
 */
public class MVTRenderer {

    /** Control point distance of a quarter circle as a cubic Bézier, in radii */
    private static final double CIRCLE_KAPPA = 0.5522847498;

    private MVTRenderer() {}

    public static void draw(PDDocument document, PDPageContentStream stream, MVTLayerData data,
            AffineTransformation transform, PDRectangle view, float layerOpacity)
            throws IOException {
        List<MVTStyleLayer> shapeLayers = data.getStyleLayers().stream()
                .filter(l -> l.getType() != MVTStyleLayer.Type.SYMBOL)
                .collect(Collectors.toList());
        List<MVTStyleLayer> symbolLayers = data.getStyleLayers().stream()
                .filter(l -> l.getType() == MVTStyleLayer.Type.SYMBOL)
                .collect(Collectors.toList());

        for (MVTTile tile : data.getTiles()) {
            List<VectorTileDecoder.Feature> features = tile.getFeatures();
            if (features.isEmpty()) {
                continue;
            }
            // MVT gives each layer its own extent; other extents are scaled to the first feature's.
            int mvtTileSize = features.get(0).getExtent();
            AffineTransform toPage = toAwt(getTransform(tile.getExtent(), mvtTileSize, transform));
            // Style sizes are pixels (0.28 mm on paper), but the form's matrix
            // scales everything in it, so they are given to it in tile units
            float unitsPerPixel = (float) (Units.PDF_DPI / Units.OGC_DPI / toPage.getScaleX());
            PDAppearanceStream form = drawTile(document, features, mvtTileSize,
                    shapeLayers, unitsPerPixel, layerOpacity);
            if (form != null) {
                form.setMatrix(toPage);
                stream.drawForm(form);
            }
        }

        drawSymbols(document, stream, data.getTiles(), symbolLayers, data.getResources(),
                transform, view, layerOpacity);
    }

    private static void drawSymbols(PDDocument document, PDPageContentStream stream,
            List<MVTTile> tiles, List<MVTStyleLayer> symbolLayers, MVTStyleResources resources,
            AffineTransformation transform, PDRectangle view, float layerOpacity)
            throws IOException {
        SpriteAtlas sprites = resources.getSprites();
        List<LabelCandidate> candidates = collectSymbols(tiles, symbolLayers, transform, resources);
        // Symbols outside the view don't take part in the collision test
        candidates.removeIf(c -> !c.getBounds().intersects(view.getLowerLeftX(),
                view.getLowerLeftY(), view.getWidth(), view.getHeight()));
        if (candidates.isEmpty()) {
            return;
        }
        Map<GlyphSource, GlyphForms> formsBySource = new HashMap<>();
        Map<String, PDImageXObject> imagesByIcon = new HashMap<>();
        for (LabelCandidate candidate : LabelCollider.place(candidates)) {
            GlyphSource source = resources.getGlyphs(candidate.getStyleLayer());
            GlyphForms forms = source == null ? null
                    : formsBySource.computeIfAbsent(source, g -> new GlyphForms(document, g));
            drawSymbol(document, stream, candidate, forms, sprites, imagesByIcon, layerOpacity);
        }
    }

    private static PDAppearanceStream drawTile(PDDocument doc,
            List<VectorTileDecoder.Feature> features, int mvtTileSize,
            List<MVTStyleLayer> styleLayers, float unitsPerPixel, float layerOpacity)
            throws IOException {
        PDAppearanceStream form = new PDAppearanceStream(doc);
        form.setResources(new PDResources());
        form.setBBox(new PDRectangle(mvtTileSize, mvtTileSize));

        boolean drawn = false;
        try (OutputStream out = form.getContentStream().createOutputStream(COSName.FLATE_DECODE);
                PDPageContentStream stream = new PDPageContentStream(doc, form, out)) {
            Map<Integer, AffineTransformation> toUnitsByExtent = new HashMap<>();
            for (MVTStyleLayer styleLayer : styleLayers) {
                drawn |= drawStyleLayer(stream, features, mvtTileSize, toUnitsByExtent, styleLayer,
                        unitsPerPixel, layerOpacity);
            }
        }
        return drawn ? form : null;
    }

    private static boolean drawStyleLayer(PDPageContentStream stream,
            List<VectorTileDecoder.Feature> features, int mvtTileSize,
            Map<Integer, AffineTransformation> toUnitsByExtent, MVTStyleLayer styleLayer,
            float unitsPerPixel, float layerOpacity) throws IOException {
        String sourceLayer = styleLayer.getSourceLayer();
        boolean styleApplied = false;
        Color currentFill = null;
        Color currentLine = null;

        for (VectorTileDecoder.Feature feature : features) {
            if (sourceLayer != null && !sourceLayer.equals(feature.getLayerName())) {
                continue;
            }
            Geometry geometry = feature.getGeometry();
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }
            Map<String, Object> attributes = feature.getAttributes();
            if (!styleLayer.matches(attributes, geometry)) {
                continue;
            }
            if (feature.getExtent() != mvtTileSize) {
                geometry = toUnitsByExtent.computeIfAbsent(feature.getExtent(), extent ->
                        AffineTransformation.scaleInstance((double) mvtTileSize / extent,
                                (double) mvtTileSize / extent))
                        .transform(geometry);
            }
            Color fill = styleLayer.getFillColor(attributes);
            Color line = styleLayer.getLineColor(attributes);
            if (fill == null && line == null) {
                continue;
            }
            if (!styleApplied) {
                // Keeps the dash pattern etc. from leaking to the next style layer
                stream.saveGraphicsState();
                applyStyle(stream, styleLayer, layerOpacity, unitsPerPixel);
                styleApplied = true;
            }
            if (styleLayer.isColoredByFeature()
                    && (!Objects.equals(fill, currentFill) || !Objects.equals(line, currentLine))) {
                setColors(stream, fill, line, layerOpacity);
                currentFill = fill;
                currentLine = line;
            }
            draw(stream, geometry, styleLayer, fill, line, unitsPerPixel);
        }
        if (styleApplied) {
            stream.restoreGraphicsState();
        }
        return styleApplied;
    }

    /**
     * Kept in style order, which LabelCollider relies on.
     */
    static List<LabelCandidate> collectSymbols(List<MVTTile> tiles,
            List<MVTStyleLayer> symbolLayers, AffineTransformation transform,
            MVTStyleResources resources) {
        SpriteAtlas sprites = resources.getSprites();
        List<LabelCandidate> candidates = new ArrayList<>();
        for (MVTStyleLayer styleLayer : symbolLayers) {
            GlyphSource glyphs = resources.getGlyphs(styleLayer);
            if (glyphs == null && sprites == null) {
                continue;
            }
            // One label per road, gathered across tiles
            Map<String, List<Geometry>> linesByLabel = styleLayer.getPlacement().isAlongLine()
                    ? new LinkedHashMap<>()
                    : null;
            String sourceLayer = styleLayer.getSourceLayer();
            for (MVTTile tile : tiles) {
                Map<Integer, AffineTransformation> toPageByExtent = new HashMap<>();
                for (VectorTileDecoder.Feature feature : tile.getFeatures()) {
                    if (sourceLayer != null && !sourceLayer.equals(feature.getLayerName())) {
                        continue;
                    }
                    Geometry geometry = feature.getGeometry();
                    if (geometry == null || geometry.isEmpty()) {
                        continue;
                    }
                    Map<String, Object> attributes = feature.getAttributes();
                    if (!styleLayer.matches(attributes, geometry)) {
                        continue;
                    }
                    AffineTransformation toPage = toPageByExtent.computeIfAbsent(
                            feature.getExtent(),
                            extent -> getTransform(tile.getExtent(), extent, transform));
                    // The buffer around the tile repeats what the neighbours hold.
                    // Like Mapbox, a tile only places the symbols of its own area.
                    if (linesByLabel != null) {
                        String label = MVTSymbols.getLabel(styleLayer, attributes);
                        if (label == null) {
                            continue;
                        }
                        // Cut at the tile edge, the pieces of neighbouring tiles
                        // meet there and are joined into one road
                        Geometry inTile = clipToTile(geometry, feature.getExtent());
                        if (!inTile.isEmpty()) {
                            linesByLabel.computeIfAbsent(label, k -> new ArrayList<>())
                                    .add(toPage.transform(inTile));
                        }
                    } else if (isAnchoredInTile(geometry, feature.getExtent())) {
                        candidates.addAll(MVTSymbols.place(styleLayer, toPage.transform(geometry),
                                attributes, glyphs, sprites,
                                pixelsToPoints(styleLayer.getTextSize()), pixelsToPoints(1)));
                    }
                }
            }
            if (linesByLabel != null) {
                for (Map.Entry<String, List<Geometry>> entry : linesByLabel.entrySet()) {
                    candidates.addAll(MVTSymbols.placeAlongLines(styleLayer, entry.getValue(),
                            entry.getKey(), glyphs, pixelsToPoints(styleLayer.getTextSize()),
                            pixelsToPoints(1)));
                }
            }
        }
        return candidates;
    }

    /**
     * The anchor is the centroid {@link MVTSymbols} places the symbol at. The
     * far edges belong to the next tile, so a point on one is placed once.
     */
    private static boolean isAnchoredInTile(Geometry geometry, int extent) {
        Coordinate c = geometry.getCentroid().getCoordinate();
        return c != null && c.x >= 0 && c.x < extent && c.y >= 0 && c.y < extent;
    }

    private static Geometry clipToTile(Geometry geometry, int extent) {
        Envelope tile = new Envelope(0, extent, 0, extent);
        if (tile.contains(geometry.getEnvelopeInternal())) {
            return geometry;
        }
        return OverlayNGRobust.overlay(geometry, geometry.getFactory().toGeometry(tile),
                OverlayNG.INTERSECTION);
    }

    /**
     * Sets the colours of a layer that picks them per feature. Features of a
     * class tend to come in runs, so this is done when the colour changes.
     */
    private static void setColors(PDPageContentStream stream, Color fill, Color line,
            float layerOpacity) throws IOException {
        if (fill != null) {
            stream.setNonStrokingColor(fill);
        }
        if (line != null) {
            stream.setStrokingColor(line);
        }
        setOpacity(stream, fill, line, layerOpacity);
    }

    /** JTS orders the matrix entries differently from AWT */
    private static AffineTransform toAwt(AffineTransformation t) {
        double[] m = t.getMatrixEntries();
        return new AffineTransform(m[0], m[3], m[1], m[4], m[2], m[5]);
    }

    /**
     * MVT coordinates run from the tile's top left corner, y growing down.
     */
    static AffineTransformation getTransform(double[] tileExtent, int extent,
            AffineTransformation toPDF) {
        double scaleX = (tileExtent[2] - tileExtent[0]) / extent;
        double scaleY = (tileExtent[3] - tileExtent[1]) / extent;

        AffineTransformation toProjected = new AffineTransformation(
                scaleX, 0, tileExtent[0],
                0, -scaleY, tileExtent[3]);

        return new AffineTransformation(toProjected).compose(toPDF);
    }

    private static void applyStyle(PDPageContentStream stream, MVTStyleLayer styleLayer,
            float layerOpacity, float unitsPerPixel) throws IOException {
        PDPrintStyle style = new PDPrintStyle();
        Color fill = styleLayer.getFillColor();
        Color line = styleLayer.getLineColor();
        float lineWidth = (float) styleLayer.getLineWidth() * unitsPerPixel;
        if (fill != null) {
            style.setFillColor(fill);
        }
        if (line != null) {
            style.setStrokeColor(line);
        }
        if (line != null || styleLayer.isColoredByFeature()) {
            style.setLineWidth(lineWidth);
            style.setLineCap(styleLayer.getLineCap());
            style.setLineJoin(styleLayer.getLineJoin());
        }
        style.apply(stream);

        // PDPrintStyle colors carry no alpha, Mapbox opacity is set separately.
        // The print layer's own opacity multiplies into it as setting the graphics
        // state here replaces whatever was set for the form.
        setOpacity(stream, fill, line, layerOpacity);

        float[] dash = styleLayer.getLineDash();
        if (dash != null && lineWidth > 0) {
            // Mapbox dash lengths are in line widths
            float[] scaled = new float[dash.length];
            boolean positive = false;
            for (int i = 0; i < dash.length; i++) {
                scaled[i] = dash[i] * lineWidth;
                positive |= scaled[i] > 0;
            }
            // An all zero dash array is invalid in PDF and would break stroking
            if (positive) {
                stream.setLineDashPattern(scaled, 0);
            }
        }
    }

    private static void setOpacity(PDPageContentStream stream, Color fill, Color line,
            float layerOpacity) throws IOException {
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setNonStrokingAlphaConstant((fill == null ? 1f : fill.getAlpha() / 255f) * layerOpacity);
        gs.setStrokingAlphaConstant((line == null ? 1f : line.getAlpha() / 255f) * layerOpacity);
        stream.setGraphicsStateParameters(gs);
    }

    private static float pixelsToPoints(double px) {
        return (float) (Units.PDF_DPI * px / Units.OGC_DPI);
    }

    private static void draw(PDPageContentStream stream, Geometry g,
            MVTStyleLayer styleLayer, Color fill, Color line, float unitsPerPixel)
            throws IOException {
        switch (styleLayer.getType()) {
        case FILL:
            fill(stream, g, fill != null, line != null);
            break;
        case LINE:
            if (line != null) {
                stroke(stream, g);
            }
            break;
        case CIRCLE:
            circle(stream, g, styleLayer, (float) styleLayer.getCircleRadius() * unitsPerPixel);
            break;
        case SYMBOL:
        default:
            // Symbols are collected and drawn after everything else
            break;
        }
    }

    private static void fill(PDPageContentStream stream, Geometry g, boolean fill, boolean stroke)
            throws IOException {
        if (g instanceof Polygon) {
            drawPolygon(stream, (Polygon) g, fill, stroke);
        } else if (g instanceof MultiPolygon || g instanceof GeometryCollection) {
            for (int i = 0; i < g.getNumGeometries(); i++) {
                fill(stream, g.getGeometryN(i), fill, stroke);
            }
        }
    }

    private static void drawPolygon(PDPageContentStream stream, Polygon g, boolean fill, boolean stroke)
            throws IOException {
        addRing(stream, g.getExteriorRing().getCoordinateSequence());
        for (int i = 0; i < g.getNumInteriorRing(); i++) {
            addRing(stream, g.getInteriorRingN(i).getCoordinateSequence());
        }
        if (fill && stroke) {
            stream.fillAndStrokeEvenOdd();
        } else if (fill) {
            stream.fillEvenOdd();
        } else {
            stream.stroke();
        }
    }

    private static void stroke(PDPageContentStream stream, Geometry g) throws IOException {
        if (g instanceof LineString) {
            add(stream, ((LineString) g).getCoordinateSequence());
            stream.stroke();
        } else if (g instanceof Polygon) {
            // Mapbox draws polygon outlines for line layers
            Polygon polygon = (Polygon) g;
            add(stream, polygon.getExteriorRing().getCoordinateSequence());
            stream.stroke();
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                add(stream, polygon.getInteriorRingN(i).getCoordinateSequence());
                stream.stroke();
            }
        } else if (g instanceof MultiLineString || g instanceof MultiPolygon
                || g instanceof GeometryCollection) {
            for (int i = 0; i < g.getNumGeometries(); i++) {
                stroke(stream, g.getGeometryN(i));
            }
        }
    }

    private static void circle(PDPageContentStream stream, Geometry g, MVTStyleLayer styleLayer,
            float radius) throws IOException {
        if (g instanceof Point) {
            drawCircle(stream, g.getCoordinate(), styleLayer, radius);
        } else if (g instanceof MultiPoint || g instanceof GeometryCollection) {
            for (int i = 0; i < g.getNumGeometries(); i++) {
                circle(stream, g.getGeometryN(i), styleLayer, radius);
            }
        }
    }

    /**
     * PDF has no circle primitive.
     */
    private static void drawCircle(PDPageContentStream stream, Coordinate c, MVTStyleLayer styleLayer,
            float radius) throws IOException {
        float x = (float) c.x;
        float y = (float) c.y;
        float k = (float) (CIRCLE_KAPPA * radius);
        stream.moveTo(x + radius, y);
        stream.curveTo(x + radius, y + k, x + k, y + radius, x, y + radius);
        stream.curveTo(x - k, y + radius, x - radius, y + k, x - radius, y);
        stream.curveTo(x - radius, y - k, x - k, y - radius, x, y - radius);
        stream.curveTo(x + k, y - radius, x + radius, y - k, x + radius, y);
        stream.closePath();
        boolean fill = styleLayer.getFillColor() != null;
        boolean stroke = styleLayer.getLineColor() != null;
        if (fill && stroke) {
            stream.fillAndStroke();
        } else if (fill) {
            stream.fill();
        } else {
            stream.stroke();
        }
    }

    private static void drawSymbol(PDDocument document, PDPageContentStream stream,
            LabelCandidate candidate, GlyphForms forms, SpriteAtlas sprites,
            Map<String, PDImageXObject> imagesByIcon, float layerOpacity) throws IOException {
        if (candidate.isIcon()) {
            drawIcon(document, stream, candidate, sprites, imagesByIcon, layerOpacity);
            return;
        }
        MVTStyleLayer styleLayer = candidate.getStyleLayer();

        float haloWidth = getHaloWidth(styleLayer.getHaloWidth(), styleLayer.getTextSize());
        if (haloWidth > 0 && styleLayer.getHaloColor() != null) {
            setSymbolColor(stream, styleLayer.getHaloColor(), layerOpacity);
            drawChunks(stream, candidate, forms, haloWidth);
        }
        setSymbolColor(stream, styleLayer.getFillColor(), layerOpacity);
        drawChunks(stream, candidate, forms, 0);
    }

    /**
     * The glyph is scaled to the label's size after the halo is cut from its
     * field, so the style's halo width is converted to the field's own em first.
     *
     * @param haloWidth text-halo-width in pixels
     * @param textSize text-size in pixels
     * @return halo width in pixels at the field's em size
     */
    static float getHaloWidth(double haloWidth, double textSize) {
        return textSize > 0 ? (float) (haloWidth * SDFGlyph.UNITS_PER_EM / textSize) : 0;
    }

    private static void drawChunks(PDPageContentStream stream, LabelCandidate candidate,
            GlyphForms forms, float haloWidth) throws IOException {
        float fontSize = candidate.getFontSize();
        float scale = fontSize / GlyphForms.UNITS_PER_EM;
        double letterSpacing = candidate.getStyleLayer().getTextLetterSpacing() * fontSize;

        for (LabelCandidate.Chunk chunk : candidate.getChunks()) {
            AffineTransform base = new AffineTransform();
            base.translate(chunk.getX(), chunk.getY());
            if (chunk.getAngle() != 0) {
                base.rotate(chunk.getAngle());
            }
            double penX = 0;
            String text = chunk.getText();
            for (int i = 0; i < text.length(); i++) {
                int codePoint = text.codePointAt(i);
                if (Character.isSupplementaryCodePoint(codePoint)) {
                    i++;
                }
                AffineTransform at = new AffineTransform(base);
                at.translate(penX, 0);
                at.scale(scale, scale);
                forms.draw(stream, codePoint, new Matrix(at), haloWidth);
                penX += forms.getSource().getAdvance(codePoint) * scale + letterSpacing;
            }
        }
    }

    private static void drawIcon(PDDocument document, PDPageContentStream stream,
            LabelCandidate candidate, SpriteAtlas sprites, Map<String, PDImageXObject> imagesByIcon,
            float layerOpacity) throws IOException {
        // One image XObject per icon, however many times it is placed
        String iconName = candidate.getIconName();
        PDImageXObject xobject = imagesByIcon.get(iconName);
        if (xobject == null && !imagesByIcon.containsKey(iconName)) {
            BufferedImage image = sprites.getImage(iconName);
            xobject = image == null ? null : LosslessFactory.createFromImage(document, image);
            imagesByIcon.put(iconName, xobject);
        }
        if (xobject == null) {
            return;
        }
        MVTStyleLayer styleLayer = candidate.getStyleLayer();

        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setNonStrokingAlphaConstant(styleLayer.getIconOpacity() * layerOpacity);
        stream.setGraphicsStateParameters(gs);

        double w = candidate.getIconWidth();
        double h = candidate.getIconHeight();
        double rotate = styleLayer.getIconRotate();
        if (rotate == 0) {
            stream.drawImage(xobject, (float) candidate.getIconX(), (float) candidate.getIconY(),
                    (float) w, (float) h);
            return;
        }
        // Turned around its own centre, as icon-rotate is defined
        AffineTransform at = new AffineTransform();
        at.translate(candidate.getIconX() + w / 2, candidate.getIconY() + h / 2);
        at.rotate(-rotate);
        at.translate(-w / 2, -h / 2);
        at.scale(w, h);
        stream.drawImage(xobject, new Matrix(at));
    }

    private static void setSymbolColor(PDPageContentStream stream, Color color, float layerOpacity)
            throws IOException {
        Color used = color == null ? Color.BLACK : color;
        stream.setNonStrokingColor(used);
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setNonStrokingAlphaConstant(used.getAlpha() / 255f * layerOpacity);
        stream.setGraphicsStateParameters(gs);
    }

    private static void add(PDPageContentStream stream, CoordinateSequence csq) throws IOException {
        for (int i = 0; i < csq.size(); i++) {
            float x = (float) csq.getX(i);
            float y = (float) csq.getY(i);
            if (i == 0) {
                stream.moveTo(x, y);
            } else {
                stream.lineTo(x, y);
            }
        }
    }

    private static void addRing(PDPageContentStream stream, CoordinateSequence csq) throws IOException {
        add(stream, csq);
        stream.closePath();
    }
}
