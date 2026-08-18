package fi.nls.oskari.csw.helper;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import static fi.nls.oskari.csw.helper.CSWISORecordParser.parseLocalizedContent;

import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQuality;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityConformanceResult;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityObject;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityQuantitativeResult;

public class CSWISORecordDataQualityParser {

    // The gmd:report elements we recognize. The DQ_-prefix is dropped and the first letter
    // lowercased for the "nodeName" we report, for example DQ_DomainConsistency => domainConsistency
    private static final Set<String> REPORT_ELEMENT_NAMES = Set.of(
        "DQ_AbsoluteExternalPositionalAccuracy",
        "DQ_CompletenessCommission",
        "DQ_CompletenessOmission",
        "DQ_ConceptualConsistency",
        "DQ_DomainConsistency",
        "DQ_FormatConsistency",
        "DQ_TopologicalConsistency",
        "DQ_GriddedDataPositionalAccuracy",
        "DQ_AccuracyOfATimeMeasurement",
        "DQ_TemporalConsistency",
        "DQ_TemporalValidity",
        "DQ_ThematicClassificationCorrectness",
        "DQ_NonQuantitativeAttributeAccuracy",
        "DQ_QuantitativeAttributeAccuracy");

    public static DataQualityObject parseDataQualities(Element mdMetadata, String localeId) {
        List<Element> dataQualities = XmlHelper.getChildElements(mdMetadata, "dataQualityInfo", "DQ_DataQuality").toList();

        DataQualityObject dataQualityObject = new DataQualityObject();
        dataQualityObject.setLineageStatements(dataQualities.stream()
            .flatMap(dq -> XmlHelper.getChildElements(dq, "lineage", "LI_Lineage", "statement"))
            .map(statement -> parseLocalizedContent(statement, localeId))
            .filter(Objects::nonNull)
            .toList());
        dataQualityObject.setDataQualities(dataQualities.stream()
            .flatMap(dq -> XmlHelper.getChildElements(dq, "report"))
            .flatMap(report -> XmlHelper.getChildElements(report, null))
            .filter(report -> REPORT_ELEMENT_NAMES.contains(XmlHelper.getLocalName(report)))
            .map(report -> parseDataQuality(report, localeId))
            .toList());
        return dataQualityObject;
    }

    private static DataQuality parseDataQuality(Element report, String localeId) {
        /**
        <gmd:DQ_DomainConsistency>
            <gmd:nameOfMeasure>
                <gco:CharacterString>ELF_ADM06</gco:CharacterString>
            </gmd:nameOfMeasure>
            <gmd:measureIdentification>
                <gmd:RS_Identifier>
                    <gmd:code>
                        <gco:CharacterString>ADM06</gco:CharacterString>
                    </gmd:code>
                </gmd:RS_Identifier>
            </gmd:measureIdentification>
            <gmd:measureDescription>
                <gco:CharacterString>Description of the measure</gco:CharacterString>
            </gmd:measureDescription>
            <gmd:evaluationMethodType>
                <gmd:DQ_EvaluationMethodTypeCode codeListValue="directInternal" />
            </gmd:evaluationMethodType>
            <gmd:dateTime>
                <gco:DateTime>2015-01-01T00:00:00</gco:DateTime>
            </gmd:dateTime>
            <gmd:result>
                <gmd:DQ_ConformanceResult />
            </gmd:result>
        </gmd:DQ_DomainConsistency>
         */
        DataQuality dataQuality = new DataQuality();
        // DQ_DomainConsistency => domainConsistency
        dataQuality.setNodeName(toNodeName(XmlHelper.getLocalName(report)));
        dataQuality.setNameOfMeasure(
            XmlHelper.getAnyChild(report, "nameOfMeasure")
                .map(x -> parseLocalizedContent(x, localeId))
                .orElse(null));
        dataQuality.setMeasureIdentificationCode(
            XmlHelper.getChildElements(report, "measureIdentification")
                .flatMap(x -> XmlHelper.getChildElements(x, null))
                .flatMap(x -> XmlHelper.getChildElements(x, "code"))
                .map(x -> parseLocalizedContent(x, localeId))
                .findAny()
                .orElse(null));
        dataQuality.setMeasureIdentificationAuthorization(
            XmlHelper.getChildElements(report, "measureIdentification")
                .flatMap(x -> XmlHelper.getChildElements(x, null))
                .flatMap(x -> XmlHelper.getChildElements(x, "authorization"))
                .map(x -> getText(x))
                .findAny()
                .orElse(null));
        dataQuality.setMeasureDescription(
            XmlHelper.getAnyChild(report, "measureDescription")
                .map(x -> parseLocalizedContent(x, localeId))
                .orElse(null));
        dataQuality.setEvaluationMethodType(
            XmlHelper.getAnyChild(report, "evaluationMethodType")
                .map(x -> getText(x))
                .orElse(null));
        dataQuality.setEvaluationMethodDescription(
            XmlHelper.getAnyChild(report, "evaluationMethodDescription")
                .map(x -> parseLocalizedContent(x, localeId))
                .orElse(null));
        // TODO parse the CI_Citation under gmd:evaluationProcedure
        dataQuality.setEvaluationProcedure(null);
        dataQuality.setDateTime(
            XmlHelper.getChildElements(report, "dateTime")
                .map(x -> getText(x))
                .filter(Objects::nonNull)
                .toList());
        dataQuality.setConformanceResultList(
            XmlHelper.getChildElements(report, "result", "DQ_ConformanceResult")
                .map(result -> parseConformanceResult(result, localeId))
                .toList());
        dataQuality.setQuantitativeResultList(
            XmlHelper.getChildElements(report, "result", "DQ_QuantitativeResult")
                .map(result -> parseQuantitativeResult(result, localeId))
                .toList());
        return dataQuality;
    }

    // DQ_DomainConsistency => domainConsistency
    private static String toNodeName(String localName) {
        String withoutPrefix = localName.substring("DQ_".length());
        return Character.toLowerCase(withoutPrefix.charAt(0)) + withoutPrefix.substring(1);
    }

    private static DataQualityConformanceResult parseConformanceResult(Element result, String localeId) {
        /**
        <gmd:DQ_ConformanceResult>
            <gmd:specification>
                <gmd:CI_Citation>
                    <gmd:title>
                        <gco:CharacterString>ELF Master LoD1</gco:CharacterString>
                    </gmd:title>
                </gmd:CI_Citation>
            </gmd:specification>
            <gmd:explanation>
                <gco:CharacterString>See the referenced specification</gco:CharacterString>
            </gmd:explanation>
            <gmd:pass>
                <gco:Boolean>false</gco:Boolean>
            </gmd:pass>
        </gmd:DQ_ConformanceResult>
         */
        DataQualityConformanceResult conformanceResult = new DataQualityConformanceResult();
        // TODO parse the whole CI_Citation instead of just the title
        conformanceResult.setSpecification(
            XmlHelper.getChildElements(result, "specification", "CI_Citation", "title")
                .map(x -> parseLocalizedContent(x, localeId))
                .findAny()
                .orElse(null));
        conformanceResult.setExplanation(
            XmlHelper.getAnyChild(result, "explanation")
                .map(x -> parseLocalizedContent(x, localeId))
                .orElse(null));
        conformanceResult.setPass(
            XmlHelper.getAnyChild(result, "pass")
                .map(x -> getBoolean(x))
                .orElse(false));
        return conformanceResult;
    }

    private static DataQualityQuantitativeResult parseQuantitativeResult(Element result, String localeId) {
        /**
        <gmd:DQ_QuantitativeResult>
            <gmd:valueType>
                <gco:RecordType>Number of errors</gco:RecordType>
            </gmd:valueType>
            <gmd:valueUnit />
            <gmd:value>
                <gco:Record>54</gco:Record>
            </gmd:value>
        </gmd:DQ_QuantitativeResult>
         */
        DataQualityQuantitativeResult quantitativeResult = new DataQualityQuantitativeResult();
        quantitativeResult.setValueType(
            XmlHelper.getAnyChild(result, "valueType")
                .map(x -> getText(x))
                .orElse(null));
        quantitativeResult.setValueUnit(
            XmlHelper.getAnyChild(result, "valueUnit")
                .map(x -> getText(x))
                .orElse(null));
        quantitativeResult.setValue(
            XmlHelper.getChildElements(result, "value")
                .map(x -> getText(x))
                .filter(Objects::nonNull)
                .toList());
        quantitativeResult.setErrorStatistic(
            XmlHelper.getAnyChild(result, "errorStatistic")
                .map(x -> parseLocalizedContent(x, localeId))
                .orElse(null));
        return quantitativeResult;
    }

    private static String getText(Element element) {
        String text = element.getTextContent();
        return text != null ? text.trim() : null;
    }

    private static boolean getBoolean(Element element) {
        String content = getText(element);
        if (content == null) {
            return false;
        }
        return "1".equals(content) || Boolean.parseBoolean(content);
    }
}