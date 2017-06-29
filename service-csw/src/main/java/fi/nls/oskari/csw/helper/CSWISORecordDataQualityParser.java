package fi.nls.oskari.csw.helper;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.commons.collections.map.LinkedMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CSWISORecordDataQualityParser {

    private static final Logger log = LogFactory.getLogger(CSWISORecordDataQualityParser.class);
    private static XPathExpression pathToLocalizedValue = null;

    //Linage statement
    private static XPathExpression XPATH_LINAGE_STATEMENT = null;

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
    private static XPathExpression XPATH_CONFORMANCE_RESULT = null;
    private static XPathExpression XPATH_CONFORMANCE_RESULT_SPECIFICATION_TITLE = null;
    private static XPathExpression XPATH_CONFORMANCE_RESULT_EXPLANATION = null;
    private static XPathExpression XPATH_CONFORMANCE_RESULT_PASS = null;

    //Data quality node quantitative result
    private static XPathExpression XPATH_QUANTITATIVE_RESULT = null;
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

            //Linage statement
            XPATH_LINAGE_STATEMENT = xpath.compile("./gmd:lineage/gmd:LI_Lineage/gmd:statement");

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
            XPATH_CONFORMANCE_RESULT = xpath.compile("./gmd:result/gmd:DQ_ConformanceResult"); //many
            XPATH_CONFORMANCE_RESULT_SPECIFICATION_TITLE = xpath.compile("./gmd:specification/gmd:CI_Citation/gmd:title");
            XPATH_CONFORMANCE_RESULT_EXPLANATION = xpath.compile("./gmd:explanation");;
            XPATH_CONFORMANCE_RESULT_PASS = xpath.compile("./gmd:pass");

            //Data quality node quantitative result
            XPATH_QUANTITATIVE_RESULT = xpath.compile("./gmd:result/gmd:DQ_QuantitativeResult"); //many
            XPATH_QUANTITATIVE_RESULT_VALUE_TYPE = xpath.compile("./gmd:valueType");
            XPATH_QUANTITATIVE_RESULT_VALUE_UNIT = xpath.compile("./gmd:valueUnit");
            XPATH_QUANTITATIVE_RESULT_ERROR_STATISTIC = xpath.compile("./gmd:errorStatistic"); //many
            XPATH_QUANTITATIVE_RESULT_VALUE = xpath.compile("./gmd:value");
        }
        catch (Exception e) {
            log.error("Setting XPaths failed in data quality parser");
            throw new RuntimeException("Setting XPaths failed in data quality parser");
        }
    }

    public CSWIsoRecord.DataQualityObject parseDataQualities(final NodeList dataQualityNodes, final Locale locale)  throws XPathExpressionException {
        if (locale != null) {
                pathToLocalizedValue = xpath.compile(
                        "../gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#"
                                + locale.getLanguage().toUpperCase() + "']");
        }
        CSWIsoRecord.DataQualityObject dataQualityObject = new CSWIsoRecord.DataQualityObject();
        List<CSWIsoRecord.DataQualityNode> dataQualityObjectNodeList = dataQualityObject.getDataQualityNodes();
        for (int i = 0; i < dataQualityNodes.getLength(); i++) {
            Node parentNode = dataQualityNodes.item(i);

            for (Map.Entry<String, String> entry : dataQualities.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                XPathExpression XPATH_DATA_QUALITY_CHILD_NODES = xpath.compile(value);
                NodeList dataQualityChildNodes = (NodeList) XPATH_DATA_QUALITY_CHILD_NODES.evaluate(parentNode, XPathConstants.NODESET);

                if(dataQualityChildNodes == null || dataQualityChildNodes.getLength() < 1) {
                    continue;
                }
                CSWIsoRecord.DataQualityNode dataQualityObjectNode = null;
                for (int j = 0;j < dataQualityChildNodes.getLength(); ++j) {
                    dataQualityObjectNode = GetDataQualityNodeInformation(dataQualityChildNodes.item(j), key);

                    Node linageStatementNode = (Node) XPATH_LINAGE_STATEMENT.evaluate(parentNode, XPathConstants.NODE);
                    dataQualityObjectNode.setLinageStatement(
                            new CSWIsoRecord.DataQualityValue("Linage statement", localize(linageStatementNode)));

                    NodeList dataQualityConformanceResultNodes =
                            (NodeList) XPATH_CONFORMANCE_RESULT.evaluate(dataQualityChildNodes.item(j), XPathConstants.NODESET);
                    for (int k = 0;k < dataQualityConformanceResultNodes.getLength(); ++k) {
                        dataQualityObjectNode.getConformanceResultList().add(GetConformanceResult(dataQualityConformanceResultNodes.item(k)));
                    }

                    NodeList dataQualityQuantitativeResultNodes =
                            (NodeList) XPATH_QUANTITATIVE_RESULT.evaluate(dataQualityChildNodes.item(j), XPathConstants.NODESET);
                    for (int l = 0;l < dataQualityQuantitativeResultNodes.getLength(); ++l) {
                        dataQualityObjectNode.getQuantitativeResultList().add(GetQuantitativeResult(dataQualityQuantitativeResultNodes.item(l)));
                    }

                    dataQualityObjectNodeList.add(dataQualityObjectNode);
                }
            }
        }
        dataQualityObject.setDataQualityNodes(dataQualityObjectNodeList);
        return dataQualityObject;
    }

    private CSWIsoRecord.DataQualityNode GetDataQualityNodeInformation(Node parentNode, String listName)  throws XPathExpressionException {
        CSWIsoRecord.DataQualityNode dataQualityObjectNode = new CSWIsoRecord.DataQualityNode();
        dataQualityObjectNode.setNodeName(listName);

        Node nameOfMeasureNode = (Node) XPATH_NAME_OF_MEASURE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setNameOfMeasure(
                new CSWIsoRecord.DataQualityValue("Name of measure", localize(nameOfMeasureNode)));

        Node measureIdentificationCodeNode = (Node) XPATH_MEASURE_IDENTIFICATION_CODE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setMeasureIdentificationCode(
                new CSWIsoRecord.DataQualityValue("Measure identification code", localize(measureIdentificationCodeNode)));

        Node measureIdentificationAuthorizationNode = (Node) XPATH_MEASURE_IDENTIFICATION_AUTHORIZATION.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setMeasureIdentificationAuthorization(
                new CSWIsoRecord.DataQualityValue("Measure identification authorization", localize(measureIdentificationAuthorizationNode)));

        Node measureDescriptionNode = (Node) XPATH_MEASURE_DESCRIPTION.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setMeasureDescription(
                new CSWIsoRecord.DataQualityValue("Measure description", localize(measureDescriptionNode)));

        Node evaluationMethodTypeNode = (Node) XPATH_EVALUATION_METHOD_TYPE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setEvaluationMethodType(
                new CSWIsoRecord.DataQualityValue("Evaluation method type", localize(evaluationMethodTypeNode)));

        Node evaluationProcedureNode = (Node) XPATH_EVALUATION_PROCEDURE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setEvaluationProcedure(null); //TODO parse

        NodeList dateTimeNode = (NodeList) XPATH_DATE_TIME.evaluate(parentNode, XPathConstants.NODESET);
        List<CSWIsoRecord.DataQualityValue> dateTimeValueList = new ArrayList<>();
        for (int i = 0;i < dateTimeNode.getLength(); ++i) {
            dateTimeValueList.add(new CSWIsoRecord.DataQualityValue("Time", localize(dateTimeNode.item(i))));
        }
        dataQualityObjectNode.setDateTime(dateTimeValueList);

        return dataQualityObjectNode;
    }

    private CSWIsoRecord.DataQualityConformanceResult GetConformanceResult(Node parentNode)  throws XPathExpressionException {
        CSWIsoRecord.DataQualityConformanceResult dataQualityObjectConformanceResult =
                new CSWIsoRecord.DataQualityConformanceResult();

        Node conformanceResultSpecificationNode = (Node) XPATH_CONFORMANCE_RESULT_SPECIFICATION_TITLE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectConformanceResult.setSpecification(
                new CSWIsoRecord.DataQualityValue("Title", localize(conformanceResultSpecificationNode))); //TODO parse

        Node conformanceResultExplanationNode = (Node) XPATH_CONFORMANCE_RESULT_EXPLANATION.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectConformanceResult.setExplanation(
                new CSWIsoRecord.DataQualityValue("Explanation", localize(conformanceResultExplanationNode)));

        Node conformanceResultPassNode = (Node) XPATH_CONFORMANCE_RESULT_PASS.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectConformanceResult.setPass(new CSWIsoRecord.DataQualityValue("Pass", localize(conformanceResultPassNode)));

        return dataQualityObjectConformanceResult;
    }

    private CSWIsoRecord.DataQualityQuantitativeResult GetQuantitativeResult(Node parentNode) throws XPathExpressionException {

        CSWIsoRecord.DataQualityQuantitativeResult dataQualityObjectQuantitativeResult =
                new CSWIsoRecord.DataQualityQuantitativeResult();

        Node quantitativeResultValueTypeNode = (Node) XPATH_QUANTITATIVE_RESULT_VALUE_TYPE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectQuantitativeResult.setValueType(
                new CSWIsoRecord.DataQualityValue("Value type", localize(quantitativeResultValueTypeNode)));

        Node quantitativeResultValueUnitNode = (Node) XPATH_QUANTITATIVE_RESULT_VALUE_UNIT.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectQuantitativeResult.setValueUnit(
                new CSWIsoRecord.DataQualityValue("Value unit", localize(quantitativeResultValueUnitNode)));

        NodeList quantitativeResultErrorStatisticNode = (NodeList) XPATH_QUANTITATIVE_RESULT_ERROR_STATISTIC.evaluate(parentNode, XPathConstants.NODESET);
        List<CSWIsoRecord.DataQualityValue> valueValueList = new ArrayList<>();
        for (int i = 0;i < quantitativeResultErrorStatisticNode.getLength(); ++i) {
            valueValueList.add(new CSWIsoRecord.DataQualityValue("Value", localize(quantitativeResultErrorStatisticNode.item(i))));
        }
        dataQualityObjectQuantitativeResult.setValue(valueValueList);

        Node quantitativeValueNode = (Node) XPATH_QUANTITATIVE_RESULT_VALUE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectQuantitativeResult.setErrorStatistic(
                new CSWIsoRecord.DataQualityValue("Error statistic", localize(quantitativeValueNode)));

        return dataQualityObjectQuantitativeResult;
    }

    //Move to common utility class
    private String localize(final Node elem) {
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