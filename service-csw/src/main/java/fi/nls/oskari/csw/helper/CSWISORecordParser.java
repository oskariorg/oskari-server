package fi.nls.oskari.csw.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.domain.CSWIsoRecord.BrowseGraphic;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataIdentification;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DataQualityObject;
import fi.nls.oskari.csw.domain.CSWIsoRecord.DistributionFormat;
import fi.nls.oskari.csw.domain.CSWIsoRecord.Identification;
import fi.nls.oskari.csw.domain.CSWIsoRecord.Identification.Citation;
import fi.nls.oskari.csw.domain.CSWIsoRecord.Identification.Citation.ResourceIdentifier;
import fi.nls.oskari.csw.domain.CSWIsoRecord.Identification.DateWithType;
import fi.nls.oskari.csw.domain.CSWIsoRecord.Identification.TemporalExtent;
import fi.nls.oskari.csw.domain.CSWIsoRecord.OnlineResource;
import fi.nls.oskari.csw.domain.CSWIsoRecord.ResponsibleParty;
import fi.nls.oskari.csw.domain.CSWIsoRecord.ServiceIdentification;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * Helper class for parsing search results for MetadataCatalogue
 */
public class CSWISORecordParser {

    private static final Logger log = LogFactory.getLogger(CSWISORecordParser.class);

    // we need to map languages from 3-letter codes to 2-letter codes so initialize a global codeMapping property
    private static final Map<String, String> ISO3letterOskariLangMapping = new HashMap<>();
    static {
        for (final String language : Locale.getISOLanguages()) {
            final Locale locale = new Locale(language);
            ISO3letterOskariLangMapping.put(locale.getISO3Language(), locale.getLanguage());
        }
    }

    public static CSWIsoRecord parse(final Node node, final Locale locale) {
        return node instanceof Element elem ? parse(elem, locale) : null;
    }

    public static CSWIsoRecord parse(final Element mdMetadata, final Locale locale) {
        // Null when the record has no translations for the requested language. The localized
        // content then falls back to the gco:CharacterString values, see parseLocalizedContent()
        String localeId = parseLocaleMap(mdMetadata).get(locale.getISO3Language());

        CSWIsoRecord record = new CSWIsoRecord();

        record.setIdentifications(parseIdentifications(mdMetadata, localeId));
        record.setDataQualityObject(parseDataQualityObject(mdMetadata, localeId));
        record.setDistributionFormats(parseDistributionFormats(mdMetadata, localeId));
        record.setOnlineResources(parseOnlineResources(mdMetadata, localeId));
        record.setFileIdentifier(
            XmlHelper.getAnyChild(mdMetadata, "fileIdentifier")
                .map(e -> parseLocalizedContent(e, localeId))
                .orElse(null));
        record.setMetadataStandardName(
            XmlHelper.getAnyChild(mdMetadata, "metadataStandardName")
                .map(e -> parseLocalizedContent(e, localeId))
                .orElse(null));
        record.setMetadataStandardVersion(
            XmlHelper.getAnyChild(mdMetadata, "metadataStandardVersion")
                .map(e -> parseLocalizedContent(e, localeId))
                .orElse(null));
        record.setMetadataLanguage(
            XmlHelper.getAnyChild(mdMetadata, "language")
                .map(e -> parseLocalizedContent(e, localeId))
                .map(e -> getLanguageIfAvailable(e))
                .orElse(null));
        record.setMetadataCharacterSet(parseMetadataCharacterSet(mdMetadata));
        record.setScopeCodes(parseScopeCodes(mdMetadata));
        record.setMetadataResponsibleParties(parseResponsibleParties(mdMetadata, localeId));
        record.setMetadataDateStamp(
            XmlHelper.getAnyChild(mdMetadata, "dateStamp")
                .map(dateStamp -> parseDateStamp(dateStamp))
                .orElse(null));
        record.setReferenceSystems(parseReferenceSystems(mdMetadata));

        return record;
    }

    private static Map<String, String> parseLocaleMap(Element mdMetadata) {
        /**
        <gmd:locale xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gml="http://www.opengis.net/gml">
            <gmd:PT_Locale id="SV">
                <gmd:languageCode>
                    <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="swe" />
                </gmd:languageCode>
                <gmd:characterEncoding>
                    <gmd:MD_CharacterSetCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_CharacterSetCode" codeListValue="UTF-8" />
                </gmd:characterEncoding>
            </gmd:PT_Locale>
        </gmd:locale>
        => { "swe": "SV" }
         */
        List<Element> ptLocales = XmlHelper.getChildElements(mdMetadata, "locale", "PT_Locale")
            .filter(ptLocale -> XmlHelper.getAttributeValue(ptLocale, "id") != null)
            .toList();

        Map<String, String> locales = new HashMap<>();
        for (Element ptLocale : ptLocales) {
            String id = XmlHelper.getAttributeValue(ptLocale, "id");
            XmlHelper.getChildElements(ptLocale, "languageCode", "LanguageCode")
                .map(languageCode -> XmlHelper.getAttributeValue(languageCode, "codeListValue"))
                .findAny()
                .ifPresent(code -> locales.put(code, id));
        }
        return locales;
    }

    private static List<Identification> parseIdentifications(Element mdMetadata, String localeId) {
        return Stream.concat(
            parseDataIdentifications(mdMetadata, localeId),
            parseServiceIdentifications(mdMetadata, localeId)
        ).toList();
    }

    private static Stream<DataIdentification> parseDataIdentifications(Element mdMetadata, String localeId) {
        return XmlHelper.getChildElements(mdMetadata, "identificationInfo", "MD_DataIdentification")
            .map(di -> parseDataIdentification(di, localeId));
    }

    private static DataIdentification parseDataIdentification(Element dataIdentification, String localeId) {
        DataIdentification di = new DataIdentification();
        parseAndSetIdentification(di, dataIdentification, localeId);
        di.setCharacterSets(
            XmlHelper.getChildElements(dataIdentification, "characterSet", "MD_CharacterSetCode")
                .map(x -> XmlHelper.getAttributeValue(x, "codeListValue"))
                .filter(Objects::nonNull)
                .toList());
        di.setLanguages(
            XmlHelper.getChildElements(dataIdentification, "language")
                .map(x -> parseLocalizedContent(x, localeId))
                .filter(Objects::nonNull)
                .map(CSWISORecordParser::getLanguageIfAvailable)
                .toList());
        di.setTopicCategories(
            XmlHelper.getChildElements(dataIdentification, "topicCategory", "MD_TopicCategoryCode")
                .map(x -> getText(x))
                .filter(Objects::nonNull)
                .toList());
        di.setSpatialResolutions(parseSpatialResolutions(dataIdentification));
        di.setSpatialRepresentationTypes(
            XmlHelper.getChildElements(dataIdentification, "spatialRepresentationType", "MD_SpatialRepresentationTypeCode")
                .map(x -> XmlHelper.getAttributeValue(x, "codeListValue"))
                .filter(Objects::nonNull)
                .toList());
        return di;
    }

    private static List<Integer> parseSpatialResolutions(Element dataIdentification) {
        /**
        <gmd:spatialResolution>
            <gmd:MD_Resolution>
                <gmd:equivalentScale>
                    <gmd:MD_RepresentativeFraction>
                        <gmd:denominator>
                            <gco:Integer>10000</gco:Integer>
                        </gmd:denominator>
                    </gmd:MD_RepresentativeFraction>
                </gmd:equivalentScale>
            </gmd:MD_Resolution>
        </gmd:spatialResolution>
         */
        return XmlHelper.getChildElements(dataIdentification, "spatialResolution", "MD_Resolution",
                "equivalentScale", "MD_RepresentativeFraction", "denominator", "Integer")
            .map(x -> getText(x))
            .map(CSWISORecordParser::parseIntOrNull)
            .filter(Objects::nonNull)
            .toList();
    }

    private static Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid resolution integer:", value);
            return null;
        }
    }

    private static Stream<ServiceIdentification> parseServiceIdentifications(Element mdMetadata, String localeId) {
        return XmlHelper.getChildElements(mdMetadata, "identificationInfo", "SV_ServiceIdentification")
            .map(si -> parseServiceIdentification(si, localeId));
    }

    private static ServiceIdentification parseServiceIdentification(Element serviceIdentification, String localeId) {
        /**
        <srv:SV_ServiceIdentification>
            <srv:serviceType>
                <gco:LocalName>view</gco:LocalName>
            </srv:serviceType>
            <srv:serviceTypeVersion>
                <gco:LocalName>1.3.0</gco:LocalName>
            </srv:serviceTypeVersion>
            <srv:operatesOn uuidref="1234-5678" />
        </srv:SV_ServiceIdentification>
         */
        ServiceIdentification si = new ServiceIdentification();
        parseAndSetIdentification(si, serviceIdentification, localeId);
        si.setOperatesOn(
            XmlHelper.getChildElements(serviceIdentification, "operatesOn")
                .map(x -> XmlHelper.getAttributeValue(x, "uuidref"))
                .filter(Objects::nonNull)
                .toList());
        si.setServiceType(
            XmlHelper.getChildElements(serviceIdentification, "serviceType", "LocalName")
                .map(x -> getText(x))
                .findAny()
                .orElse(null));
        si.setServiceTypeVersion(
            XmlHelper.getChildElements(serviceIdentification, "serviceTypeVersion", "LocalName")
                .map(x -> getText(x))
                .findAny()
                .orElse(null));
        return si;
    }

    private static void parseAndSetIdentification(Identification identification, Element e, String localeId) {
        identification.setCitation(
            XmlHelper.getChildElements(e, "citation", "CI_Citation")
                .map(citation -> parseCitation(citation, localeId))
                .findAny()
                .orElse(null));
        identification.setAbstractText(
            XmlHelper.getAnyChild(e, "abstract")
                .map(x -> parseLocalizedContent(x, localeId))
                .orElse(null));
        identification.setResponsibleParties(
            XmlHelper.getChildElements(e, "pointOfContact", "CI_ResponsibleParty")
                .map(rp -> parseResponsibleParty(rp, localeId))
                .toList());
        identification.setBrowseGraphics(
            XmlHelper.getChildElements(e, "graphicOverview", "MD_BrowseGraphic")
                .map(bg -> parseBrowseGraphic(bg, localeId))
                .toList());
        identification.setDescriptiveKeywords(
            XmlHelper.getChildElements(e, "descriptiveKeywords", "MD_Keywords", "keyword")
                .map(x -> parseAnchorOrLocalizedContent(x, localeId))
                // the keyword can be explicitly missing: <gmd:keyword gco:nilReason="missing">
                .filter(keyword -> keyword != null && !keyword.isEmpty())
                .toList());
        identification.setAccessConstraints(
            XmlHelper.getChildElements(e, "resourceConstraints", "MD_LegalConstraints", "accessConstraints", "MD_RestrictionCode")
                .map(x -> XmlHelper.getAttributeValue(x, "codeListValue"))
                .filter(Objects::nonNull)
                .toList());
        identification.setOtherConstraints(
            XmlHelper.getChildElements(e, "resourceConstraints", "MD_LegalConstraints", "otherConstraints")
                .map(x -> parseAnchorOrLocalizedContent(x, localeId))
                .filter(Objects::nonNull)
                .toList());
        identification.setClassifications(
            XmlHelper.getChildElements(e, "resourceConstraints", "MD_SecurityConstraints", "classification", "MD_ClassificationCode")
                .map(x -> XmlHelper.getAttributeValue(x, "codeListValue"))
                .filter(Objects::nonNull)
                .toList());
        identification.setUseLimitations(
            XmlHelper.getChildElements(e, "resourceConstraints", "MD_Constraints", "useLimitation")
                .map(x -> parseLocalizedContent(x, localeId))
                .filter(Objects::nonNull)
                .toList());
        identification.setTemporalExtents(
            XmlHelper.getChildElements(e, "extent", "EX_Extent", "temporalElement", "EX_TemporalExtent", "extent", "TimePeriod")
                .map(x -> parseTimePeriod(x))
                .toList());
        identification.setEnvelopes(
            XmlHelper.getChildElements(e, "extent", "EX_Extent", "geographicElement", "EX_GeographicBoundingBox")
                .map(x -> parseGeographicBoundingBox(x))
                .toList());
    }

    private static Citation parseCitation(Element ciCitation, String localeId) {
        /**
        <gmd:CI_Citation>
            <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
                <gco:CharacterString>Maastotietokanta</gco:CharacterString>
            </gmd:title>
            <gmd:date>
                <gmd:CI_Date>
                    <gmd:date>
                        <gco:Date>2010-04-01</gco:Date>
                    </gmd:date>
                    <gmd:dateType>
                        <gmd:CI_DateTypeCode codeListValue="revision" />
                    </gmd:dateType>
                </gmd:CI_Date>
            </gmd:date>
            <gmd:identifier>
                <gmd:RS_Identifier>
                    <gmd:code>
                        <gco:CharacterString>1000007</gco:CharacterString>
                    </gmd:code>
                    <gmd:codeSpace>
                        <gco:CharacterString>FI</gco:CharacterString>
                    </gmd:codeSpace>
                </gmd:RS_Identifier>
            </gmd:identifier>
        </gmd:CI_Citation>
         */
        Citation citation = new Citation();
        citation.setTitle(
            XmlHelper.getAnyChild(ciCitation, "title")
                .map(x -> parseLocalizedContent(x, localeId))
                .orElse(null));
        citation.setAlternateTitle(
            XmlHelper.getAnyChild(ciCitation, "alternateTitle")
                .map(x -> parseLocalizedContent(x, localeId))
                .orElse(null));
        citation.setDate(
            XmlHelper.getChildElements(ciCitation, "date", "CI_Date")
                .map(ciDate -> parseDateWithType(ciDate))
                .findAny()
                .orElseGet(DateWithType::new));
        citation.setResourceIdentifiers(
            XmlHelper.getChildElements(ciCitation, "identifier", "RS_Identifier")
                .map(rsIdentifier -> parseResourceIdentifier(rsIdentifier, localeId))
                .toList());
        return citation;
    }

    private static DateWithType parseDateWithType(Element ciDate) {
        DateWithType dateWithType = new DateWithType();
        dateWithType.setDateType(
            XmlHelper.getChildElements(ciDate, "dateType", "CI_DateTypeCode")
                .map(x -> XmlHelper.getAttributeValue(x, "codeListValue"))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null));
        String date = XmlHelper.getChildElements(ciDate, "date", "Date")
            .map(x -> getText(x))
            .findAny()
            .orElse(null);
        if (date != null) {
            try {
                dateWithType.setDate(LocalDate.parse(date));
            } catch (DateTimeParseException e) {
                // keep the raw value around when it isn't an ISO date
                dateWithType.setXmlDate(date);
            }
        }
        return dateWithType;
    }

    private static ResourceIdentifier parseResourceIdentifier(Element rsIdentifier, String localeId) {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier();
        resourceIdentifier.setCode(
            XmlHelper.getAnyChild(rsIdentifier, "code")
                .map(x -> parseLocalizedContent(x, localeId))
                .orElse(null));
        resourceIdentifier.setCodeSpace(
            XmlHelper.getAnyChild(rsIdentifier, "codeSpace")
                .map(x -> parseLocalizedContent(x, localeId))
                .orElse(null));
        return resourceIdentifier;
    }

    private static List<String> parseScopeCodes(Element mdMetadata) {
        /**
        <gmd:hierarchyLevel>
            <gmd:MD_ScopeCode codeListValue="dataset" codeList="..." />
        </gmd:hierarchyLevel>
         */
        return XmlHelper.getChildElements(mdMetadata, "hierarchyLevel", "MD_ScopeCode")
            .map(scopeCode -> XmlHelper.getAttributeValue(scopeCode, "codeListValue"))
            .filter(Objects::nonNull)
            .toList();
    }

    private static List<ResponsibleParty> parseResponsibleParties(Element mdMetadata, String localeId) {
        return XmlHelper.getChildElements(mdMetadata, "contact", "CI_ResponsibleParty")
            .map(rp -> parseResponsibleParty(rp, localeId))
            .toList();
    }

    private static ResponsibleParty parseResponsibleParty(Element responsibleParty, String localeId) {
        /**
        <gmd:CI_ResponsibleParty>
            <gmd:organisationName xsi:type="gmd:PT_FreeText_PropertyType">
                <gco:CharacterString>Maanmittauslaitos</gco:CharacterString>
                <gmd:PT_FreeText>
                    <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#SV">Lantmäteriverket</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                    <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#EN">National Land Survey of Finland</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                </gmd:PT_FreeText>
            </gmd:organisationName>
            <gmd:contactInfo>
                <gmd:CI_Contact>
                    <gmd:address>
                        <gmd:CI_Address>
                            <gmd:electronicMailAddress xsi:type="gmd:PT_FreeText_PropertyType">
                                <gco:CharacterString>asiakaspalvelu@maanmittauslaitos.fi</gco:CharacterString>
                                <gmd:PT_FreeText>
                                    <gmd:textGroup>
                                        <gmd:LocalisedCharacterString locale="#SV">kundservice@lantmateriverket.fi</gmd:LocalisedCharacterString>
                                    </gmd:textGroup>
                                    <gmd:textGroup>
                                        <gmd:LocalisedCharacterString locale="#EN">customerservice@nls.fi</gmd:LocalisedCharacterString>
                                    </gmd:textGroup>
                                </gmd:PT_FreeText>
                            </gmd:electronicMailAddress>
                        </gmd:CI_Address>
                    </gmd:address>
                </gmd:CI_Contact>
            </gmd:contactInfo>
            <gmd:role>
                <gmd:CI_RoleCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode" codeListValue="owner" />
            </gmd:role>
        </gmd:CI_ResponsibleParty>
         */
        String organisationName = XmlHelper.getAnyChild(responsibleParty, "organisationName")
            .map(orgName -> parseLocalizedContent(orgName, localeId))
            .orElse(null);
        List<String> emailAddresses = XmlHelper.getChildElements(responsibleParty, "contactInfo", "CI_Contact", "address", "CI_Address", "electronicMailAddress")
            .map(electronicMailAddress -> parseLocalizedContent(electronicMailAddress, localeId))
            .toList();

        ResponsibleParty p = new ResponsibleParty();
        p.setOrganisationName(organisationName);
        p.setElectronicMailAddresses(emailAddresses);
        return p;
    }

    private static BrowseGraphic parseBrowseGraphic(Element e, String localeId) {
        /**
        <gmd:MD_BrowseGraphic>
            <gmd:fileName xsi:type="gmd:PT_FreeText_PropertyType">
                <gco:CharacterString>maastotietokanta_s.png</gco:CharacterString>
                <gmd:PT_FreeText>
                    <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#SV">maastotietokanta_s.png</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                    <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#EN">maastotietokanta_s.png</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                </gmd:PT_FreeText>
            </gmd:fileName>
            <gmd:fileDescription xsi:type="gmd:PT_FreeText_PropertyType">
                <gco:CharacterString>thumbnail</gco:CharacterString>
                <gmd:PT_FreeText>
                    <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#SV">thumbnail</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                    <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#EN">thumbnail</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                </gmd:PT_FreeText>
            </gmd:fileDescription>
            <gmd:fileType xsi:type="gmd:PT_FreeText_PropertyType">
                <gco:CharacterString>png</gco:CharacterString>
                <gmd:PT_FreeText>
                    <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#SV">png</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                    <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#EN">png</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                </gmd:PT_FreeText>
            </gmd:fileType>
        </gmd:MD_BrowseGraphic>
         */
        BrowseGraphic g = new BrowseGraphic();
        g.setFileName(XmlHelper.getAnyChild(e, "fileName").map(x -> parseLocalizedContent(x, localeId)).orElse(null));
        g.setFileDescription(XmlHelper.getAnyChild(e, "fileDescription").map(x -> parseLocalizedContent(x, localeId)).orElse(null));
        g.setFileType(XmlHelper.getAnyChild(e, "fileType").map(x -> parseLocalizedContent(x, localeId)).orElse(null));
        return g;
    }


    private static DataQualityObject parseDataQualityObject(Element mdMetadata, String localeId) {
        return CSWISORecordDataQualityParser.parseDataQualities(mdMetadata, localeId);
    }

    private static List<DistributionFormat> parseDistributionFormats(Element mdMetadata, String localeId) {
        return XmlHelper.getChildElements(mdMetadata, "distributionInfo", "MD_Distribution", "distributionFormat", "MD_Format")
            .map(mdFormat -> parseDistributionFormat(mdFormat, localeId))
            .toList();
    }

    private static DistributionFormat parseDistributionFormat(Element mdFormat, String localeId) {
        /**
        <gmd:MD_Format>
            <gmd:name xsi:type="gmd:PT_FreeText_PropertyType">
                <gco:CharacterString>GML</gco:CharacterString>
                <gmd:PT_FreeText>
                    <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#SV">MIF</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                    <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#EN">MIF</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                </gmd:PT_FreeText>
            </gmd:name>
            <gmd:version gco:nilReason="missing">
                <gco:CharacterString />
            </gmd:version>
        </gmd:MD_Format>
         */
        String name = XmlHelper.getAnyChild(mdFormat, "name")
            .map(n -> parseLocalizedContent(n, localeId))
            .orElse(null);
        String version = XmlHelper.getAnyChild(mdFormat, "version")
            .map(v -> parseLocalizedContent(v, localeId))
            .orElse(null);
        return new DistributionFormat(name, version);
    }

    private static List<OnlineResource> parseOnlineResources(Element mdMetadata, String localeId) {
        return XmlHelper.getChildElements(mdMetadata, "distributionInfo", "MD_Distribution", "transferOptions", "MD_DigitalTransferOptions", "onLine")
            .map(onLine -> parseOnLine(onLine, localeId))
            .toList();
    }

    private static OnlineResource parseOnLine(Element onLine, String localeId) {
        /**
        <gmd:onLine>
            <gmd:CI_OnlineResource>
                <gmd:linkage>
                    <gmd:URL>https://avoinapi.vaylapilvi.fi/vaylatiedot/ows?service=wms&amp;request=getCapabilities</gmd:URL>
                </gmd:linkage>
                <gmd:protocol>
                    <gco:CharacterString>http://www.opengis.net/def/serviceType/ogc/wms</gco:CharacterString>
                </gmd:protocol>
                <gmd:name>
                    <gco:CharacterString>Radan kilometripisteet</gco:CharacterString>
                </gmd:name>
                <gmd:description>
                    <gco:CharacterString>track_kilometer</gco:CharacterString>
                </gmd:description>
            </gmd:CI_OnlineResource>
        </gmd:onLine>
         */
        String name = XmlHelper.getChildElements(onLine, "CI_OnlineResource", "name", "CharacterString")
            .map(Element::getTextContent)
            .findAny()
            .orElse(null);
        String url = XmlHelper.getChildElements(onLine, "CI_OnlineResource", "linkage", "URL")
            .map(Element::getTextContent)
            .findAny()
            .orElse(null);
        
        return new OnlineResource(name, url);
    }

    private static CSWIsoRecord.Envelope parseGeographicBoundingBox(Element geographicBoundingBox) {
        /**
        <gmd:EX_GeographicBoundingBox>
            <gmd:westBoundLongitude>
                <gco:Decimal>19.08317359</gco:Decimal>
            </gmd:westBoundLongitude>
            <gmd:eastBoundLongitude>
                <gco:Decimal>31.58672881</gco:Decimal>
            </gmd:eastBoundLongitude>
            <gmd:southBoundLatitude>
                <gco:Decimal>59.45414258</gco:Decimal>
            </gmd:southBoundLatitude>
            <gmd:northBoundLatitude>
                <gco:Decimal>70.09229553</gco:Decimal>
            </gmd:northBoundLatitude>
        </gmd:EX_GeographicBoundingBox>
         */
        List<Double> decimals = Stream.of("westBoundLongitude", "eastBoundLongitude", "southBoundLatitude", "northBoundLatitude")
            .map(name -> XmlHelper.getChildElements(geographicBoundingBox, name, "Decimal").findAny())
            .flatMap(Optional::stream)
            .map(decimal -> getText(decimal))
            .map(Double::parseDouble)
            .toList();
        CSWIsoRecord.Envelope e = new CSWIsoRecord.Envelope();
        e.setWestBoundLongitude(decimals.get(0));
        e.setEastBoundLongitude(decimals.get(1));
        e.setSouthBoundLatitude(decimals.get(2));
        e.setNorthBoundLatitude(decimals.get(3));
        return e;
    }

    private static String getLanguageIfAvailable(String langCode) {
        String ret = ISO3letterOskariLangMapping.get(langCode);
        return ret != null ? ret : langCode;
    }

    /**
     * The value can be given as a gmx:Anchor instead of the usual localized gco:CharacterString, for example:
     * <gmd:keyword>
     *     <gmx:Anchor xlink:href="http://rdfdata.eionet.europa.eu/inspirethemes/themes/7">Liikenneverkot</gmx:Anchor>
     * </gmd:keyword>
     */
    private static String parseAnchorOrLocalizedContent(Element e, String localeId) {
        return XmlHelper.getAnyChild(e, "Anchor")
            .map(x -> getText(x))
            .orElseGet(() -> parseLocalizedContent(e, localeId));
    }

    private static String parseMetadataCharacterSet(Element mdMetadata) {
        return XmlHelper.getChildElements(mdMetadata, "characterSet", "MD_CharacterSetCode")
            .map(code -> XmlHelper.getAttributeValue(code, "codeListValue"))
            .findAny()
            .orElse(null);
    }

    private static LocalDateTime parseDateStamp(Element dateStamp) {
        return XmlHelper.getAnyChild(dateStamp, "DateTime")
            .map(x -> getText(x))
            .map(dateTime -> LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME))
            .or(() -> XmlHelper.getAnyChild(dateStamp, "Date")
                .map(x -> getText(x))
                .map(LocalDate::parse)
                .map(LocalDate::atStartOfDay)
            )
            .orElse(null);
    }

    private static TemporalExtent parseTimePeriod(Element e) {
        /**
        <gml:TimePeriod gml:id="d1360720e941a1051934">
            <gml:beginPosition />
            <gml:endPosition />
        </gml:TimePeriod>
         */
        return new TemporalExtent(
            XmlHelper.getAnyChild(e, "beginPosition").map(x -> getText(x)).orElse(null),
            XmlHelper.getAnyChild(e, "endPosition").map(x -> getText(x)).orElse(null)
        );
    }

    private static List<String> parseReferenceSystems(Element mdMetadata) {
        return XmlHelper.getChildElements(mdMetadata, "referenceSystemInfo", "MD_ReferenceSystem", "referenceSystemIdentifier", "RS_Identifier", "code")
            .map(code -> parseLocalizedContent(code, null))
            .filter(Objects::nonNull)
            .toList();
    }

    static String parseLocalizedContent(Element e, String localeId) {
        /**
        <e>
            <gco:CharacterString>GML</gco:CharacterString>
            <gmd:PT_FreeText>
                <gmd:textGroup>
                    <gmd:LocalisedCharacterString locale="#SV">MIF</gmd:LocalisedCharacterString>
                </gmd:textGroup>
                <gmd:textGroup>
                    <gmd:LocalisedCharacterString locale="#EN">MIF</gmd:LocalisedCharacterString>
                </gmd:textGroup>
            </gmd:PT_FreeText>
        </e>
         */
        return Optional.ofNullable(localeId)
            .flatMap(id -> XmlHelper.getChildElements(e, "PT_FreeText", "textGroup", "LocalisedCharacterString")
                .filter(x -> ("#" + id).equals(XmlHelper.getAttributeValue(x, "locale")))
                .map(x -> getText(x))
                // the translation can be an empty element, use the default value in that case
                .filter(text -> text != null && !text.isEmpty())
                .findAny())
            .or(() -> XmlHelper.getAnyChild(e, "CharacterString").map(x -> getText(x)))
            .orElse(null);
    }

    /**
     * Returns text content or null if element is null
     *
     * @param element Node
     * @return Element's text content or null if there's no element
     */
    private static String getText(final Node element) {
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
