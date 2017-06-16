package fi.nls.oskari.csw.helper;

import org.apache.commons.collections.map.LinkedMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.Map;

public class CSWISORecordDataQualityParser {

    private XPath xpath = XPathFactory.newInstance().newXPath();
    private final Map<String, String> dataQualities = new LinkedMap();

    public CSWISORecordDataQualityParser() {
        dataQualities.put("AbsoluteExternalPositionalAccuracy", "./gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy");
        dataQualities.put("CompletenessCommission", "./gmd:report/gmd:DQ_CompletenessCommission");
        dataQualities.put("CompletenessOmission", "./gmd:report/gmd:DQ_CompletenessOmission");
        dataQualities.put("ConceptualConsistency", "./gmd:report/gmd:DQ_ConceptualConsistency");
        dataQualities.put("DomainConsistency", "./gmd:report/gmd:DQ_DomainConsistency");
        dataQualities.put("FormatConsistency", "./gmd:report/gmd:DQ_FormatConsistency");
        dataQualities.put("TopologicalConsistency", "./gmd:report/gmd:DQ_TopologicalConsistency");
        dataQualities.put("GriddedDataPositionalAccuracy", "./gmd:report/gmd:DQ_GriddedDataPositionalAccuracy");
        dataQualities.put("AccuracyOfATimeMeasurement", "./gmd:report/gmd:DQ_AccuracyOfATimeMeasurement");
        dataQualities.put("TemporalConsistency", "./gmd:report/gmd:DQ_TemporalConsistency");
        dataQualities.put("TemporalValidity ", "./gmd:report/gmd:DQ_TemporalValidity ");
        dataQualities.put("ThematicClassificationCorrectness", "./gmd:report/gmd:DQ_ThematicClassificationCorrectness");
        dataQualities.put("NonQuantitativeAttributeAccuracy", "./gmd:report/gmd:DQ_NonQuantitativeAttributeAccuracy");
        dataQualities.put("QuantitativeAttributeAccuracy", "./gmd:report/gmd:DQ_QuantitativeAttributeAccuracy");
    }

    public void parseDataQualities(NodeList dqNodes) {
        Node parentNode;

        for (Map.Entry<String, String> entry : dataQualities.entrySet()) {
            try {
                for (int i = 0; i < dqNodes.getLength(); i++) {
                    parentNode = dqNodes.item(i);
                    String key = entry.getKey();
                    String value = entry.getValue();
                    XPathExpression XPATH_DATA_QUALITY_NODE = xpath.compile(value);
                    NodeList dataQualityNodeList = (NodeList) XPATH_DATA_QUALITY_NODE.evaluate(parentNode, XPathConstants.NODESET);
                }
            } catch (Exception e) {
                //TODO
            }
        }
    }
}