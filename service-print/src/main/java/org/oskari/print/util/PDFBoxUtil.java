package org.oskari.print.util;

import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.util.Matrix;

public class PDFBoxUtil {

    private PDFBoxUtil() {}

    public static float mmToPt(int mm) {
        return (float) (mm * Units.PDF_DPI / Units.MM_PER_INCH);
    }

    public static float getTextWidth(String text, PDFont font, float fontSize)
            throws IOException {
        return font.getStringWidth(text) * fontSize / 1000f;
    }

    public static void drawTextCentered(PDPageContentStream stream,
                                        String text, PDFont font, float fontSize, float x, float y)
            throws IOException {
        if (text == null || text.length() == 0) {
            return;
        }

        float textWidthHalf = getTextWidth(text, font, fontSize) / 2;
        drawText(stream, text, font, fontSize, x - textWidthHalf, y);
    }

    public static void drawText(PDPageContentStream stream,
                                String text, PDFont font, float fontSize, float x, float y)
            throws IOException {
        if (text == null || text.length() == 0) {
            return;
        }

        stream.saveGraphicsState();
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.setTextMatrix(Matrix.getTranslateInstance(x, y));
        stream.showText(text);
        stream.endText();
        stream.restoreGraphicsState();
    }

    public static PDOptionalContentGroup getOCG(PDDocument doc, String name) {
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        PDOptionalContentProperties ocprops = catalog.getOCProperties();
        if (ocprops == null) {
            ocprops = new PDOptionalContentProperties();
            catalog.setOCProperties(ocprops);
        }

        PDOptionalContentGroup ocg = ocprops.getGroup(name);
        if (ocg == null) {
            ocg = new PDOptionalContentGroup(name);
            ocprops.addGroup(ocg);
        }

        return ocg;
    }

    public static void setOCG(PDXObject object, PDOptionalContentGroup ocg) {
        if (object != null && ocg != null) {
            object.getCOSObject().setItem(COSName.OC, ocg);
        }
    }

    public static void closeSilently(PDDocument doc) {
        try {
            doc.close();
        } catch (IOException ignore) {}
    }

}