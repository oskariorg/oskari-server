package org.oskari.print;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.oskari.print.request.PrintFormat;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;

public class PrintServiceTest {

    @Test
    @Ignore("Depends on karttamoottori.maanmittauslaitos.fi, doesn't test anything")
    public void testPNG() throws ServiceException, IOException {
        PrintRequest request = new PrintRequest();
        request.setFormat(PrintFormat.PNG);

        request.setEast(500000);
        request.setNorth(6750000);
        request.setSrsName("EPSG:3067");
        request.setWidth(512);
        request.setHeight(512);
        request.setResolution(2);

        PrintLayer bg = new PrintLayer();
        bg.setId(1);
        bg.setName("ortokuva_vaaravari");
        bg.setType(OskariLayer.TYPE_WMTS);
        bg.setVersion("1.0.0");
        bg.setUrl("https://karttamoottori.maanmittauslaitos.fi/maasto/wmts");
        bg.setStyle("default");
        bg.setOpacity(100);

        PrintLayer fg = new PrintLayer();
        fg.setId(2);
        fg.setName("kiinteistotunnukset");
        fg.setType(OskariLayer.TYPE_WMTS);
        fg.setVersion("1.0.0");
        fg.setUrl("https://karttamoottori.maanmittauslaitos.fi/kiinteisto/wmts");
        fg.setStyle("default");
        fg.setOpacity(100);

        request.setLayers(Arrays.asList(bg, fg));

        String dataBg = CapabilitiesCacheService.getFromService(bg.getUrl(), bg.getType(), bg.getUsername(), bg.getPassword(), bg.getVersion());
        OskariLayerCapabilities answerBg = new OskariLayerCapabilities(1L, bg.getUrl(), bg.getType(), bg.getVersion(), dataBg, null, null);

        String dataFg = CapabilitiesCacheService.getFromService(fg.getUrl(), fg.getType(), fg.getUsername(), fg.getPassword(), fg.getVersion());
        OskariLayerCapabilities answerFg = new OskariLayerCapabilities(1L, fg.getUrl(), fg.getType(), fg.getVersion(), dataFg, null, null);

        CapabilitiesCacheService mock = Mockito.mock(CapabilitiesCacheService.class);
        Mockito.when(mock.getCapabilities(bg.getUrl(), bg.getType(), bg.getUsername(), bg.getPassword(), bg.getVersion())).thenReturn(answerBg);
        Mockito.when(mock.getCapabilities(fg.getUrl(), fg.getType(), fg.getUsername(), fg.getPassword(), fg.getVersion())).thenReturn(answerFg);

        PrintService service = new PrintService(mock);
        BufferedImage img = service.getPNG(request);
        File file = File.createTempFile("print-test", ".png");
        ImageIO.write(img, "png", file);
        Desktop.getDesktop().open(file);
    }

    @Test
    @Ignore("Depends on karttamoottori.maanmittauslaitos.fi, doesn't test anything")
    public void testPDF() throws ServiceException, IOException {
        PrintRequest request = new PrintRequest();
        request.setFormat(PrintFormat.PDF);

        request.setEast(500000);
        request.setNorth(6750000);
        request.setSrsName("EPSG:3067");

        request.setTitle("Hello world!");

        request.setWidth(PDF.mmToPx(210 - 20));
        request.setHeight(PDF.mmToPx(297 - 30));
        request.setResolution(2);

        request.setShowLogo(true);
        request.setShowScale(true);

        PrintLayer bg = new PrintLayer();
        bg.setId(1);
        bg.setName("taustakartta");
        bg.setType(OskariLayer.TYPE_WMTS);
        bg.setVersion("1.0.0");
        bg.setUrl("https://karttamoottori.maanmittauslaitos.fi/maasto/wmts");
        bg.setStyle("default");
        bg.setOpacity(100);

        PrintLayer fg = new PrintLayer();
        fg.setId(2);
        fg.setType(OskariLayer.TYPE_WMTS);
        fg.setVersion("1.0.0");
        fg.setName("kiinteistotunnukset");
        fg.setUrl("https://karttamoottori.maanmittauslaitos.fi/kiinteisto/wmts");
        fg.setStyle("default");
        fg.setOpacity(30);

        request.setLayers(Arrays.asList(bg, fg));

        String dataBg = CapabilitiesCacheService.getFromService(bg.getUrl(), bg.getType(), bg.getUsername(), bg.getPassword(), bg.getVersion());
        OskariLayerCapabilities answerBg = new OskariLayerCapabilities(1L, bg.getUrl(), bg.getType(), bg.getVersion(), dataBg, null, null);

        String dataFg = CapabilitiesCacheService.getFromService(fg.getUrl(), fg.getType(), fg.getUsername(), fg.getPassword(), fg.getVersion());
        OskariLayerCapabilities answerFg = new OskariLayerCapabilities(1L, fg.getUrl(), fg.getType(), fg.getVersion(), dataFg, null, null);

        CapabilitiesCacheService mock = Mockito.mock(CapabilitiesCacheService.class);
        Mockito.when(mock.getCapabilities(bg.getUrl(), bg.getType(), bg.getUsername(), bg.getPassword(), bg.getVersion())).thenReturn(answerBg);
        Mockito.when(mock.getCapabilities(fg.getUrl(), fg.getType(), fg.getUsername(), fg.getPassword(), fg.getVersion())).thenReturn(answerFg);

        PrintService service = new PrintService(mock);
        PDDocument doc = new PDDocument();
        service.getPDF(request, doc);
        File file = File.createTempFile("print-test", ".pdf");
        doc.save(file);
        doc.close();
        Desktop.getDesktop().open(file);
    }

}
