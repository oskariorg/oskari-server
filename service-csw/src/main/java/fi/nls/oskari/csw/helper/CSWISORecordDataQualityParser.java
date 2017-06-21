package fi.nls.oskari.csw.helper;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.commons.collections.map.LinkedMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CSWISORecordDataQualityParser {

    private static final Logger log = LogFactory.getLogger(CSWISORecordDataQualityParser.class);
    private static XPathExpression pathToLocalizedValue = null;

    //Data quality node information
    private final static XPath xpath = XPathFactory.newInstance().newXPath();
    private final static Map<String, String> dataQualities = new LinkedMap();
    private static XPathExpression XPATH_NAME_OF_MEASURE = null; //many
    private static XPathExpression XPATH_MEASURE_IDENTIFICATION_CODE = null;
    private static XPathExpression XPATH_MEASURE_IDENTIFICATION_AUTHORIZATION = null;
    private static XPathExpression XPATH_MEASURE_DESCRIPTION = null;
    private static XPathExpression XPATH_EVALUATION_METHOD_TYPE = null;
    private static XPathExpression XPATH_EVALUATION_METHOD_DESCRIPTION = null;
    private static XPathExpression XPATH_EVALUATION_PROCEDURE = null; //TODO parse
    private static XPathExpression XPATH_DATE_TIME = null; //many

    //Data quality node conformance result
    private static XPathExpression XPATH_CONFORMANCE_RESULT_SPECIFICATION = null; //TODO parse
    private static XPathExpression XPATH_CONFORMANCE_RESULT_EXPLANATION = null;
    private static XPathExpression XPATH_CONFORMANCE_RESULT_PASS = null;

    //Data quality node quantitative result
    private static XPathExpression XPATH_QUANTITATIVE_RESULT_VALUE_TYPE = null;
    private static XPathExpression XPATH_QUANTITATIVE_RESULT_VALUE_UNIT = null;
    private static XPathExpression XPATH_QUANTITATIVE_RESULT_ERROR_STATISTIC = null;
    private static XPathExpression XPATH_QUANTITATIVE_RESULT_VALUE = null;

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

        try {

            xpath.setNamespaceContext(new CSWISORecordNamespaceContext());

            //Data quality node information
            XPATH_NAME_OF_MEASURE = xpath.compile("./gmd:nameOfMeasure"); //many
            XPATH_MEASURE_IDENTIFICATION_CODE =  xpath.compile("./gmd:measureIdentification/gmd:code");
            XPATH_MEASURE_IDENTIFICATION_AUTHORIZATION =  xpath.compile("./gmd:measureIdentification/gmd:authorization");
            XPATH_MEASURE_DESCRIPTION =  xpath.compile("./gmd:measureDescription");
            XPATH_EVALUATION_METHOD_TYPE =  xpath.compile("./gmd:evaluationMethodType");
            XPATH_EVALUATION_METHOD_DESCRIPTION =  xpath.compile("./gmd:evaluationMethodDescription");
            XPATH_EVALUATION_PROCEDURE =  xpath.compile("./gmd:evaluationProcedure");
            XPATH_DATE_TIME =  xpath.compile("./gmd:dateTime"); //many

            //Data quality node conformance result
            XPATH_CONFORMANCE_RESULT_SPECIFICATION = xpath.compile("./gmd:result/gmd:DQ_ConformanceResult/gmd:specification");;
            XPATH_CONFORMANCE_RESULT_EXPLANATION = xpath.compile("./gmd:result/gmd:DQ_ConformanceResult/gmd:explanation");;
            XPATH_CONFORMANCE_RESULT_PASS = xpath.compile("./gmd:result/gmd:DQ_ConformanceResult/gmd:pass");

            //Data quality node quantitative result
            XPATH_QUANTITATIVE_RESULT_VALUE_TYPE = xpath.compile("./gmd:result/gmd:DQ_QuantitativeResult/gmd:valueType");
            XPATH_QUANTITATIVE_RESULT_VALUE_UNIT = xpath.compile("./gmd:result/gmd:DQ_QuantitativeResult/gmd:valueUnit");
            XPATH_QUANTITATIVE_RESULT_ERROR_STATISTIC = xpath.compile("./gmd:result/gmd:DQ_QuantitativeResult/gmd:errorStatistic");
            XPATH_QUANTITATIVE_RESULT_VALUE = xpath.compile("./gmd:result/gmd:DQ_QuantitativeResult/gmd:value");
        }
        catch (Exception e) {
            ///TODO
        }
    }

    public CSWIsoRecord.DataQualityObject parseDataQualities(final NodeList dataQualityNodes, final Locale locale) {
        if (locale != null) {
            try {
                pathToLocalizedValue = xpath.compile(
                        "../gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#"
                                + locale.getLanguage().toUpperCase() + "']");
            }
            catch (Exception e) {
                //TODO
            }
        }
        CSWIsoRecord.DataQualityObject dataQualityObject = new CSWIsoRecord.DataQualityObject();
        List<CSWIsoRecord.DataQualityNode> dataQualityObjectNodeList = dataQualityObject.getDataQualityNodes();
        for (int i = 0; i < dataQualityNodes.getLength(); i++) {
            Node parentNode = dataQualityNodes.item(i);
            try {
                for (Map.Entry<String, String> entry : dataQualities.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    XPathExpression XPATH_DATA_QUALITY_NODE = xpath.compile(value);
                    Node dataQualityNode = (Node) XPATH_DATA_QUALITY_NODE.evaluate(parentNode, XPathConstants.NODE);

                    if(dataQualityNode == null) {
                        continue;
                    }

                    CSWIsoRecord.DataQualityNode dataQualityObjectNode = GetDataQualityNodeInformation(dataQualityNode, key);
                    dataQualityObjectNode.setConformanceResult(GetConformanceResult(dataQualityNode));
                    dataQualityObjectNode.setQuantitativeResult(GetQuantitativeResult(dataQualityNode));

                    dataQualityObjectNodeList.add(dataQualityObjectNode);
                }
            } catch (Exception e) {
                //TODO
            }
        }
        dataQualityObject.setDataQualityNodes(dataQualityObjectNodeList);
        return dataQualityObject;
    }

    private CSWIsoRecord.DataQualityNode GetDataQualityNodeInformation(Node parentNode, String listName) {
        try {
            CSWIsoRecord.DataQualityNode dataQualityObjectNode = new CSWIsoRecord.DataQualityNode();

            Node nameOfMeasureNode = (Node) XPATH_NAME_OF_MEASURE.evaluate(parentNode, XPathConstants.NODE);
            dataQualityObjectNode.setNodeName(listName);
            CSWIsoRecord.DataQualityValue nameOfMeasureValue = new CSWIsoRecord.DataQualityValue();
            nameOfMeasureValue.setLabel("Name of measure");
            nameOfMeasureValue.setValue(getLocalizedContent(nameOfMeasureNode));
            dataQualityObjectNode.setNameOfMeasure(nameOfMeasureValue);

            Node measureIdentificationCodeNode = (Node) XPATH_MEASURE_IDENTIFICATION_CODE.evaluate(parentNode, XPathConstants.NODE);
            CSWIsoRecord.DataQualityValue measureIdentificationCodeValue = new CSWIsoRecord.DataQualityValue();
            measureIdentificationCodeValue.setLabel("Measure identification code");
            measureIdentificationCodeValue.setValue(getLocalizedContent(measureIdentificationCodeNode));
            dataQualityObjectNode.setMeasureIdentificationCode(measureIdentificationCodeValue);

            Node measureIdentificationAuthorizationNode = (Node) XPATH_MEASURE_IDENTIFICATION_AUTHORIZATION.evaluate(parentNode, XPathConstants.NODE);
            CSWIsoRecord.DataQualityValue measureIdentificationAuthorizationValue = new CSWIsoRecord.DataQualityValue();
            measureIdentificationAuthorizationValue.setLabel("Measure identification authorization");
            measureIdentificationAuthorizationValue.setValue(getLocalizedContent(measureIdentificationAuthorizationNode));
            dataQualityObjectNode.setMeasureIdentificationAuthorization(measureIdentificationAuthorizationValue);

            Node measureDescriptionNode = (Node) XPATH_MEASURE_DESCRIPTION.evaluate(parentNode, XPathConstants.NODE);
            CSWIsoRecord.DataQualityValue measureDescriptionValue = new CSWIsoRecord.DataQualityValue();
            measureDescriptionValue.setLabel("Measure description");
            measureDescriptionValue.setValue(getLocalizedContent(measureDescriptionNode));
            dataQualityObjectNode.setMeasureDescription(measureDescriptionValue);

            Node evaluationMethodTypeNode = (Node) XPATH_EVALUATION_METHOD_TYPE.evaluate(parentNode, XPathConstants.NODE);
            CSWIsoRecord.DataQualityValue evaluationMethodTypeValue = new CSWIsoRecord.DataQualityValue();
            evaluationMethodTypeValue.setLabel("Evaluation method type");
            evaluationMethodTypeValue.setValue(getLocalizedContent(evaluationMethodTypeNode));
            dataQualityObjectNode.setEvaluationMethodType(evaluationMethodTypeValue);

            Node evaluationProcedureNode = (Node) XPATH_EVALUATION_PROCEDURE.evaluate(parentNode, XPathConstants.NODE);
            dataQualityObjectNode.setEvaluationProcedure(null); //TODO parse

            NodeList dateTimeNode = (NodeList) XPATH_DATE_TIME.evaluate(parentNode, XPathConstants.NODESET);
            List<CSWIsoRecord.DataQualityValue> dateTimeValueList = new ArrayList<>();
            CSWIsoRecord.DataQualityValue dateTimeValue = new CSWIsoRecord.DataQualityValue();
            dateTimeValue.setLabel("Time");
            dateTimeValue.setValue(getLocalizedContent(null));
            dateTimeValueList.add(dateTimeValue);
            dataQualityObjectNode.setDateTime(dateTimeValueList);

            return dataQualityObjectNode;
        }
        catch (Exception e) {
            //TODO
            throw new RuntimeException();
        }
    }

    private CSWIsoRecord.DataQualityConformanceResult GetConformanceResult(Node parentNode) {
        try {
            Node conformanceResultSpecificationNode = (Node) XPATH_CONFORMANCE_RESULT_SPECIFICATION.evaluate(parentNode, XPathConstants.NODE);
            CSWIsoRecord.DataQualityConformanceResult dataQualityObjectConformanceResult =
                    new CSWIsoRecord.DataQualityConformanceResult();
            dataQualityObjectConformanceResult.setSpecification(null); //TODO parse

            Node conformanceResultExplanationNode = (Node) XPATH_CONFORMANCE_RESULT_EXPLANATION.evaluate(parentNode, XPathConstants.NODE);
            CSWIsoRecord.DataQualityValue explanationValue = new CSWIsoRecord.DataQualityValue();
            explanationValue.setLabel("Explanation");
            explanationValue.setValue(getLocalizedContent(conformanceResultExplanationNode));
            dataQualityObjectConformanceResult.setExplanation(explanationValue);

            Node conformanceResultPassNode = (Node) XPATH_CONFORMANCE_RESULT_PASS.evaluate(parentNode, XPathConstants.NODE);
            CSWIsoRecord.DataQualityValue passValue = new CSWIsoRecord.DataQualityValue();
            passValue.setLabel("Pass");
            passValue.setValue(getLocalizedContent(conformanceResultPassNode));
            dataQualityObjectConformanceResult.setPass(passValue);

            return dataQualityObjectConformanceResult;
        }
        catch (Exception e) {
            //TODO
            throw new RuntimeException();
        }
    }

    private CSWIsoRecord.DataQualityQuantitativeResult GetQuantitativeResult(Node parentNode) {

        CSWIsoRecord.DataQualityQuantitativeResult dataQualityObjectQuantitativeResult =
                new CSWIsoRecord.DataQualityQuantitativeResult();

        try {
            Node quantitativeResultValueTypeNode = (Node) XPATH_QUANTITATIVE_RESULT_VALUE_TYPE.evaluate(parentNode, XPathConstants.NODE);
            CSWIsoRecord.DataQualityValue valueTypeValue = new CSWIsoRecord.DataQualityValue();
            valueTypeValue.setLabel("Value type");
            valueTypeValue.setValue(getLocalizedContent(quantitativeResultValueTypeNode));
            dataQualityObjectQuantitativeResult.setValueType(valueTypeValue);

            Node quantitativeResultValueUnitNode = (Node) XPATH_QUANTITATIVE_RESULT_VALUE_UNIT.evaluate(parentNode, XPathConstants.NODE);
            CSWIsoRecord.DataQualityValue valueUnitValue = new CSWIsoRecord.DataQualityValue();
            valueUnitValue.setLabel("Value unit");
            valueUnitValue.setValue(getLocalizedContent(quantitativeResultValueUnitNode));
            dataQualityObjectQuantitativeResult.setValueUnit(valueUnitValue);

            NodeList quantitativeResultErrorStatisticNode = (NodeList) XPATH_QUANTITATIVE_RESULT_ERROR_STATISTIC.evaluate(parentNode, XPathConstants.NODESET);
            List<CSWIsoRecord.DataQualityValue> valueValueList = new ArrayList<>();
            CSWIsoRecord.DataQualityValue valueValue = new CSWIsoRecord.DataQualityValue();
            valueValue.setLabel("Value");
            valueValue.setValue(getLocalizedContent(null));
            valueValueList.add(valueValue);
            dataQualityObjectQuantitativeResult.setValue(valueValueList);

            Node quantitativeValueNode = (Node) XPATH_QUANTITATIVE_RESULT_VALUE.evaluate(parentNode, XPathConstants.NODE);
            CSWIsoRecord.DataQualityValue errorStatisticValue = new CSWIsoRecord.DataQualityValue();
            errorStatisticValue.setLabel("Error statistic");
            errorStatisticValue.setValue(getLocalizedContent(quantitativeValueNode));
            dataQualityObjectQuantitativeResult.setErrorStatistic(errorStatisticValue);

            return dataQualityObjectQuantitativeResult;
        }
        catch (Exception e) {
            //TODO
            throw new RuntimeException();
        }
    }

    //Move to common utility class
    private String getLocalizedContent(final Node elem) {
        String ret = getText(elem);
        String localized;
        if (elem != null && pathToLocalizedValue != null) {
            try {
                final Node localeNode = (Node) pathToLocalizedValue.evaluate(elem, XPathConstants.NODE);
                localized  = getText(localeNode);
                if (localized != null && !localized.isEmpty()) {
                    ret = localized;
                }
            } catch (Exception e) {
                log.warn("Error parsing localized value for:", elem.getLocalName(), ". Message:", e.getMessage());
            }
        }
        return ret;
    }

    //Move to common utility class
    private String getText(final Node element) {
        String ret = null;
        if (element != null) {
            ret = element.getTextContent();
            if (ret != null) {
                ret = ret.trim();
            }
        }
        return ret;
    }
}