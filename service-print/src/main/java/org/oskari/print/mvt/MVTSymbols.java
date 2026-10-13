package org.oskari.print.mvt;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.oskari.print.mvt.glyph.GlyphForms;
import org.oskari.print.mvt.glyph.GlyphSource;
import org.oskari.print.mvt.sprite.SpriteAtlas;

/**
 * Lays out a symbol layer's labels and icons without drawing them.
 */
class MVTSymbols {

    /**
     * Line labels repeat at this many times symbol-spacing. Taken literally,
     * the spacing names a road several times over on a printed page where the
     * map on screen names it once or twice.
     */
    private static final float REPEAT_FACTOR = 2;
    /** A label is centred on the cap height rather than the whole em box */
    private static final float CAP_HEIGHT = 0.717f;
    /** Gap between the lines of a wrapped label, in ems */
    private static final float LINE_HEIGHT = 1.2f;

    private MVTSymbols() {}

    /**
     * @param geometry feature geometry, already in PDF coordinates
     * @param fontSize label size in points
     * @param pixelScale how many points one style pixel is
     * @return the candidates this feature contributes, empty when it has nothing
     *         to show or doesn't fit
     */
    public static List<LabelCandidate> place(MVTStyleLayer styleLayer, Geometry geometry,
            Map<String, Object> attributes, GlyphSource glyphs, SpriteAtlas sprites,
            float fontSize, float pixelScale) {
        List<LabelCandidate> candidates = new ArrayList<>();

        String label = glyphs != null ? getLabel(styleLayer, attributes) : null;
        String icon = null;
        if (styleLayer.hasIcon()) {
            icon = MVTTextField.resolve(styleLayer.getIconImage(), attributes);
        }
        if (label == null && icon == null) {
            return candidates;
        }

        placeAtPoint(candidates, styleLayer, geometry, label, icon, glyphs, sprites,
                fontSize, pixelScale);
        return candidates;
    }

    /**
     * @return the label the feature carries on this layer, null when it has none
     */
    public static String getLabel(MVTStyleLayer styleLayer, Map<String, Object> attributes) {
        if (!styleLayer.hasText()) {
            return null;
        }
        String label = MVTTextField.resolve(styleLayer.getTextField(), attributes);
        return MVTTextField.transform(label, styleLayer.getTextTransform());
    }

    /**
     * Lays out one label along every line that carries it, joined across
     * features and tiles.
     *
     * @param lines the geometries carrying the label, in PDF coordinates
     */
    public static List<LabelCandidate> placeAlongLines(MVTStyleLayer styleLayer,
            List<Geometry> lines, String label, GlyphSource glyphs, float fontSize,
            float pixelScale) {
        List<LabelCandidate> candidates = new ArrayList<>();
        if (glyphs == null) {
            return candidates;
        }
        List<double[]> flat = new ArrayList<>();
        for (Geometry geometry : lines) {
            collectLines(geometry, flat);
        }
        placeOnLines(candidates, styleLayer, joinLines(flat), label, glyphs, fontSize, pixelScale);
        return candidates;
    }

    private static void placeAtPoint(List<LabelCandidate> candidates, MVTStyleLayer styleLayer,
            Geometry geometry, String label, String icon, GlyphSource glyphs, SpriteAtlas sprites,
            float fontSize, float pixelScale) {
        Coordinate c = geometry.getCentroid().getCoordinate();
        if (c == null) {
            return;
        }

        LabelCandidate iconCandidate = null;
        if (icon != null && sprites != null) {
            SpriteAtlas.Icon sprite = sprites.getIcon(icon);
            if (sprite != null) {
                double w = sprite.getDisplayWidth() * styleLayer.getIconSize() * pixelScale;
                double h = sprite.getDisplayHeight() * styleLayer.getIconSize() * pixelScale;
                double x = c.x + styleLayer.getIconAnchor().getXFactor() * w
                        + styleLayer.getIconOffsetX() * pixelScale;
                double y = c.y + styleLayer.getIconAnchor().getYFactor() * h
                        - styleLayer.getIconOffsetY() * pixelScale;
                double pad = styleLayer.getIconPadding() * pixelScale;
                iconCandidate = LabelCandidate.icon(styleLayer, icon, x, y, w, h,
                        new Rectangle2D.Double(x - pad, y - pad, w + 2 * pad, h + 2 * pad),
                        styleLayer.isIconAllowOverlap(), styleLayer.isIconIgnorePlacement(),
                        styleLayer.isIconOptional());
                candidates.add(iconCandidate);
            }
        }

        if (label == null) {
            return;
        }
        List<String> lines = wrap(label, styleLayer, glyphs);
        float lineHeight = fontSize * LINE_HEIGHT;
        float blockHeight = fontSize * CAP_HEIGHT + lineHeight * (lines.size() - 1);

        // The anchor says which part of the whole block sits at the point
        double blockWidth = 0;
        for (String line : lines) {
            blockWidth = Math.max(blockWidth, width(line, styleLayer, glyphs, fontSize));
        }
        double originX = c.x + styleLayer.getTextAnchor().getXFactor() * blockWidth
                + styleLayer.getTextOffsetX() * fontSize;
        double originY = c.y + styleLayer.getTextAnchor().getYFactor() * blockHeight
                - styleLayer.getTextOffsetY() * fontSize;

        double pad = styleLayer.getTextPadding() * pixelScale;
        Rectangle2D bounds = new Rectangle2D.Double(originX - pad, originY - pad,
                blockWidth + 2 * pad, blockHeight + 2 * pad);

        List<LabelCandidate.Chunk> chunks = new ArrayList<>();
        // Lines run downwards from the top of the block
        double lineY = originY + blockHeight - fontSize * CAP_HEIGHT;
        for (String line : lines) {
            double lineWidth = width(line, styleLayer, glyphs, fontSize);
            // Wrapped lines are centred on the block, matching the browser
            double lineX = originX + (blockWidth - lineWidth) / 2;
            chunks.add(new LabelCandidate.Chunk(line, lineX, lineY, 0));
            lineY -= lineHeight;
        }
        LabelCandidate textCandidate = LabelCandidate.text(styleLayer, chunks, fontSize,
                bounds, styleLayer.isTextAllowOverlap(), styleLayer.isTextIgnorePlacement(),
                styleLayer.isTextOptional());
        candidates.add(textCandidate);
        if (iconCandidate != null) {
            LabelCandidate.pair(iconCandidate, textCandidate);
        }
    }

    private static void placeOnLines(List<LabelCandidate> candidates, MVTStyleLayer styleLayer,
            List<double[]> lines, String label, GlyphSource glyphs, float fontSize,
            float pixelScale) {
        double textLength = width(label, styleLayer, glyphs, fontSize);
        for (double[] line : lines) {
            LinePath path = new LinePath(line);
            // Mapbox doesn't overflow a label past the line it belongs to
            if (textLength == 0 || textLength > path.getLength()) {
                continue;
            }
            for (double start : getStarts(styleLayer, path.getLength(), textLength, pixelScale)) {
                List<LabelCandidate.Chunk> chunks = LineLabel.layout(path, label, start,
                        styleLayer.getTextMaxAngle(), glyphs, fontSize,
                        styleLayer.getTextLetterSpacing(), styleLayer.isTextKeepUpright());
                if (chunks != null) {
                    candidates.add(toCandidate(styleLayer, chunks, glyphs, fontSize, pixelScale));
                }
            }
        }
    }

    /**
     * @return where along the line each repeat of the label starts: centred
     *         once on line-center, otherwise centred on each stretch of
     *         {@link #REPEAT_FACTOR} times symbol-spacing the line has room for
     */
    private static List<Double> getStarts(MVTStyleLayer styleLayer, double length,
            double textLength, float pixelScale) {
        if (styleLayer.getPlacement() == MVTStyleLayer.Placement.LINE_CENTER) {
            return List.of((length - textLength) / 2);
        }
        double spacing = styleLayer.getSymbolSpacing() * REPEAT_FACTOR * pixelScale;
        int repeats = spacing > textLength ? (int) Math.max(1, length / spacing) : 1;
        double step = length / repeats;
        List<Double> starts = new ArrayList<>(repeats);
        for (int i = 0; i < repeats; i++) {
            starts.add(step * i + (step - textLength) / 2);
        }
        return starts;
    }

    private static LabelCandidate toCandidate(MVTStyleLayer styleLayer,
            List<LabelCandidate.Chunk> chunks, GlyphSource glyphs, float fontSize,
            float pixelScale) {
        double halfHeight = fontSize * CAP_HEIGHT / 2;
        Rectangle2D bounds = null;
        for (LabelCandidate.Chunk chunk : chunks) {
            double width = width(chunk.getText(), styleLayer, glyphs, fontSize);
            double endX = chunk.getX() + Math.cos(chunk.getAngle()) * width;
            double endY = chunk.getY() + Math.sin(chunk.getAngle()) * width;
            Rectangle2D box = new Rectangle2D.Double(
                    Math.min(chunk.getX(), endX) - halfHeight,
                    Math.min(chunk.getY(), endY) - halfHeight,
                    Math.abs(endX - chunk.getX()) + 2 * halfHeight,
                    Math.abs(endY - chunk.getY()) + 2 * halfHeight);
            bounds = bounds == null ? box : bounds.createUnion(box);
        }
        double pad = styleLayer.getTextPadding() * pixelScale;
        Rectangle2D padded = new Rectangle2D.Double(bounds.getX() - pad, bounds.getY() - pad,
                bounds.getWidth() + 2 * pad, bounds.getHeight() + 2 * pad);
        return LabelCandidate.text(styleLayer, chunks, fontSize, padded,
                styleLayer.isTextAllowOverlap(), styleLayer.isTextIgnorePlacement(),
                styleLayer.isTextOptional());
    }

    private static List<String> wrap(String label, MVTStyleLayer styleLayer, GlyphSource glyphs) {
        return MVTTextField.wrap(label, styleLayer.getTextMaxWidth(),
                line -> measure(line, styleLayer, glyphs) / GlyphForms.UNITS_PER_EM);
    }

    /**
     * @return width of the text in em units, letter spacing included
     */
    private static double measure(String text, MVTStyleLayer styleLayer, GlyphSource glyphs) {
        return glyphs.getWidth(text)
                + styleLayer.getTextLetterSpacing() * GlyphForms.UNITS_PER_EM * text.length();
    }

    private static double width(String text, MVTStyleLayer styleLayer, GlyphSource glyphs,
            float fontSize) {
        return measure(text, styleLayer, glyphs) / GlyphForms.UNITS_PER_EM * fontSize;
    }

    /**
     * Chains lines that share an endpoint into single runs, reversing pieces as
     * needed, so a name longer than any one piece still fits.
     */
    static List<double[]> joinLines(List<double[]> lines) {
        if (lines.size() < 2) {
            return lines;
        }
        Map<Long, List<double[]>> byEndpoint = new HashMap<>();
        for (double[] line : lines) {
            byEndpoint.computeIfAbsent(startKey(line), k -> new ArrayList<>()).add(line);
            byEndpoint.computeIfAbsent(endKey(line), k -> new ArrayList<>()).add(line);
        }

        List<double[]> joined = new ArrayList<>();
        Set<double[]> used = Collections.newSetFromMap(new IdentityHashMap<>());
        for (double[] line : lines) {
            if (used.add(line)) {
                // Grown at the end, then at the start by growing the reversed run
                double[] run = extendEnd(line, byEndpoint, used);
                joined.add(reverse(extendEnd(reverse(run), byEndpoint, used)));
            }
        }
        return joined;
    }

    private static double[] extendEnd(double[] run, Map<Long, List<double[]>> byEndpoint,
            Set<double[]> used) {
        while (true) {
            long key = endKey(run);
            double[] next = getContinuation(byEndpoint.get(key), used);
            if (next == null) {
                return run;
            }
            used.add(next);
            run = concat(run, startKey(next) == key ? next : reverse(next));
        }
    }

    /**
     * @param meeting every line that has an endpoint here, used or not: a
     *        junction is a property of the geometry, not of the order the runs
     *        are built in
     * @return the one unused line that goes on from here, null at a dead end
     *         or at a junction, where which way the road goes on is not
     *         something the geometry says
     */
    private static double[] getContinuation(List<double[]> meeting, Set<double[]> used) {
        if (meeting == null || meeting.size() != 2) {
            return null;
        }
        for (double[] line : meeting) {
            if (!used.contains(line)) {
                return line;
            }
        }
        return null;
    }

    private static double[] concat(double[] first, double[] second) {
        // The shared point is in both, keep it once
        double[] joined = new double[first.length + second.length - 2];
        System.arraycopy(first, 0, joined, 0, first.length);
        System.arraycopy(second, 2, joined, first.length, second.length - 2);
        return joined;
    }

    private static double[] reverse(double[] line) {
        double[] reversed = new double[line.length];
        for (int i = 0, j = line.length - 2; i < line.length; i += 2, j -= 2) {
            reversed[i] = line[j];
            reversed[i + 1] = line[j + 1];
        }
        return reversed;
    }

    private static long startKey(double[] line) {
        return key(line[0], line[1]);
    }

    private static long endKey(double[] line) {
        return key(line[line.length - 2], line[line.length - 1]);
    }

    /**
     * Endpoints are compared at a tenth of a point, which is finer than anything
     * the tile's own coordinates can distinguish once they are on the page.
     */
    private static long key(double x, double y) {
        return (Math.round(x * 10) << 32) ^ (Math.round(y * 10) & 0xffffffffL);
    }

    private static void collectLines(Geometry geometry, List<double[]> lines) {
        if (geometry instanceof LineString) {
            CoordinateSequence csq = ((LineString) geometry).getCoordinateSequence();
            double[] flat = new double[csq.size() * 2];
            for (int i = 0; i < csq.size(); i++) {
                flat[i * 2] = csq.getX(i);
                flat[i * 2 + 1] = csq.getY(i);
            }
            lines.add(flat);
        } else if (geometry instanceof MultiLineString || geometry instanceof GeometryCollection) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                collectLines(geometry.getGeometryN(i), lines);
            }
        }
    }
}
