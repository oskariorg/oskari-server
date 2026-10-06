package fi.nls.oskari.csw.helper;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQuality;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityConformanceResult;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityObject;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityQuantitativeResult;
import org.oskari.xml.XmlHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class CSWISORecordDataQualityParserTest {

    private String METADATA_ID = "MD_Metadata";
    private String DATA_QUALITIES = "dataQualities"; 
    private String LINEAGE_STATEMENTS = "lineageStatements";

    private Node getMetadataNode() throws Exception {
        InputStream in = getClass().getResourceAsStream("csw.xml");
        Element ret = XmlHelper.parseXML(in, true);
        return XmlHelper.getFirstChild(ret, METADATA_ID);
    }

    private DataQualityConformanceResult parseConformanceSpecification(String titleXml) {
        Element metadata = XmlHelper.parseXML("""
            <gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd"
                             xmlns:gmx="http://www.isotc211.org/2005/gmx"
                             xmlns:xlink="http://www.w3.org/1999/xlink">
                <gmd:dataQualityInfo>
                    <gmd:DQ_DataQuality>
                        <gmd:report>
                            <gmd:DQ_DomainConsistency>
                                <gmd:result>
                                    <gmd:DQ_ConformanceResult>
                                        <gmd:specification>
                                            <gmd:CI_Citation>
                                                <gmd:title>%s</gmd:title>
                                            </gmd:CI_Citation>
                                        </gmd:specification>
                                    </gmd:DQ_ConformanceResult>
                                </gmd:result>
                            </gmd:DQ_DomainConsistency>
                        </gmd:report>
                    </gmd:DQ_DataQuality>
                </gmd:dataQualityInfo>
            </gmd:MD_Metadata>
            """.formatted(titleXml), true);
        Assertions.assertNotNull(metadata);
        CSWIsoRecord record = CSWISORecordParser.parse(metadata, Locale.forLanguageTag("fi"));
        return record.getDataQualityObject().getDataQualities().get(0).getConformanceResultList().get(0);
    }

    @Test
    public void testConformanceSpecificationAnchor() {
        DataQualityConformanceResult result = parseConformanceSpecification(
            "<gmx:Anchor xlink:href=\"http://data.europa.eu/eli/reg/2010/1089\">KOMISSION ASETUS (EU) N:o 1089/2010</gmx:Anchor>");
        Assertions.assertEquals("KOMISSION ASETUS (EU) N:o 1089/2010", result.getSpecification());
    }

    @Test
    public void testConformanceSpecificationWithoutText() {
        DataQualityConformanceResult result = parseConformanceSpecification("");
        Assertions.assertNull(result.getSpecification());
    }

    @Test
    public void TestDataQualityParsing() throws Exception {
        Node metaDataNode = getMetadataNode();
        Locale locale = new Locale("FI");
        CSWIsoRecord record = CSWISORecordParser.parse(metaDataNode, locale);
        CSWISORecordDataQualityParser dqParser = new CSWISORecordDataQualityParser();
        DataQualityObject object = record.getDataQualityObject();
        List<DataQuality> nodeList = object.getDataQualities();
        Assertions.assertTrue(nodeList.size() == 4, "There should be 4 nodes");

        DataQuality dq = nodeList.stream().filter(n -> "topologicalConsistency".equals(n.getNodeName())).findAny().orElseThrow();
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

        List <String> lineages = object.getLineageStatements();
        Assertions.assertTrue(lineages.size() == 1, "There should be only one lineage statement");
        //Check that lineage doesn't contain localized (SV, EN) content 
        Assertions.assertTrue((lineages.get(0).length() == 1313), "FI lineage statement shold contain 1313 chars");
    }

    @Test
    public void TestDataQualityJson() throws Exception {
        Node metaDataNode = getMetadataNode();
        Locale locale = new Locale("FI");
        CSWIsoRecord record = CSWISORecordParser.parse(metaDataNode, locale);
        JSONObject json = record.toJSON();
        JSONArray dataQualities = json.getJSONArray(DATA_QUALITIES);
        Assertions.assertTrue(dataQualities.length() == 4, "There should be 4 DataQuality items in JSONArray");
        JSONArray lineages = json.getJSONArray(LINEAGE_STATEMENTS);
        Assertions.assertTrue(lineages.length() == 1, "There should be 1 lineage statement");
    }

    @Test 
    public void TestLocalization() throws Exception {
        Node metaDataNode = getMetadataNode();
        Locale locale = new Locale("EN");
        CSWIsoRecord record = CSWISORecordParser.parse(metaDataNode, locale);
        DataQualityObject object = record.getDataQualityObject();
        List <String> lineages = object.getLineageStatements();
        //Check that lineage contains localized (EN) content 
        Assertions.assertEquals("Regarding", lineages.get(0).substring(0, 9));
    }
}