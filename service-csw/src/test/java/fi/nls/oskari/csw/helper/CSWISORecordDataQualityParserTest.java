package fi.nls.oskari.csw.helper;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQuality;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityConformanceResult;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityObject;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityQuantitativeResult;
import fi.nls.oskari.util.XmlHelper;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import static org.junit.Assert.*;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class CSWISORecordDataQualityParserTest {

    private String TARGET_CRS = "EPSG:4326";
    private String SOURCE_CRS = "EPSG:3067";
    private String CSW_INPUT_FILE_NAME = "/fi/nls/oskari/csw/helper/csw.xml";
    private String METADATA_ID = "MD_Metadata";
    private String DATA_QUALITIES = "dataQualities"; 
    private String LINEAGE_STATEMENTS = "lineageStatements";

    private MathTransform getMathTransform() {
        MathTransform transform = null;
        try {
            CoordinateReferenceSystem targetCRS = CRS.decode(TARGET_CRS, true);
            CoordinateReferenceSystem sourceCRS = CRS.decode(SOURCE_CRS);
            transform = CRS.findMathTransform(sourceCRS, targetCRS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transform;
    }

    private Node getMetadataNode() {
        DocumentBuilderFactory dbf = XmlHelper.newDocumentBuilderFactory();
        dbf.setNamespaceAware(true);
        InputStream xmlInputStream = getClass().getResourceAsStream(CSW_INPUT_FILE_NAME);

        NodeList children = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlInputStream);
            Node root = doc.getDocumentElement();
            children = root.getChildNodes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Node metaDataNode = null;
        for (int i = 0; i < children.getLength(); i++) {
            if (METADATA_ID.equals(children.item(i).getLocalName())) {                
                metaDataNode = children.item(i);
            }
        }
        return metaDataNode;
    }

    @Test
    public void TestDataQualityParsing() {
        
        Node metaDataNode = getMetadataNode();
        MathTransform transform = getMathTransform();
        Locale locale = new Locale("FI");
        CSWISORecordParser parser;
        try {
            parser = new CSWISORecordParser();
            CSWIsoRecord record = parser.parse(metaDataNode, locale, transform);
            CSWISORecordDataQualityParser dqParser = new CSWISORecordDataQualityParser();
            DataQualityObject object = record.getDataQualityObject();
            List<DataQuality> nodeList = object.getDataQualities();
            assertTrue("There should be 4 nodes", nodeList.size() == 4);
            for (DataQuality dq : nodeList){
                assertTrue("Data quality's nodename should be in dataQualities map", dqParser.getDataQualitiesMap().containsKey(dq.getNodeName()));
                if ("topologicalConsistency".equals(dq.getNodeName())){
                    assertEquals("Topological ", "ELF_ADM06", dq.getNameOfMeasure());
                    assertNotNull(dq.getMeasureDescription());
                    assertNotNull(dq.getEvaluationMethodDescription());
                    //Conformance results
                    DataQualityConformanceResult conformance = dq.getConformanceResultList().get(0);
                    assertEquals("ELF Master LoD1", conformance.getSpecification());
                    assertNotNull(conformance.getExplanation());
                    assertFalse(conformance.getPass());
                    //Quantitative results
                    DataQualityQuantitativeResult quantitative = dq.getQuantitativeResultList().get(0);
                    assertEquals("Number of errors", quantitative.getValueType());
                    assertEquals("54", quantitative.getValue().get(0));
                    quantitative = dq.getQuantitativeResultList().get(1);
                    assertEquals("Percentage of errors", quantitative.getValueType());
                    assertEquals("7.96460177%", quantitative.getValue().get(0));
                }
            }
            List <String> lineages = object.getLineageStatements();
            assertTrue("There should be only one lineage statement", lineages.size() == 1);
            //Check that lineage doesn't contain localized (SV, EN) content 
            assertTrue("FI lineage statement shold contain 1313 chars", (lineages.get(0).length() == 1313));            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestDataQualityJson () {
        Node metaDataNode = getMetadataNode();
        MathTransform transform = getMathTransform();
        Locale locale = new Locale("FI");

        CSWISORecordParser parser;
        try {
            parser = new CSWISORecordParser();
            CSWIsoRecord record = parser.parse(metaDataNode, locale, transform);
            JSONObject json = record.toJSON();
            JSONArray dataQualities = json.getJSONArray(DATA_QUALITIES);
            assertTrue("There should be 4 DataQuality items in JSONArray", dataQualities.length() == 4);
            JSONArray lineages = json.getJSONArray(LINEAGE_STATEMENTS);
            assertTrue("There should be 1 lineage statement", lineages.length() == 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test 
    public void TestLocalization () {
        Node metaDataNode = getMetadataNode();
        MathTransform transform = getMathTransform();
        Locale locale = new Locale("EN");
        CSWISORecordParser parser;
        try {
            parser = new CSWISORecordParser();
            CSWIsoRecord record = parser.parse(metaDataNode, locale, transform);
            DataQualityObject object = record.getDataQualityObject();
            List <String> lineages = object.getLineageStatements();
            //Check that lineage contains localized (EN) content 
            assertEquals("Regarding", lineages.get(0).substring(0, 9));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}