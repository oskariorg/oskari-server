package org.oskari.print;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

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
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.print.loader.AsyncImageLoader;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.util.PDFBoxUtil;
import org.oskari.print.util.Units;
import org.oskari.print.wmts.WMTSCapabilitiesCache;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;

public class PDF {

    private static final Logger LOG = LogFactory.getLogger(PDF.class);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private static final PDRectangle[] PAGESIZES = new PDRectangle[] {
            PDRectangle.A4,
            PDRectangle.A3,
            PDRectangle.A2
    };
    private static final PDRectangle[] PAGESIZES_LANDSCAPE;

    private static final int A4W_MM = 210;
    private static final int A4H_MM = 297;
    private static final int A3W_MM = 297;
    private static final int A3H_MM = 420;
    private static final int A2W_MM = 420;
    private static final int A2H_MM = 594;
    private static final int MAP_MARGIN_MIN_LEFT_RIGHT_NM = 20;
    private static final int MAP_MARGIN_MIN_BOTTOM_TOP_MM = 30;
    private static final int[] MAP_MAX_WIDTH_PX = {
            mmToPx(A4W_MM - MAP_MARGIN_MIN_LEFT_RIGHT_NM), // A4
            mmToPx(A3W_MM - MAP_MARGIN_MIN_LEFT_RIGHT_NM), // A3
            mmToPx(A2W_MM - MAP_MARGIN_MIN_LEFT_RIGHT_NM)  // A2
    };
    private static final int[] MAP_MAX_HEIGHT_PX = {
            mmToPx(A4H_MM - MAP_MARGIN_MIN_BOTTOM_TOP_MM), // A4
            mmToPx(A3H_MM - MAP_MARGIN_MIN_BOTTOM_TOP_MM), // A3
            mmToPx(A2H_MM - MAP_MARGIN_MIN_BOTTOM_TOP_MM)  // A2
    };
    private static final int[] MAP_MAX_WIDTH_PX_LANDSCAPE = {
            mmToPx(A4H_MM - MAP_MARGIN_MIN_LEFT_RIGHT_NM), // A4_LS
            mmToPx(A3H_MM - MAP_MARGIN_MIN_LEFT_RIGHT_NM), // A3_LS
            mmToPx(A2H_MM - MAP_MARGIN_MIN_LEFT_RIGHT_NM)  // A2_LS
    };
    private static final int[] MAP_MAX_HEIGHT_PX_LANDSCAPE = {
            mmToPx(A4W_MM - MAP_MARGIN_MIN_BOTTOM_TOP_MM), // A4_LS
            mmToPx(A3W_MM - MAP_MARGIN_MIN_BOTTOM_TOP_MM), // A3_LS
            mmToPx(A2W_MM - MAP_MARGIN_MIN_BOTTOM_TOP_MM)  // A2_LS
    };

    private static final PDFont FONT = PDType1Font.HELVETICA;
    private static final float FONT_SIZE = 12f;
    private static final float FONT_SIZE_SCALE = 10f;

    private static final float OFFSET_DATE_RIGHT = PDFBoxUtil.mmToPt(40);
    private static final float OFFSET_DATE_TOP = PDFBoxUtil.mmToPt(10);

    private static final float OFFSET_LOGO_LEFT = PDFBoxUtil.mmToPt(10);
    private static final float OFFSET_LOGO_BOTTOM = PDFBoxUtil.mmToPt(5);
    private static final float LOGO_HEIGHT = PDFBoxUtil.mmToPt(9);

    private static final float OFFSET_SCALE_LEFT = PDFBoxUtil.mmToPt(40);
    private static final float OFFSET_SCALE_BOTTOM = PDFBoxUtil.mmToPt(5);

    private static final double[] SCALE_LINE_DISTANCES_METRES = new double[24];

    private static final String LOGO_PATH_DEFAULT = "/img/logo.png";
    private static final String LOGO_PATH = PropertyUtil.get("print.logo.path", LOGO_PATH_DEFAULT);

    static {
        PAGESIZES_LANDSCAPE = new PDRectangle[PAGESIZES.length];
        for (int i = 0; i < PAGESIZES.length; i++) {
            PDRectangle rect = PAGESIZES[i];
            PAGESIZES_LANDSCAPE[i] = new PDRectangle(rect.getHeight(), rect.getWidth());
        }

        // 1, 2, 5, 10, 20, 50, ...
        SCALE_LINE_DISTANCES_METRES[0] = 1;
        SCALE_LINE_DISTANCES_METRES[1] = 2;
        SCALE_LINE_DISTANCES_METRES[2] = 5;
        for (int i = 3; i < SCALE_LINE_DISTANCES_METRES.length; i++) {
            SCALE_LINE_DISTANCES_METRES[i] = SCALE_LINE_DISTANCES_METRES[i - 3] * 10;
        }
    }

    public static int mmToPx(int mm) {
        return (int) Math.round((Units.OGC_DPI * mm) / Units.MM_PER_INCH);
    }

    /**
     * This method should be called via PrintService
     */
    protected static void getPDF(PrintRequest request, PDDocument doc, WMTSCapabilitiesCache wmtsCapsCache)
            throws IOException, ServiceException {
        int mapWidthPx = request.getWidth();
        int mapHeightPx = request.getHeight();

        PDRectangle pageSize = findMinimalPageSize(mapWidthPx, mapHeightPx);
        if (pageSize == null) {
            LOG.info("Could not find page size! width:", mapWidthPx, "height:", mapHeightPx);
            throw new ServiceException("Could not find a proper page size!");
        }

        float mapWidth = pixelsToPoints(mapWidthPx);
        float mapHeight = pixelsToPoints(mapHeightPx);

        // Init requests to run in the background
        List<Future<BufferedImage>> layerImages = AsyncImageLoader.initLayers(request, wmtsCapsCache);

        PDPage page = new PDPage(pageSize);
        doc.addPage(page);

        // Center map
        float x = (pageSize.getWidth() - mapWidth) / 2;
        float y = (pageSize.getHeight() - mapHeight) / 2;

        try (PDPageContentStream stream = new PDPageContentStream(doc, page, AppendMode.APPEND, false)) {
            drawTitle(stream, request, pageSize, mapHeight);
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
    private static PDRectangle findMinimalPageSize(int mapWidthPx, int mapHeightPx) {
        boolean landscape = mapWidthPx > mapHeightPx;
        int[] maxWidth;
        int[] maxHeight;
        PDRectangle[] pageSizes;
        if (landscape) {
            maxWidth = MAP_MAX_WIDTH_PX_LANDSCAPE;
            maxHeight = MAP_MAX_HEIGHT_PX_LANDSCAPE;
            pageSizes = PAGESIZES_LANDSCAPE;
        } else {
            maxWidth = MAP_MAX_WIDTH_PX;
            maxHeight = MAP_MAX_HEIGHT_PX;
            pageSizes = PAGESIZES;
        }
        for (int i = 0; i < maxWidth.length; i++) {
            if (mapWidthPx <= maxWidth[i] && mapHeightPx <= maxHeight[i]) {
                return pageSizes[i];
            }
        }
        return null;
    }

    private static void drawTitle(PDPageContentStream stream,
            PrintRequest request, PDRectangle pageSize, float mapHeight) throws IOException {
        String title = request.getTitle();
        if (title == null || title.isEmpty()) {
            return;
        }

        float x = pageSize.getWidth() / 2;
        float marginBottomPx = (pageSize.getHeight() - mapHeight) / 2;
        float y = marginBottomPx + mapHeight + 5;

        PDFBoxUtil.drawTextCentered(stream, title, FONT, FONT_SIZE, x, y);
    }

    private static void drawLogo(PDDocument doc, PDPageContentStream stream,
            PrintRequest request) throws IOException {
        if (!request.isShowLogo() || LOGO_PATH == null || LOGO_PATH.isEmpty()) {
            return;
        }

        BufferedImage logo = null;
        try (InputStream in = PDF.class.getResourceAsStream(LOGO_PATH)) {
            if (in == null) {
                LOG.debug("Logo file not found");
                return;
            }
            logo = ImageIO.read(new BufferedInputStream(in));
        } catch (IOException e) {
            LOG.warn(e, "Failed to read logo");
            return;
        }
        if (logo == null) {
            LOG.info("Couldn't read logo with ImageIO");
            return;
        }
        try {
            PDImageXObject img = LosslessFactory.createFromImage(doc, logo);
            float x = OFFSET_LOGO_LEFT;
            float y = OFFSET_LOGO_BOTTOM;
            // Maintain the aspect ratio of the image
            float f = LOGO_HEIGHT / img.getHeight();
            float w = img.getWidth() * f;
            float h = LOGO_HEIGHT;
            stream.drawImage(img, x, y, w, h);
        } catch (IOException e) {
            LOG.warn(e, "Failed to draw logo");
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

        String units = getUnits(request.getSrsName());
        if (units == null) {
            return;
        }

        double mppx;
        switch (units) {
        case "m":
            mppx = request.getResolution();
            break;
        case "°":
            LOG.info("Map units is deegrees, not drawing Scale Line");
            return;
        default:
            LOG.info("Unknown unit", units, "- not drawing Scale line");
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

    private static String getUnits(String srsName) {
        try {
            CoordinateReferenceSystem crs = CRS.decode(srsName);
            return crs.getCoordinateSystem().getAxis(0).getUnit().toString();
        } catch (FactoryException e) {
            LOG.warn(e, "Unable to decode CRS from", srsName);
            return null;
        }
    }

    private static void drawLayers(PDDocument doc, PDPageContentStream stream,
            List<PrintLayer> layers, List<Future<BufferedImage>> images,
            float x, float y, float w, float h) throws IOException {
        for (int i = 0; i < layers.size(); i++) {
            PrintLayer layer = layers.get(i);
            Future<BufferedImage> image = images.get(i);
            try {
                BufferedImage bi = image.get();
                if (bi == null) {
                    continue;
                }
                PDImageXObject imgObject = LosslessFactory.createFromImage(doc, bi);

                // Set layer (Optional Content Group)
                PDOptionalContentGroup ocg = PDFBoxUtil.getOCG(doc, layer.getName());
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