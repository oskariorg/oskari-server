package org.oskari.print.mvt.glyph;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;

/**
 * Glyph outlines as form XObjects, so each character is written once per
 * document and drawn by reference. Glyphs stay paths rather than a PDF font so
 * they can carry a halo and follow a line.
 */
public class GlyphForms {

    /** Em square the outlines are produced in */
    public static final float UNITS_PER_EM = 1000f;

    private final PDDocument document;
    private final GlyphSource source;
    private final Map<Object, PDFormXObject> forms = new HashMap<>();

    public GlyphForms(PDDocument document, GlyphSource source) {
        this.document = document;
        this.source = source;
    }

    public GlyphSource getSource() {
        return source;
    }

    /**
     * @param haloWidthPx halo width in pixels, 0 for the glyph itself
     */
    public void draw(PDPageContentStream stream, int codePoint, Matrix transform,
            float haloWidthPx) throws IOException {
        PDFormXObject form = getForm(codePoint, haloWidthPx);
        if (form == null) {
            return;
        }
        stream.saveGraphicsState();
        stream.transform(transform);
        stream.drawForm(form);
        stream.restoreGraphicsState();
    }

    private PDFormXObject getForm(int codePoint, float haloWidthPx) throws IOException {
        Object key = haloWidthPx > 0 ? new HaloKey(codePoint, haloWidthPx) : (Integer) codePoint;
        if (forms.containsKey(key)) {
            // A character the source can't draw is remembered as a null so that
            // it isn't contoured again for every label that uses it
            return forms.get(key);
        }
        GeneralPath outline = haloWidthPx > 0
                ? source.getHaloOutline(codePoint, haloWidthPx)
                : source.getOutline(codePoint);
        PDFormXObject form = outline == null ? null : createForm(outline);
        forms.put(key, form);
        return form;
    }

    private PDFormXObject createForm(GeneralPath outline) throws IOException {
        PDStream stream = new PDStream(document);
        try (OutputStream out = stream.createOutputStream(COSName.FLATE_DECODE)) {
            writePath(out, outline);
        }
        PDFormXObject form = new PDFormXObject(stream);
        Rectangle2D bounds = outline.getBounds2D();
        form.setBBox(new PDRectangle((float) bounds.getX(), (float) bounds.getY(),
                (float) bounds.getWidth(), (float) bounds.getHeight()));
        form.setResources(new PDResources());
        return form;
    }

    /**
     * Writes the outline as path operators. The form carries no colour of its
     * own, so it is filled with whatever the caller set before drawing it.
     */
    private static void writePath(OutputStream out, GeneralPath outline) throws IOException {
        StringBuilder sb = new StringBuilder();
        float[] c = new float[6];
        PathIterator it = outline.getPathIterator(null);
        while (!it.isDone()) {
            switch (it.currentSegment(c)) {
            case PathIterator.SEG_MOVETO:
                sb.append(f(c[0])).append(' ').append(f(c[1])).append(" m\n");
                break;
            case PathIterator.SEG_LINETO:
                sb.append(f(c[0])).append(' ').append(f(c[1])).append(" l\n");
                break;
            case PathIterator.SEG_CUBICTO:
                sb.append(f(c[0])).append(' ').append(f(c[1])).append(' ')
                        .append(f(c[2])).append(' ').append(f(c[3])).append(' ')
                        .append(f(c[4])).append(' ').append(f(c[5])).append(" c\n");
                break;
            case PathIterator.SEG_CLOSE:
                sb.append("h\n");
                break;
            default:
                break;
            }
            it.next();
        }
        // Even-odd keeps the counters of "o" and "a" open
        sb.append("f*\n");
        out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Two decimals is well under a thousandth of the 1000 unit em
     */
    private static String f(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static class HaloKey {
        private final int codePoint;
        private final float haloWidthPx;

        private HaloKey(int codePoint, float haloWidthPx) {
            this.codePoint = codePoint;
            this.haloWidthPx = haloWidthPx;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof HaloKey)) {
                return false;
            }
            HaloKey key = (HaloKey) other;
            return codePoint == key.codePoint && Float.compare(haloWidthPx, key.haloWidthPx) == 0;
        }

        @Override
        public int hashCode() {
            return 31 * codePoint + Float.floatToIntBits(haloWidthPx);
        }
    }
}
