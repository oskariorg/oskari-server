package fi.nls.oskari.csw.helper;

import com.vividsolutions.jts.geom.*;
import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Helper class for parsing search results for MetadataCatalogue:
 * Created by TMIKKOLAINEN on 2.9.2014.
 */
public class CSWISORecordParser {

    private static final Logger log = LogFactory.getLogger(CSWISORecordParser.class);

    // we need to map languages from 3-letter codes to 2-letter codes so initialize a global codeMapping property
    private static final Map<String, String> ISO3letterOskariLangMapping = new HashMap<String, String>();
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'kk:mm:ss"); // or ISO_DATE_TIME
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static {
        for (final String language : Locale.getISOLanguages()) {
            final Locale locale = new Locale(language);
            ISO3letterOskariLangMapping.put(locale.getISO3Language(), locale.getLanguage());
        }
    }

    GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
    private XPath xpath = XPathFactory.newInstance().newXPath();

    private XPathExpression XPATH_DATA_QUALITY = null;
    private XPathExpression XPATH_DISTRIBUTION_INFO = null;
    private XPathExpression XPATH_DISTRIBUTION_INFO_DISTRIBUTION_FORMATS = null;
    private XPathExpression XPATH_DISTRIBUTION_INFO_DISTRIBUTION_FORMAT_NAME = null;
    private XPathExpression XPATH_DISTRIBUTION_INFO_DISTRIBUTION_FORMAT_VERSION = null;
    private XPathExpression XPATH_DISTRIBUTION_INFO_ONLINE_RESOURCES = null;
    private XPathExpression XPATH_DISTRIBUTION_INFO_ONLINE_RESOURCES_LINK_NAME = null;
    private XPathExpression XPATH_DISTRIBUTION_INFO_ONLINE_RESOURCES_LINK_URL = null;
    private XPathExpression XPATH_DI_SI = null;
    private XPathExpression XPATH_DI_SI_ABSTRACT = null;
    private XPathExpression XPATH_DI_SI_TEMPORAL_EXTENTS = null;
    private XPathExpression XPATH_DI_SI_TEMPORAL_EXTENTS_PERIOD_BEGIN = null;
    private XPathExpression XPATH_DI_SI_TEMPORAL_EXTENTS_PERIOD_END = null;
    private XPathExpression XPATH_DI_SI_CITATION = null;
    private XPathExpression XPATH_DI_SI_CITATION_TITLE = null;
    private XPathExpression XPATH_DI_SI_CITATION_DATE_TYPE = null;
    private XPathExpression XPATH_DI_SI_CITATION_DATE_VALUE = null;
    private XPathExpression XPATH_DI_SI_CITATION_RESOURCE_IDENTIFIERS = null;
    private XPathExpression XPATH_DI_SI_CITATION_RESOURCE_IDENTIFIERS_CODE = null;
    private XPathExpression XPATH_DI_SI_CITATION_RESOURCE_IDENTIFIERS_CODESPACE = null;
    private XPathExpression XPATH_DI_SI_KEYWORDS = null;
    private XPathExpression XPATH_DI_SI_BROWSE_GRAPHICS = null;
    private XPathExpression XPATH_DI_SI_BROWSE_GRAPHICS_FILE_NAME = null;
    private XPathExpression XPATH_DI_SI_BROWSE_GRAPHICS_FILE_DESCRIPTION = null;
    private XPathExpression XPATH_DI_SI_BROWSE_GRAPHICS_FILE_TYPE = null;
    private XPathExpression XPATH_DI_SI_RESPONSIBLE_PARTIES = null;
    private XPathExpression XPATH_RESPONSIBLE_PARTY_ORG_NAME = null;
    private XPathExpression XPATH_RESPONSIBLE_PARTY_ORG_EMAILS = null;
    private XPathExpression XPATH_DI_SI_RESOURCE_CONSTRAINTS = null;
    private XPathExpression XPATH_DI_SI_RESOURCE_CONSTRAINTS_ACCESS_CONSTRAINTS = null;
    private XPathExpression XPATH_DI_SI_RESOURCE_CONSTRAINTS_OTHER_CONSTRAINTS = null;
    private XPathExpression XPATH_DI_SI_RESOURCE_CONSTRAINTS_CLASSIFICATIONS = null;
    private XPathExpression XPATH_DI_SI_RESOURCE_CONSTRAINTS_USE_LIMITATIONS = null;
    private XPathExpression XPATH_DI_CHARSETS = null;
    private XPathExpression XPATH_DI_TOPICS = null;
    private XPathExpression XPATH_DI_RESOLUTIONS = null;
    private XPathExpression XPATH_DI_SPATIAL_REPR_TYPES = null;
    private XPathExpression XPATH_SI_SERVICE_TYPE = null;
    private XPathExpression XPATH_SI_SERVICE_TYPE_VERSION = null;
    private XPathExpression XPATH_SI_OPERATES_ON = null;
    private XPathExpression XPATH_DI_SI_EXTENT = null;
    private XPathExpression XPATH_FILE_IDENTIFIER = null;
    private XPathExpression XPATH_SCOPE_CODES = null;
    private XPathExpression XPATH_LOCALE_MAP = null;
    private XPathExpression XPATH_METADATA_STANDARD_NAME = null;
    private XPathExpression XPATH_METADATA_STANDARD_VERSION = null;
    private XPathExpression XPATH_LANGUAGE = null;
    private XPathExpression XPATH_METADATA_CHARSET = null;
    private XPathExpression XPATH_METADATA_RESPONSIBLE_PARTIES = null;
    private XPathExpression XPATH_METADATA_DATE = null;
    private XPathExpression XPATH_METADATA_REFERENCESYSTEM = null;

    public CSWISORecordParser() throws XPathExpressionException {
        xpath.setNamespaceContext(new CSWISORecordNamespaceContext());
        // Some parents that have multiple evaluated children
        //(0..*)
        XPATH_DATA_QUALITY = xpath.compile(
                "./gmd:dataQualityInfo/gmd:DQ_DataQuality");

        //(0..1)
        XPATH_DISTRIBUTION_INFO = xpath.compile(
                "./gmd:distributionInfo/gmd:MD_Distribution");

        //(0..*)
        XPATH_DISTRIBUTION_INFO_DISTRIBUTION_FORMATS = xpath.compile(
                "./gmd:distributionFormat/gmd:MD_Format");

        XPATH_DISTRIBUTION_INFO_DISTRIBUTION_FORMAT_NAME = xpath.compile(
                "./gmd:name/gco:CharacterString"
        );

        XPATH_DISTRIBUTION_INFO_DISTRIBUTION_FORMAT_VERSION = xpath.compile(
                "./gmd:version/gco:CharacterString"
        );

        //(0..*)
        XPATH_DISTRIBUTION_INFO_ONLINE_RESOURCES = xpath.compile(
                "./gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[not(gmd:protocol/gco:CharacterString='WWW:DOWNLOAD-1.0-ftp--download')]");

        //(1)
        XPATH_DISTRIBUTION_INFO_ONLINE_RESOURCES_LINK_NAME = xpath.compile(
                "./gmd:name/gco:CharacterString");
        //(0..1)
        XPATH_DISTRIBUTION_INFO_ONLINE_RESOURCES_LINK_URL = xpath.compile(
                "./gmd:linkage/gmd:URL");


        //(0..*)
        XPATH_DI_SI = xpath.compile(
                "./gmd:identificationInfo/gmd:MD_DataIdentification | ./gmd:identificationInfo/srv:SV_ServiceIdentification");
        //(1..1)
        XPATH_DI_SI_ABSTRACT = xpath.compile(
                "./gmd:abstract/gco:CharacterString");
        //(0..*)
        try {
            XPATH_DI_SI_TEMPORAL_EXTENTS = xpath.compile(
                    "./gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod");
        } catch (Exception e) {
            log.error(e, "Error compiling xpath XPATH_DI_SI_TEMPORAL_EXTENTS");
        }
        //(0..1) (0..1 as these could be gml:begin and gml:end)
        XPATH_DI_SI_TEMPORAL_EXTENTS_PERIOD_BEGIN = xpath.compile(
                "./gml:beginPosition");
        XPATH_DI_SI_TEMPORAL_EXTENTS_PERIOD_END = xpath.compile(
                "./gml:endPosition");

        XPATH_DI_SI_CITATION = xpath.compile(
                "./gmd:citation/gmd:CI_Citation");
        //(1..1)
        XPATH_DI_SI_CITATION_TITLE = xpath.compile(
                "./gmd:title/gco:CharacterString");
        XPATH_DI_SI_CITATION_DATE_TYPE = xpath.compile(
                "./gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue");
        XPATH_DI_SI_CITATION_DATE_VALUE = xpath.compile(
                "./gmd:date/gmd:CI_Date/gmd:date/gco:Date");
        //(0..*)
        XPATH_DI_SI_CITATION_RESOURCE_IDENTIFIERS = xpath.compile(
                "./gmd:identifier/gmd:RS_Identifier");
        XPATH_DI_SI_CITATION_RESOURCE_IDENTIFIERS_CODE = xpath.compile(
                "./gmd:code/gco:CharacterString");
        XPATH_DI_SI_CITATION_RESOURCE_IDENTIFIERS_CODESPACE = xpath.compile(
                "./gmd:codeSpace/gco:CharacterString");

        XPATH_DI_SI_KEYWORDS = xpath.compile(
                "./gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");

        XPATH_DI_SI_BROWSE_GRAPHICS = xpath.compile(
                "./gmd:graphicOverview/gmd:MD_BrowseGraphic");
        //(1..1)
        XPATH_DI_SI_BROWSE_GRAPHICS_FILE_NAME = xpath.compile(
                "./gmd:fileName/gco:CharacterString");
        //(0..1)
        XPATH_DI_SI_BROWSE_GRAPHICS_FILE_DESCRIPTION = xpath.compile(
                "./gmd:fileDescription/gco:CharacterString");
        //(0..1)
        XPATH_DI_SI_BROWSE_GRAPHICS_FILE_TYPE = xpath.compile(
                "./gmd:fileType/gco:CharacterString");

        XPATH_DI_SI_RESPONSIBLE_PARTIES = xpath.compile(
                "./gmd:pointOfContact/gmd:CI_ResponsibleParty");

        XPATH_RESPONSIBLE_PARTY_ORG_NAME = xpath.compile(
                "./gmd:organisationName/gco:CharacterString");

        XPATH_RESPONSIBLE_PARTY_ORG_EMAILS = xpath.compile(
                "./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString");


        XPATH_DI_SI_RESOURCE_CONSTRAINTS = xpath.compile(
                "./gmd:resourceConstraints");

        XPATH_DI_SI_RESOURCE_CONSTRAINTS_ACCESS_CONSTRAINTS = xpath.compile(
                "./gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue");

        XPATH_DI_SI_RESOURCE_CONSTRAINTS_OTHER_CONSTRAINTS = xpath.compile(
                "./gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString");

        XPATH_DI_SI_RESOURCE_CONSTRAINTS_CLASSIFICATIONS = xpath.compile(
                "./gmd:MD_SecurityConstraints/gmd:classification/gmd:MD_ClassificationCode/@codeListValue");

        XPATH_DI_SI_RESOURCE_CONSTRAINTS_USE_LIMITATIONS = xpath.compile(
                "./gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString");

        // DI ONLY
        XPATH_DI_CHARSETS = xpath.compile(
                "./gmd:characterSet/@codeListValue");
        XPATH_DI_TOPICS = xpath.compile(
                "./gmd:topicCategory/gmd:MD_TopicCategoryCode");
        XPATH_DI_RESOLUTIONS = xpath.compile(
                "./gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer");
        XPATH_DI_SPATIAL_REPR_TYPES = xpath.compile(
                "./gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue");

        // SI ONLY
        XPATH_SI_SERVICE_TYPE = xpath.compile(
                "./srv:serviceType/gco:LocalName");

        // SI ONLY
        XPATH_SI_SERVICE_TYPE_VERSION = xpath.compile(
                "./srv:serviceTypeVersion/gco:LocalName");

        XPATH_SI_OPERATES_ON = xpath.compile(
                "./srv:operatesOn/@uuidref");

        //(0..*)
        XPATH_DI_SI_EXTENT = xpath.compile(
                "./*[local-name()='extent']/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox");


        // From root
        XPATH_FILE_IDENTIFIER = xpath.compile(
                "./gmd:fileIdentifier/gco:CharacterString");

        // From root
        XPATH_SCOPE_CODES = xpath.compile(
                "./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue");
        // From root
        XPATH_LOCALE_MAP = xpath.compile(
                "./gmd:locale/gmd:PT_Locale");
        // From root
        XPATH_METADATA_STANDARD_NAME = xpath.compile(
                "./gmd:metadataStandardName/gco:CharacterString");
        // From root
        XPATH_METADATA_STANDARD_VERSION = xpath.compile(
                "./gmd:metadataStandardVersion/gco:CharacterString");
        // WAS METADATA_LANGUAGE (which is from root, but this is also used elsewhere from another parent)
        XPATH_LANGUAGE = xpath.compile(
                "./gmd:language/gco:CharacterString");
        // From root
        XPATH_METADATA_CHARSET = xpath.compile(
                "./gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue");
        // From root
        XPATH_METADATA_RESPONSIBLE_PARTIES = xpath.compile(
                "./gmd:contact/gmd:CI_ResponsibleParty");
        // From root
        XPATH_METADATA_DATE = xpath.compile(
                "./gmd:dateStamp/gco:DateTime");
        // From root
        XPATH_METADATA_REFERENCESYSTEM = xpath.compile(
                "./gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString");


    }

    public CSWIsoRecord parse(final Node elem, final Locale locale, MathTransform transform) throws XPathExpressionException, ParseException, TransformException {
        int i;
        Node node;
        NodeList nodeList;
        CSWIsoRecord record = new CSWIsoRecord();
        final Map<String, String> locales = getLocaleMap(elem);
        String value;
        XPathExpression pathToLocalizedValue = null;
        if (locales != null && locales.containsKey(locale.getISO3Language())) {
            pathToLocalizedValue = xpath.compile(
                    "../gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#" + locales.get(locale.getISO3Language()) + "']");
        }

        nodeList = (NodeList) XPATH_DI_SI.evaluate(elem, XPathConstants.NODESET);
        if (nodeList.getLength() > 0) {
            parseIdentifications(nodeList, record.getIdentifications(), transform, pathToLocalizedValue);
        }

        nodeList = (NodeList) XPATH_DATA_QUALITY.evaluate(elem, XPathConstants.NODESET);
        if (nodeList.getLength() > 0) {
            try {
                CSWISORecordDataQualityParser dataQualityParser = new CSWISORecordDataQualityParser();
                record.setDataQualityObject(dataQualityParser.parseDataQualities(nodeList, pathToLocalizedValue));
            }
            catch (Exception e) {
                log.warn("parseDataQualities FAIL! "+e.getMessage());
            }
        }

        node = (Node) XPATH_DISTRIBUTION_INFO.evaluate(elem, XPathConstants.NODE);
        if (node != null) {
            parseDistributionInfo(node, record, pathToLocalizedValue);
        }

        node = (Node) XPATH_FILE_IDENTIFIER.evaluate(elem, XPathConstants.NODE);
        if (node != null) {
            record.setFileIdentifier(getLocalizedContent(node, pathToLocalizedValue));
        }

        nodeList = (NodeList) XPATH_SCOPE_CODES.evaluate(elem, XPathConstants.NODESET);
        List<String> list = record.getScopeCodes();
        for (i = 0; i < nodeList.getLength(); i++) {
            list.add(getText(nodeList.item(i)));
        }

        node = (Node) XPATH_METADATA_STANDARD_NAME.evaluate(elem, XPathConstants.NODE);
        if (node != null) {
            record.setMetadataStandardName(getLocalizedContent(node, pathToLocalizedValue));
        }

        node = (Node) XPATH_METADATA_STANDARD_VERSION.evaluate(elem, XPathConstants.NODE);
        if (node != null) {
            record.setMetadataStandardVersion(getLocalizedContent(node, pathToLocalizedValue));
        }

        node = (Node) XPATH_LANGUAGE.evaluate(elem, XPathConstants.NODE);
        if (node != null) {
            record.setMetadataLanguage(getLanguageIfAvailable(getLocalizedContent(node, pathToLocalizedValue)));
        }

        node = (Node) XPATH_METADATA_CHARSET.evaluate(elem, XPathConstants.NODE);
        if (node != null) {
            record.setMetadataCharacterSet(getLocalizedContent(node, pathToLocalizedValue));
        }

        nodeList = (NodeList) XPATH_METADATA_RESPONSIBLE_PARTIES.evaluate(elem, XPathConstants.NODESET);
        List<CSWIsoRecord.ResponsibleParty> rpList = record.getMetadataResponsibleParties();
        parseResponsibleParties(nodeList, rpList, pathToLocalizedValue);

        node = (Node) XPATH_METADATA_DATE.evaluate(elem, XPathConstants.NODE);
        if (node != null) {
            value = getLocalizedContent(node, pathToLocalizedValue);
            try{
                record.setMetadataDateStamp(LocalDateTime.parse(value, DATE_TIME_FORMAT));
            }catch (Exception e){
                // TODO: should we add raw xml content if parsing fails
            }
        }

        nodeList = (NodeList) XPATH_METADATA_REFERENCESYSTEM.evaluate(elem, XPathConstants.NODESET);
        List<String> referenceSystemList = record.getReferenceSystems();
        for (i = 0; i < nodeList.getLength(); i++) {
            referenceSystemList.add(getText(nodeList.item(i)));
        }

        return record;
    }

    private void parseIdentifications(NodeList nodeList, List<CSWIsoRecord.Identification> identifications, MathTransform transform, XPathExpression pathToLocalizedValue) throws XPathExpressionException, ParseException, TransformException {
        CSWIsoRecord.Identification identification;
        Node node;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            // See if type is data (gmd:MD_DataIdentification) or service (srv:SV_ServiceIdentification)
            if ("MD_DataIdentification".equals(node.getLocalName())) {
                identification = new CSWIsoRecord.DataIdentification();
                // Parse data specific stuff
                parseDataIdentificationFields(node, (CSWIsoRecord.DataIdentification) identification, pathToLocalizedValue);
            } else {
                identification = new CSWIsoRecord.ServiceIdentification();
                // Parse service specific stuff
                parseServiceIdentificationFields(node, (CSWIsoRecord.ServiceIdentification) identification, pathToLocalizedValue);
            }
            parseCommonIdentificationFields(node, identification, transform, pathToLocalizedValue);

            identifications.add(identification);
        }
    }

    private void parseCommonIdentificationFields(Node idNode, CSWIsoRecord.Identification identification, MathTransform transform, XPathExpression pathToLocalizedValue) throws XPathExpressionException, ParseException, TransformException {
        Node node;
        NodeList nodeList;
        List<String> list;
        node = (Node) XPATH_DI_SI_ABSTRACT.evaluate(idNode, XPathConstants.NODE);
        identification.setAbstractText(getLocalizedContent(node, pathToLocalizedValue));
        nodeList = (NodeList) XPATH_DI_SI_TEMPORAL_EXTENTS.evaluate(idNode, XPathConstants.NODESET);
        if (nodeList.getLength() > 0) {
            parseTemporalExtents(nodeList, identification.getTemporalExtents());
        }
        node = (Node) XPATH_DI_SI_CITATION.evaluate(idNode, XPathConstants.NODE);
        if (node != null) {
            CSWIsoRecord.Identification.Citation citation = new CSWIsoRecord.Identification.Citation();
            identification.setCitation(citation);
            parseCitation(node, citation, pathToLocalizedValue);
        }
        list = identification.getDescriptiveKeywords();
        nodeList = (NodeList) XPATH_DI_SI_KEYWORDS.evaluate(idNode, XPathConstants.NODESET);
        parseNodeListStrings(nodeList, list, pathToLocalizedValue);
        nodeList = (NodeList) XPATH_DI_SI_BROWSE_GRAPHICS.evaluate(idNode, XPathConstants.NODESET);
        if (nodeList.getLength() > 0) {
            parseBrowseGraphics(nodeList, identification.getBrowseGraphics(), pathToLocalizedValue);
        }
        nodeList = (NodeList) XPATH_DI_SI_RESPONSIBLE_PARTIES.evaluate(idNode, XPathConstants.NODESET);
        if (nodeList.getLength() > 0) {
            parseResponsibleParties(nodeList, identification.getResponsibleParties(), pathToLocalizedValue);
        }
        nodeList = (NodeList) XPATH_DI_SI_RESOURCE_CONSTRAINTS.evaluate(idNode, XPathConstants.NODESET);
        if (nodeList.getLength() > 0) {
            parseResourceConstraints(nodeList, identification, pathToLocalizedValue);
        }
        // TODO double triple check that extents are common
        nodeList = (NodeList) XPATH_DI_SI_EXTENT.evaluate(idNode, XPathConstants.NODESET);
        if (nodeList.getLength() > 0) {
            parseBBoxes(identification, nodeList, transform);
        }
    }

    private void parseDataIdentificationFields(Node diNode, CSWIsoRecord.DataIdentification identification, XPathExpression pathToLocalizedValue) throws XPathExpressionException {
        int i;
        List<Integer> intList;
        List<String> list;
        Node node;
        NodeList nodeList;

        nodeList = (NodeList) XPATH_DI_CHARSETS.evaluate(diNode, XPathConstants.NODESET);
        list = identification.getCharacterSets();
        parseNodeListStrings(nodeList, list, pathToLocalizedValue);

        nodeList = (NodeList) XPATH_LANGUAGE.evaluate(diNode, XPathConstants.NODESET);
        list = identification.getLanguages();
        for (i = 0; i < nodeList.getLength(); i++) {
            list.add(getLanguageIfAvailable(getLocalizedContent(nodeList.item(i), pathToLocalizedValue)));
        }

        nodeList = (NodeList) XPATH_DI_TOPICS.evaluate(diNode, XPathConstants.NODESET);
        list = identification.getTopicCategories();
        parseNodeListStrings(nodeList, list, pathToLocalizedValue);

        nodeList = (NodeList) XPATH_DI_RESOLUTIONS.evaluate(diNode, XPathConstants.NODESET);
        intList = identification.getSpatialResolutions();
        for (i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            try {
                intList.add(Integer.parseInt(getText(node)));
            } catch (NumberFormatException nfe) {
                log.warn("Invalid resolution integer:", getText(node));
            }
        }

        nodeList = (NodeList) XPATH_DI_SPATIAL_REPR_TYPES.evaluate(diNode, XPathConstants.NODESET);
        list = identification.getSpatialRepresentationTypes();
        parseNodeListStrings(nodeList, list, pathToLocalizedValue);
    }

    private void parseServiceIdentificationFields(Node siNode, CSWIsoRecord.ServiceIdentification identification, XPathExpression pathToLocalizedValue) throws XPathExpressionException {
        List<String> list;
        Node node;
        NodeList nodeList;

        nodeList = (NodeList) XPATH_SI_OPERATES_ON.evaluate(siNode, XPathConstants.NODESET);
        list = identification.getOperatesOn();
        parseNodeListStrings(nodeList, list, pathToLocalizedValue);

        node = (Node) XPATH_SI_SERVICE_TYPE.evaluate(siNode, XPathConstants.NODE);
        if (node != null) {
            identification.setServiceType(getLocalizedContent(node, pathToLocalizedValue));
        }

        node = (Node) XPATH_SI_SERVICE_TYPE_VERSION.evaluate(siNode, XPathConstants.NODE);
        if (node != null) {
            identification.setServiceTypeVersion(getLocalizedContent(node, pathToLocalizedValue));
        }
    }

    private void parseResponsibleParties(NodeList rpNodes, List<CSWIsoRecord.ResponsibleParty> rpList, XPathExpression pathToLocalizedValue) throws XPathExpressionException {
        int i;
        Node node;
        NodeList nodeList;
        List<String> list;
        CSWIsoRecord.ResponsibleParty responsibleParty;
        for (i = 0; i < rpNodes.getLength(); i++) {
            node = rpNodes.item(i);
            responsibleParty = new CSWIsoRecord.ResponsibleParty();
            responsibleParty.setOrganisationName(
                    getLocalizedContent(((Node) XPATH_RESPONSIBLE_PARTY_ORG_NAME.evaluate(node, XPathConstants.NODE)), pathToLocalizedValue)
            );
            nodeList = (NodeList) XPATH_RESPONSIBLE_PARTY_ORG_EMAILS.evaluate(node, XPathConstants.NODESET);
            list = responsibleParty.getElectronicMailAddresses();
            parseNodeListStrings(nodeList, list, pathToLocalizedValue);
            rpList.add(responsibleParty);
        }
    }

    private void parseBrowseGraphics(NodeList bgNodes, List<CSWIsoRecord.BrowseGraphic> browseGraphics, XPathExpression pathToLocalizedValue) throws XPathExpressionException {
        Node node, node2;
        CSWIsoRecord.BrowseGraphic browseGraphic;
        for (int i = 0; i < bgNodes.getLength(); i++) {
            node = bgNodes.item(i);
            browseGraphic = new CSWIsoRecord.BrowseGraphic();
            node2 = (Node) XPATH_DI_SI_BROWSE_GRAPHICS_FILE_NAME.evaluate(node, XPathConstants.NODE);
            if (node2 != null) {
                browseGraphic.setFileName(getLocalizedContent(node2, pathToLocalizedValue));
            }
            node2 = (Node) XPATH_DI_SI_BROWSE_GRAPHICS_FILE_DESCRIPTION.evaluate(node, XPathConstants.NODE);
            if (node2 != null) {
                browseGraphic.setFileDescription(getLocalizedContent(node2, pathToLocalizedValue));
            }
            node2 = (Node) XPATH_DI_SI_BROWSE_GRAPHICS_FILE_TYPE.evaluate(node, XPathConstants.NODE);
            if (node2 != null) {
                browseGraphic.setFileType(getLocalizedContent(node2, pathToLocalizedValue));
            }
            browseGraphics.add(browseGraphic);
        }
    }

    private void parseTemporalExtents(NodeList teNodes, List<CSWIsoRecord.Identification.TemporalExtent> temporalExtents) throws XPathExpressionException, ParseException {
        Node node, node2, node3;
        CSWIsoRecord.Identification.TemporalExtent temporalExtent;
        for (int i = 0; i < teNodes.getLength(); i++) {
            node = teNodes.item(i);
            node2 = (Node) XPATH_DI_SI_TEMPORAL_EXTENTS_PERIOD_BEGIN.evaluate(node, XPathConstants.NODE);
            node3 = (Node) XPATH_DI_SI_TEMPORAL_EXTENTS_PERIOD_END.evaluate(node, XPathConstants.NODE);
            temporalExtent = new CSWIsoRecord.Identification.TemporalExtent();
            if (node2 != null) {
                temporalExtent.setBegin(getText(node2));
            }
            if (node3 != null){
                temporalExtent.setEnd(getText(node3));
            }
            temporalExtents.add(temporalExtent);
        }
    }

    private void parseCitation(Node cNode, CSWIsoRecord.Identification.Citation citation, XPathExpression pathToLocalizedValue) throws XPathExpressionException, ParseException {
        Node node;
        NodeList nodeList;
        node = (Node) XPATH_DI_SI_CITATION_TITLE.evaluate(cNode, XPathConstants.NODE);
        if (node != null) {
            citation.setTitle(getLocalizedContent(node, pathToLocalizedValue));
        }
        CSWIsoRecord.Identification.DateWithType dateWithType = new CSWIsoRecord.Identification.DateWithType();
        node = (Node) XPATH_DI_SI_CITATION_DATE_TYPE.evaluate(cNode, XPathConstants.NODE);
        if (node != null) {
            dateWithType.setDateType(getText(node));
        }
        node = (Node) XPATH_DI_SI_CITATION_DATE_VALUE.evaluate(cNode, XPathConstants.NODE);
        if (node != null) {
            try {
                dateWithType.setDate(LocalDate.parse(getText(node), DATE_FORMAT));
            }
            catch (Exception e) {
                dateWithType.setXmlDate(getText(node));
            }

        }
        citation.setDate(dateWithType);
        nodeList = (NodeList) XPATH_DI_SI_CITATION_RESOURCE_IDENTIFIERS.evaluate(cNode, XPathConstants.NODESET);
        if (nodeList.getLength() > 0) {
            List<CSWIsoRecord.Identification.Citation.ResourceIdentifier> resourceIdentifiers = citation.getResourceIdentifiers();
            CSWIsoRecord.Identification.Citation.ResourceIdentifier resourceIdentifier;
            for (int i = 0; i < nodeList.getLength(); i++) {
                resourceIdentifier = new CSWIsoRecord.Identification.Citation.ResourceIdentifier();
                node = (Node) XPATH_DI_SI_CITATION_RESOURCE_IDENTIFIERS_CODE.evaluate(nodeList.item(i), XPathConstants.NODE);
                if (node != null) {
                    resourceIdentifier.setCode(getLocalizedContent(node, pathToLocalizedValue));
                }
                node = (Node) XPATH_DI_SI_CITATION_RESOURCE_IDENTIFIERS_CODESPACE.evaluate(nodeList.item(i), XPathConstants.NODE);
                if (node != null) {
                    resourceIdentifier.setCodeSpace(getLocalizedContent(node, pathToLocalizedValue));
                }
                resourceIdentifiers.add(resourceIdentifier);
            }
        }
    }

    private void parseDistributionInfo(Node diNode, CSWIsoRecord record, XPathExpression pathToLocalizedValue) throws XPathExpressionException {
        int i;
        Node node;
        NodeList nodeList;
        String s1, s2;
        List<CSWIsoRecord.DistributionFormat> list = record.getDistributionFormats();
        nodeList = (NodeList) XPATH_DISTRIBUTION_INFO_DISTRIBUTION_FORMATS.evaluate(diNode, XPathConstants.NODESET);
        for (i = 0; i < nodeList.getLength(); i++) {
            s1 = null;
            s2 = null;
            node = (Node) XPATH_DISTRIBUTION_INFO_DISTRIBUTION_FORMAT_NAME.evaluate(nodeList.item(i), XPathConstants.NODE);
            if (node != null) {
                s1 = getLocalizedContent(node, pathToLocalizedValue);
            }
            node = (Node) XPATH_DISTRIBUTION_INFO_DISTRIBUTION_FORMAT_VERSION.evaluate(nodeList.item(i), XPathConstants.NODE);
            if (node != null) {
                s2 = getLocalizedContent(node, pathToLocalizedValue);
            }
            list.add(new CSWIsoRecord.DistributionFormat(s1, s2));
        }

        nodeList = (NodeList) XPATH_DISTRIBUTION_INFO_ONLINE_RESOURCES.evaluate(diNode, XPathConstants.NODESET);
        List<CSWIsoRecord.OnlineResource> onlineResources = record.getOnlineResources();
        for (i = 0; i < nodeList.getLength(); i++) {
            s1 = null;
            s2 = null;
            node = (Node) XPATH_DISTRIBUTION_INFO_ONLINE_RESOURCES_LINK_NAME.evaluate(nodeList.item(i), XPathConstants.NODE);
            if (node != null) {
                s1 = getLocalizedContent(node, pathToLocalizedValue);
            }
            node = (Node) XPATH_DISTRIBUTION_INFO_ONLINE_RESOURCES_LINK_URL.evaluate(nodeList.item(i), XPathConstants.NODE);
            if (node != null) {
                s2 = getLocalizedContent(node, pathToLocalizedValue);
            }
            onlineResources.add(new CSWIsoRecord.OnlineResource(s1, s2));
        }
    }

    // Piles up constraint types from multiple resource constraint elements as there doesn't seem to be much difference between them
    private void parseResourceConstraints(NodeList rcNodes, CSWIsoRecord.Identification identification, XPathExpression pathToLocalizedValue) throws XPathExpressionException {
        int i;
        Node node;
        NodeList nodeList;
        List<String> list;

        // evaluate doesn't take in nodelists... ffs
        for (i = 0; i < rcNodes.getLength(); i++) {
            node = rcNodes.item(i);
            list = identification.getAccessConstraints();
            nodeList = (NodeList) XPATH_DI_SI_RESOURCE_CONSTRAINTS_ACCESS_CONSTRAINTS.evaluate(node, XPathConstants.NODESET);
            parseNodeListStrings(nodeList, list, pathToLocalizedValue);

            list = identification.getOtherConstraints();
            nodeList = (NodeList) XPATH_DI_SI_RESOURCE_CONSTRAINTS_OTHER_CONSTRAINTS.evaluate(node, XPathConstants.NODESET);
            parseNodeListStrings(nodeList, list, pathToLocalizedValue);

            list = identification.getClassifications();
            nodeList = (NodeList) XPATH_DI_SI_RESOURCE_CONSTRAINTS_CLASSIFICATIONS.evaluate(node, XPathConstants.NODESET);
            parseNodeListStrings(nodeList, list, pathToLocalizedValue);

            list = identification.getUseLimitations();
            nodeList = (NodeList) XPATH_DI_SI_RESOURCE_CONSTRAINTS_USE_LIMITATIONS.evaluate(node, XPathConstants.NODESET);
            parseNodeListStrings(nodeList, list, pathToLocalizedValue);
        }


    }

    private void parseBBoxes(final CSWIsoRecord.Identification identification, final NodeList extentNodes, MathTransform transform) throws TransformException {
        if (extentNodes == null || extentNodes.getLength() == 0) {
            // Nothing to do
            return;
        }
        int i, n;
        Node node;
        NodeList nodeList;
        String westBoundLongitude, southBoundLatitude, eastBoundLongitude, northBoundLatitude;
        List<Polygon> extents = new ArrayList<Polygon>();
        List<CSWIsoRecord.Envelope> wktExtents = identification.getEnvelopes();

        for (n = 0; n < extentNodes.getLength(); n++) {
            westBoundLongitude = null;
            southBoundLatitude = null;
            eastBoundLongitude = null;
            northBoundLatitude = null;

            nodeList = extentNodes.item(n).getChildNodes();

            for (i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if ("westBoundLongitude".equals(node.getLocalName())) {
                    westBoundLongitude = getLatLonValue(node);
                } else if ("southBoundLatitude".equals(node.getLocalName())) {
                    southBoundLatitude = getLatLonValue(node);
                } else if ("eastBoundLongitude".equals(node.getLocalName())) {
                    eastBoundLongitude = getLatLonValue(node);
                } else if ("northBoundLatitude".equals(node.getLocalName())) {
                    northBoundLatitude = getLatLonValue(node);
                }
            }

            // Skippety skip if we're missing one

            if (westBoundLongitude == null || westBoundLongitude.isEmpty()) {
                continue;
            }
            if (southBoundLatitude == null || southBoundLatitude.isEmpty()) {
                continue;
            }
            if (eastBoundLongitude == null || eastBoundLongitude.isEmpty()) {
                continue;
            }
            if (northBoundLatitude == null || northBoundLatitude.isEmpty()) {
                continue;
            }
            double x1 = Double.parseDouble(westBoundLongitude),
                   x2 = Double.parseDouble(eastBoundLongitude),
                   y1 = Double.parseDouble(northBoundLatitude),
                   y2 = Double.parseDouble(southBoundLatitude);

            // Coordinate axis order is a kinky issue.
            // We'll do it like GeoTools sees it, that is
            // it's lat, lon unless otherwise defined

            // GeoTools
            CSWIsoRecord.Envelope envStr = new CSWIsoRecord.Envelope();
            Envelope env = new Envelope(x1, x2, y1, y2);
            if (transform != null) {
                env = JTS.transform(env, transform);
            }
            envStr.setWestBoundLongitude(env.getMinX());
            envStr.setEastBoundLongitude(env.getMaxX());
            envStr.setSouthBoundLatitude(env.getMinY());
            envStr.setNorthBoundLatitude(env.getMaxY());
            wktExtents.add(envStr);
            Polygon extent = gf
                    .createPolygon(gf
                            .createLinearRing(new Coordinate[]{
                                    new Coordinate(env.getMinX(), env
                                            .getMinY()),
                                    new Coordinate(env.getMaxX(), env
                                            .getMinY()),
                                    new Coordinate(env.getMaxX(), env
                                            .getMaxY()),
                                    new Coordinate(env.getMinX(), env
                                            .getMaxY()),
                                    new Coordinate(env.getMinX(), env
                                            .getMinY())}), null);

            extents.add(extent);
        }
        if (extents.size() > 0) {
            GeometryCollection gc = gf.createGeometryCollection(
                    extents.toArray(new Polygon[extents.size()])
            );
            identification.setExtents(gc);
        }
    }

    private String getLatLonValue(Node element) {
        NodeList children = element.getChildNodes();
        String ret = null;
        for (int i = 0; i < children.getLength(); i++) {
            if ("Decimal".equals(children.item(i).getLocalName())) {
                ret = children.item(i).getTextContent();
            }
        }
        return ret;
    }

    private Map<String, String> getLocaleMap(final Node elem) {
        final Map<String, String> locales = new HashMap<String, String>();
        try {
            final NodeList localeNodes = (NodeList) XPATH_LOCALE_MAP.evaluate(elem, XPathConstants.NODESET);
            Node loc;
            for (int i = 0; i < localeNodes.getLength(); i++) {
                loc = localeNodes.item(i);
                final String localeKey = loc.getAttributes().getNamedItem("id").getTextContent();
                // Note! assuming only one exists
                NodeList locChildren = loc.getChildNodes();
                Node langCode = null;
                Node theLangCode;
                for (int j = 0; j < locChildren.getLength(); j++) {
                    if ("languageCode".equals(locChildren.item(j).getLocalName())) {
                        langCode = locChildren.item(j);
                        break;
                    }
                }
                if (langCode != null) {
                    locChildren = langCode.getChildNodes();
                    for (int j = 0; j < locChildren.getLength(); j++) {
                        if ("LanguageCode".equals(locChildren.item(j).getLocalName())) {
                            theLangCode = locChildren.item(j);
                            final String lang3letter = theLangCode.getAttributes().getNamedItem("codeListValue").getTextContent();
                            if (lang3letter != null) {
                                locales.put(lang3letter, localeKey);
                            } else {
                                log.warn("Failed to find locale mapping");
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing locales:", e.getMessage());
        }
        return locales;
    }

    private void parseNodeListStrings(NodeList nodeList, List<String> list, XPathExpression pathToLocalizedValue) {
        if (nodeList == null || nodeList.getLength() == 0) {
            return;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(getLocalizedContent(nodeList.item(i), pathToLocalizedValue));
        }
    }

    private String getLanguageIfAvailable(String langCode) {
        String ret = ISO3letterOskariLangMapping.get(langCode);
        return ret != null ? ret : langCode;
    }

    private String getLocalizedContent(final Node elem, final XPathExpression pathToLocaledValue) {
        String ret = getText(elem);
        String localized;
        if (elem != null && pathToLocaledValue != null) {
            try {
                final Node localeNode = (Node) pathToLocaledValue.evaluate(elem, XPathConstants.NODE);
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

    /**
     * Returns text content or null if element is null
     *
     * @param element Node
     * @return Element's text content or null if there's no element
     */
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
