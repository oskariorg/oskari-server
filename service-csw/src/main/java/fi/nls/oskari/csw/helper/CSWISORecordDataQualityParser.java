package fi.nls.oskari.csw.helper;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import org.apache.commons.collections.map.LinkedMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
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
                CSWIsoRecord.DataQualityObject dataQualityObject = new CSWIsoRecord.DataQualityObject();
                List<CSWIsoRecord.DataQualityNode> dataQualityObjectNodeList = dataQualityObject.getDataQualityNodes();

                for (int i = 0; i < dqNodes.getLength(); i++) {
                    parentNode = dqNodes.item(i);
                    String key = entry.getKey();
                    String value = entry.getValue();
                    XPathExpression XPATH_DATA_QUALITY_NODE = xpath.compile(value);
                    NodeList dataQualityNodeList = (NodeList) XPATH_DATA_QUALITY_NODE.evaluate(parentNode, XPathConstants.NODESET);

                    CSWIsoRecord.DataQualityNode dataQualityObjectNode = new CSWIsoRecord.DataQualityNode();
                    dataQualityObjectNode.setNodeName(null);
                    CSWIsoRecord.DataQualityValue nameOfMeasureValue = new CSWIsoRecord.DataQualityValue();
                    nameOfMeasureValue.setLabel("");
                    nameOfMeasureValue.setValue("");
                    dataQualityObjectNode.setNameOfMeasure(null);
                    CSWIsoRecord.DataQualityValue measureIdentificationCodeValue = new CSWIsoRecord.DataQualityValue();
                    measureIdentificationCodeValue.setLabel("");
                    measureIdentificationCodeValue.setValue("");
                    dataQualityObjectNode.setMeasureIdentificationCode(measureIdentificationCodeValue);
                    CSWIsoRecord.DataQualityValue measureIdentificationAuthorization = new CSWIsoRecord.DataQualityValue();
                    measureIdentificationAuthorization.setLabel("");
                    measureIdentificationAuthorization.setValue("");
                    dataQualityObjectNode.setMeasureIdentificationAuthorization(measureIdentificationAuthorization);
                    CSWIsoRecord.DataQualityValue measureDescriptionValue = new CSWIsoRecord.DataQualityValue();
                    measureDescriptionValue.setLabel("");
                    measureDescriptionValue.setValue("");
                    dataQualityObjectNode.setMeasureDescription(measureDescriptionValue);
                    CSWIsoRecord.DataQualityValue evaluationMethodType = new CSWIsoRecord.DataQualityValue();
                    evaluationMethodType.setLabel("");
                    evaluationMethodType.setValue("");
                    dataQualityObjectNode.setEvaluationMethodType(evaluationMethodType);
                    dataQualityObjectNode.setEvaluationProcecdure(null); //TODO parse
                    List<CSWIsoRecord.DataQualityValue> dateTimeValueList = new ArrayList<>();
                    CSWIsoRecord.DataQualityValue dateTimeValue = new CSWIsoRecord.DataQualityValue();
                    dateTimeValue.setLabel("");
                    dateTimeValue.setValue("");
                    dateTimeValueList.add(dateTimeValue);
                    dataQualityObjectNode.setDateTime(dateTimeValueList);

                    CSWIsoRecord.DataQualityConformanceResult dataQualityObjectConformanceResult =
                            new CSWIsoRecord.DataQualityConformanceResult();
                    dataQualityObjectConformanceResult.setSpecification(null); //TODO parse
                    CSWIsoRecord.DataQualityValue explanationValue = new CSWIsoRecord.DataQualityValue();
                    explanationValue.setLabel("");
                    explanationValue.setValue("");
                    dataQualityObjectConformanceResult.setExplanation(explanationValue);
                    CSWIsoRecord.DataQualityValue passValue = new CSWIsoRecord.DataQualityValue();
                    passValue.setLabel("");
                    passValue.setValue("");
                    dataQualityObjectConformanceResult.setPass(passValue);
                    dataQualityObjectNode.setConformanceResult(dataQualityObjectConformanceResult);

                    CSWIsoRecord.DataQualityQuantitativeResult dataQualityObjectQuantitativeResult =
                            new CSWIsoRecord.DataQualityQuantitativeResult();
                    CSWIsoRecord.DataQualityValue valueTypeValue = new CSWIsoRecord.DataQualityValue();
                    valueTypeValue.setLabel("");
                    valueTypeValue.setValue("");
                    dataQualityObjectQuantitativeResult.setValueType(valueTypeValue);
                    CSWIsoRecord.DataQualityValue valueUnitValue = new CSWIsoRecord.DataQualityValue();
                    valueUnitValue.setLabel("");
                    valueUnitValue.setValue("");
                    dataQualityObjectQuantitativeResult.setValueUnit(valueUnitValue);
                    List<CSWIsoRecord.DataQualityValue> valueValueList = new ArrayList<>();
                    CSWIsoRecord.DataQualityValue valueValue = new CSWIsoRecord.DataQualityValue();
                    valueValue.setLabel("");
                    valueValue.setValue("");
                    valueValueList.add(valueValue);
                    dataQualityObjectQuantitativeResult.setValue(valueValueList);
                    CSWIsoRecord.DataQualityValue errorStatisticValue = new CSWIsoRecord.DataQualityValue();
                    errorStatisticValue.setLabel("");
                    errorStatisticValue.setValue("");
                    dataQualityObjectQuantitativeResult.setErrorStatistic(errorStatisticValue);
                    dataQualityObjectNode.setQuantitativeResult(dataQualityObjectQuantitativeResult);

                    dataQualityObjectNodeList.add(dataQualityObjectNode);
                }
            } catch (Exception e) {
                //TODO
            }
        }
    }
}