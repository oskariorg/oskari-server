package fi.nls.oskari.control.feature;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.geotools.referencing.CRS;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fi.nls.oskari.domain.map.Feature;

public class VectorFeatureWriterHandlerTest {

    @Test
    public void testUpdateXYAxisOrder() throws Exception {
        String epsg = "EPSG:3067";
        CoordinateReferenceSystem crs = CRS.decode(epsg);

        double[] pts = {
                473183.20423224,6680301.618281904,
                473257.20423224,6680411.618281904,
                473365.70423224,6680308.118281904,
                473251.20423224,6680215.618281904,
                473183.20423224,6680301.618281904
        };

        Feature oskariFeature = new Feature();
        oskariFeature.setLayerName("foo");
        oskariFeature.setId("12345");
        oskariFeature.setProperties(new HashMap<>());
        oskariFeature.setGMLGeometryProperty("geometry");
        oskariFeature.setGeometry(createPolygon(pts));

        String wfsTransaction = VectorFeatureWriterHandler.createWFSTMessageForUpdate(oskariFeature, crs);
        double[] actual = readPosList(wfsTransaction);

        assertArrayEquals(pts, actual, 1e-10);
    }

    @Test
    public void testInsertYXAxisOrder() throws Exception {
        String epsg = "EPSG:3879";
        CoordinateReferenceSystem crs = CRS.decode(epsg);

        double[] pts = {
                25473183.20423224,6680301.618281904,
                25473257.20423224,6680411.618281904,
                25473365.70423224,6680308.118281904,
                25473251.20423224,6680215.618281904,
                25473183.20423224,6680301.618281904
        };

        Feature oskariFeature = new Feature();
        oskariFeature.setLayerName("foo");
        oskariFeature.setProperties(new HashMap<>());
        oskariFeature.setGMLGeometryProperty("geometry");
        oskariFeature.setGeometry(createPolygon(pts));

        String wfsTransaction = VectorFeatureWriterHandler.createWFSTMessageForInsert(oskariFeature, crs);
        double[] actual = readPosList(wfsTransaction);
        for (int i = 0; i < pts.length / 2; i++) {
            assertEquals(pts[i * 2 + 0], actual[i * 2 + 1], 1e-10);
            assertEquals(pts[i * 2 + 1], actual[i * 2 + 0], 1e-10);
        }
    }

    private Polygon createPolygon(double[] pts) {
        GeometryFactory gf = new GeometryFactory();
        CoordinateSequence seq = gf.getCoordinateSequenceFactory().create(pts.length / 2, 2);
        for (int i = 0; i < pts.length / 2; i++) {
            seq.setOrdinate(i, 0, pts[i * 2 + 0]);
            seq.setOrdinate(i, 1, pts[i * 2 + 1]);
        }
        return gf.createPolygon(seq);
    }
    
    private double[] readPosList(String wfsTransaction) throws SAXException, IOException, ParserConfigurationException {
        byte[] wfsTransactionUTF8 = wfsTransaction.getBytes(StandardCharsets.UTF_8);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(wfsTransactionUTF8));
        String posList = doc.getElementsByTagNameNS("http://www.opengis.net/gml", "posList").item(0).getTextContent();
        return Arrays.stream(posList.split(" ")).mapToDouble(Double::parseDouble).toArray();
    }

}
