package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.XmlHelper;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.SimpleNamespaceContext;
import org.json.JSONObject;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Helper class for parsing search results for MetadataCatalogue:
 *
 */
public class MetadataCatalogueResultParser {

    private static final Logger log = LogFactory.getLogger(MetadataCatalogueResultParser.class);

    private AXIOMXPath XPATH_IDENTIFICATION = null;
    private AXIOMXPath XPATH_IDENTIFICATION_TITLE = null;
    private AXIOMXPath XPATH_IDENTIFICATION_DESC = null;
    private AXIOMXPath XPATH_IDENTIFICATION_IMAGE = null;
    private AXIOMXPath XPATH_IDENTIFICATION_ORGANIZATION = null;
    private AXIOMXPath XPATH_IDENTIFICATION_BBOX = null;
    private AXIOMXPath XPATH_IDENTIFICATION_UUID = null;
    private AXIOMXPath XPATH_IDENTIFICATION_DATE = null;
    private AXIOMXPath XPATH_IDENTIFICATION_MAINTENANCE_AND_UPDATE_FREQUENCY_CODELIST = null;
    private AXIOMXPath XPATH_IDENTIFICATION_CODELIST = null;
    private AXIOMXPath XPATH_CODELISTVALUE = null;
    private AXIOMXPath XPATH_DISTINFO = null;
    private AXIOMXPath XPATH_FILEID = null;
    private AXIOMXPath XPATH_LOCALE_MAP = null;

    protected SimpleNamespaceContext NAMESPACE_CTX = null;

    private final QName QNAME_ID = QName.valueOf("id");
    private final QName QNAME_CODELISTVALUE = QName.valueOf("codeListValue");
    // naming ftw (notice capital L in the second LanguageCode tag...)
    private final QName QNAME_lANGUAGECODE = new QName("http://www.isotc211.org/2005/gmd","languageCode", "gmd");
    private final QName QNAME_THE_LANGUAGECODE = new QName("http://www.isotc211.org/2005/gmd","LanguageCode", "gmd");

    public static final String KEY_IDENTIFICATION = "identification";
    public static final String KEY_IDENTIFICATION_DATE = "date";
    public static final String KEY_IDENTIFICATION_CODELIST = "code";
    public static final String KEY_MAINTENANCE_AND_UPDATE_FREQUENCY_CODELIST = "updateFrequency";
    public static final String KEY_NATUREOFTHETARGET = "natureofthetarget";
    // we need to map languages from 3-letter codes to 2-letter codes so initialize a global codeMapping property
    private final static Map<String, String> ISO3letterOskariLangMapping = new HashMap<String, String>();

    public MetadataCatalogueResultParser() {
        NAMESPACE_CTX = new SimpleNamespaceContext();
        NAMESPACE_CTX.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
        NAMESPACE_CTX.addNamespace("gco", "http://www.isotc211.org/2005/gco");
        NAMESPACE_CTX.addNamespace("srv", "http://www.isotc211.org/2005/srv");
/*
gmd:identificationInfo/gmd:MD_DataIdentification
  -> gmd:citation/gmd:CI_Citation/gmd:title = title (gco:CharacterString)
  -> gmd:abstract = description (gco:CharacterString)
  -> gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName(position = last) = setContentURL (gco:CharacterString)
  -> gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName = MetadataField.RESULT_KEY_ORGANIZATION (gco:CharacterString)
  -> gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox = WestBoundLongitude jne (gco:Decimal)

gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource
  -> gmd:linkage = setGmdURL (gmd:URL)
  -> set downloadable (no example, skipped)

gmd:fileIdentifier = uuid (gco:CharacterString)
  -> setResourceId(uuid)

setResourceNameSpace(serverURL)
 */
        XPATH_IDENTIFICATION = XmlHelper.buildXPath("./gmd:identificationInfo/*[local-name()='MD_DataIdentification' or local-name()='SV_ServiceIdentification']", NAMESPACE_CTX);

        XPATH_IDENTIFICATION_TITLE = XmlHelper.buildXPath("./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString", NAMESPACE_CTX);
        XPATH_IDENTIFICATION_DESC = XmlHelper.buildXPath("./gmd:abstract/gco:CharacterString", NAMESPACE_CTX);
        XPATH_IDENTIFICATION_IMAGE = XmlHelper.buildXPath("./gmd:graphicOverview[position()=last()]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString", NAMESPACE_CTX);
        XPATH_IDENTIFICATION_ORGANIZATION = XmlHelper.buildXPath("./gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString", NAMESPACE_CTX);
        XPATH_IDENTIFICATION_CODELIST = XmlHelper.buildXPath("./gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode", NAMESPACE_CTX);
        XPATH_IDENTIFICATION_DATE = XmlHelper.buildXPath("./gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date", NAMESPACE_CTX);

        XPATH_IDENTIFICATION_MAINTENANCE_AND_UPDATE_FREQUENCY_CODELIST = XmlHelper.buildXPath("./gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode", NAMESPACE_CTX);

        // extend can be gmd or srv namespaced
        XPATH_IDENTIFICATION_BBOX = XmlHelper.buildXPath("./*[local-name()='extent']/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox", NAMESPACE_CTX);

        XPATH_DISTINFO = XmlHelper.buildXPath("./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL", NAMESPACE_CTX);

        XPATH_FILEID = XmlHelper.buildXPath("./gmd:fileIdentifier/gco:CharacterString", NAMESPACE_CTX);

        XPATH_LOCALE_MAP = XmlHelper.buildXPath("./gmd:locale/gmd:PT_Locale", NAMESPACE_CTX);

        XPATH_IDENTIFICATION_UUID = XmlHelper.buildXPath("./srv:operatesOn", NAMESPACE_CTX);
        XPATH_LOCALE_MAP = XmlHelper.buildXPath("./gmd:locale/gmd:PT_Locale", NAMESPACE_CTX);


        XPATH_CODELISTVALUE = XmlHelper.buildXPath("./gmd:hierarchyLevel/gmd:MD_ScopeCode", NAMESPACE_CTX);

        if(ISO3letterOskariLangMapping.isEmpty()) {
            final String[] languages = Locale.getISOLanguages();
            for (String language : languages) {
                Locale locale = new Locale(language);
                ISO3letterOskariLangMapping.put(locale.getISO3Language(), locale.getLanguage());
            }
        }
    }


    public SearchResultItem parseResult(final OMElement elem, final String locale) throws Exception {
        //final String locale = "fi";
        final Map<String, String> locales = getLocaleMap(elem);
        AXIOMXPath pathToLocalizedValue = null;
        if(locales != null && locales.containsKey(locale)) {
            pathToLocalizedValue = XmlHelper.buildXPath("../gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#" + locales.get(locale) + "']", NAMESPACE_CTX);
        }
        final OMElement idNode = (OMElement) XPATH_IDENTIFICATION.selectSingleNode(elem);

        final SearchResultItem item = new SearchResultItem();
        final OMElement titleNode = (OMElement) XPATH_IDENTIFICATION_TITLE.selectSingleNode(idNode);
        item.setTitle(getLocalizedContent(titleNode, pathToLocalizedValue));
        final OMElement descNode = (OMElement) XPATH_IDENTIFICATION_DESC.selectSingleNode(idNode);
        item.setDescription(getLocalizedContent(descNode, pathToLocalizedValue));
        final OMElement imageNode = (OMElement) XPATH_IDENTIFICATION_IMAGE.selectSingleNode(idNode);
        item.setContentURL(getLocalizedContent(imageNode, pathToLocalizedValue));
        final OMElement orgNode = (OMElement) XPATH_IDENTIFICATION_ORGANIZATION.selectSingleNode(idNode);
        item.addValue(MetadataField.RESULT_KEY_ORGANIZATION, getLocalizedContent(orgNode, pathToLocalizedValue));
        final OMElement bboxNode = (OMElement) XPATH_IDENTIFICATION_BBOX.selectSingleNode(idNode);
        setupBBox(item, bboxNode);
        final OMElement distInfoNode = (OMElement) XPATH_DISTINFO.selectSingleNode(elem);
        item.setGmdURL(getLocalizedContent(distInfoNode, pathToLocalizedValue));
        final OMElement uuidNode = (OMElement) XPATH_FILEID.selectSingleNode(elem);

        final List<OMElement> operatesOnNodes = XPATH_IDENTIFICATION_UUID.selectNodes(idNode);


        final OMElement codeNode = (OMElement) XPATH_IDENTIFICATION_CODELIST.selectSingleNode(idNode);
        final OMElement dateNode = (OMElement) XPATH_IDENTIFICATION_DATE.selectSingleNode(idNode);
        final OMElement maintenanceAndUpdateFrequencyNode = (OMElement) XPATH_IDENTIFICATION_MAINTENANCE_AND_UPDATE_FREQUENCY_CODELIST.selectSingleNode(idNode);
        JSONObject identification = new JSONObject();
        identification.put(KEY_IDENTIFICATION_CODELIST, getAttributeValue(codeNode, QName.valueOf("codeListValue")));
        identification.put(KEY_IDENTIFICATION_DATE, getLocalizedContent(dateNode, pathToLocalizedValue));
        identification.put(KEY_MAINTENANCE_AND_UPDATE_FREQUENCY_CODELIST, getAttributeValue(maintenanceAndUpdateFrequencyNode, QName.valueOf("codeListValue")));
        item.addValue(KEY_IDENTIFICATION, identification);


        log.debug("==1");
        final OMElement codeListValue = (OMElement) XPATH_CODELISTVALUE.selectSingleNode(elem);
        log.debug("====: " + codeListValue.getAttributeValue(QNAME_CODELISTVALUE));
        item.setNatureOfTarget(codeListValue.getAttributeValue(QNAME_CODELISTVALUE));
        item.addValue(KEY_NATUREOFTHETARGET, item.getNatureOfTarget());

        for(OMElement operatesOnNode  : operatesOnNodes){
            if(operatesOnNode != null){
                if(operatesOnNode.getAllAttributes() == null)
                	log.debug("attributes == null");

                Iterator<OMAttribute> i = operatesOnNode.getAllAttributes();
                if(i.hasNext()){
                	OMAttribute ao = (OMAttribute)i.next();
                	log.debug("AO value: " + ao.getAttributeValue());
                	item.addUuId(ao.getAttributeValue());

                }
            }
        }
        item.setContentURL(getLocalizedContent(imageNode, pathToLocalizedValue));
        item.setResourceId(getLocalizedContent(uuidNode, pathToLocalizedValue));
        return item;
    }

    /**
     * Parses bbox element for coordinate tags if available:
            <gmd:westBoundLongitude>
                <gco:Decimal>24.78279528</gco:Decimal>
            </gmd:westBoundLongitude>
            <gmd:southBoundLatitude>
                <gco:Decimal>59.92248873</gco:Decimal>
            </gmd:southBoundLatitude>
            <gmd:eastBoundLongitude>
                <gco:Decimal>25.25448506</gco:Decimal>
            </gmd:eastBoundLongitude>
            <gmd:northBoundLatitude>
                <gco:Decimal>60.29783894</gco:Decimal>
            </gmd:northBoundLatitude>
     */
    private void setupBBox(final SearchResultItem item, final OMElement bbox) {
        if(bbox == null) {
            return;
        }
        Iterator<OMElement> elems = bbox.getChildElements();
        while(elems.hasNext()) {
            OMElement elem = elems.next();
            if("westBoundLongitude".equals(elem.getLocalName())) {
                item.setWestBoundLongitude(getText(elem.getFirstElement()));
            }
            else if("southBoundLatitude".equals(elem.getLocalName())) {
                item.setSouthBoundLatitude(getText(elem.getFirstElement()));
            }
            else if("eastBoundLongitude".equals(elem.getLocalName())) {
                item.setEastBoundLongitude(getText(elem.getFirstElement()));
            }
            else if("northBoundLatitude".equals(elem.getLocalName())) {
                item.setNorthBoundLatitude(getText(elem.getFirstElement()));
            }
        }
    }

    /**
     * Parses locale references from feature so we can map localized names to locale IDs
  <gmd:locale>
      <gmd:PT_Locale xmlns:xlink="http://www.w3.org/1999/xlink"
                     xmlns:srv="http://www.isotc211.org/2005/srv"
                     xmlns:gmx="http://www.isotc211.org/2005/gmx"
                     id="EN">
         <gmd:languageCode>
            <gmd:LanguageCode codeList="" codeListValue="eng"/>
         </gmd:languageCode>
         <gmd:characterEncoding>
            <gmd:MD_CharacterSetCode codeList="" codeListValue=""/>
         </gmd:characterEncoding>
      </gmd:PT_Locale>
  </gmd:locale>
  <gmd:locale>
      <gmd:PT_Locale xmlns:xlink="http://www.w3.org/1999/xlink"
                     xmlns:srv="http://www.isotc211.org/2005/srv"
                     xmlns:gmx="http://www.isotc211.org/2005/gmx"
                     id="SW">
         <gmd:languageCode>
            <gmd:LanguageCode codeList="" codeListValue="swe"/>
         </gmd:languageCode>
         <gmd:characterEncoding>
            <gmd:MD_CharacterSetCode codeList="" codeListValue=""/>
         </gmd:characterEncoding>
      </gmd:PT_Locale>
  </gmd:locale>
     */
    private Map<String, String> getLocaleMap(final OMElement elem) {
        final Map<String, String> locales = new HashMap<String, String>();
        try {
            final List<OMElement> localeNodes = (List<OMElement>) XPATH_LOCALE_MAP.selectNodes(elem);
            for(OMElement loc : localeNodes) {
                final String localeKey = loc.getAttributeValue(QNAME_ID);
                // Note! assuming only one exists
                final OMElement langCode = loc.getFirstChildWithName(QNAME_lANGUAGECODE);
                final OMElement theLangCode = langCode.getFirstChildWithName(QNAME_THE_LANGUAGECODE);
                final String lang3letter = theLangCode.getAttributeValue(QNAME_CODELISTVALUE);
                // value is 3-letter code -> transform to 2-letter code since oskari lang is the 2-letter code
                final String lang = ISO3letterOskariLangMapping.get(lang3letter);
                if(lang != null) {
                    locales.put(lang, localeKey);
                }
                else {
                    log.warn("Failed to find locale mapping for:", lang3letter);
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing locales:", e.getMessage());
        }
        return locales;
    }
//
    /**
     * Tries to find a localized version of the property value and returns the default if localized value is not available.
     * Note that the locale-property value is an ID reference so we need to use #getLocaleMap() to map it to Oskari-language
         <gmd:organisationName xsi:type="gmd:PT_FreeText_PropertyType">
            <gco:CharacterString>Tilastokeskus</gco:CharacterString>
            <gmd:PT_FreeText>
               <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#SW">Statistikcentralen</gmd:LocalisedCharacterString>
               </gmd:textGroup>
               <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#EN">Statistics Finland</gmd:LocalisedCharacterString>
               </gmd:textGroup>
            </gmd:PT_FreeText>
         </gmd:organisationName>
     */
    private String getLocalizedContent(final OMElement elem, final AXIOMXPath pathToLocaledValue) {
        final String defaultContent = getText(elem);
        if(elem == null || pathToLocaledValue == null) {
            return defaultContent;
        }
        try {
            final OMElement localeNode = (OMElement) pathToLocaledValue.selectSingleNode(elem);
            final String localized = getText(localeNode);
            if(localized != null && !localized.isEmpty()) {
                return localized;
            }
        } catch (Exception e) {
            log.warn("Error parsing localized value for:", elem.getLocalName(), ". Message:", e.getMessage());
        }

        return defaultContent;
    }

    /**
     * Returns text content or null if element is null
     * @param elem
     * @return
     */
    private String getAttributeValue(final OMElement elem, final QName attributeName) {
        String text = null;
        if(elem != null) {
            text = elem.getAttributeValue(attributeName);
        }
        return text;
    }

    /**
     * Returns text content or null if element is null
     * @param elem
     * @return
     */
    private String getText(final OMElement elem) {
        String text = null;
        if(elem != null) {
            text = elem.getText();
        }
        return text;
    }
}
