package fi.nls.oskari.csw.helper;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQuality;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityConformanceResult;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityObject;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityQuantitativeResult;
import org.oskari.xml.XmlHelper;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class CSWISORecordDataQualityParserTest {

    private String TARGET_CRS = "EPSG:4326";
    private String SOURCE_CRS = "EPSG:3067";
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

    private Node getMetadataNode() throws Exception {
        InputStream in = getClass().getResourceAsStream("csw.xml");
        Element ret = XmlHelper.parseXML(in, true);
        return XmlHelper.getFirstChild(ret, "MD_Metadata");
    }

    @Test
    public void TestDataQualityParsing() throws Exception {
        
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
            Assertions.assertTrue(nodeList.size() == 4, "There should be 4 nodes");
            for (DataQuality dq : nodeList){
                Assertions.assertTrue(dqParser.getDataQualitiesMap().containsKey(dq.getNodeName()), "Data quality's nodename should be in dataQualities map");
                if ("topologicalConsistency".equals(dq.getNodeName())){
                    Assertions.assertEquals("ELF_ADM06", dq.getNameOfMeasure(), "Topological ");
                    Assertions.assertNotNull(dq.getMeasureDescription());
                    Assertions.assertNotNull(dq.getEvaluationMethodDescription());
                    //Conformance results
                    DataQualityConformanceResult conformance = dq.getConformanceResultList().get(0);
                    Assertions.assertEquals("ELF Master LoD1", conformance.getSpecification());
                    Assertions.assertNotNull(conformance.getExplanation());
                    Assertions.assertFalse(conformance.getPass());
                    //Quantitative results
                    DataQualityQuantitativeResult quantitative = dq.getQuantitativeResultList().get(0);
                    Assertions.assertEquals("Number of errors", quantitative.getValueType());
                    Assertions.assertEquals("54", quantitative.getValue().get(0));
                    quantitative = dq.getQuantitativeResultList().get(1);
                    Assertions.assertEquals("Percentage of errors", quantitative.getValueType());
                    Assertions.assertEquals("7.96460177%", quantitative.getValue().get(0));
                }
            }
            List <String> lineages = object.getLineageStatements();
            Assertions.assertTrue(lineages.size() == 1, "There should be only one lineage statement");
            //Check that lineage doesn't contain localized (SV, EN) content 
            Assertions.assertTrue((lineages.get(0).length() == 1313), "FI lineage statement shold contain 1313 chars");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestDataQualityJson () throws Exception {
        Node metaDataNode = getMetadataNode();
        MathTransform transform = getMathTransform();
        Locale locale = new Locale("FI");

        CSWISORecordParser parser;
        try {
            parser = new CSWISORecordParser();
            CSWIsoRecord record = parser.parse(metaDataNode, locale, transform);
            JSONObject json = record.toJSON();
            JSONArray dataQualities = json.getJSONArray(DATA_QUALITIES);
            Assertions.assertTrue(dataQualities.length() == 4, "There should be 4 DataQuality items in JSONArray");
            JSONArray lineages = json.getJSONArray(LINEAGE_STATEMENTS);
            Assertions.assertTrue(lineages.length() == 1, "There should be 1 lineage statement");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test 
    public void TestLocalization () throws Exception {
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
            Assertions.assertEquals("Regarding", lineages.get(0).substring(0, 9));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}