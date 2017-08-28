package org.oskari.print;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;

import fi.nls.oskari.domain.map.OskariLayer;

public class PrintServiceTest {

    @BeforeClass
    public static void init() {
        System.setProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", "5000");
        System.setProperty("hystrix.threadpool.default.maxQueueSize", "200");
        System.setProperty("hystrix.threadpool.default.queueSizeRejectionThreshold", "200");
    }

    @Test
    public void pdf() throws IOException {
        PrintRequest request = getPrintRequest();
        request.setFormat(PrintFormat.PDF.contentType);
        request.setLogo("TestLogo");
        request.setShowDate(true);
        request.setShowScale(true);
        PrintService.validate(request);
        try (PDDocument doc = new PDDocument()) {
            PrintService.getPDF(request, doc);
            File file = new File("C:/Omat/temp/foobarbbb.pdf");
            doc.save(file);
        }
    }

    @Test
    @Ignore
    public void png() throws IOException {
        PrintRequest request = getPrintRequest();
        request.setFormat(PrintFormat.PNG.contentType);
        PrintService.validate(request);
        BufferedImage bi = PrintService.getPNG(request);
        File file = new File("C:/Omat/temp/foobarbbb.png");
        ImageIO.write(bi, "png", file);
    }

    private PrintRequest getPrintRequest() {
        PrintRequest request = new PrintRequest();
        request.setWidth(1920);
        request.setHeight(1080);
        request.setEast(500000);
        request.setNorth(6822000);

        request.setSrsName("EPSG:3067");
        request.setResolution(8);
        request.setUnits("m");
        request.setMetersPerUnit(1);
        request.setZoomLevel(10);

        request.setShowScale(true);
        request.setLayers(getLayers());
        return request;
    }

    private List<PrintLayer> getLayers() {
        List<PrintLayer> layers = new ArrayList<>();

        PrintLayer l1 = new PrintLayer();

        l1.setId("Taustakartta");
        l1.setName("Taustakartta");
        l1.setType(OskariLayer.TYPE_WMTS);
        l1.setVersion("1.0.0");
        l1.setUrl("http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/taustakartta/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png");
        l1.setSrsName("EPSG:3067");
        l1.setTileMatrixSet("ETRS-TM35FIN");
        l1.setOpacity(50);

        PrintLayer l2 = new PrintLayer();
        l2.setId("Ortokuva");
        l2.setName("Ortokuva");
        l2.setType(OskariLayer.TYPE_WMTS);
        l2.setVersion("1.0.0");
        l2.setUrl("http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/ortokuva/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.jpg");
        l2.setSrsName("EPSG:3067");
        l2.setTileMatrixSet("ETRS-TM35FIN");
        l2.setOpacity(100);

        layers.add(l2);
        layers.add(l1);

        return layers;
    }

}
