package org.oskari.print;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.print.loader.AsyncFeatureLoader;
import org.oskari.print.loader.AsyncImageLoader;
import org.oskari.print.request.PDPrintStyle;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.request.PrintVectorRule;
import org.oskari.print.util.PDFBoxUtil;
import org.oskari.print.util.StyleUtil;
import org.oskari.print.util.Units;
import org.oskari.service.wfs.client.OskariFeatureClient;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import org.oskari.util.Customization;

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

    private static final float OFFSET_DATE_RIGHT = PDFBoxUtil.mmToPt(40);
    private static final float OFFSET_DATE_TOP = PDFBoxUtil.mmToPt(10);

    private static final float OFFSET_LOGO_LEFT = PDFBoxUtil.mmToPt(10);
    private static final float OFFSET_LOGO_BOTTOM = PDFBoxUtil.mmToPt(5);
    private static final float LOGO_HEIGHT = PDFBoxUtil.mmToPt(9);

    private static final float OFFSET_SCALE_BOTTOM = PDFBoxUtil.mmToPt(5);

    private static final float OFFSET_TIMESERIES_RIGHT = PDFBoxUtil.mmToPt(50);
    private static final float OFFSET_TIMESERIES_LABEL_BOTTOM = PDFBoxUtil.mmToPt(10);
    private static final float OFFSET_TIME_IN_TIMESERIES_BOTTOM = PDFBoxUtil.mmToPt(5);

    private static final double[] SCALE_LINE_DISTANCES_METRES = new double[24];

    private static final String MARKER_FIELD_SEPARATOR = "\\|";
    private static final String MARKER_SEPARATOR= "___";
    private static final String MARKER_COORD_SEPARATOR= "_";

    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
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
        Map<Integer, Future<BufferedImage>> layerImages = AsyncImageLoader.initLayers(request);
        Map<Integer, Future<SimpleFeatureCollection>> featureCollections = AsyncFeatureLoader.initLayers(request, featureClient);

        PDPage page = new PDPage(pageSize);
        doc.addPage(page);

        // Center map
        float x = (pageSize.getWidth() - mapWidth) / 2;
        float y = (pageSize.getHeight() - mapHeight) / 2;

        try (PDPageContentStream stream = new PDPageContentStream(doc, page, AppendMode.APPEND, false)) {
            drawTitle(stream, request, pageSize, mapHeight);
            float logoWidth = drawLogoAndGetWidth(doc, stream, request);
            drawScale(stream, request, logoWidth);
            drawDate(stream, request, pageSize);
            drawTimeseriesTexts(stream, request, pageSize);
            drawLayers(doc, stream, request, layerImages, featureCollections,
                    x, y, mapWidth, mapHeight);
            drawBorder(stream, x, y, mapWidth, mapHeight);
        }
    }
    protected static BufferedImage getVectorLayerImage (PrintLayer layer, Future<SimpleFeatureCollection> ffc, double [] bbox, int w, int h )
            throws IOException {
        float mapWidth = pixelsToPoints(w);
        float mapHeight = pixelsToPoints(h);
        PDPage page = new PDPage(new PDRectangle(mapWidth, mapHeight) );

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page, AppendMode.APPEND, false)) {
                AffineTransformation transformation = getTransform(bbox, mapWidth, mapHeight);
                drawVectorLayer(doc, stream, layer, ffc, transformation, 0, 0, mapWidth, mapHeight);
            }
            PDFRenderer renderer = new PDFRenderer(doc);
            float scale = h / mapHeight;
            return renderer.renderImage(0, scale, ImageType.ARGB);
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

        PDFBoxUtil.drawTextCentered(stream, title, PDPrintStyle.FONT, PDPrintStyle.FONT_SIZE, x, y);
    }

    private static float drawLogoAndGetWidth(PDDocument doc, PDPageContentStream stream,
            PrintRequest request) throws IOException {
        float logoWidth = 0;

        if (!request.isShowLogo()) {
            return logoWidth;
        }

        BufferedImage logo;
        try (InputStream in = new ByteArrayInputStream(Customization.getLogo("print"))) {
            logo = ImageIO.read(in);
        } catch (IOException e) {
            LOG.warn(e, "Failed to read logo from file");
            return logoWidth;
        }

        try {
            PDImageXObject img = LosslessFactory.createFromImage(doc, logo);
            float x = OFFSET_LOGO_LEFT;
            float y = OFFSET_LOGO_BOTTOM;
            // Maintain the aspect ratio of the image
            float f = LOGO_HEIGHT / img.getHeight();
            float w = img.getWidth() * f;
            logoWidth = w;
            float h = LOGO_HEIGHT;
            stream.drawImage(img, x, y, w, h);
        } catch (IOException e) {
            LOG.warn(e, "Failed to draw logo");
        }
        return logoWidth;
    }

    private static void drawDate(PDPageContentStream stream,
            PrintRequest request, PDRectangle pageSize) throws IOException {
        if (!request.isShowDate()) {
            return;
        }

        String date = SDF.format(new Date());
        float x = pageSize.getWidth() - OFFSET_DATE_RIGHT;
        float y = pageSize.getHeight() - OFFSET_DATE_TOP;
        PDFBoxUtil.drawText(stream, date, PDPrintStyle.FONT, PDPrintStyle.FONT_SIZE, x, y);
    }

    private static void drawTimeseriesTexts(PDPageContentStream stream,
            PrintRequest request, PDRectangle pageSize) throws IOException {
        if (!request.isShowTimeSeriesTime()
                || StringUtils.isEmpty(request.getTimeseriesLabel())
                    || StringUtils.isEmpty(request.getFormattedTime())) {
            return;
        }

        float x = pageSize.getWidth() - OFFSET_TIMESERIES_RIGHT;

        PDFBoxUtil.drawText(stream, request.getTimeseriesLabel(), PDPrintStyle.FONT, PDPrintStyle.FONT_SIZE_TIMESERIES,
                x, OFFSET_TIMESERIES_LABEL_BOTTOM);
        PDFBoxUtil.drawText(stream, request.getFormattedTime(), PDPrintStyle.FONT, PDPrintStyle.FONT_SIZE_TIMESERIES,
                x, OFFSET_TIME_IN_TIMESERIES_BOTTOM);
    }

    private static void drawScale(PDPageContentStream stream, PrintRequest request, float logoWidth)
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

        // create an offset point for the scalebar
        float OFFSET_SCALE_LEFT = OFFSET_LOGO_LEFT + logoWidth + 10;

        // PDF (and PDFBox) uses single precision floating point numbers
        float x1 = (float) OFFSET_SCALE_LEFT;
        float y1 = (float) OFFSET_SCALE_BOTTOM;
        float x2 = (float) (OFFSET_SCALE_LEFT + pt);
        float y2 = y1 + 10;

        // If scale text is defined then draw scale text.
        if(request.isScaleText()) {
            float cx = x1 + ((x2 - x1) / 2);
            PDFBoxUtil.drawTextCentered(stream, request.getScaleText(),
                    PDPrintStyle.FONT, PDPrintStyle.FONT_SIZE_SCALE, cx, y1 + 5);
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
                    PDPrintStyle.FONT, PDPrintStyle.FONT_SIZE_SCALE, cx, y1 + 5);
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
        drawMarkers(doc, stream, request.getMarkers(), transformation, x, y, w, h);
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
        PDResources resources = new PDResources();
        form.setResources(resources);

        String geomName = fc.getSchema().getGeometryDescriptor().getLocalName();
        // Create PDPrintStyles to add fill pattern to resources before creating content stream
        List <PrintVectorRule> rules = getRules(doc, resources, layer, geomName);
        // Don't draw outside the bbox (PDF renderer will do the clipping for us)
        form.setBBox(new PDRectangle(w, h));
        // Draw the form in the correct place
        form.setMatrix(AffineTransform.getTranslateInstance(x, y));
        // Make the form Optional Content ("layer" that can be hidden)
        PDOptionalContentGroup ocg = PDFBoxUtil.getOCG(doc, layer.getName());
        PDFBoxUtil.setOCG(form, ocg);

        try (OutputStream out = form.getContentStream().createOutputStream(COSName.FLATE_DECODE);
                PDPageContentStream stream = new PDPageContentStream(doc, form, out)) {
                setOpacity(stream, layer.getOpacity());
            for (PrintVectorRule rule : rules) {
                SimpleFeatureCollection subFc = fc.subCollection(rule.getFilter());
                if (subFc.isEmpty()) continue;
                PDPrintStyle style = rule.getStyle();
                style.apply(stream);
                try (SimpleFeatureIterator it = subFc.features()) {
                    while (it.hasNext()) {
                        drawFeature(stream, transform, it.next(), style);
                    }
                }
            }
        }

        pageStream.drawForm(form);
    }
    private static void drawMarkers(PDDocument doc, PDPageContentStream pageStream, String markers,
                                        AffineTransformation transform, float x, float y, float w, float h) throws IOException {
        if (markers == null || markers.isEmpty()) return;
        // Create a Form XObject
        PDFormXObject form = new PDFormXObject(doc);
        PDResources resources = new PDResources();
        form.setResources(resources);

        // Don't draw outside the bbox (PDF renderer will do the clipping for us)
        form.setBBox(new PDRectangle(w, h));
        // Draw the form in the correct place
        form.setMatrix(AffineTransform.getTranslateInstance(x, y));
        // Make the form Optional Content ("layer" that can be hidden)
        PDOptionalContentGroup ocg = PDFBoxUtil.getOCG(doc, "Markers");
        PDFBoxUtil.setOCG(form, ocg);

        try (OutputStream out = form.getContentStream().createOutputStream(COSName.FLATE_DECODE);
             PDPageContentStream stream = new PDPageContentStream(doc, form, out)) {
            draw(doc, stream, resources, transform, markers);
        }
        pageStream.drawForm(form);
    }
    public static BufferedImage getMarkersImage (String markers, double [] bbox, int w, int h ) throws IOException {
        if (markers.isEmpty()) return null;
        float mapWidth = pixelsToPoints(w);
        float mapHeight = pixelsToPoints(h);
        PDPage page = new PDPage(new PDRectangle(mapWidth, mapHeight));
        PDResources resources = new PDResources();
        page.setResources(resources);
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page, AppendMode.APPEND, false)) {
                AffineTransformation transformation = getTransform(bbox, mapWidth, mapHeight);
                draw(doc, stream, resources, transformation, markers);
            }
            PDFRenderer renderer = new PDFRenderer(doc);
            float scale = h / mapHeight;
            return renderer.renderImage(0, scale, ImageType.ARGB);
        }
    }
    private static void draw (PDDocument doc, PDPageContentStream stream, PDResources resources,
                              AffineTransformation transform, String markers) throws IOException {
        String [] marks = markers.split(MARKER_SEPARATOR);
        for (int i = 0; i < marks.length; i++) {
            String[] mark = marks[i].split(MARKER_FIELD_SEPARATOR);
            if (mark.length < 4) continue;
            String[] coord = mark[3].split(MARKER_COORD_SEPARATOR);
            if (coord.length < 2) continue;
            int shape = Integer.valueOf(mark[0]);
            int size = Integer.valueOf(mark[1]);
            String color = "#" + mark[2];
            String label = mark.length > 4 ? mark[4] : "";
            Coordinate c = new Coordinate(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));
            PDFormXObject icon = StyleUtil.getIcon(doc, shape, color, size);
            resources.add(icon);
            transform.transform(c,c);
            stream.saveGraphicsState();
            stream.transform(Matrix.getTranslateInstance((float) c.x, (float) c.y));
            stream.drawForm(icon);
            stream.restoreGraphicsState();
            if (!label.isEmpty()) {
                drawLabelAt(stream, c, StyleUtil.LABEL_ALIGN_MAP.get("markers"), label);
            }
        }
    }

    private static  List <PrintVectorRule> getRules (PDDocument doc, PDResources resources, PrintLayer layer, String geomName) throws IOException {
        Function pointFunc = ff.function("in2", ff.function("geometryType", ff.property(geomName)), ff.literal("Point"), ff.literal("MultiPoint"));
        Function lineFunc = ff.function("in2", ff.function("geometryType", ff.property(geomName)), ff.literal("LineString"), ff.literal("MultiLineString"));
        Function polygonFunc = ff.function("in2", ff.function("geometryType", ff.property(geomName)), ff.literal("Polygon"), ff.literal("MultiPolygon"));
        Expression _true = ff.literal(true);

        JSONObject oskariStyle = layer.getOskariStyle();

        List <PrintVectorRule> rules = new ArrayList<>();
        rules.add(new PrintVectorRule(
                ff.equals(polygonFunc, _true),
                StyleUtil.getPolygonStyle(oskariStyle, resources)));
        rules.add(new PrintVectorRule(
                ff.equals(lineFunc, _true),
                StyleUtil.getLineStyle(oskariStyle)));
        rules.add(new PrintVectorRule(
                ff.equals(pointFunc, _true),
                StyleUtil.getPointStyle(oskariStyle, doc)));

        return rules;
    }

    private static void setOpacity(PDPageContentStream stream, int opacity) throws IOException {
        if (opacity < 100) {
            float alpha = 0.01f * opacity;
            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setStrokingAlphaConstant(alpha);
            gs.setNonStrokingAlphaConstant(alpha);
            stream.setGraphicsStateParameters(gs);
        }
    }

    private static void drawFeature(PDPageContentStream stream, AffineTransformation transform,
            SimpleFeature f, PDPrintStyle style) throws IOException {
        Geometry g = (Geometry) f.getDefaultGeometry();
        if (g == null) {
            return;
        }
        // Transform the Geometry to PDF coordinate space
        // We could also do the opposite with the PDF CTM
        // but this way we can better control the floating
        // point imprecision issues
        g = transform.transform(g);
        draw(stream, g, style);
        if (style.hasLabels()){
            // take first property with content
            String label = style.getLabelProperty().stream()
                    .map(it -> f.getAttribute(it))
                    .filter(it -> it != null)
                    .map(it -> it.toString())
                    .filter(it -> !it.isEmpty())
                    .findFirst()
                    .orElse("");
            if (!label.isEmpty()) {
                drawLabel(stream, g, style.getLabelAlign(), label);
            }

        }
    }

    private static void draw(PDPageContentStream stream, Geometry g, PDPrintStyle style ) throws IOException {
        if (g instanceof Point) {
            draw(stream, (Point) g, style.getIcon());
        } else if (g instanceof LineString) {
            draw(stream, (LineString) g);
        } else if (g instanceof Polygon) {
            draw(stream, (Polygon) g,style.hasFillColor(), style.hasLineColor());
        } else if (g instanceof MultiPoint) {
            draw(stream, (MultiPoint) g, style.getIcon());
        } else if (g instanceof MultiLineString) {
            draw(stream, (MultiLineString) g);
        } else if (g instanceof MultiPolygon) {
            draw(stream, (MultiPolygon) g, style.hasFillColor(), style.hasLineColor());
        } else if (g instanceof GeometryCollection) {
            draw(stream, (GeometryCollection) g, style);
        }
    }

    private static void drawLabel(PDPageContentStream stream, Geometry g,  PDPrintStyle.LabelAlign align, String label) throws IOException {
        Coordinate c;
        if (g instanceof MultiPoint || g instanceof MultiPolygon ) {
            for (int i = 0 ; i < g.getNumGeometries(); i++){
                c = g.getGeometryN(i).getCentroid().getCoordinate();
                drawLabelAt(stream, c, align, label);
            }
        } else if (g instanceof LineString) {
            c = getLineCentroid ((LineString) g);
            drawLabelAt(stream, c, align, label);

        } else if (g instanceof MultiLineString) {
            for (int i = 0; i < g.getNumGeometries(); i++) {
                c = getLineCentroid ((LineString) g.getGeometryN(i));
                drawLabelAt(stream, c, align, label);
            }
        }
    }
    private static void setLabelStyle (PDPageContentStream stream) throws IOException  {
        stream.setLineDashPattern(PDPrintStyle.LinePattern.solid.f.apply(0f), 0);
        stream.setRenderingMode(RenderingMode.FILL_STROKE);
        stream.setNonStrokingColor(Color.BLACK);
        stream.setStrokingColor(Color.WHITE);
        stream.setLineWidth(0.2f);
    }
    private static Coordinate getLineCentroid (LineString line) {
        int i = line.getNumPoints()/2;
        return line.getPointN(i).getCoordinate();
    }
    private static void drawLabelAt (PDPageContentStream stream, Coordinate c, PDPrintStyle.LabelAlign align, String label) throws IOException {
        stream.saveGraphicsState();
        //setLabelStyle(stream);

        //setLabelStyle's white background (stroke) creates blurry text
        //use black fill color only for now
        stream.setNonStrokingColor(Color.BLACK);

        stream.beginText();
        stream.setFont(PDPrintStyle.FONT_BOLD, PDPrintStyle.FONT_SIZE);
        stream.setTextMatrix(Matrix.getTranslateInstance((float) c.x + align.getLabelX(label), (float) c.y + align.getLabelY()));
        stream.showText(label);
        stream.endText();
        stream.restoreGraphicsState();
    }

    private static void draw(PDPageContentStream stream, Point g, PDFormXObject icon)
            throws IOException {
        Coordinate c = g.getCoordinate();
        stream.saveGraphicsState();
        stream.transform(Matrix.getTranslateInstance((float) c.x , (float)c.y));
        stream.drawForm(icon);
        stream.restoreGraphicsState();
    }

    private static void draw(PDPageContentStream stream, LineString g) throws IOException {
        add(stream, g.getCoordinateSequence());
        stream.stroke();
    }

    private static void draw(PDPageContentStream stream, Polygon g, boolean fill, boolean stroke) throws IOException {
        if (!fill && !stroke) return;
        add(stream, g.getExteriorRing().getCoordinateSequence(), true);
        for (int i = 0; i < g.getNumInteriorRing(); i++) {
            add(stream, g.getInteriorRingN(i).getCoordinateSequence(), true);
        }
        if (fill && stroke) {
            stream.fillAndStrokeEvenOdd();
        } else if (fill) {
            stream.fillEvenOdd();
        }
        stream.stroke();
    }

    private static void draw(PDPageContentStream stream, MultiPoint g, PDFormXObject icon) throws IOException {
        for (int i = 0; i < g.getNumGeometries(); i++) {
            draw(stream, (Point) g.getGeometryN(i), icon);
        }
    }

    private static void draw(PDPageContentStream stream, MultiLineString g) throws IOException {
        for (int i = 0; i < g.getNumGeometries(); i++) {
            draw(stream, (LineString) g.getGeometryN(i));
        }
    }

    private static void draw(PDPageContentStream stream, MultiPolygon g, boolean fill, boolean stroke) throws IOException {
        for (int i = 0; i < g.getNumGeometries(); i++) {
            draw(stream, (Polygon) g.getGeometryN(i), fill, stroke);
        }
    }

    private static void draw(PDPageContentStream stream, GeometryCollection g, PDPrintStyle style) throws IOException {
        for (int i = 0; i < g.getNumGeometries(); i++) {
            draw(stream, g.getGeometryN(i), style);
        }
    }
    private static void add(PDPageContentStream stream, CoordinateSequence csq) throws IOException {
        add(stream, csq, false);
    }
    private static void add(PDPageContentStream stream, CoordinateSequence csq, boolean closePath) throws IOException {
        for (int i = 0; i < csq.size(); i++) {
            float x = (float) csq.getX(i);
            float y = (float) csq.getY(i);
            if (i == 0) {
                stream.moveTo(x, y);
            } else {
                stream.lineTo(x, y);
            }
        }
        if (closePath) {
            stream.closePath();
        }

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