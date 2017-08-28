package org.oskari.print;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.oskari.print.loader.AsyncImageLoader;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.util.PDFBoxUtil;
import org.oskari.print.util.Units;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class PDF {

    private static final Logger LOG = LogFactory.getLogger(PDF.class);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private static final PDRectangle[] PAGESIZES = 
            new PDRectangle[] { PDRectangle.A4, PDRectangle.A3, PDRectangle.A2 };
    private static final PDRectangle[] PAGESIZES_LANDSCAPE;

    private static final float MAP_MIN_MARGINALS = PDFBoxUtil.mmToPt(10); 

    private static final PDFont FONT = PDType1Font.HELVETICA;
    private static final float FONT_SIZE = 12f;
    private static final float FONT_SIZE_SCALE = 10f;

    private static final float OFFSET_DATE_RIGHT = PDFBoxUtil.mmToPt(40);
    private static final float OFFSET_DATE_TOP = PDFBoxUtil.mmToPt(10);

    private static final float OFFSET_LOGO_LEFT = PDFBoxUtil.mmToPt(10);
    private static final float OFFSET_LOGO_BOTTOM = PDFBoxUtil.mmToPt(5);

    private static final float OFFSET_SCALE_LEFT = PDFBoxUtil.mmToPt(40);
    private static final float OFFSET_SCALE_BOTTOM = PDFBoxUtil.mmToPt(5);

    private static final double[] SCALE_LINE_DISTANCES_METRES = new double[24];

    static {
        PAGESIZES_LANDSCAPE = new PDRectangle[PAGESIZES.length];
        for (int i = 0; i < PAGESIZES.length; i++) {
            PDRectangle rect = PAGESIZES[i];
            PAGESIZES_LANDSCAPE[i] = new PDRectangle(rect.getHeight(), rect.getHeight());
        }

        // 1, 2, 5, 10, 20, 50, ...
        SCALE_LINE_DISTANCES_METRES[0] = 1;
        SCALE_LINE_DISTANCES_METRES[1] = 2;
        SCALE_LINE_DISTANCES_METRES[2] = 5;
        for (int i = 3; i < SCALE_LINE_DISTANCES_METRES.length; i++) {
            SCALE_LINE_DISTANCES_METRES[i] = SCALE_LINE_DISTANCES_METRES[i - 3] * 10;
        }
    }

    /**
     * This method should be called via PrintService
     */
    protected static void getPDF(PrintRequest request, PDDocument doc) 
            throws IOException, IllegalArgumentException {
        float mapWidth = pixelsToPoints(request.getWidth());
        float mapHeight = pixelsToPoints(request.getHeight());

        PDRectangle pageSize = findMinimalPageSize(
                mapWidth + MAP_MIN_MARGINALS, 
                mapHeight + MAP_MIN_MARGINALS);
        if (pageSize == null) {
            LOG.info("Could not find page size! width:", mapWidth, "height:", mapHeight);
            throw new IllegalArgumentException("Could not find a proper page size!");
        }

        // Init requests to run in the background
        List<Future<BufferedImage>> layerImages = AsyncImageLoader.initLayers(request);

        PDPage page = new PDPage(pageSize);
        doc.addPage(page);

        // Center map
        float x = (pageSize.getWidth() - mapWidth) / 2;
        float y = (pageSize.getHeight() - mapHeight) / 2;

        try (PDPageContentStream stream = new PDPageContentStream(doc, page, AppendMode.APPEND, false)) {
            drawTitle(stream, request, pageSize);
            drawLogo(doc, stream, request);
            drawScale(stream, request);
            drawDate(stream, request, pageSize);
            drawLayers(doc, stream, request.getLayers(), layerImages, 
                    x, y, mapWidth, mapHeight);
            drawBorder(stream, x, y, mapWidth, mapHeight);
        }
    }

    /**
     * Convert pixels in OGC DPI => PDF Dots
     */
    private static float pixelsToPoints(int px) {
        return (float) (Units.PDF_DPI * px / Units.OGC_DPI);
    }

    /**
     * Find minimum pagesize that will fit the map
     */
    private static PDRectangle findMinimalPageSize(float width, float height) {
        boolean landscape = width > height;
        PDRectangle[] pageSizes = landscape ? PAGESIZES_LANDSCAPE : PAGESIZES;
        for (PDRectangle pageSize : pageSizes) {
            if (width <= pageSize.getWidth()
                    && height <= pageSize.getHeight()) {
                return pageSize;
            }
        }
        return null;
    }

    private static void drawTitle(PDPageContentStream stream, 
            PrintRequest request, PDRectangle pageSize) throws IOException {
        String title = request.getTitle();
        if (title == null || title.length() == 0) {
            return;
        }

        float x = pageSize.getWidth() / 2;
        float mapHeight = request.getHeight();
        float marginBottomPx = (pageSize.getHeight() - mapHeight) / 2;
        float y = marginBottomPx + mapHeight + 5;

        PDFBoxUtil.drawTextCentered(stream, title, FONT, FONT_SIZE, x, y);
    }

    private static void drawLogo(PDDocument doc, PDPageContentStream stream,
            PrintRequest request) throws IOException {
        String logoPath = request.getLogo();
        if (logoPath == null || logoPath.length() == 0) {
            return;
        }

        try {
            PDImageXObject img = PDImageXObject.createFromFile(logoPath, doc);
            float x = OFFSET_LOGO_LEFT;
            float y = OFFSET_LOGO_BOTTOM;
            stream.drawImage(img, x, y);
        } catch (IllegalArgumentException | IOException e) {
            LOG.warn("Failed to draw logo from path:", logoPath);
        }
    }

    private static void drawDate(PDPageContentStream stream, 
            PrintRequest request, PDRectangle pageSize) throws IOException {
        if (!request.isShowDate()) {
            return;
        }

        String date = SDF.format(new Date());
        float x = pageSize.getWidth() - OFFSET_DATE_RIGHT;
        float y = pageSize.getHeight() - OFFSET_DATE_TOP;
        PDFBoxUtil.drawText(stream, date, FONT, FONT_SIZE, x, y);
    }

    private static void drawScale(PDPageContentStream stream, PrintRequest request) 
            throws IOException {
        if (!request.isShowScale()) {
            return;
        }

        String units = request.getUnits();
        if (units == null) {
            LOG.debug("Units not available in request, not drawing Scale Line");
            return;
        }
        units = units.toLowerCase();

        double mppx = Double.NaN;

        switch (units) {
        case "degrees":
        case "dd":
            LOG.debug("Map units is deegrees, not drawing Scale Line");
            return;
        case "m":
            mppx = request.getResolution();
            break;
        case "km":
            mppx = request.getResolution() * 1000;
            break;
        case "ft":
            mppx = request.getResolution() * Units.METRES_PER_FOOT;
            break;
        case "mi":
            mppx = request.getResolution() * Units.METRES_PER_MILE;
            break;
        default:
            LOG.warn("Unknown unit", units, "- not drawing Scale line");
            return;
        }

        double mppt = mppx * Units.PDF_DPI / Units.OGC_DPI;

        // Draw atleast 50pt
        double minDistance = mppt * 50;
        double distance = SCALE_LINE_DISTANCES_METRES[0];
        for (int i = 1; i < SCALE_LINE_DISTANCES_METRES.length; i++) {
            double d = SCALE_LINE_DISTANCES_METRES[i];
            if (d > minDistance) {
                distance = d;
                break;
            }
        }

        String distanceStr;
        if (distance > 1000) {
            distanceStr = Math.round(distance / 1000) + " km";
        } else {
            distanceStr = Math.round(distance) + " m";
        }

        double pt = distance / mppt;

        // PDF (and PDFBox) uses single precision floating point numbers
        float x1 = (float) OFFSET_SCALE_LEFT;
        float y1 = (float) OFFSET_SCALE_BOTTOM;
        float x2 = (float) (OFFSET_SCALE_LEFT + pt);
        float y2 = y1 + 10;

        stream.moveTo(x1, y2);
        stream.lineTo(x1, y1);
        stream.lineTo(x2, y1);
        stream.lineTo(x2, y2);
        stream.stroke();

        float cx = x1 + ((x2 - x1) / 2);
        PDFBoxUtil.drawTextCentered(stream, distanceStr, 
                FONT, FONT_SIZE_SCALE, cx, y1 + 5);
    }

    private static void drawLayers(PDDocument doc, PDPageContentStream stream, 
            List<PrintLayer> layers, List<Future<BufferedImage>> images, 
            float x, float y, float w, float h) throws IOException {
        for (int i = 0; i < layers.size(); i++) {
            PrintLayer layer = layers.get(i);
            Future<BufferedImage> image = images.get(i);
            try {
                BufferedImage bi = image.get();
                PDImageXObject imgObject = LosslessFactory.createFromImage(doc, bi);

                // Set layer (Optional Content Group)
                PDOptionalContentGroup ocg = PDFBoxUtil.getOCG(doc, layer.getId());
                PDFBoxUtil.setOCG(imgObject, ocg);

                int opacity = layer.getOpacity();

                if (opacity < 100) {
                    stream.saveGraphicsState();
                    PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                    gs.setNonStrokingAlphaConstant(0.01f * opacity);
                    stream.setGraphicsStateParameters(gs);
                    stream.drawImage(imgObject, x, y, w, h);
                    stream.restoreGraphicsState();
                } else {
                    stream.drawImage(imgObject, x, y, w, h);
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn(e);
                throw new IOException(e.getMessage());
            }
        }
    }

    private static void drawBorder(PDPageContentStream stream, 
            float x, float y, float mapWidthPt, float mapHeightPt) 
                    throws IOException {
        stream.saveGraphicsState();
        stream.setLineWidth(0.5f);
        stream.addRect(x, y, mapWidthPt, mapHeightPt);
        stream.stroke();
        stream.restoreGraphicsState();
    }

}
