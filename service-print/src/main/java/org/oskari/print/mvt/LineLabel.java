package org.oskari.print.mvt;

import java.util.ArrayList;
import java.util.List;

import org.oskari.print.mvt.glyph.GlyphForms;
import org.oskari.print.mvt.glyph.GlyphSource;

/**
 * Lays a label out along a line one character at a time: each character is
 * centred on the line at its own distance along it and turned to the segment
 * under it. A placement is rejected where the line turns more sharply between
 * two characters than the text can follow (cf. Wolff et al. 2001, "A simple and
 * efficient algorithm for high-quality line labeling").
 */
class LineLabel {

    private LineLabel() {}

    /**
     * @param start how far along the line the label starts
     * @param maxAngle sharpest turn allowed between neighbouring characters, in
     *        radians
     * @param fontSize label size, in the units of the line
     * @param letterSpacing extra space after each character, in em
     * @param keepUpright whether a label that would read right to left is laid
     *        out from the other end of the line instead
     * @return the characters as runs that share a segment, each starting where
     *         its first character does, null when the line bends too sharply
     */
    public static List<LabelCandidate.Chunk> layout(LinePath path, String text, double start,
            double maxAngle, GlyphSource glyphs, float fontSize, double letterSpacing,
            boolean keepUpright) {
        List<String> characters = split(text);
        double[] advances = getAdvances(characters, glyphs, fontSize, letterSpacing);
        double end = start + sum(advances);
        if (keepUpright && path.getX(end) < path.getX(start)) {
            path = path.reverse();
            start = path.getLength() - end;
        }

        List<LabelCandidate.Chunk> chunks = new ArrayList<>();
        int previousSegment = -1;
        double distance = start;
        for (int i = 0; i < characters.size(); i++) {
            double middle = distance + advances[i] / 2;
            int segment = path.getSegment(middle);
            if (segment == previousSegment) {
                appendToLast(chunks, characters.get(i));
            } else {
                if (previousSegment >= 0 && bendsTooSharply(path, previousSegment, segment, maxAngle)) {
                    return null;
                }
                double angle = path.getAngle(segment);
                // Centred on the line, so the character starts half its advance back
                double x = path.getX(middle) - Math.cos(angle) * advances[i] / 2;
                double y = path.getY(middle) - Math.sin(angle) * advances[i] / 2;
                chunks.add(new LabelCandidate.Chunk(characters.get(i), x, y, angle));
            }
            previousSegment = segment;
            distance += advances[i];
        }
        return chunks;
    }

    private static boolean bendsTooSharply(LinePath path, int fromSegment, int toSegment,
            double maxAngle) {
        for (int segment = fromSegment + 1; segment <= toSegment; segment++) {
            if (Math.abs(path.getTurn(segment)) > maxAngle) {
                return true;
            }
        }
        return false;
    }

    private static void appendToLast(List<LabelCandidate.Chunk> chunks, String character) {
        LabelCandidate.Chunk last = chunks.get(chunks.size() - 1);
        chunks.set(chunks.size() - 1, new LabelCandidate.Chunk(last.getText() + character,
                last.getX(), last.getY(), last.getAngle()));
    }

    private static List<String> split(String text) {
        List<String> characters = new ArrayList<>();
        text.codePoints().forEach(codePoint -> characters.add(Character.toString(codePoint)));
        return characters;
    }

    /**
     * @return how far each character moves the pen, letter spacing included
     */
    private static double[] getAdvances(List<String> characters, GlyphSource glyphs,
            float fontSize, double letterSpacing) {
        double[] advances = new double[characters.size()];
        for (int i = 0; i < advances.length; i++) {
            advances[i] = (glyphs.getWidth(characters.get(i)) / GlyphForms.UNITS_PER_EM
                    + letterSpacing) * fontSize;
        }
        return advances;
    }

    private static double sum(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }
}
