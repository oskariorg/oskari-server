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
import java.util.Map;

public class CSWISORecordDataQualityParser {

    private static final Logger log = LogFactory.getLogger(CSWISORecordDataQualityParser.class);
    private static XPathExpression pathToLocalizedValue = null;

    //Lineage statement
    private XPathExpression XPATH_LINEAGE_STATEMENT = null;

    //Data quality node information
    private final XPath xpath = XPathFactory.newInstance().newXPath();
    private final Map<String, String> dataQualities = new LinkedMap();
    private XPathExpression XPATH_NAME_OF_MEASURE = null; //many
    private XPathExpression XPATH_MEASURE_IDENTIFICATION_CODE = null;
    private XPathExpression XPATH_MEASURE_IDENTIFICATION_AUTHORIZATION = null;
    private XPathExpression XPATH_MEASURE_DESCRIPTION = null;
    private XPathExpression XPATH_EVALUATION_METHOD_TYPE = null;
    private XPathExpression XPATH_EVALUATION_METHOD_DESCRIPTION = null;
    private XPathExpression XPATH_EVALUATION_PROCEDURE = null; //TODO parse
    private XPathExpression XPATH_DATE_TIME = null; //many

    //Data quality node conformance result
    private XPathExpression XPATH_CONFORMANCE_RESULT = null;
    private XPathExpression XPATH_CONFORMANCE_RESULT_SPECIFICATION_TITLE = null;
    private XPathExpression XPATH_CONFORMANCE_RESULT_EXPLANATION = null;
    private XPathExpression XPATH_CONFORMANCE_RESULT_PASS = null;

    //Data quality node quantitative result
    private XPathExpression XPATH_QUANTITATIVE_RESULT = null;
    private XPathExpression XPATH_QUANTITATIVE_RESULT_VALUE_TYPE = null;
    private XPathExpression XPATH_QUANTITATIVE_RESULT_VALUE_UNIT = null;
    private XPathExpression XPATH_QUANTITATIVE_RESULT_ERROR_STATISTIC = null;
    private XPathExpression XPATH_QUANTITATIVE_RESULT_VALUE = null;
    //Free text (character string)
    private XPathExpression XPATH_CHARACTER_STRING = null;

    public CSWISORecordDataQualityParser() {
        dataQualities.put("absoluteExternalPositionalAccuracy", "./gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy");
        dataQualities.put("completenessCommission", "./gmd:report/gmd:DQ_CompletenessCommission");
        dataQualities.put("completenessOmission", "./gmd:report/gmd:DQ_CompletenessOmission");
        dataQualities.put("conceptualConsistency", "./gmd:report/gmd:DQ_ConceptualConsistency");
        dataQualities.put("domainConsistency", "./gmd:report/gmd:DQ_DomainConsistency");
        dataQualities.put("formatConsistency", "./gmd:report/gmd:DQ_FormatConsistency");
        dataQualities.put("topologicalConsistency", "./gmd:report/gmd:DQ_TopologicalConsistency");
        dataQualities.put("griddedDataPositionalAccuracy", "./gmd:report/gmd:DQ_GriddedDataPositionalAccuracy");
        dataQualities.put("accuracyOfTimeMeasurement", "./gmd:report/gmd:DQ_AccuracyOfATimeMeasurement");
        dataQualities.put("temporalConsistency", "./gmd:report/gmd:DQ_TemporalConsistency");
        dataQualities.put("temporalValidity ", "./gmd:report/gmd:DQ_TemporalValidity ");
        dataQualities.put("thematicClassificationCorrectness", "./gmd:report/gmd:DQ_ThematicClassificationCorrectness");
        dataQualities.put("nonQuantitativeAttributeAccuracy", "./gmd:report/gmd:DQ_NonQuantitativeAttributeAccuracy");
        dataQualities.put("quantitativeAttributeAccuracy", "./gmd:report/gmd:DQ_QuantitativeAttributeAccuracy");

        try {
            xpath.setNamespaceContext(new CSWISORecordNamespaceContext());

            //Lineage statement
            XPATH_LINEAGE_STATEMENT = xpath.compile("./gmd:lineage/gmd:LI_Lineage/gmd:statement");

            //Data quality node information: Aspect of quantitative quality information
            XPATH_NAME_OF_MEASURE = xpath.compile("./gmd:nameOfMeasure"); //many
            XPATH_MEASURE_IDENTIFICATION_CODE =  xpath.compile("./gmd:measureIdentification/gmd:code"); //MD_Identifier (code, authority, RS_Identifier)
            XPATH_MEASURE_IDENTIFICATION_AUTHORIZATION =  xpath.compile("./gmd:measureIdentification/gmd:authorization"); //MD_Identifier (code, authority, RS_Identifier)
            XPATH_MEASURE_DESCRIPTION =  xpath.compile("./gmd:measureDescription");
            XPATH_EVALUATION_METHOD_TYPE =  xpath.compile("./gmd:evaluationMethodType"); //b.1.17
            XPATH_EVALUATION_METHOD_DESCRIPTION =  xpath.compile("./gmd:evaluationMethodDescription");
            XPATH_EVALUATION_PROCEDURE =  xpath.compile("./gmd:evaluationProcedure"); //CI_Citation
            XPATH_DATE_TIME =  xpath.compile("./gmd:dateTime"); //many

            //Data quality node conformance result
            XPATH_CONFORMANCE_RESULT = xpath.compile("./gmd:result/gmd:DQ_ConformanceResult"); //many
            XPATH_CONFORMANCE_RESULT_SPECIFICATION_TITLE = xpath.compile("./gmd:specification/gmd:CI_Citation/gmd:title"); //FreeText
            XPATH_CONFORMANCE_RESULT_EXPLANATION = xpath.compile("./gmd:explanation"); //FreeText
            XPATH_CONFORMANCE_RESULT_PASS = xpath.compile("./gmd:pass"); //gco:Boolean

            //Data quality node quantitative result
            XPATH_QUANTITATIVE_RESULT = xpath.compile("./gmd:result/gmd:DQ_QuantitativeResult"); //many
            XPATH_QUANTITATIVE_RESULT_VALUE_TYPE = xpath.compile("./gmd:valueType"); //RecordType
            XPATH_QUANTITATIVE_RESULT_VALUE_UNIT = xpath.compile("./gmd:valueUnit"); //UOM
            XPATH_QUANTITATIVE_RESULT_ERROR_STATISTIC = xpath.compile("./gmd:errorStatistic"); //FreeText
            XPATH_QUANTITATIVE_RESULT_VALUE = xpath.compile("./gmd:value"); //many //Record

            //Free text (character string)
            XPATH_CHARACTER_STRING = xpath.compile("./gco:CharacterString");
        }
        catch (Exception e) {
            log.error("Setting XPaths failed in data quality parser");
            throw new RuntimeException("Setting XPaths failed in data quality parser");
        }
    }
    public Map<String, String> getDataQualitiesMap (){
        return this.dataQualities;
    }

    public CSWIsoRecord.DataQualityObject parseDataQualities(final NodeList dataQualityNodes, final XPathExpression pathToLoc)  throws XPathExpressionException {
        pathToLocalizedValue = pathToLoc;

        CSWIsoRecord.DataQualityObject dataQualityObject = new CSWIsoRecord.DataQualityObject();
        List<CSWIsoRecord.DataQuality> dataQualityList = new ArrayList<>();

        for (int i = 0; i < dataQualityNodes.getLength(); i++) {
            Node parentNode = dataQualityNodes.item(i);
            // parse scope: The specific data to which the data quality information applies
            //TODO: should we parse scope?? or is it always dataset <gmd:MD_ScopeCode codeListValue="dataset">

            // parse lineage statements
            Node lineageStatementNode = (Node) XPATH_LINEAGE_STATEMENT.evaluate(parentNode, XPathConstants.NODE);
            if (lineageStatementNode != null){
                String lineageStatement = localize(lineageStatementNode);
                if (lineageStatement != null){
                    dataQualityObject.getLineageStatements().add(lineageStatement);
                }
            }

            // parse dataQualities (gmd:report)
            for (Map.Entry<String, String> entry : dataQualities.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                XPathExpression pathToDQChildNode = xpath.compile(value);
                NodeList dataQualityChildNodes = (NodeList) pathToDQChildNode.evaluate(parentNode, XPathConstants.NODESET);

                if(dataQualityChildNodes == null || dataQualityChildNodes.getLength() < 1) {
                    continue;
                }
                for (int j = 0;j < dataQualityChildNodes.getLength(); ++j) {
                    CSWIsoRecord.DataQuality dataQuality = new CSWIsoRecord.DataQuality();
                    dataQuality.setNodeName(key);
                    getDataQualityNodeInformation(dataQualityChildNodes.item(j), dataQuality);

                    NodeList dataQualityConformanceResultNodes =
                            (NodeList) XPATH_CONFORMANCE_RESULT.evaluate(dataQualityChildNodes.item(j), XPathConstants.NODESET);
                    for (int k = 0;k < dataQualityConformanceResultNodes.getLength(); ++k) {
                        dataQuality.getConformanceResultList().add(getConformanceResult(dataQualityConformanceResultNodes.item(k)));
                    }

                    NodeList dataQualityQuantitativeResultNodes =
                            (NodeList) XPATH_QUANTITATIVE_RESULT.evaluate(dataQualityChildNodes.item(j), XPathConstants.NODESET);
                    for (int l = 0;l < dataQualityQuantitativeResultNodes.getLength(); ++l) {
                        dataQuality.getQuantitativeResultList().add(getQuantitativeResult(dataQualityQuantitativeResultNodes.item(l)));
                    }

                    dataQualityList.add(dataQuality);
                }
            }
        }
        dataQualityObject.setDataQualities(dataQualityList);
        return dataQualityObject;
    }

    private void getDataQualityNodeInformation(Node parentNode, CSWIsoRecord.DataQuality dataQualityObjectNode)  throws XPathExpressionException {
        Node nameOfMeasureNode = (Node) XPATH_NAME_OF_MEASURE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setNameOfMeasure(localize(nameOfMeasureNode));

        Node measureIdentificationCodeNode = (Node) XPATH_MEASURE_IDENTIFICATION_CODE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setMeasureIdentificationCode(localize(measureIdentificationCodeNode));

        Node measureIdentificationAuthorizationNode = (Node) XPATH_MEASURE_IDENTIFICATION_AUTHORIZATION.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setMeasureIdentificationAuthorization(getText(measureIdentificationAuthorizationNode));

        Node measureDescriptionNode = (Node) XPATH_MEASURE_DESCRIPTION.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setMeasureDescription(localize(measureDescriptionNode));

        Node evaluationMethodTypeNode = (Node) XPATH_EVALUATION_METHOD_TYPE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setEvaluationMethodType(getText(evaluationMethodTypeNode));

        Node evaluationMethodDescriptionNode = (Node) XPATH_EVALUATION_METHOD_DESCRIPTION.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setEvaluationMethodDescription(localize(evaluationMethodDescriptionNode));

        Node evaluationProcedureNode = (Node) XPATH_EVALUATION_PROCEDURE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectNode.setEvaluationProcedure(null); //TODO parse //CI_Citation

        NodeList dateTimeNode = (NodeList) XPATH_DATE_TIME.evaluate(parentNode, XPathConstants.NODESET);
        List<String> dateTimeValueList = new ArrayList<>();
        for (int i = 0;i < dateTimeNode.getLength(); ++i) {
            dateTimeValueList.add(getText(dateTimeNode.item(i)));
        }
        dataQualityObjectNode.setDateTime(dateTimeValueList);
    }

    private CSWIsoRecord.DataQualityConformanceResult getConformanceResult(Node parentNode)  throws XPathExpressionException {
        CSWIsoRecord.DataQualityConformanceResult dataQualityObjectConformanceResult =
                new CSWIsoRecord.DataQualityConformanceResult();

        Node conformanceResultSpecificationNode = (Node) XPATH_CONFORMANCE_RESULT_SPECIFICATION_TITLE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectConformanceResult.setSpecification(localize(conformanceResultSpecificationNode)); //TODO parse

        Node conformanceResultExplanationNode = (Node) XPATH_CONFORMANCE_RESULT_EXPLANATION.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectConformanceResult.setExplanation(localize(conformanceResultExplanationNode));

        Node conformanceResultPassNode = (Node) XPATH_CONFORMANCE_RESULT_PASS.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectConformanceResult.setPass(getBoolean(conformanceResultPassNode));

        return dataQualityObjectConformanceResult;
    }

    private CSWIsoRecord.DataQualityQuantitativeResult getQuantitativeResult(Node parentNode) throws XPathExpressionException {

        CSWIsoRecord.DataQualityQuantitativeResult dataQualityObjectQuantitativeResult =
                new CSWIsoRecord.DataQualityQuantitativeResult();

        Node quantitativeResultValueTypeNode = (Node) XPATH_QUANTITATIVE_RESULT_VALUE_TYPE.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectQuantitativeResult.setValueType(getText(quantitativeResultValueTypeNode));

        Node quantitativeResultValueUnitNode = (Node) XPATH_QUANTITATIVE_RESULT_VALUE_UNIT.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectQuantitativeResult.setValueUnit(getText(quantitativeResultValueUnitNode));

        NodeList quantitativeValueNodeList = (NodeList) XPATH_QUANTITATIVE_RESULT_VALUE.evaluate(parentNode, XPathConstants.NODESET);
        List<String> valueList = new ArrayList<>();
        for (int i = 0;i < quantitativeValueNodeList.getLength(); ++i) {
            valueList.add(getText(quantitativeValueNodeList.item(i)));
        }
        dataQualityObjectQuantitativeResult.setValue(valueList);

        Node quantitativeResultErrorStatisticNode = (Node) XPATH_QUANTITATIVE_RESULT_ERROR_STATISTIC.evaluate(parentNode, XPathConstants.NODE);
        dataQualityObjectQuantitativeResult.setErrorStatistic(localize(quantitativeResultErrorStatisticNode));

        return dataQualityObjectQuantitativeResult;
    }

    //Move to common utility class
    //Only for gmd:PT_FreeText
    private String localize(final Node elem) {
        String ret = null;
        String localized;
        if (elem == null) {
            return null;
        }
        try {
            Node contentNode = (Node) XPATH_CHARACTER_STRING.evaluate(elem, XPathConstants.NODE);
            ret = getText(contentNode);
            if (pathToLocalizedValue != null){
                final Node localeNode = (Node) pathToLocalizedValue.evaluate(contentNode, XPathConstants.NODE);
                localized  = getText(localeNode);
                if (localized != null && !localized.isEmpty()) {
                    ret = localized;
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing localized value for:", elem.getLocalName(), ". Message:", e.getMessage());
        }
        return ret;
    }

    //Move to common utility class
    //also: CSWISORecordParser getText
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
    //Move to common utility class
    private boolean getBoolean (final Node element) {
        if (element == null) {
            return false;
        }
        String content = element.getTextContent().trim();
        if ("1".equals(content)){
            return true;
        }
        return Boolean.valueOf(content);
    }
}