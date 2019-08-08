package fi.nls.oskari.map.analysis.service;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;

public class TransformationServiceTest {

    private static final String WPS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><wfs:FeatureCollection xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:feature=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><feature:boundedBy><feature:Envelope srsDimension=\"2\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\"><feature:lowerCorner>389273.87 6674100.3344</feature:lowerCorner><feature:upperCorner>389273.87 6674100.3344</feature:upperCorner></feature:Envelope></feature:boundedBy><feature:featureMember><feature:analysis_data feature:id=\"fid--1592edd0_16c66a5ffe5_-7fe0\"><feature:boundedBy><feature:Envelope srsDimension=\"2\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\"><feature:lowerCorner>389273.87 6674100.3344</feature:lowerCorner><feature:upperCorner>389273.87 6674100.3344</feature:upperCorner></feature:Envelope></feature:boundedBy><feature:geom><feature:MultiPoint srsDimension=\"2\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\"><feature:pointMember><feature:Point srsDimension=\"2\"><feature:pos>389273.87 6674100.3344</feature:pos></feature:Point></feature:pointMember></feature:MultiPoint></feature:geom></feature:analysis_data></feature:featureMember></wfs:FeatureCollection>";
    private static final String UUID = "test-uuid";
    private static final long ANALYSIS_ID = 1L;
    private static final String GEOMETRY_PROPERTY = "geom";
    private static final String NS_PREFIX = "feature";
    private static final String EXPECTED_FILE_PATH= "fi/nls/oskari/map/analysis/service/TransformationServiceTest-expected-wfst-result.xml";
    private static Map<String, String> fieldTypes = null;
    private TransformationService service = new TransformationService();

    @Before
    public void setup() {
        fieldTypes = new HashMap<>();
        fieldTypes.put("Mediaani", "numeric");
        fieldTypes.put("Pienin_arvo", "numeric");
        fieldTypes.put("Summa", "numeric");
        fieldTypes.put("Keskihajonta", "numeric");
        fieldTypes.put("Kohteiden_lukumäärä", "numeric");
        fieldTypes.put("Suurin_arvo", "numeric");
        fieldTypes.put("Keskiarvo", "numeric");
    }

    @Test
    public void testWpsFeatureCollectionToWfst() throws ServiceException, SAXException, IOException {
        List<String> fields = new ArrayList<String>();
        String result = service.wpsFeatureCollectionToWfst(WPS, UUID, ANALYSIS_ID, fields, fieldTypes,
                GEOMETRY_PROPERTY, NS_PREFIX);
    
       assertXmlIsValid(result);
    }

    /**
     * Initial idea to validate result against GeoServer wfs.xsd was dumped since schema
     * contained multiple imports so test needs to be runned with proxy params to
     * avoid timeouts. WFST also contains oskari.org feature namespace which did not
     * pass schema validation. Because of mentioned reasons, validation is done
     * without schema by comparing result contents to loaded xml test resource.
     * @throws IOException 
     * @throws SAXException 
     */
    private void assertXmlIsValid(String xml) throws IOException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        String expectedResult = readResource(EXPECTED_FILE_PATH);
        Diff xmlDiff = new Diff(expectedResult, xml);
        assertTrue(String.format("Result xml does not equal expected: %s.",xmlDiff.toString()),xmlDiff.similar()); 
    }

    private String readResource(String p) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(p)) {
            return new String(IOHelper.readBytes(in), StandardCharsets.UTF_8);
        }
    }
}
