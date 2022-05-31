package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.ontology.service.KeywordService;
import fi.nls.oskari.ontology.service.KeywordServiceMybatisImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 7.11.2013
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class GetLayerKeywords {
    private static final String CSW_NS = "http://www.opengis.net/cat/csw/2.0.2";
    private static final String DC_NS = "http://purl.org/dc/elements/1.1/";
    private static final String DCT_NS = "http://purl.org/dc/terms/";
    private static final String GMD_NS = "http://www.isotc211.org/2005/gmd";
    private static final String GCO_NS = "http://www.isotc211.org/2005/gco";
    private static final String SRV_NS = "http://www.isotc211.org/2005/srv";

    private Map<String, URI> nsmap = new HashMap<String, URI>();
    private Map<String, Locale> localeMap = null;
    private Map<String, List<String>> countryMap = null;
    private String baseUrl; //  http://www.paikkatietohakemisto.fi/geonetwork/srv/fi/iso19139.xml

    private static Logger log = LogFactory.getLogger(GetLayerKeywords.class);

    public void updateLayerKeywords(Integer layerId, String uuid) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, URISyntaxException, TransformerException {
        if (layerId == null) {
            //log.warn("No layerId, skipping");
            return;
        }
        if (uuid == null || uuid.isEmpty()) {
            //log.warn("No UUID for layer " + layerId + ", skipping");
            return;
        }
        // fetch layer metadata
        Document doc = getLayerData(layerId, uuid, getBaseUrl());
        if (doc != null) {
            // extract keywords from metadata
            List<Keyword> keywords = parseLayerKeywords(layerId, uuid, doc);
            // shove keywords + relations to db
            saveKeywords(layerId, keywords);
        }
    }

    private void initLocaleMapping() {
        String[] languages = Locale.getISOLanguages();
        localeMap = new HashMap<String, Locale>(languages.length);
        for (String language : languages) {
            Locale locale = new Locale(language);
            localeMap.put(locale.getISO3Language().toLowerCase(), locale);
        }
        countryMap = new HashMap<String, List<String>>();
        Locale[] locales = Locale.getAvailableLocales();
        List<String> localeLanguages;
        for (Locale locale : locales) {
            localeLanguages = countryMap.get(locale.getCountry());
            if (localeLanguages == null) {
                localeLanguages = new ArrayList<String>();
            }
            localeLanguages.add(locale.getLanguage());
            countryMap.put(locale.getCountry(), localeLanguages);
        }
    }

    private String getISO2Language(String iso3Language) {
        if (localeMap == null) {
            initLocaleMapping();
        }
        Locale locale = localeMap.get(iso3Language.toLowerCase());
        String iso2Language = null;
        if (locale != null) {
            iso2Language = locale.getLanguage().toLowerCase();
        }
        return iso2Language;
    }

    private String getBaseUrl() {
        if (baseUrl == null) {
            //baseUrl = PropertyUtil.getNecessary("baseUrl");
            //baseUrl = "http://www.paikkatietohakemisto.fi/geonetwork/srv/fi/iso19139.xml";
            baseUrl = "http://geonetwork.nls.fi/geonetwork/srv/fi/iso19139.xml";
        }
        return baseUrl;
    }

    public MetadataNamespaceContext getNsContext() throws URISyntaxException {
        return new MetadataNamespaceContext();
    }

    private List<Keyword> parseLayerKeywords(Integer layerId, String uuid, Document doc) throws XPathExpressionException, URISyntaxException, TransformerException {
        Element root = doc.getDocumentElement();
        if (root == null) {
            throw new RuntimeException("Root is null");
        }
        List<Keyword> keywords = new ArrayList<Keyword>();
        /* Set up XPath */
        nsmap.put("gmd", new URI(GMD_NS));
        nsmap.put("gco", new URI(GCO_NS));
        nsmap.put("csw", new URI(CSW_NS));
        nsmap.put("dc", new URI(DC_NS));
        nsmap.put("dct", new URI(DCT_NS));
        nsmap.put("srv", new URI(SRV_NS));
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(getNsContext());
		/* Default locale XPath */
        String defaultLang = null;
        Node defaultLangElement = (Node) xpath.compile("gmd:MD_Metadata/gmd:language").evaluate(doc, XPathConstants.NODE);
        if (defaultLangElement != null) {
            defaultLang = (String) xpath.compile("gco:CharacterString/text()").evaluate(defaultLangElement, XPathConstants.STRING);
            if (defaultLang == null) {
                defaultLang = (String) xpath.compile("gmd:LanguageCode/@codeListValue").evaluate(defaultLangElement, XPathConstants.STRING);
            }
            defaultLang = getISO2Language(defaultLang);
        }
		/* Locale XPath */
        NodeList localeElements = (NodeList) xpath.compile("gmd:MD_Metadata/gmd:locale/gmd:PT_Locale").evaluate(doc, XPathConstants.NODESET); // locale element
        XPathExpression xpathLocaleId = xpath.compile("@id"); // locale ID, referred to in localized strings (in locale element)
        XPathExpression xpathLocaleLangCodeElement = xpath.compile("gmd:languageCode/gmd:LanguageCode"); // language code element (in locale element)
        XPathExpression xpathLocaleCodeList = xpath.compile("@codeList"); // language code format (in language code element)
        XPathExpression xpathLocaleCodeListValue = xpath.compile("@codeListValue"); // language code (in language code element)
        Node localeElement, langCodeElement;
        Map<String, String> localeMapping = new HashMap<String, String>();
        for (int i = 0; i < localeElements.getLength(); i++) {
            localeElement = localeElements.item(i);
            langCodeElement = (Node) xpathLocaleLangCodeElement.evaluate(localeElement, XPathConstants.NODE);
            String localeId = (String) xpathLocaleId.evaluate(localeElement, XPathConstants.STRING);
            //String localeCodeList = (String) xpathLocaleCodeList.evaluate(langCodeElement, XPathConstants.STRING); // Not used ATM, but locales could come in something else than ISO3...
            String localeCodeListValue = (String) xpathLocaleCodeListValue.evaluate(langCodeElement, XPathConstants.STRING);
            localeMapping.put(localeId, getISO2Language(localeCodeListValue));
        }

        if (defaultLang == null) {
            // can't find/recognize default lang, see if our default lang is in use
            if (localeMapping.get(PropertyUtil.getDefaultLanguage().toLowerCase()) == null) {
                // make an educated quess that the default language is the same as ours...
                defaultLang = PropertyUtil.getDefaultLanguage().toLowerCase();
                /* Too common, don't log
                log.warn("Default language for " + uuid + " not found, using " + defaultLang);*/
            } else {
                log.warn("Default language for " + layerId + " not found and" + defaultLang + " is already in use, ignoring default lang keywords");
            }
        }

		/* Keyword XPath */
        NodeList keywordElements = (NodeList) xpath.compile(
                "gmd:MD_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword").evaluate(doc, XPathConstants.NODESET); // keyword element
        XPathExpression xpathKeywordInDefaultLanguage = xpath.compile("gco:CharacterString/text()"); // keyword in default language
        XPathExpression xpathKeywordLocalizedElement = xpath.compile("gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"); //localized keyword element
        XPathExpression xpathKeywordLocalizedLocaleReference = xpath.compile("@locale"); // locale ID reference
        XPathExpression xpathKeywordLocalized = xpath.compile("text()"); // localized keyword
        NodeList localizedKeywords;
        Node keyword, localizedKeyword;
        Keyword kw;
        String locale, localeId;
        for (int i = 0; i < keywordElements.getLength(); i++) {
            keyword = keywordElements.item(i);
            String kwDefault = (String) xpathKeywordInDefaultLanguage.evaluate(keyword, XPathConstants.STRING);
            if (defaultLang != null && kwDefault != null && !kwDefault.isEmpty()) {
                kw = new Keyword();
                kw.setLang(defaultLang);
                kw.setValue(kwDefault);
                keywords.add(kw);
            }
            localizedKeywords = (NodeList) xpathKeywordLocalizedElement.evaluate(keyword, XPathConstants.NODESET);
            Set<String> supportedLanguages = new HashSet<String>(Arrays.asList(PropertyUtil.getSupportedLanguages()));
            for (int j = 0; j < localizedKeywords.getLength(); j++) {
                localizedKeyword = localizedKeywords.item(j);
                localeId = (String) xpathKeywordLocalizedLocaleReference.evaluate(localizedKeyword, XPathConstants.STRING);
                if (localeId != null && localeId.length() > 1) {
                    locale = localeMapping.get(localeId.substring(1));
                    if (locale == null) {
                        if (localeId.length() == 3) {
                            // FIXME check that this is a valid locale...
                            locale = localeId.substring(1).toLowerCase();
                            if (supportedLanguages.contains(locale)) {
                                log.warn("Invalid locale reference for layer " + layerId + ": " + localeId + ", using locale ID as language: " + locale);
                            } else {
                                log.warn("Invalid locale reference for layer " + layerId + ": " + localeId);
                                locale = null;
                            }
                        } else if (localeId.indexOf("#locale-") == 0) {
                            locale = getISO2Language(localeId.substring(8));
                            if (supportedLanguages.contains(locale)) {
                                log.warn("Invalid locale reference for layer " + layerId + ": " + localeId + ", using part of locale ID to get language: " + locale);
                            } else {
                                log.warn("Invalid locale reference for layer " + layerId + ": " + localeId);
                                locale = null;
                            }
                        } else {
                            log.warn("Invalid locale reference for layer " + layerId + ": " + localeId);
                        }
                    }
                    if (locale != null) {
                        String kwInLocale = (String) xpathKeywordLocalized.evaluate(localizedKeyword, XPathConstants.STRING);
                        if (kwInLocale != null && !kwInLocale.isEmpty()) {
                            kw = new Keyword();
                            kw.setLang(locale);
                            kw.setValue(kwInLocale);
                            keywords.add(kw);
                        }
                    }
                }
            }
        }
        return keywords;
    }

    private Document getLayerData(Integer layerId, String uuid, String baseUrl) {
        URL metadataURL = null;
        try {
            metadataURL = new URL(baseUrl + "?uuid=" + uuid.trim());
        } catch (MalformedURLException mfe) {
            log.warn("Couldn't create metadata URL for layer " + layerId);
            return null;
        }
        DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.warn("Couldn't init documentbuilder");
            return null;
        }
        Document doc = null;
        try {
            doc = builder.parse(metadataURL.openStream());
        } catch (SAXException e) {
            log.warn("Failed to parse metadata for layer " + layerId);
            return null;
        } catch (IOException e) {
            log.warn("Failed to fetch metadata for layer " + layerId + ":", e.getMessage());
            return null;
        }
        log.warn("Got layer data for layer " + layerId + " with uuid " + uuid);
        return doc;
    }

    private void saveKeywords(Integer layerId, List<Keyword> keywords) {
		KeywordService keywordService = new KeywordServiceMybatisImpl();
		Long keywordId;
		for (Keyword keyword : keywords) {
			// This'll return the keyword ID whether it was created or already existed
		    keywordId = keywordService.addKeyword(keyword);
			keywordService.linkKeywordToLayer(keywordId, new Long(layerId));
		}
    }

    public class MetadataNamespaceContext implements NamespaceContext {

        public MetadataNamespaceContext() throws URISyntaxException {

        }

        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new NullPointerException("Null prefix");
            } else if ("xml".equals(prefix)) {
                return XMLConstants.NULL_NS_URI;
            }
            return nsmap.get(prefix).toString();

        }

        // This method isn't necessary for XPath processing.
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        // This method isn't necessary for XPath processing either.
        public Iterator<String> getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }

    }
}
