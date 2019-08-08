package fi.nls.oskari.map.analysis.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;

public class TransformationServiceTest {

    private static final Logger LOGGER = LogFactory.getLogger(TransformationServiceTest.class);

    private static final String WPS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><wfs:FeatureCollection xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:feature=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><feature:boundedBy><feature:Envelope srsDimension=\"2\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\"><feature:lowerCorner>389273.87 6674100.3344</feature:lowerCorner><feature:upperCorner>389273.87 6674100.3344</feature:upperCorner></feature:Envelope></feature:boundedBy><feature:featureMember><feature:analysis_data feature:id=\"fid--1592edd0_16c66a5ffe5_-7fe0\"><feature:boundedBy><feature:Envelope srsDimension=\"2\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\"><feature:lowerCorner>389273.87 6674100.3344</feature:lowerCorner><feature:upperCorner>389273.87 6674100.3344</feature:upperCorner></feature:Envelope></feature:boundedBy><feature:geom><feature:MultiPoint srsDimension=\"2\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\"><feature:pointMember><feature:Point srsDimension=\"2\"><feature:pos>389273.87 6674100.3344</feature:pos></feature:Point></feature:pointMember></feature:MultiPoint></feature:geom></feature:analysis_data></feature:featureMember></wfs:FeatureCollection>";
    //private static final String SCHEMA_FILE = "src/test/resources/wfs.xsd";
    private static final String UUID = "test-uuid";
    private static final long ANALYSIS_ID = 1L;
    private static final String GEOMETRY_PROPERTY = "geom";
    private static final String NS_PREFIX = "feature";
    private static final int EXPECTED_ELEMENT_COUNT = 1;
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
    
        LOGGER.debug(result);
        assertXmlIsValid(result);
    }

    /**
     * Initial idea to validate against GeoServer wfs.xsd was dumped since schema
     * contained multiple imports so test needs to be runned with proxy params to
     * avoid timeouts. WFST also contains oskari.org feature namespace which did not
     * pass schema validation. Because of mentioned reasons, validation is done
     * without schema by checking counts of elements in document.
     */
    private void assertXmlIsValid(String xml) {

        Document doc = convertStringToXMLDocument(xml);

        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("wfs:Transaction").getLength());
        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("wfs:Insert").getLength());
        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("feature:analysis_data").getLength());
        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("gml:geometry").getLength());
        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("gml:MultiPoint").getLength());
        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("gml:pointMember").getLength());
        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("gml:Point").getLength());
        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("gml:pos").getLength());
        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("feature:t1").getLength());
        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("feature:analysis_id").getLength());
        assertEquals(EXPECTED_ELEMENT_COUNT, doc.getElementsByTagName("feature:uuid").getLength());
    }

    private static Document convertStringToXMLDocument(String xmlString) {
        // Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try {
            // Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            // Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return null;
    }

    /*
     * private boolean xmlIsValid(String xml,String schemaFileName) { SAXSource
     * xmlSource=new SAXSource(new InputSource(new StringReader(xml))); StreamSource
     * schemaDocument=new StreamSource(new File(schemaFileName)); SchemaFactory
     * sf=SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); Schema s;
     * try { s = sf.newSchema(schemaDocument); Validator v=s.newValidator();
     * v.validate(xmlSource); return true; } catch (SAXException e ) {
     * LOGGER.error(e); return false; } catch (IOException e) { LOGGER.error(e);
     * return false; } }
     */

}
