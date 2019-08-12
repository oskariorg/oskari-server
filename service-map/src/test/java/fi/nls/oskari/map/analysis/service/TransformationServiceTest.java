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
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;

public class TransformationServiceTest {

    private static final String UUID = "test-uuid";
    private static final long ANALYSIS_ID = 1L;
    private static final String GEOMETRY_PROPERTY = "geom";
    private static final String NS_PREFIX = "feature";
    private static final String TEST_RESOURCE_FOLDER = "fi/nls/oskari/map/analysis/service/";
    private static final String INPUT_FILE_PATH_BUFFER = TEST_RESOURCE_FOLDER
            + "TransformationServiceTest-wps-input-buffer.xml";
    private static final String EXPECTED_FILE_PATH_BUFFER = TEST_RESOURCE_FOLDER
            + "TransformationServiceTest-expected-wfst-result-buffer.xml";
    private static final String INPUT_FILE_PATH_DESCRIPTIVE_STATISTICS = TEST_RESOURCE_FOLDER
            + "TransformationServiceTest-wps-input-descriptive-statistic.xml";
    private static final String EXPECTED_FILE_PATH_DESCRIPTIVE_STATISTICS = TEST_RESOURCE_FOLDER
            + "TransformationServiceTest-expected-wfst-result-descriptive-statistic.xml";
   
    
    private TransformationService service = new TransformationService();
    
    @BeforeClass
    public static void setup() {
        XMLUnit.setIgnoreWhitespace(true);
    }
    
    @Test
    public void testWpsFeatureCollectionToWfstBuffer() throws ServiceException, SAXException, IOException {

        Map<String, String> fieldTypes = new HashMap<>();
        fieldTypes.put("onntyyppi", "string");
        fieldTypes.put("lkmpp", "numeric");
        fieldTypes.put("lkmjk", "numeric");
        fieldTypes.put("geom", "string");
        fieldTypes.put("lkmmuukulk", "numeric");
        fieldTypes.put("lkmhapa", "numeric");
        fieldTypes.put("lkmlaka", "numeric");
        fieldTypes.put("lkmmo", "numeric");
        fieldTypes.put("lkmmp", "numeric");
        fieldTypes.put("x", "numeric");
        fieldTypes.put("vvonn", "string");
        fieldTypes.put("kkonn", "numeric");
        fieldTypes.put("vakav", "string");
        fieldTypes.put("y", "numeric");
        
        List<String> fields = new ArrayList<String>();
        testWpsToWfs(INPUT_FILE_PATH_BUFFER, EXPECTED_FILE_PATH_BUFFER, fields, fieldTypes);
    }

    @Test
    public void testWpsFeatureCollectionToWfstDescriptiveStatistics()
            throws ServiceException, SAXException, IOException {

        Map<String, String> fieldTypes = new HashMap<>();
        fieldTypes.put("Mediaani", "numeric");
        fieldTypes.put("Pienin_arvo", "numeric");
        fieldTypes.put("Summa", "numeric");
        fieldTypes.put("Keskihajonta", "numeric");
        fieldTypes.put("Kohteiden_lukumäärä", "numeric");
        fieldTypes.put("Suurin_arvo", "numeric");
        fieldTypes.put("Keskiarvo", "numeric");
        
        List<String> fields = new ArrayList<String>();
        testWpsToWfs(INPUT_FILE_PATH_DESCRIPTIVE_STATISTICS, EXPECTED_FILE_PATH_DESCRIPTIVE_STATISTICS, fields, fieldTypes);
    }

    private void testWpsToWfs(String inputFilePath, String expectedFilePath, List<String> fields, Map<String, String> fieldTypes)
            throws ServiceException, IOException, SAXException {
        
        String wpsFeatures = readResource(inputFilePath);
        String expected = readResource(expectedFilePath);

        String result = service.wpsFeatureCollectionToWfst(wpsFeatures, UUID, ANALYSIS_ID, fields, fieldTypes,
                GEOMETRY_PROPERTY, NS_PREFIX);

        assertXmlIsValid(expected, result);
    }

    private String readResource(String p) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(p)) {
            return new String(IOHelper.readBytes(in), StandardCharsets.UTF_8);
        }
    }
    
    private void assertXmlIsValid(String expected, String actual) throws IOException, SAXException {
        
        Diff xmlDiff = new Diff(expected, actual);
        assertTrue(String.format("Result xml does not equal expected: %s.", xmlDiff.toString()), xmlDiff.similar());
    }
}
