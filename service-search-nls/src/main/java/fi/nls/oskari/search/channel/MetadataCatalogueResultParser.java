package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.SimpleNamespaceContext;

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

    private AXIOMXPath XPATH_DISTINFO = null;

    private AXIOMXPath XPATH_FILEID = null;

    private AXIOMXPath XPATH_LOCALE_MAP = null;

    private SimpleNamespaceContext NAMESPACE_CTX = null;

    private final QName QNAME_ID = QName.valueOf("id");
    private final QName QNAME_CODELISTVALUE = QName.valueOf("codeListValue");
    // naming ftw (notice capital L in the second LanguageCode tag...)
    private final QName QNAME_lANGUAGECODE = new QName("http://www.isotc211.org/2005/gmd","languageCode", "gmd");
    private final QName QNAME_THE_LANGUAGECODE = new QName("http://www.isotc211.org/2005/gmd","LanguageCode", "gmd");

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
        XPATH_IDENTIFICATION = getXPath("./gmd:identificationInfo/*[local-name()='MD_DataIdentification' or local-name()='SV_ServiceIdentification']");

        XPATH_IDENTIFICATION_TITLE = getXPath("./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        XPATH_IDENTIFICATION_DESC = getXPath("./gmd:abstract/gco:CharacterString");
        XPATH_IDENTIFICATION_IMAGE = getXPath("./gmd:graphicOverview[position()=last()]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString");
        XPATH_IDENTIFICATION_ORGANIZATION = getXPath("./gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        // extend can be gmd or srv namespaced
        XPATH_IDENTIFICATION_BBOX = getXPath("./*[local-name()='extent']/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox");

        XPATH_DISTINFO = getXPath("./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");

        XPATH_FILEID = getXPath("./gmd:fileIdentifier/gco:CharacterString");

        XPATH_LOCALE_MAP = getXPath("./gmd:locale/gmd:PT_Locale");

        if(ISO3letterOskariLangMapping.isEmpty()) {
            final String[] languages = Locale.getISOLanguages();
            for (String language : languages) {
                Locale locale = new Locale(language);
                log.debug("Adding mapping:", locale.getISO3Language(), " -> ", locale.getLanguage());
                ISO3letterOskariLangMapping.put(locale.getISO3Language(), locale.getLanguage());
            }
        }
    }

    private AXIOMXPath getXPath(final String str) {
        try {
            AXIOMXPath xpath = new AXIOMXPath(str);
            xpath.setNamespaceContext(NAMESPACE_CTX);
            return xpath;
        } catch (Exception ex) {
            log.error(ex, "Error creating xpath:", str);
        }
        return null;
    }

    public SearchResultItem parseResult(final OMElement elem, final String locale) throws Exception {
        //final String locale = "fi";
        final Map<String, String> locales = getLocaleMap(elem);
        AXIOMXPath pathToLocalizedValue = null;
        if(locales != null && locales.containsKey(locale)) {
            pathToLocalizedValue = getXPath("../gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#" + locales.get(locale) + "']");
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
    private String getText(final OMElement elem) {
        if(elem != null) {
            return elem.getText();
        }
        return null;
    }
}