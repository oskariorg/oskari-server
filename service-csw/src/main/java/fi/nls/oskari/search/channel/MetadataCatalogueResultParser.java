package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.oskari.xml.XmlHelper;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Helper class for parsing search results for MetadataCatalogue:
 *
 */
public class MetadataCatalogueResultParser {

    public static final String KEY_IDENTIFICATION = "identification";
    public static final String KEY_IDENTIFICATION_DATE = "date";
    public static final String KEY_IDENTIFICATION_CODELIST = "code";
    public static final String KEY_NATUREOFTHETARGET = "natureofthetarget";
    // we need to map languages from 3-letter codes to 2-letter codes so initialize a global codeMapping property
    private final static Map<String, String> ISO3letterOskariLangMapping = new HashMap<>();

    public MetadataCatalogueResultParser() {
        if(ISO3letterOskariLangMapping.isEmpty()) {
            final String[] languages = Locale.getISOLanguages();
            for (String language : languages) {
                Locale locale = new Locale(language);
                ISO3letterOskariLangMapping.put(locale.getISO3Language(), locale.getLanguage());
            }
        }
    }

    public SearchResultItem parseResult(final Element elem) throws Exception {
        final SearchResultItem item = new SearchResultItem();
        // id / uuid
        String uuid = XmlHelper.getChildValue(
                XmlHelper.getFirstChild(elem, "fileIdentifier"),
                "CharacterString");
        item.setResourceId(uuid);
        // lang
        Element languageCode = XmlHelper.getFirstChild(
                XmlHelper.getFirstChild(elem, "language"),
                "LanguageCode");
        String lang3code = XmlHelper.getAttributeValue(languageCode, "codeListValue");
        item.setLang(ISO3letterOskariLangMapping.getOrDefault(lang3code, lang3code));

        // hierarchyLevel
        Element MD_ScopeCode = XmlHelper.getFirstChild(
                XmlHelper.getFirstChild(elem, "hierarchyLevel"),
                "MD_ScopeCode");
        String codeListValue = XmlHelper.getAttributeValue(MD_ScopeCode, "codeListValue");
        if (codeListValue != null) {
            item.setNatureOfTarget(codeListValue);
            item.addValue(KEY_NATUREOFTHETARGET, item.getNatureOfTarget());
        }
        String datasetType = XmlHelper.getChildValue(
                XmlHelper.getFirstChild(elem, "hierarchyLevelName"),
                "CharacterString");
        item.setType(datasetType);
/*
// 2021-12-10T20:16:47
        String timestamp = XmlHelper.getChildValue(
                XmlHelper.getFirstChild(elem, "dateStamp"),
                "DateTime");
  */
        Element idInfo = XmlHelper.getFirstChild(elem, "identificationInfo");
        Element dataIdentity = XmlHelper.getFirstChild(idInfo,"MD_DataIdentification");
        if (dataIdentity == null) {
            dataIdentity = XmlHelper.getFirstChild(idInfo, "SV_ServiceIdentification");
        }
        //
        Element citation = XmlHelper.getFirstChild(
                XmlHelper.getFirstChild(dataIdentity, "citation"),
                "CI_Citation");
        String title = XmlHelper.getChildValue(
                XmlHelper.getFirstChild(citation, "title"),
                "CharacterString");
        item.setTitle(title);

        Element CI_Date = XmlHelper.getFirstChild(
                XmlHelper.getFirstChild(citation, "date"),
                "CI_Date");
        // 2020-01-01
        String date = XmlHelper.getChildValue(
                XmlHelper.getFirstChild(CI_Date, "date"),
                "Date");
        // publication
        String dateType = XmlHelper.getAttributeValue(
                XmlHelper.getFirstChild(
                        XmlHelper.getFirstChild(CI_Date, "dateType"), "CI_DateTypeCode"),
                "codeListValue");

        JSONObject identification = new JSONObject();
        identification.put(KEY_IDENTIFICATION_CODELIST, dateType);
        identification.put(KEY_IDENTIFICATION_DATE, date);
        item.addValue(KEY_IDENTIFICATION, identification);

        item.setDescription(XmlHelper.getChildValue(
                XmlHelper.getFirstChild(dataIdentity, "abstract"),
                "CharacterString"));

        // organization
        Element CI_ResponsibleParty = XmlHelper.getFirstChild(
                XmlHelper.getFirstChild(dataIdentity, "pointOfContact"),
                "CI_ResponsibleParty");
        String org = XmlHelper.getChildValue(
                XmlHelper.getFirstChild(CI_ResponsibleParty, "organisationName"),
                "CharacterString");
        item.addValue(MetadataField.RESULT_KEY_ORGANIZATION, org);

        // bbox
        Element EX_Extent = XmlHelper.getFirstChild(
                XmlHelper.getFirstChild(dataIdentity, "extent"),
                "EX_Extent");

        Element bbox = XmlHelper.getFirstChild(
                XmlHelper.getFirstChild(EX_Extent, "geographicElement"),
                "EX_GeographicBoundingBox");
        setupBBox(item, bbox);
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
    private void setupBBox(final SearchResultItem item, final Element bbox) {
        if (bbox == null) {
            return;
        }
        item.setWestBoundLongitude(XmlHelper.getChildValue(
                XmlHelper.getFirstChild(bbox, "westBoundLongitude"),
                "Decimal"));
        item.setEastBoundLongitude(XmlHelper.getChildValue(
                XmlHelper.getFirstChild(bbox, "eastBoundLongitude"),
                "Decimal"));

        item.setSouthBoundLatitude(XmlHelper.getChildValue(
                XmlHelper.getFirstChild(bbox, "southBoundLatitude"),
                "Decimal"));

        item.setNorthBoundLatitude(XmlHelper.getChildValue(
                XmlHelper.getFirstChild(bbox, "northBoundLatitude"),
                "Decimal"));
    }
}
