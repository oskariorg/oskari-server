package fi.nls.oskari.pojo;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.wfs.WFSImage;
import fi.nls.test.util.ResourceHelper;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class WFSCustomStyleStoreTest {
    private static WFSCustomStyleStore customStyle;

    String jsonResult = "{\"client\":\"test\",\"layerId\":\"216\",\"fillColor\":\"#ffde00\",\"fillPattern\":-1,\"borderColor\":\"#000000\",\"borderLinejoin\":\"mitre\",\"borderDasharray\":\"\",\"borderWidth\":1,\"strokeLinecap\":\"butt\",\"strokeColor\":\"#3233ff\",\"strokeLinejoin\":\"mitre\",\"strokeDasharray\":\"\",\"strokeWidth\":1,\"dotColor\":\"#000000\",\"dotShape\":1,\"dotSize\":3,\"geometry\":\"geom\"}";
    String jsonFail = "{\"client\"\"test\",\"layerId\":\"216\",\"fillColor\":\"#ffde00\",\"fillPattern\":-1,\"borderColor\":\"#000000\",\"borderLinejoin\":\"mitre\",\"borderDasharray\":\"\",\"borderWidth\":1,\"strokeLinecap\":\"butt\",\"strokeColor\":\"#3233ff\",\"strokeLinejoin\":\"mitre\",\"strokeDasharray\":\"\",\"strokeWidth\":1,\"dotColor\":\"#000000\",\"dotShape\":1,\"dotSize\":3}\n";

    @BeforeClass
    public static void setUp() {
        customStyle = new WFSCustomStyleStore();

        customStyle.setLayerId(Integer.toString(216));
        customStyle.setClient("test");

        customStyle.setFillColor("#ffde00");
        customStyle.setFillPattern(-1);
        customStyle.setBorderColor("#000000");
        customStyle.setBorderLinejoin("mitre");
        customStyle.setBorderDasharray("");
        customStyle.setBorderWidth(1);

        customStyle.setStrokeLinecap("butt");
        customStyle.setStrokeColor("#3233ff");
        customStyle.setStrokeLinejoin("mitre");
        customStyle.setStrokeDasharray("");
        customStyle.setStrokeWidth(1);

        customStyle.setDotColor("#000000");
        customStyle.setDotShape(1);
        customStyle.setDotSize(3);

        customStyle.setGeometry("geom");

        // use relaxed comparison settings
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreAttributeOrder(true);
    }

    @Test
    public void testJSON() throws IOException {
        String json = customStyle.getAsJSON();
        assertEquals("should get right result", jsonResult, json);
    }

    @Test
    public void testSetJSON() throws IOException {
        customStyle = WFSCustomStyleStore.setJSON(jsonResult);
        String color = customStyle.getFillColor();
        assertTrue("should have same fill color", color.equals("#ffde00"));
    }

    @Test
    public void testXML() throws IOException, SAXException {
        InputStream resource = WFSImage.class.getResourceAsStream(WFSImage.OSKARI_CUSTOM_SLD);
        if(resource == null) {
            fail("Resource not found");
        }

        String xml = IOHelper.readString(resource, "ISO-8859-1");
        customStyle.replaceValues(xml, false);
        xml = customStyle.getSld();
        if(xml == null) {
            fail("sld not found");
        }

        String res = ResourceHelper.readStringResource("custom_sld.xml", this);

        Diff xmlDiff = new Diff(xml, res);
        assertTrue("should have same result" + xmlDiff, xmlDiff.similar());
    }

    @Test
    public void testHighlightXML() throws IOException, SAXException {
        InputStream resource = WFSImage.class.getResourceAsStream(WFSImage.OSKARI_CUSTOM_SLD);
        if(resource == null) {
            fail("Resource not found");
        }

        String xml = IOHelper.readString(resource, "ISO-8859-1");
        customStyle.replaceValues(xml, true);
        xml = customStyle.getSld();
        if(xml == null) {
            fail("sld not found");
        }

        String res = ResourceHelper.readStringResource("custom_highlight_sld.xml", this);
        Diff xmlDiff = new Diff(xml, res);
        assertTrue("should have same result" + xmlDiff, xmlDiff.similar());
    }

    @Test(expected=IOException.class)
    public void testJSONIOException() throws IOException {
        WFSLayerPermissionsStore.setJSON(jsonFail);
    }
}
