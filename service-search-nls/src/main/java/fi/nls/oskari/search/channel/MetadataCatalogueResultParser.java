package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.SimpleNamespaceContext;

import java.util.Iterator;

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

    private SimpleNamespaceContext NAMESPACE_CTX = null;

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
  -> setActionURL(fetchPageURLs.get(locale) + uuid);
  -> setContentURL(imageURLs.get(locale) + "uuid=" + uuid + "&fname=" + imageFileName) IF imageurl doesn't start with http://

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

    public SearchResultItem parseResult(final OMElement elem) throws Exception {
        final OMElement idNode = (OMElement) XPATH_IDENTIFICATION.selectSingleNode(elem);

        final SearchResultItem item = new SearchResultItem();
        final OMElement titleNode = (OMElement) XPATH_IDENTIFICATION_TITLE.selectSingleNode(idNode);
        item.setTitle(getText(titleNode));
        final OMElement descNode = (OMElement) XPATH_IDENTIFICATION_DESC.selectSingleNode(idNode);
        item.setDescription(getText(descNode));
        final OMElement imageNode = (OMElement) XPATH_IDENTIFICATION_IMAGE.selectSingleNode(idNode);
        item.setContentURL(getText(imageNode));
        final OMElement orgNode = (OMElement) XPATH_IDENTIFICATION_ORGANIZATION.selectSingleNode(idNode);
        item.addValue(MetadataField.RESULT_KEY_ORGANIZATION, getText(orgNode));

        final OMElement bboxNode = (OMElement) XPATH_IDENTIFICATION_BBOX.selectSingleNode(idNode);
        setupBBox(item, bboxNode);

        final OMElement distInfoNode = (OMElement) XPATH_DISTINFO.selectSingleNode(elem);
        item.setGmdURL(getText(distInfoNode));
        final OMElement uuidNode = (OMElement) XPATH_FILEID.selectSingleNode(elem);
        item.setResourceId(getText(uuidNode));

        return item;
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
}