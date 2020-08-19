package org.oskari.print.request;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.oskari.print.util.PDFBoxUtil;

public class PDPrintStyle {

    public static final PDFont FONT = PDType1Font.HELVETICA;
    public static final PDFont FONT_BOLD = PDType1Font.HELVETICA_BOLD;
    public static final float FONT_SIZE = 12f;
    public static final float FONT_SIZE_SCALE = 10f;
    public static final float FONT_SIZE_TIMESERIES = 10f;

    private float lineWidth;
    private LineJoin lineJoin;
    private LineCap lineCap;
    private LinePattern linePattern;
    private PDColor strokeColor;
    private PDColor fillColor;
    private PDFormXObject icon;
    private List<String> labelProperty;
    private LabelAlign labelAlign;

    public PDPrintStyle () {
        lineWidth = 1f;
        lineJoin = LineJoin.miter;
        lineCap = LineCap.butt;
        linePattern = LinePattern.solid;
    }

    public void apply(PDPageContentStream stream) throws IOException {
        stream.setLineWidth(lineWidth);
        stream.setLineJoinStyle(lineJoin.code);
        stream.setLineCapStyle(lineCap.code);
        stream.setLineDashPattern(linePattern.f.apply(lineWidth), 0);
        if (strokeColor != null) {
            stream.setStrokingColor(strokeColor);
        }
        if (fillColor != null) {
            stream.setNonStrokingColor(fillColor);
        }
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public void setLineJoin(LineJoin lineJoin) {
        this.lineJoin = lineJoin;
    }

    public void setLineCap(LineCap lineCap) {
        this.lineCap = lineCap;
    }

    public void setLinePattern(LinePattern linePattern) {
        this.linePattern = linePattern;
    }

    public void setStrokeColor(PDColor strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setStrokeColor(Color strokeColor) {
        setStrokeColor(toRGBColor(strokeColor));
    }

    public void setFillColor(PDColor fillColor) {
        this.fillColor = fillColor;
    }

    public void setFillColor(Color color) {
        setFillColor(toRGBColor(color));
    }

    public PDFormXObject getIcon() {
        return icon;
    }

    public void setIcon(PDFormXObject icon) {
        this.icon = icon;
    }

    public List<String> getLabelProperty() {
        return labelProperty;
    }

    public void setLabelProperty(List<String> labelProperty) {
        this.labelProperty = labelProperty;
    }

    public LabelAlign getLabelAlign() {
        return labelAlign;
    }

    public void setLabelAlign(LabelAlign labelAlign) {
        this.labelAlign = labelAlign;
    }

    public boolean hasLineColor() {
        return strokeColor != null;
    }

    public boolean hasFillColor() {
        return fillColor != null;
    }

    public boolean hasLabels () {
        return labelProperty != null && !labelProperty.isEmpty();
    }

    private static PDColor toRGBColor(Color color) {
        if (color == null) {
            return null;
        }
        float[] components = new float[] {
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f
        };
        return new PDColor(components, PDDeviceRGB.INSTANCE);
    }

    public static class LabelAlign {
        @FunctionalInterface
        public interface AlignStrategy {
            public float alignX(float width);
        }
        public enum Align {
            left(width -> 0),
            center(width -> width / 2),
            right(width -> width),
            end(width -> 0),
            start(width -> width);

            final AlignStrategy strategy;

            private Align(AlignStrategy strategy) {
                this.strategy = strategy;
            }
        }

        private float offsetX;
        private float offsetY;
        private Align align;
        public LabelAlign (String align, float offsetX, float offsetY) {
            try {
                this.align = Align.valueOf(align.toLowerCase());
            }catch (Exception ignored) {
                this.align = Align.left;
            }
            this.offsetX = offsetX;
            this.offsetY = -offsetY; // different direction than in frontend
        }
        private float getWidth(String label) {
            try {
                return PDFBoxUtil.getTextWidth(label, FONT, FONT_SIZE);
            } catch (IOException ignored) {}
            return 0;
        }
        public float getLabelX (String label) {
            float width = getWidth(label);
            return offsetX - align.strategy.alignX(width);
        }
        public float getLabelY () {
            return offsetY;
        }
    }

    public enum LineCap {

        butt(0),
        round(1),
        square(2);

        public final int code;

        private LineCap(int code) {
            this.code = code;
        }

        public static LineCap get(String key) {
            try {
                return valueOf(key);
            } catch (Exception ignore) {
                return null;
            }
        }
    }

    public enum LineJoin {

        mitre(0),
        miter(0),
        round(1),
        bevel(2);

        public final int code;

        private LineJoin(int code) {
            this.code = code;
        }

        public static LineJoin get(String key) {
            try {
                return valueOf(key);
            } catch (Exception ignore) {
                return null;
            }
        }
    }

    public enum LinePattern {

        solid(width -> new float[] { 0 }),
        dash(width -> new float[] { 5, 4 + width }),
        dashdot(width -> new float[] { 1, 1 + width }),
        dot(width -> new float[] { 1, 1 + width }),
        longdash(width -> new float[] { 10, 4 + width }),
        longdashdot(width -> new float[] { 5, 1 + width, 1, 1 + width });

        public final Function<Float, float[]> f;

        private LinePattern(Function<Float, float[]> f) {
            this.f = f;
        }

        public static LinePattern get(String key) {
            try {
                return valueOf(key);
            } catch (Exception ignore) {
                return null;
            }
        }

    }

}
