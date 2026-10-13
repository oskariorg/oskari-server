package org.oskari.print.mvt;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.print.mvt.glyph.BlockGlyphSource;
import org.oskari.print.mvt.glyph.GlyphSource;

public class LineLabelTest {

    private static final double DELTA = 0.001;

    private static final GlyphSource GLYPHS = new BlockGlyphSource();

    /** Half an em per character, so a character is 10 wide */
    private static final float FONT_SIZE = 20;

    private static List<LabelCandidate.Chunk> layout(double[] line, String text, double maxAngle,
            double letterSpacing, boolean keepUpright) {
        return LineLabel.layout(new LinePath(line), text, 0, maxAngle, GLYPHS, FONT_SIZE,
                letterSpacing, keepUpright);
    }

    @Test
    public void followsAGentleBend() {
        double[] line = { 0, 0, 20, 0, 40, 5 };
        List<LabelCandidate.Chunk> chunks = layout(line, "ABCDEF", Math.PI / 4, 0, true);
        Assertions.assertNotNull(chunks, "a gentle bend takes the label");
        Assertions.assertEquals(2, chunks.size(), "the label splits at the bend");
        Assertions.assertEquals("AB", chunks.get(0).getText(), "two characters fit the first segment");
        Assertions.assertEquals("CDEF", chunks.get(1).getText(), "the rest are on the second");

        // C is centred on the line 25 along it, turned to the second segment
        LinePath path = new LinePath(line);
        LabelCandidate.Chunk second = chunks.get(1);
        Assertions.assertEquals(Math.atan2(5, 20), second.getAngle(), DELTA,
                "the chunk follows its own segment");
        Assertions.assertEquals(path.getX(25), second.getX() + Math.cos(second.getAngle()) * 5, DELTA,
                "the first character of the chunk is centred on the line, x");
        Assertions.assertEquals(path.getY(25), second.getY() + Math.sin(second.getAngle()) * 5, DELTA,
                "the first character of the chunk is centred on the line, y");
    }

    @Test
    public void rejectsALineThatBendsTooSharply() {
        // A right angle in the middle of the label
        List<LabelCandidate.Chunk> chunks = layout(new double[] { 0, 0, 30, 0, 30, 40 }, "ABCDEF",
                Math.PI / 8, 0, true);
        Assertions.assertNull(chunks, "a bend past max-angle rejects the label");
    }

    @Test
    public void reversesALabelThatWouldReadBackwards() {
        // Westward line: the label would read right to left over x 100..70
        List<LabelCandidate.Chunk> chunks = layout(new double[] { 100, 0, 0, 0 }, "ABC",
                Math.PI / 4, 0, true);
        Assertions.assertNotNull(chunks, "a westward line takes the label");
        Assertions.assertEquals(0, chunks.get(0).getAngle(), DELTA, "the label is turned upright");
        Assertions.assertEquals(70, chunks.get(0).getX(), DELTA,
                "laid out from the other end, over the same stretch of line");
    }

    @Test
    public void coversTheLetterSpacingAcrossABend() {
        double[] line = { 0, 0, 20, 0, 100, 20 };
        // A quarter em after each character: six characters take 90 instead of 60
        List<LabelCandidate.Chunk> chunks = layout(line, "ABCDEF", Math.PI / 4, 0.25, true);
        Assertions.assertNotNull(chunks, "a gentle bend takes the label");
        Assertions.assertEquals("A", chunks.get(0).getText(),
                "the first segment holds only the characters that fit with their spacing");

        // The second chunk starts at B, 15 along, and its five characters take 75
        LinePath path = new LinePath(line);
        LabelCandidate.Chunk last = chunks.get(chunks.size() - 1);
        Assertions.assertEquals("BCDEF", last.getText(), "the rest share the second segment");
        Assertions.assertEquals(path.getX(90), last.getX() + Math.cos(last.getAngle()) * 75, DELTA,
                "the label ends at the spaced length, x");
        Assertions.assertEquals(path.getY(90), last.getY() + Math.sin(last.getAngle()) * 75, DELTA,
                "the label ends at the spaced length, y");
    }
}
