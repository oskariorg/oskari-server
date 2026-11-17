package org.oskari.print.mvt.glyph;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contours a glyph's signed distance field into an outline with marching
 * squares. Coordinates are in a 1000 unit em with the origin on the baseline at
 * the pen position.
 */
public class SDFVectorizer {

    /**
     * Simplification tolerance in field pixels. One field pixel is about 42 em
     * units, so this keeps the error far below an output pixel at any label size
     * while removing the staircase of collinear marching squares segments.
     */
    private static final double SIMPLIFY_TOLERANCE = 0.02;

    private SDFVectorizer() {}

    /**
     * @param level distance field value to contour at. {@link SDFGlyph#EDGE} is
     *              the glyph's own outline, a lower level grows it outwards for a halo.
     * @return outline in em units, or null when the glyph has no shape
     */
    public static GeneralPath getOutline(SDFGlyph glyph, float level) {
        if (!glyph.hasShape()) {
            return null;
        }
        List<List<double[]>> rings = getRings(glyph, level);
        if (rings.isEmpty()) {
            return null;
        }

        // Field rows grow downwards from the glyph's top edge
        double scale = GlyphForms.UNITS_PER_EM / SDFGlyph.UNITS_PER_EM;
        double originX = glyph.getLeft() - SDFGlyph.BUFFER;
        double originY = glyph.getTopAboveBaseline() + SDFGlyph.BUFFER;

        GeneralPath path = new GeneralPath(Path2D.WIND_EVEN_ODD);
        for (List<double[]> ring : rings) {
            List<double[]> simplified = simplify(ring);
            if (simplified.size() < 3) {
                continue;
            }
            for (int i = 0; i < simplified.size(); i++) {
                double[] p = simplified.get(i);
                float x = (float) ((originX + p[0]) * scale);
                float y = (float) ((originY - p[1]) * scale);
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            path.closePath();
        }
        return path.getCurrentPoint() == null ? null : path;
    }

    /**
     * Marching squares over the field.
     *
     * Each cell of four neighbouring values contributes the segments where the
     * level crosses it, with the crossing interpolated linearly along the cell's
     * edges. The segments are then stitched into closed rings by their endpoints.
     *
     * Segments are emitted with the inside of the glyph on the left.
     */
    private static List<List<double[]>> getRings(SDFGlyph glyph, float level) {
        int w = glyph.getBitmapWidth();
        int h = glyph.getBitmapHeight();

        // Segments keyed by their start point so a ring can be walked from any
        // of its segments without searching
        Map<Long, List<double[][]>> segmentsByStart = new HashMap<>();
        List<double[][]> segments = new ArrayList<>();

        // One cell past the bitmap on every side, where the field reads 0, so that
        // a halo reaching the border still closes
        for (int y = -1; y < h; y++) {
            for (int x = -1; x < w; x++) {
                float tl = glyph.getValue(x, y);
                float tr = glyph.getValue(x + 1, y);
                float br = glyph.getValue(x + 1, y + 1);
                float bl = glyph.getValue(x, y + 1);
                addCellSegments(segments, x, y, tl, tr, br, bl, level);
            }
        }

        for (double[][] segment : segments) {
            segmentsByStart.computeIfAbsent(key(segment[0]), k -> new ArrayList<>()).add(segment);
        }
        return stitch(segments, segmentsByStart);
    }

    /**
     * Adds the level's crossings through one cell.
     *
     * The case number is the four corners as bits, set when the corner is inside
     * the glyph. The ambiguous saddle cases (5 and 10) are resolved with the
     * average of the corners, which is what bilinear interpolation would give at
     * the cell's centre.
     */
    private static void addCellSegments(List<double[][]> segments, int x, int y,
            float tl, float tr, float br, float bl, float level) {
        int caseIndex = 0;
        if (tl >= level) {
            caseIndex |= 8;
        }
        if (tr >= level) {
            caseIndex |= 4;
        }
        if (br >= level) {
            caseIndex |= 2;
        }
        if (bl >= level) {
            caseIndex |= 1;
        }
        if (caseIndex == 0 || caseIndex == 15) {
            return;
        }

        double[] top = {x + interpolate(tl, tr, level), y};
        double[] right = {x + 1, y + interpolate(tr, br, level)};
        double[] bottom = {x + interpolate(bl, br, level), y + 1};
        double[] left = {x, y + interpolate(tl, bl, level)};

        switch (caseIndex) {
        case 1:
            segments.add(new double[][] {left, bottom});
            break;
        case 2:
            segments.add(new double[][] {bottom, right});
            break;
        case 3:
            segments.add(new double[][] {left, right});
            break;
        case 4:
            segments.add(new double[][] {right, top});
            break;
        case 5:
            if ((tl + tr + br + bl) / 4 >= level) {
                segments.add(new double[][] {left, top});
                segments.add(new double[][] {right, bottom});
            } else {
                segments.add(new double[][] {left, bottom});
                segments.add(new double[][] {right, top});
            }
            break;
        case 6:
            segments.add(new double[][] {bottom, top});
            break;
        case 7:
            segments.add(new double[][] {left, top});
            break;
        case 8:
            segments.add(new double[][] {top, left});
            break;
        case 9:
            segments.add(new double[][] {top, bottom});
            break;
        case 10:
            if ((tl + tr + br + bl) / 4 >= level) {
                segments.add(new double[][] {top, right});
                segments.add(new double[][] {bottom, left});
            } else {
                segments.add(new double[][] {top, left});
                segments.add(new double[][] {bottom, right});
            }
            break;
        case 11:
            segments.add(new double[][] {top, right});
            break;
        case 12:
            segments.add(new double[][] {right, left});
            break;
        case 13:
            segments.add(new double[][] {right, bottom});
            break;
        case 14:
            segments.add(new double[][] {bottom, left});
            break;
        default:
            break;
        }
    }

    /**
     * @return where between the two values the level sits, 0.5 when they are equal
     */
    private static double interpolate(float from, float to, float level) {
        float delta = to - from;
        if (delta == 0) {
            return 0.5;
        }
        double t = (level - from) / delta;
        return Math.max(0, Math.min(1, t));
    }

    /**
     * Walks the segments end to end into rings.
     */
    private static List<List<double[]>> stitch(List<double[][]> segments,
            Map<Long, List<double[][]>> segmentsByStart) {
        List<List<double[]>> rings = new ArrayList<>();
        Set<double[][]> used = new HashSet<>();

        for (double[][] start : segments) {
            if (used.contains(start)) {
                continue;
            }
            List<double[]> ring = new ArrayList<>();
            double[][] current = start;
            while (current != null && used.add(current)) {
                ring.add(current[0]);
                current = next(current[1], segmentsByStart, used);
            }
            if (ring.size() >= 3) {
                rings.add(ring);
            }
        }
        return rings;
    }

    private static double[][] next(double[] end, Map<Long, List<double[][]>> segmentsByStart,
            Set<double[][]> used) {
        List<double[][]> candidates = segmentsByStart.get(key(end));
        if (candidates == null) {
            return null;
        }
        for (double[][] candidate : candidates) {
            if (!used.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Endpoints are produced by interpolating along a cell edge, so two cells
     * sharing an edge produce bit identical values. Quantising guards against
     * the cases where they don't.
     */
    private static long key(double[] point) {
        long x = Math.round(point[0] * 4096);
        long y = Math.round(point[1] * 4096);
        return (x << 26) ^ y;
    }

    /**
     * Douglas-Peucker on a closed ring. Marching squares produces a vertex per
     * cell edge crossing, most of which are collinear along a straight stem.
     */
    private static List<double[]> simplify(List<double[]> ring) {
        if (ring.size() < 4) {
            return ring;
        }
        boolean[] keep = new boolean[ring.size()];
        keep[0] = true;
        keep[ring.size() - 1] = true;
        simplifySection(ring, 0, ring.size() - 1, keep);

        List<double[]> simplified = new ArrayList<>();
        for (int i = 0; i < ring.size(); i++) {
            if (keep[i]) {
                simplified.add(ring.get(i));
            }
        }
        return simplified;
    }

    private static void simplifySection(List<double[]> ring, int first, int last,
            boolean[] keep) {
        if (last <= first + 1) {
            return;
        }
        double[] a = ring.get(first);
        double[] b = ring.get(last);
        double maxDistance = -1;
        int farthest = -1;
        for (int i = first + 1; i < last; i++) {
            double distance = distanceToSegment(ring.get(i), a, b);
            if (distance > maxDistance) {
                maxDistance = distance;
                farthest = i;
            }
        }
        if (maxDistance <= SIMPLIFY_TOLERANCE) {
            return;
        }
        keep[farthest] = true;
        simplifySection(ring, first, farthest, keep);
        simplifySection(ring, farthest, last, keep);
    }

    private static double distanceToSegment(double[] p, double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) {
            return Math.hypot(p[0] - a[0], p[1] - a[1]);
        }
        double t = ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        return Math.hypot(p[0] - (a[0] + t * dx), p[1] - (a[1] + t * dy));
    }
}
