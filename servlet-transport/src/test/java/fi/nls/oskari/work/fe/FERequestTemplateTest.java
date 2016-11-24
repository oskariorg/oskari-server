package fi.nls.oskari.work.fe;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

public class FERequestTemplateTest {

    @Test
    public void testRYSPGenericWFS11Template() throws XPathExpressionException,
            ParserConfigurationException, SAXException, IOException,
            TransformerException, NoSuchAuthorityCodeException,
            FactoryException, TransformException {
        FERequestTemplate template = new FERequestTemplate(
                "http://example.com/wfs", true);

        String templatePath = "/fi/nls/oskari/fe/input/request/wfs/generic/RYSP_generic_wfs_1_1_0_template.xml";
        String geomNs = ""; // Not Used ATM
        String wFSver = "1.1.0";
        String featureNs = "http://www.paikkatietopalvelu.fi/gml/asemakaava"; // Not Used ATM
        String featurePrefix = "akaava";
        String geomProp = "akaava:reunaviiva";
        String featureName = "Rakennusala";
        final String maxCount = "1000";  // Not used - only for 2.0.0
        String srsName = "EPSG:3067"; // compatible with most WFS servers...
        template.setRequestFeatures(srsName, featureNs,featurePrefix, featureName, wFSver,
                geomProp, geomNs, maxCount, false);

        CoordinateReferenceSystem crs = CRS
                .decode(srsName);

        ReferencedEnvelope env = new ReferencedEnvelope(crs);
        BoundingBox bbox = env.toBounds(crs);

        InputStream inp = getClass().getResourceAsStream(templatePath);

        ByteArrayOutputStream outs = new ByteArrayOutputStream();

        template.buildBBOXRequest_XPath(null, inp, outs, bbox, false);
        outs.flush();

        String result = new String(outs.toByteArray());
        System.out.println(result);

        assertTrue(result.indexOf("[SRSNAME]") == -1);
        assertTrue(result.indexOf("[FEATURENAME]") == -1);
        assertTrue(result.indexOf("[GEOMETRYNAME]") == -1);
        assertTrue(result.indexOf("[LOWER_CORNER]") == -1);
        assertTrue(result.indexOf("[UPPER_CORNER]") == -1);
    }
    @Test
    public void testELFGenericAUTemplate() throws XPathExpressionException,
            ParserConfigurationException, SAXException, IOException,
            TransformerException, NoSuchAuthorityCodeException,
            FactoryException, TransformException {
        FERequestTemplate template = new FERequestTemplate(
                "http://example.com/wfs", true);

        String templatePath = "/fi/nls/oskari/fe/input/request/wfs/au/ELF_generic_AU_wfs_template.xml";
        String geomNs = ""; // Not Used ATM
        String wFSver = "1.1.0";
        String featureNs = ""; // Not Used ATM
        String featurePrefix = ""; // Not Used ATM
        String geomProp = "geometry";
        String featureName = "AdministrativeUnit";
        final String maxCount = "1000";  // Not used - only for 2.0.0
        String srsName = "EPSG:900913"; // compatible with most WFS servers...
        template.setRequestFeatures(srsName, featureNs,featurePrefix, featureName, wFSver,
                geomProp, geomNs, maxCount, false);

        CoordinateReferenceSystem crs = CRS
                .decode(srsName.indexOf("900913") != -1 ? "EPSG:3785" : srsName);

        ReferencedEnvelope env = new ReferencedEnvelope(crs);
        BoundingBox bbox = env.toBounds(crs);

        InputStream inp = getClass().getResourceAsStream(templatePath);

        ByteArrayOutputStream outs = new ByteArrayOutputStream();

        template.buildBBOXRequest_XPath(null, inp, outs, bbox, false);
        outs.flush();

        String result = new String(outs.toByteArray());
        System.out.println(result);

        assertTrue(result.indexOf("[SRSNAME]") == -1);
        assertTrue(result.indexOf("[FEATURENAME]") == -1);
        assertTrue(result.indexOf("[GEOMETRYNAME]") == -1);
        assertTrue(result.indexOf("[LOWER_CORNER]") == -1);
        assertTrue(result.indexOf("[UPPER_CORNER]") == -1);
    }

    @Test
    public void testELFLegacyAUTemplate() throws XPathExpressionException,
            ParserConfigurationException, SAXException, IOException,
            TransformerException, NoSuchAuthorityCodeException,
            FactoryException, TransformException {
        FERequestTemplate template = new FERequestTemplate(
                "http://example.com/wfs", true);

        String templatePath = "/fi/nls/oskari/fe/input/format/gml/inspire/au/fgi_fi_elf_wfs_template.xml";
        String geomNs = ""; // Not Used ATM
        String wFSver = "1.1.0";
        String featureNs = ""; // Not Used ATM
        String featurePrefix = ""; // Not Used ATM
        String geomProp = "geometry";
        String featureName = "AdministrativeBoundary";
        final String maxCount = "1000";  // Not used for 1.1.0 version
        String srsName = "EPSG:900913"; // compatible with most WFS servers...
        template.setRequestFeatures(srsName, featureNs, featurePrefix, featureName, wFSver,
                geomProp, geomNs, maxCount, false);

        CoordinateReferenceSystem crs = CRS
                .decode(srsName.indexOf("900913") != -1 ? "EPSG:3785" : srsName);

        ReferencedEnvelope env = new ReferencedEnvelope(crs);
        BoundingBox bbox = env.toBounds(crs);

        InputStream inp = getClass().getResourceAsStream(templatePath);
        ByteArrayOutputStream outs = new ByteArrayOutputStream();

        template.buildBBOXRequest_XPath(null, inp, outs, bbox, false);
        outs.flush();

        String result = new String(outs.toByteArray());
        System.out.println(result);

        assertTrue(result.indexOf("[SRSNAME]") == -1);
        assertTrue(result.indexOf("[FEATURENAME]") == -1);
        assertTrue(result.indexOf("[GEOMETRYNAME]") == -1);
        assertTrue(result.indexOf("[LOWER_CORNER]") == -1);
        assertTrue(result.indexOf("[UPPER_CORNER]") == -1);
    }

}
