package org.oskari.print;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.print.loader.AsyncFeatureLoader;
import org.oskari.print.loader.AsyncImageLoader;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.util.PDFBoxUtil;
import org.oskari.print.util.Units;
import org.oskari.print.wmts.WMTSCapabilitiesCache;
import org.oskari.service.wfs.client.OskariFeatureClient;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.AffineTransformation;

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

    private static final String LOGO_PATH_DEFAULT = "logo.png";
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
     * This method should be called (only) via PrintService
     */
    protected static void getPDF(PrintRequest request,
            WMTSCapabilitiesCache wmtsCapsCache,
            OskariFeatureClient featureClient,
            PDDocument doc) throws IOException, ServiceException {
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
        Map<Integer, Future<BufferedImage>> layerImages = AsyncImageLoader.initLayers(request, wmtsCapsCache);
        Map<Integer, Future<SimpleFeatureCollection>> featureCollections = AsyncFeatureLoader.initLayers(request, featureClient);

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
            drawLayers(doc, stream, request, layerImages, featureCollections,
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

        // Try file
        try (InputStream in = Files.newInputStream(Paths.get(LOGO_PATH))) {
            logo = ImageIO.read(new BufferedInputStream(in));
        } catch (NoSuchFileException e) {
            LOG.debug("Logo file " + LOGO_PATH + " does not exist");
        } catch (IOException e) {
            LOG.warn(e, "Failed to read logo from file");
        }

        // File didn't work, try resources file
        if (logo == null) {
            try (InputStream in = PDF.class.getResourceAsStream(LOGO_PATH)) {
                if (in == null) {
                    LOG.debug("Resource file " + LOGO_PATH + " does not exist");
                    return;
                }
                logo = ImageIO.read(new BufferedInputStream(in));
            } catch (IOException e) {
                LOG.warn(e, "Failed to read logo from resource " + LOGO_PATH);
                return;
            }
            if (logo == null) {
                LOG.info("Couldn't read logo with ImageIO");
                return;
            }
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

        double mppt = mppx * Units.OGC_DPI / Units.PDF_DPI;

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

        double pt = distance / mppt;


        // PDF (and PDFBox) uses single precision floating point numbers
        float x1 = (float) OFFSET_SCALE_LEFT;
        float y1 = (float) OFFSET_SCALE_BOTTOM;
        float x2 = (float) (OFFSET_SCALE_LEFT + pt);
        float y2 = y1 + 10;

        // If scale text is defined then draw scale text.
        if(request.isScaleText()) {
            float cx = x1 + ((x2 - x1) / 2);
            PDFBoxUtil.drawTextCentered(stream, request.getScaleText(),
                    FONT, FONT_SIZE_SCALE, cx, y1 + 5);
        }
        // else force to draw scalebar
        else {
            String distanceStr;
            if (distance > 1000) {
                distanceStr = Math.round(distance / 1000) + " km";
            } else {
                distanceStr = Math.round(distance) + " m";
            }

            stream.moveTo(x1, y2);
            stream.lineTo(x1, y1);
            stream.lineTo(x2, y1);
            stream.lineTo(x2, y2);
            stream.stroke();

            float cx = x1 + ((x2 - x1) / 2);
            PDFBoxUtil.drawTextCentered(stream, distanceStr,
                    FONT, FONT_SIZE_SCALE, cx, y1 + 5);
        }
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
            PrintRequest request,
            Map<Integer, Future<BufferedImage>> layerImages,
            Map<Integer, Future<SimpleFeatureCollection>> featureCollections,
            float x, float y, float w, float h) throws IOException {
        List<PrintLayer> layers = request.getLayers();

        Collections.sort(layers, Comparator.comparing(PrintLayer::getZIndex));
        AffineTransformation transformation = getTransform(request.getBoundingBox(), w, h);

        for (PrintLayer layer : layers) {
            int zIndex = layer.getZIndex();
            Future<BufferedImage> futureImage = layerImages.get(zIndex);
            if (futureImage != null) {
                drawImageLayer(doc, stream, layer, futureImage, x, y, w, h);
            } else {
                Future<SimpleFeatureCollection> futureFc = featureCollections.get(zIndex);
                if (futureFc != null) {
                    drawVectorLayer(doc, stream, layer, futureFc, transformation, x, y, w, h);
                }
            }
        }
    }

    private static void drawImageLayer(PDDocument doc, PDPageContentStream stream,
            PrintLayer layer, Future<BufferedImage> future,
            float x, float y, float w, float h) throws IOException {
        try {
            BufferedImage bi = future.get();
            if (bi != null) {
                drawImageLayer(doc, stream, layer, bi, x, y, w, h);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn(e);
            throw new IOException(e.getMessage());
        }
    }

    private static void drawImageLayer(PDDocument doc, PDPageContentStream stream,
            PrintLayer layer, BufferedImage bi,
            float x, float y, float w, float h) throws IOException {
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
    }

    private static AffineTransformation getTransform(double[] bbox, float w, float h) {
        double widthNature = Math.abs((bbox[2] - bbox[0]));
        double heightNature = Math.abs((bbox[3] - bbox[1]));

        // Scale everything to PDF points
        double sx = (double) w / widthNature;
        double sy = (double) h / heightNature;

        // Move the origo from (0, 0) to (bbox[0], bbox[1])
        // by translating all coordinates (-bbox[0], -bbox[1])
        // and by taking scaling into account
        double tx = sx * -bbox[0];
        double ty = sy * -bbox[1];

        // List out all the parameters explicitly because JTS AffineTransformation uses
        // an unorthodox order for the parameters (compared to java.awt.geom.AffineTransform)
        double m00 = sx;
        double m10 = 0;
        double m01 = 0;
        double m11 = sy;
        double m02 = tx;
        double m12 = ty;
        return new AffineTransformation(m00, m01, m02, m10, m11, m12);
    }

    private static void drawVectorLayer(PDDocument doc, PDPageContentStream pageStream,
            PrintLayer layer, Future<SimpleFeatureCollection> futureFc,
            AffineTransformation transform, float x, float y, float w, float h) throws IOException {
        SimpleFeatureCollection fc;
        try {
            fc = futureFc.get();
            if (fc == null || fc.isEmpty()) {
                return;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn(e);
            throw new IOException(e.getMessage());
        }

        // Create a Form XObject
        PDFormXObject form = new PDFormXObject(doc);
        // Don't draw outside the bbox (PDF renderer will do the clipping for us)
        form.setBBox(new PDRectangle(w, h));
        // Draw the form in the correct place
        form.setMatrix(AffineTransform.getTranslateInstance(x, y));
        // Make the form Optional Content ("layer" that can be hidden)
        PDOptionalContentGroup ocg = PDFBoxUtil.getOCG(doc, layer.getName());
        PDFBoxUtil.setOCG(form, ocg);

        try (OutputStream out = form.getContentStream().createOutputStream(COSName.FLATE_DECODE);
                PDPageContentStream stream = new PDPageContentStream(doc, form, out)) {

            // TODO: foreach "rule" (combination of filter and "style"):
            // - Set drawing style
            // - draw features that match the filter

            try (SimpleFeatureIterator it = fc.features()) {
                while (it.hasNext()) {
                    setStyle(stream, layer);
                    drawFeature(stream, transform, it.next());
                }
            }
        }

        pageStream.drawForm(form);
    }

    private static void setStyle(PDPageContentStream stream, PrintLayer layer) throws IOException {
        int opacity = layer.getOpacity();
        if (opacity < 100) {
            float alpha = 0.01f * opacity;
            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setStrokingAlphaConstant(alpha);
            gs.setNonStrokingAlphaConstant(alpha);
            setDrawingStyle(gs, layer);
            stream.setGraphicsStateParameters(gs);
        } else {
            setDrawingStyle(stream, layer);
        }
    }

    private static void setDrawingStyle(PDExtendedGraphicsState gs, PrintLayer layer) {
        // TODO
    }

    private static void setDrawingStyle(PDPageContentStream stream, PrintLayer layer) {
        // TODO
    }

    private static void drawFeature(PDPageContentStream stream, AffineTransformation transform,
            SimpleFeature f) throws IOException {
        Geometry g = (Geometry) f.getDefaultGeometry();
        if (g == null) {
            return;
        }
        // Transform the Geometry to PDF coordinate space
        // We could also do the opposite with the PDF CTM
        // but this way we can better control the floating
        // point imprecision issues
        g = transform.transform(g);
        draw(stream, g);
    }

    private static void draw(PDPageContentStream stream, Geometry g) throws IOException {
        if (g instanceof Point) {
            draw(stream, (Point) g);
        } else if (g instanceof LineString) {
            draw(stream, (LineString) g);
        } else if (g instanceof Polygon) {
            draw(stream, (Polygon) g);
        } else if (g instanceof MultiPoint) {
            draw(stream, (MultiPoint) g);
        } else if (g instanceof MultiLineString) {
            draw(stream, (MultiLineString) g);
        } else if (g instanceof MultiPolygon) {
            draw(stream, (MultiPolygon) g);
        } else if (g instanceof GeometryCollection) {
            draw(stream, (GeometryCollection) g);
        }
    }

    private static void draw(PDPageContentStream stream, Point g)
            throws IOException {
        // TODO: Draw something meaningful instead of a filling and stroking a rectangle
        Coordinate c = g.getCoordinate();
        stream.addRect((float) c.x - 5f, (float) c.y - 5f, 10, 10);
        stream.fillAndStroke();
    }

    private static void draw(PDPageContentStream stream, LineString g) throws IOException {
        add(stream, g.getCoordinateSequence());
        stream.stroke();
    }

    private static void draw(PDPageContentStream stream, Polygon g) throws IOException {
        add(stream, g.getExteriorRing().getCoordinateSequence());
        for (int i = 0; i < g.getNumInteriorRing(); i++) {
            add(stream, g.getInteriorRingN(i).getCoordinateSequence());
        }
        stream.fillAndStrokeEvenOdd();
    }

    private static void draw(PDPageContentStream stream, MultiPoint g) throws IOException {
        for (int i = 0; i < g.getNumGeometries(); i++) {
            draw(stream, (Point) g.getGeometryN(i));
        }
    }

    private static void draw(PDPageContentStream stream, MultiLineString g) throws IOException {
        for (int i = 0; i < g.getNumGeometries(); i++) {
            draw(stream, (LineString) g.getGeometryN(i));
        }
    }

    private static void draw(PDPageContentStream stream, MultiPolygon g) throws IOException {
        for (int i = 0; i < g.getNumGeometries(); i++) {
            draw(stream, (Polygon) g.getGeometryN(i));
        }
    }

    private static void draw(PDPageContentStream stream, GeometryCollection g) throws IOException {
        for (int i = 0; i < g.getNumGeometries(); i++) {
            draw(stream, g.getGeometryN(i));
        }
    }

    private static void add(PDPageContentStream stream, CoordinateSequence csq) throws IOException {
        for (int i = 0; i < csq.size(); i++) {
            float x = (float) csq.getX(i);
            float y = (float) csq.getY(i);
            if (i == 0) {
                stream.moveTo(x, y);
            } else {
                stream.lineTo(x, y);
            }
        }
        stream.closePath();
    }

    private static void drawBorder(PDPageContentStream stream,
            float x, float y, float mapWidthPt, float mapHeightPt) throws IOException {
        stream.saveGraphicsState();
        stream.setLineWidth(0.5f);
        stream.addRect(x, y, mapWidthPt, mapHeightPt);
        stream.stroke();
        stream.restoreGraphicsState();
    }

}