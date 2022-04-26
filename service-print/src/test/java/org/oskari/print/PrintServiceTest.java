package org.oskari.print;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.oskari.print.request.PrintFormat;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.wmts.WMTSCapabilitiesCache;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;

public class PrintServiceTest {

    @Test
    @Ignore("Depends on karttamoottori.maanmittauslaitos.fi, doesn't test anything")
    public void testPNG() throws ServiceException, IOException, NoSuchAuthorityCodeException, FactoryException {
        PrintRequest request = new PrintRequest();
        request.setFormat(PrintFormat.PNG);

        request.setEast(500000);
        request.setNorth(6750000);
        request.setSrsName("EPSG:3067");
        request.setWidth(512);
        request.setHeight(512);
        request.setResolution(2);
        request.setUser(new User());

        OskariLayer ortokuva_vaaravari = new OskariLayer();
        ortokuva_vaaravari.setId(1);
        ortokuva_vaaravari.setName("ortokuva_vaaravari");
        ortokuva_vaaravari.setType(OskariLayer.TYPE_WMTS);
        ortokuva_vaaravari.setVersion("1.0.0");
        ortokuva_vaaravari.setUrl("https://karttamoottori.maanmittauslaitos.fi/maasto/wmts");
        ortokuva_vaaravari.setStyle("default");

        OskariLayer kiinteistotunnukset = new OskariLayer();
        kiinteistotunnukset.setId(2);
        kiinteistotunnukset.setName("kiinteistotunnukset");
        kiinteistotunnukset.setType(OskariLayer.TYPE_WMTS);
        kiinteistotunnukset.setVersion("1.0.0");
        kiinteistotunnukset.setUrl("https://karttamoottori.maanmittauslaitos.fi/kiinteisto/wmts");
        kiinteistotunnukset.setStyle("default");

        PrintLayer bg = new PrintLayer(0);
        bg.setOskariLayer(ortokuva_vaaravari);
        bg.setOpacity(100);

        PrintLayer fg = new PrintLayer(1);
        fg.setOskariLayer(kiinteistotunnukset);
        fg.setOpacity(100);

        OskariLayer rakennus = new OskariLayer();
        rakennus.setId(3);
        rakennus.setName("rakennus");
        rakennus.setType(OskariLayer.TYPE_WFS);
        rakennus.setVersion("3.0.0");
        rakennus.setUrl("http://visukysely01.nls.fi:8080/mtkgml");

        PrintLayer fg2 = new PrintLayer(2);
        fg2.setOskariLayer(rakennus);
        fg2.setOpacity(80);
        fg2.setStyle("default");

        request.setLayers(Arrays.asList(bg, fg, fg2));

        WMTSCapabilitiesCache cache = new WMTSCapabilitiesCache();
        PrintService service = new PrintService(cache);

        BufferedImage img = service.getPNG(request);
        File file = File.createTempFile("print-test", ".png");
        ImageIO.write(img, "png", file);
        Desktop.getDesktop().open(file);
    }

    @Test
    @Ignore("Depends on karttamoottori.maanmittauslaitos.fi, doesn't test anything")
    public void testPDF() throws ServiceException, IOException, NoSuchAuthorityCodeException, FactoryException {
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

        OskariLayer taustakartta = new OskariLayer();
        taustakartta.setId(1);
        taustakartta.setName("taustakartta");
        taustakartta.setType(OskariLayer.TYPE_WMTS);
        taustakartta.setVersion("1.0.0");
        taustakartta.setUrl("https://karttamoottori.maanmittauslaitos.fi/maasto/wmts");
        taustakartta.setStyle("default");

        OskariLayer kiinteistotunnukset = new OskariLayer();
        kiinteistotunnukset.setId(2);
        kiinteistotunnukset.setType(OskariLayer.TYPE_WMTS);
        kiinteistotunnukset.setVersion("1.0.0");
        kiinteistotunnukset.setName("kiinteistotunnukset");
        kiinteistotunnukset.setUrl("https://karttamoottori.maanmittauslaitos.fi/kiinteisto/wmts");
        kiinteistotunnukset.setStyle("default");

        PrintLayer bg = new PrintLayer(0);
        bg.setOskariLayer(taustakartta);
        bg.setOpacity(100);

        PrintLayer fg = new PrintLayer(1);
        fg.setOskariLayer(kiinteistotunnukset);
        fg.setOpacity(30);

        request.setLayers(Arrays.asList(bg, fg));

        WMTSCapabilitiesCache cache = new WMTSCapabilitiesCache();
        PrintService service = new PrintService(cache);

        PDDocument doc = new PDDocument();
        service.getPDF(request, doc);
        File file = File.createTempFile("print-test", ".pdf");
        doc.save(file);
        doc.close();
        Desktop.getDesktop().open(file);
    }

    @Test
    @Ignore("Depends on outside resources, doesn't test anything")
    public void testPDFVector() throws ServiceException, IOException, NoSuchAuthorityCodeException, FactoryException, DuplicateException, JSONException {
        PropertyUtil.addProperty("oskari.native.srs", "EPSG:3067", true);

        PrintRequest request = new PrintRequest();
        request.setFormat(PrintFormat.PDF);

        request.setUser(new User());

        request.setEast(385940);
        request.setNorth(6672046);
        request.setSrsName("EPSG:3067");

        request.setTitle("Hello world!");

        request.setWidth(PDF.mmToPx(210 - 20));
        request.setHeight(PDF.mmToPx(297 - 30));
        request.setResolution(1);

        request.setShowLogo(true);
        request.setShowScale(true);

        OskariLayer taustakartta = new OskariLayer();
        taustakartta.setId(1);
        taustakartta.setName("taustakartta");
        taustakartta.setType(OskariLayer.TYPE_WMTS);
        taustakartta.setVersion("1.0.0");
        taustakartta.setUrl("https://karttamoottori.maanmittauslaitos.fi/maasto/wmts");
        taustakartta.setStyle("default");

        PrintLayer bg = new PrintLayer(0);
        bg.setOskariLayer(taustakartta);
        bg.setOpacity(100);

        OskariLayer tieviiva = new OskariLayer();
        tieviiva.setId(2);
        tieviiva.setName("tieviiva");
        tieviiva.setType(OskariLayer.TYPE_WFS);
        tieviiva.setVersion("3.0.0");
        tieviiva.setUrl("http://visukysely01.nls.fi:8080/mtkgml");
        JSONObject opt = new JSONObject("" +
                "{\"styles\":{\"StyleName\":{\"tieviiva\":{\"featureStyle\":" +
                "{\"stroke\":{\"color\":\"#ff0000\",\"width\":2,\"lineDash\": \"dot\"}}" +
                "}}}}"
        );
        tieviiva.setOptions(opt);

        PrintLayer fg = new PrintLayer(1);
        fg.setOskariLayer(tieviiva);
        fg.setOpacity(100);
        fg.setStyle("StyleName");

        OskariLayer rakennus = new OskariLayer();
        rakennus.setId(3);
        rakennus.setName("rakennus");
        rakennus.setType(OskariLayer.TYPE_WFS);
        rakennus.setVersion("3.0.0");
        rakennus.setUrl("http://visukysely01.nls.fi:8080/mtkgml");
        JSONObject opt2 = new JSONObject("" +
                "{\"styles\":{\"StyleName\":{\"rakennus\":{\"featureStyle\":" +
                "{\"fill\":{\"color\":\"#00ff00\",\"area\":{\"pattern\": 2}}}," +
                "\"stroke\":{\"area\":{\"color\":\"#0000ff\"}}}" +
                "}}}}"
        );
        rakennus.setOptions(opt2);

        PrintLayer fg2 = new PrintLayer(2);
        fg2.setOskariLayer(rakennus);
        fg2.setOpacity(50);
        fg2.setStyle("StyleName");

        request.setLayers(Arrays.asList(bg, fg, fg2));

        WMTSCapabilitiesCache cache = new WMTSCapabilitiesCache();
        PrintService service = new PrintService(cache);

        PDDocument doc = new PDDocument();
        service.getPDF(request, doc);
        File file = File.createTempFile("print-test", ".pdf");
        doc.save(file);
        doc.close();
        Desktop.getDesktop().open(file);
    }

}
