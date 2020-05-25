package org.oskari.print.request;

import java.awt.Color;
import java.util.List;

import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.oskari.print.util.StyleUtil;

public class PDPrintStyle {
    private Color lineColor;
    private float [] linePattern;
    private Color fillColor;
    private float lineWidth;
    private int lineJoin;
    private int lineCap;
    private PDColor fillPattern;
    private PDFormXObject icon;
    private List<String> labelProperty;
    private LabelAlign labelAlign;

    public PDPrintStyle () {
        lineWidth = 1f;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public float[] getLinePattern() {
        return linePattern;
    }

    public void setLinePattern(float[] linePattern) {
        this.linePattern = linePattern;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public PDColor getFillPattern() {
        return fillPattern;
    }

    public void setFillPattern(PDColor fillPattern) {
        this.fillPattern = fillPattern;
    }

    public int getLineJoin() {
        return lineJoin;
    }

    public void setLineJoin(int lineJoin) {
        this.lineJoin = lineJoin;
    }

    public int getLineCap() {
        return lineCap;
    }

    public void setLineCap(int lineCap) {
        this.lineCap = lineCap;
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
    public LabelAlign getLabelAlign () {
        return labelAlign;
    }
    public void setLabelAlign (LabelAlign labelAlign) {
        this.labelAlign = labelAlign;
    }

    public boolean hasFillPattern () {
        return fillPattern != null;
    }
    public boolean hasLinePattern () {
        return linePattern != null;
    }
    public boolean hasFillColor () {
        return fillColor != null;
    }
    public boolean hasLineColor () {
        return lineColor != null;
    }
    public boolean hasLabels () {
        return labelProperty != null && !labelProperty.isEmpty();
    }

    public static class LabelAlign {
        private float offsetX;
        private float offsetY;
        private String align;
        public LabelAlign (String align, float offsetX, float offsetY) {
            this.align = align;
            this.offsetX = offsetX;
            this.offsetY = -offsetY; // different direction than in frontend
        }
        public float getOffsetX() { return offsetX; }
        public float getOffsetY() { return offsetY; }
        public String getAlign() { return align; }
    }

}
