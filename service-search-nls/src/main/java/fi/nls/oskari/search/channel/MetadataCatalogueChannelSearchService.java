package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.util.GeonetworkSpatialOperation;
import fi.nls.oskari.util.PropertyUtil;
import org.deegree.datatypes.QualifiedName;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.filterencoding.*;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.csw.discovery.*;
import org.deegree.ogcwebservices.csw.discovery.GetRecords.RESULT_TYPE;
import org.deegree.ogcwebservices.csw.discovery.XMLFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MetadataCatalogueChannelSearchService implements SearchableChannel {

    private final static String PROPERTY_IS_LIKE_OPERATION = "PROPERTY_IS_LIKE_OPERATION";
    private final static String PROPERTY_IS_COMP_OPERATION = "PROPERTY_IS_COMP_OPERATION";
    private final static String SPATIAL_OPERATION = "SPATIAL_OPERATION";
    private static final String GCO_NAMESPACE = "gco";
    private static final String GMD_NAMESPACE = "gmd";
    // Some have inline name spacing, so we're using local name...
    private static final String MD_METADATA_EXPRESSION =
            "./*[local-name()='MD_Metadata']";
    // root for title, description, imageFileName and bounding box
    private static final String IDENTIFICATION_EXPRESSION =
            "./" + GMD_NAMESPACE + ":identificationInfo/*[local-name()='MD_DataIdentification' or local-name()='SV_ServiceIdentification']";
    private static final String TITLE_EXPRESSION =
            "./" + GMD_NAMESPACE + ":citation/" + GMD_NAMESPACE + ":CI_Citation/" + GMD_NAMESPACE + ":title";
    private static final String DESCRIPTION_EXPRESSION =
            "./" + GMD_NAMESPACE + ":abstract";
    private static final String IMAGE_FILE_NAME_EXPRESSION =
            "./" + GMD_NAMESPACE + ":graphicOverview[position()=last()]/" + GMD_NAMESPACE + ":MD_BrowseGraphic/" + GMD_NAMESPACE + ":fileName";
    // root for west, south, east and north
    private static final String BBOX_EXPRESSION =
            // Someone is serving extent with srv namespace...
            "./*[local-name()='extent']/" + GMD_NAMESPACE + ":EX_Extent/" + GMD_NAMESPACE + ":geographicElement/" + GMD_NAMESPACE + ":EX_GeographicBoundingBox";
    private static final String WEST_EXPRESSION =
            "./" + GMD_NAMESPACE + ":westBoundLongitude/" + GCO_NAMESPACE + ":Decimal/text()";
    private static final String SOUTH_EXPRESSION =
            "./" + GMD_NAMESPACE + ":southBoundLatitude/" + GCO_NAMESPACE + ":Decimal/text()";
    private static final String EAST_EXPRESSION =
            "./" + GMD_NAMESPACE + ":eastBoundLongitude/" + GCO_NAMESPACE + ":Decimal/text()";
    private static final String NORTH_EXPRESSION =
            "./" + GMD_NAMESPACE + ":northBoundLatitude/" + GCO_NAMESPACE + ":Decimal/text()";
    // TODO schema allows [0..N] transferOptions :P We should handle the possibility of multiple onlineResources somehow
    // root for downloadable and gmdURL, replaced localName="" with transferOptions... schema seems to only allow gmd:transferOptions there?
    private static final String ONLINE_RESOURCE_EXPRESSION =
            "./" + GMD_NAMESPACE + ":distributionInfo/" + GMD_NAMESPACE + ":MD_Distribution/" + GMD_NAMESPACE + ":transferOptions/" + GMD_NAMESPACE + ":MD_DigitalTransferOptions/" + GMD_NAMESPACE + ":onLine/" + GMD_NAMESPACE + ":CI_OnlineResource";
    private static final String DOWNLOADABLE_EXPRESSION =
            "boolean(./" + GMD_NAMESPACE + ":protocol)";
    private static final String GMD_URL_EXPRESSION =
            "./" + GMD_NAMESPACE + ":linkage/" + GMD_NAMESPACE + ":URL/text()";
    private static final String UUID_EXPRESSION =
            "./" + GMD_NAMESPACE + ":fileIdentifier";
    public static final String ID = "METADATA_CATALOGUE_CHANNEL";
    private final static char WILDCARD_CHARACTER = '*';
    private final Logger log = LogFactory.getLogger(this.getClass());
    private String queryURL = null;
    private String serverURL = null;
    private final Map<String, String> imageURLs = new HashMap<String, String>();
    private final Map<String, String> fetchPageURLs = new HashMap<String, String>();

    /**
     * @param xpath    XPath instance
     * @param rootNode SearchResults node containing the string in one format or another
     * @param locales  List of locales in order of preference
     * @return Localized string if available
     * @throws XPathExpressionException
     */
    private static String getLocalizedString(XPath xpath, Node rootNode, List<String> locales) throws XPathExpressionException {
        final String getGenericCharacterStringExpression = "./" + GCO_NAMESPACE + ":CharacterString/text()";
        final String getLocalizedCharacterStringsExpression =
                "./" + GMD_NAMESPACE + ":PT_FreeText/" + GMD_NAMESPACE + ":textGroup/" + GMD_NAMESPACE + ":LocalisedCharacterString";
        String localizedString = (String) xpath.evaluate(getGenericCharacterStringExpression, rootNode, XPathConstants.STRING);
        if (localizedString != null && !localizedString.isEmpty()) {
            return localizedString;
        }
        // Value is localized, get all locales in case we don't find ours...
        Map<String, String> localizedContentValues = new HashMap<String, String>();
        NodeList nodes = (NodeList) xpath.evaluate(getLocalizedCharacterStringsExpression, rootNode, XPathConstants.NODESET);
        for (int i = 0, j = nodes.getLength(); i < j; i++) {
            Node textGroupChild = nodes.item(i);
            Node localeAttrValue = textGroupChild.getAttributes().getNamedItem("locale");

            if (localeAttrValue != null) {
                localizedContentValues.put(
                        localeAttrValue.getTextContent().toLowerCase(),
                        textGroupChild.getTextContent());
            }
        }

        // FIXME this seems to be rather wrong. locales are marked thus: locale="#locale-eng", we're looking for locale="#en"

        String locale = locales.get(0);

        // try to find a perfect match
        String returnValue = localizedContentValues.get("#" + locale);

        if (returnValue != null && !"".equals(returnValue)) {
            return returnValue;
        }

        // try to find some match in provided locales
        for (String matchLocale : locales) {
            if (!locale.equals(matchLocale)) {
                returnValue = localizedContentValues.get("#" + matchLocale);

                if (returnValue != null && !"".equals(returnValue)) {
                    // log.warn("No character string value found for locale '" + locale
                    //       + "'. Returning value for locale '" + localeOrderArray[j] + "': " + returnValue);
                    return returnValue;
                }
            }
        }

        // return something non-empty if possible
        for (String key : localizedContentValues.keySet()) {
            returnValue = localizedContentValues.get(key);

            if (returnValue != null && !"".equals(returnValue)) {
                // log.warn("No character string value found for locale '" + locale
                //       + "'. Returning value for locale '" + key + "': " + returnValue);
                return returnValue;
            }
        }

        return localizedString;
    }

    /**
     * Creates and initializes an instance of XPath
     * @return XPath instance with namespaces set.
     */
    private static XPath getXPath() {
        // init xpath
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        // Add namespaces. It would be nice to get this from the xml, but that would be hard.
        xpath.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                if (prefix == null) {
                    throw new NullPointerException("Null prefix");
                } else if (GCO_NAMESPACE.equals(prefix)) {
                    return "http://www.isotc211.org/2005/gco";
                } else if (GMD_NAMESPACE.equals(prefix)) {
                    return "http://www.isotc211.org/2005/gmd";
                } else if ("srv".equals(prefix)) {
                    return "http://www.isotc211.org/2005/srv";
                }
                return XMLConstants.NULL_NS_URI;
            }

            // This method isn't necessary for XPath processing.
            public String getPrefix(String uri) {
                throw new UnsupportedOperationException();
            }

            // This method isn't necessary for XPath processing either.
            public Iterator getPrefixes(String uri) {
                throw new UnsupportedOperationException();
            }
        });
        return xpath;
    }

    /**
     * Parses a single search result
     * @param xpath XPath instance with the proper namespaces set
     * @param rootNode MD_Metadata node
     * @param locale Locale from search criteria
     * @param locales List of locales in order of preference
     * @param fetchPageURLs ActionURLs
     * @param imageURLs BaseURLs for images
     * @param serverURL Resource namespace
     * @return SearchResultItem filled with the MD_Metadata node's contents
     * @throws XPathExpressionException
     */
    private static SearchResultItem parseSearchResultItem(XPath xpath, Node rootNode, String locale, List<String> locales, Map<String, String> fetchPageURLs, Map<String, String> imageURLs, String serverURL) throws XPathExpressionException {
        SearchResultItem searchResultItem = new SearchResultItem();
        if (rootNode == null) {
            return searchResultItem;
        }
        Node identificationNode = (Node) xpath.evaluate(
                IDENTIFICATION_EXPRESSION,
                rootNode,
                XPathConstants.NODE
        );
        String imageFileName = null;
        if (identificationNode != null) {

            Node titleNode = (Node) xpath.evaluate(
                    TITLE_EXPRESSION,
                    identificationNode,
                    XPathConstants.NODE
            );

            if (titleNode != null) {
                searchResultItem.setTitle(
                        getLocalizedString(xpath, titleNode, locales)
                );
            }
            Node descriptionNode = (Node) xpath.evaluate(
                    DESCRIPTION_EXPRESSION,
                    identificationNode,
                    XPathConstants.NODE
            );

            if (descriptionNode != null) {
                searchResultItem.setDescription(
                        getLocalizedString(xpath, descriptionNode, locales)
                );
            }

            Node imageFileNameNode = (Node) xpath.evaluate(
                    IMAGE_FILE_NAME_EXPRESSION,
                    identificationNode,
                    XPathConstants.NODE
            );

            if (imageFileNameNode != null) {
                imageFileName = getLocalizedString(xpath, imageFileNameNode, locales);
            }

            Node boundingBoxnode = (Node) xpath.evaluate(BBOX_EXPRESSION, identificationNode, XPathConstants.NODE);
            if (boundingBoxnode != null) {
                String west = (String) xpath.evaluate(WEST_EXPRESSION, boundingBoxnode, XPathConstants.STRING);
                searchResultItem.setWestBoundLongitude(west);

                String south = (String) xpath.evaluate(SOUTH_EXPRESSION, boundingBoxnode, XPathConstants.STRING);
                searchResultItem.setSouthBoundLatitude(south);

                String east = (String) xpath.evaluate(EAST_EXPRESSION, boundingBoxnode, XPathConstants.STRING);
                searchResultItem.setEastBoundLongitude(east);

                String north = (String) xpath.evaluate(NORTH_EXPRESSION, boundingBoxnode, XPathConstants.STRING);
                searchResultItem.setNorthBoundLatitude(north);
            }
        }

        Node onlineResourceNode = (Node) xpath.evaluate(ONLINE_RESOURCE_EXPRESSION, rootNode, XPathConstants.NODE);
        if (null != onlineResourceNode) {
            Boolean downloadable = (Boolean) xpath.evaluate(DOWNLOADABLE_EXPRESSION, onlineResourceNode, XPathConstants.BOOLEAN);
            if (downloadable) {
                searchResultItem.setDownloadable(true);
            }
            String gmdUrl = (String) xpath.evaluate(GMD_URL_EXPRESSION, onlineResourceNode, XPathConstants.STRING);
            searchResultItem.setGmdURL(gmdUrl);
        }
        Node uuidNode = (Node) xpath.evaluate(
                UUID_EXPRESSION,
                rootNode,
                XPathConstants.NODE
        );
        String uuid = null;
        if (uuidNode != null) {
            uuid = getLocalizedString(xpath, uuidNode, locales);
            searchResultItem.setActionURL(fetchPageURLs.get(locale) + uuid);
        }
        if (imageFileName != null && imageFileName.startsWith("http://")) {
            searchResultItem.setContentURL(imageFileName);
        } else if (uuid != null && !uuid.isEmpty() && imageFileName != null && !imageFileName.isEmpty()) {
            try {
                searchResultItem.setContentURL(
                        imageURLs.get(locale)
                                + "uuid=" + uuid + "&fname=" + imageFileName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get image url from property", e);
            }
        }

        if (uuid != null && !uuid.isEmpty()) {
            searchResultItem.setResourceId(uuid);
            try {
                searchResultItem.setResourceNameSpace(serverURL);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get geonetwork server url from property", e);
            }
        }
        return searchResultItem;
    }

    /**
     * @param node           Root node for xpath queries
     * @param searchCriteria Criteria for search, locale and such
     * @return List of search results
     * @throws XPathExpressionException
     */
    public static List<SearchResultItem> parseSearchResultItems(Node node, SearchCriteria searchCriteria, Map<String, String> fetchPageURLs, Map<String, String> imageURLs, String serverURL, List<String> locales) throws XPathExpressionException {
        List<SearchResultItem> searchResultItems = new ArrayList<SearchResultItem>();
        if (node == null) {
            return searchResultItems;
        }

        XPath xpath = getXPath();

        // get result elements
        NodeList mdMetadataNodes = (NodeList) xpath.evaluate(MD_METADATA_EXPRESSION, node, XPathConstants.NODESET);

        for (int i = 0; i < mdMetadataNodes.getLength(); i++) {
           searchResultItems.add(parseSearchResultItem(xpath, mdMetadataNodes.item(i), searchCriteria.getLocale(), locales, fetchPageURLs, imageURLs, serverURL));
        }

        return searchResultItems;
    }

    public void setProperty(String propertyName, String propertyValue) {
        if (null != propertyName) {
            if ("query.url".equals(propertyName)) {
                queryURL = propertyValue;
            } else if ("server.url".equals(propertyName)) {
                serverURL = propertyValue;
            } else if (propertyName.indexOf("image.url.") == 0) {
                imageURLs.put(propertyName.substring(10), propertyValue);
            } else if (propertyName.indexOf("fetchpage.url.") == 0) {
                fetchPageURLs.put(propertyName.substring(14), propertyValue);
            } else {
                log.warn("Unknown property for " + ID + " search channel: " + propertyName);
            }
        }
    }

    public String getId() {
        return ID;
    }

    public ChannelSearchResult doSearch(SearchCriteria searchCriteria)
            throws IllegalSearchCriteriaException {
        List<String> locales = new ArrayList<String>();
        locales.add(searchCriteria.getLocale());
        for (String supportedLocale : PropertyUtil.getSupportedLocales()) {
            String localeLang = supportedLocale.split("_")[0];
            if (!localeLang.matches(searchCriteria.getLocale())) {
                locales.add(localeLang);
            }
        }
        ChannelSearchResult searchResultList = readQueryData(searchCriteria, locales);
        searchResultList.setChannelId(getId());

        return searchResultList;
    }

    private ChannelSearchResult readQueryData(SearchCriteria searchCriteria, List<String> locales) {
        ChannelSearchResult channelSearchResult = new ChannelSearchResult();

        try {
            Node resultNode = makeQuery(searchCriteria);
            channelSearchResult.setSearchResultItems(parseSearchResultItems(resultNode, searchCriteria, fetchPageURLs, imageURLs, serverURL, locales));
            channelSearchResult.setQueryFailed(false);

        } catch (Exception x) {
            channelSearchResult.setException(x);
            channelSearchResult.setQueryFailed(true);
        }

        return channelSearchResult;
    }

    /* If search criterion is set, add to operations list */
    @SuppressWarnings("deprecation")
    private void addOperation(
            List<Operation> operations,
            String operationType,
            int operationId,
            String searchCriterion,
            String searchElementName) {
        if (searchCriterion != null
                && !"".equals(searchCriterion)
                && !"-1".equals(searchCriterion)
                && !"-2".equals(searchCriterion)
                && !"-3".equals(searchCriterion)
                && !"-4".equals(searchCriterion)
                && !"-5".equals(searchCriterion)
                && !"0".equals(searchCriterion)) {
            if (PROPERTY_IS_LIKE_OPERATION.equals(operationType)) {
                PropertyIsLikeOperation op = new PropertyIsLikeOperation(
                        new PropertyName(new QualifiedName(searchElementName)),
                        new Literal(searchCriterion), WILDCARD_CHARACTER, '?', '/');
                operations.add(op);
            } else if (PROPERTY_IS_COMP_OPERATION.equals(operationType)) {
                PropertyIsCOMPOperation op = new PropertyIsCOMPOperation(
                        operationId,
                        new PropertyName(new QualifiedName(searchElementName)),
                        new Literal(searchCriterion),
                        false);

                operations.add(op);
            } else if (SPATIAL_OPERATION.equals(operationType)) {
                CoordinateSystem crs = CRSFactory.createDummyCRS("EPSG:4326");

                try {
                    Geometry geom = WKTAdapter.wrap(searchCriterion, crs);
                    GeonetworkSpatialOperation op = new GeonetworkSpatialOperation(
                            OperationDefines.INTERSECTS,
                            new PropertyName(new QualifiedName(searchElementName)),
                            geom,
                            searchCriterion);
                    operations.add(op);
                } catch (GeometryException e) {
                    throw new RuntimeException("Failed create geometry.", e);
                }
            } else {
                throw new RuntimeException("Unknown operationType: '" + operationType + "'");
            }
        }
    }

    private List<Operation> getOperations(SearchCriteria searchCriteria) {
        List<Operation> operations = new ArrayList<Operation>();

        addOperation(
                operations,
                PROPERTY_IS_LIKE_OPERATION,
                -1,
                searchCriteria.getSearchString(),
                "csw:anyText");

        if (searchCriteria.getMetadataCatalogueSearchCriteria() != null) {
            addOperation(
                    operations,
                    PROPERTY_IS_LIKE_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getType(),
                    "gmd:hierarchyLevel");
            addOperation(
                    operations,
                    PROPERTY_IS_LIKE_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getInspireTheme(),
                    "keyword");
            addOperation(
                    operations,
                    PROPERTY_IS_LIKE_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getName(),
                    "gmd:title");
            addOperation(
                    operations,
                    PROPERTY_IS_COMP_OPERATION,
                    OperationDefines.PROPERTYISEQUALTO,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getKeyWord(),
                    "keyword");
            addOperation(
                    operations,
                    PROPERTY_IS_COMP_OPERATION,
                    OperationDefines.PROPERTYISEQUALTO,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getServiceType(),
                    "srv:serviceType");
            addOperation(
                    operations,
                    PROPERTY_IS_LIKE_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getTopicClass(),
                    "gmd:topicCategory");
            addOperation(
                    operations,
                    PROPERTY_IS_LIKE_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getResourceId(),
                    "ResourceIdentifier");
            addOperation(
                    operations,
                    PROPERTY_IS_LIKE_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getResponsibility(),
                    "orgName");
            addOperation(
                    operations,
                    SPATIAL_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getAreaId(),
                    "iso:BoundingBox");
            addOperation(
                    operations,
                    SPATIAL_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getBbox(),
                    "ows:boundingBox");
            addOperation(
                    operations,
                    PROPERTY_IS_COMP_OPERATION,
                    OperationDefines.PROPERTYISGREATERTHANOREQUALTO,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getSampleResolutionMin(),
                    "DistanceValue");
            addOperation(
                    operations,
                    PROPERTY_IS_COMP_OPERATION,
                    OperationDefines.PROPERTYISLESSTHANOREQUALTO,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getSampleResolutionMax(),
                    "DistanceValue");
            addOperation(
                    operations,
                    PROPERTY_IS_LIKE_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getAccessConstraints(),
                    "AccessConstraints");
            addOperation(
                    operations,
                    PROPERTY_IS_LIKE_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getSecurityClassification(),
                    "Classification");
            addOperation(
                    operations,
                    PROPERTY_IS_LIKE_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getRule(),
                    "SpecificationTitle");
            addOperation(
                    operations,
                    PROPERTY_IS_LIKE_OPERATION,
                    -1,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getConformanceDegree(),
                    "Degree");
            addOperation(
                    operations,
                    PROPERTY_IS_COMP_OPERATION,
                    OperationDefines.PROPERTYISGREATERTHANOREQUALTO,
                    getDateAsString(searchCriteria.getMetadataCatalogueSearchCriteria().getConformanceDateStart()),
                    "SpecificationDate");
            addOperation(
                    operations,
                    PROPERTY_IS_COMP_OPERATION,
                    OperationDefines.PROPERTYISLESSTHANOREQUALTO,
                    getDateAsString(searchCriteria.getMetadataCatalogueSearchCriteria().getConformanceDateEnd()),
                    "SpecificationDate");
            addOperation(
                    operations,
                    PROPERTY_IS_COMP_OPERATION,
                    OperationDefines.PROPERTYISEQUALTO,
                    searchCriteria.getMetadataCatalogueSearchCriteria().getConformanceDateType(),
                    "SpecificationDateType");

            if (searchCriteria.getMetadataCatalogueSearchCriteria().isShowOnlyDownloadable()) {
                addOperation(
                        operations,
                        PROPERTY_IS_LIKE_OPERATION,
                        -1,
                        WILDCARD_CHARACTER + "download" + WILDCARD_CHARACTER,
                        "protocol");
            }

            if (searchCriteria.getMetadataCatalogueSearchCriteria().getOtherConstraints() != null
                    && !"".equals(searchCriteria.getMetadataCatalogueSearchCriteria().getOtherConstraints())) {
                addOperation(
                        operations,
                        PROPERTY_IS_LIKE_OPERATION,
                        -1,
                        WILDCARD_CHARACTER
                                + searchCriteria.getMetadataCatalogueSearchCriteria().getOtherConstraints()
                                + WILDCARD_CHARACTER,
                        "OtherConstraints");
            }

            if (searchCriteria.getMetadataCatalogueSearchCriteria().getAccessConditions() != null
                    && !"".equals(searchCriteria.getMetadataCatalogueSearchCriteria().getAccessConditions())) {
                addOperation(
                        operations,
                        PROPERTY_IS_LIKE_OPERATION,
                        -1,
                        WILDCARD_CHARACTER
                                + searchCriteria.getMetadataCatalogueSearchCriteria().getAccessConditions()
                                + WILDCARD_CHARACTER,
                        "ConditionApplyingToAccessAndUse");
            }

            if (searchCriteria.getMetadataCatalogueSearchCriteria().isScaleSelected()) {
                addOperation(
                        operations,
                        PROPERTY_IS_COMP_OPERATION,
                        OperationDefines.PROPERTYISGREATERTHANOREQUALTO,
                        searchCriteria.getMetadataCatalogueSearchCriteria().getScaleMinDenominator(),
                        "Denominator");
                addOperation(
                        operations,
                        PROPERTY_IS_COMP_OPERATION,
                        OperationDefines.PROPERTYISLESSTHANOREQUALTO,
                        searchCriteria.getMetadataCatalogueSearchCriteria().getScaleMaxDenominator(),
                        "gmd:denominator");
            }

            if (searchCriteria.getMetadataCatalogueSearchCriteria().getTimeStart() != null &&
                    !"".equals(searchCriteria.getMetadataCatalogueSearchCriteria().getTimeStart())) {
                List<Operation> timeStartOperations = new ArrayList<Operation>();
                addOperation(
                        timeStartOperations,
                        PROPERTY_IS_COMP_OPERATION,
                        OperationDefines.PROPERTYISGREATERTHANOREQUALTO,
                        getDateAsString(searchCriteria.getMetadataCatalogueSearchCriteria().getTimeStart()),
                        "RevisionDate");
                addOperation(
                        timeStartOperations,
                        PROPERTY_IS_COMP_OPERATION,
                        OperationDefines.PROPERTYISGREATERTHANOREQUALTO,
                        getDateAsString(searchCriteria.getMetadataCatalogueSearchCriteria().getTimeStart()),
                        "CreationDate");
                addOperation(
                        timeStartOperations,
                        PROPERTY_IS_COMP_OPERATION,
                        OperationDefines.PROPERTYISGREATERTHANOREQUALTO,
                        getDateAsString(searchCriteria.getMetadataCatalogueSearchCriteria().getTimeStart()),
                        "PublicationDate");
                operations.add(new LogicalOperation(OperationDefines.OR, timeStartOperations));
            }

            if (searchCriteria.getMetadataCatalogueSearchCriteria().getTimeEnd() != null &&
                    !"".equals(searchCriteria.getMetadataCatalogueSearchCriteria().getTimeEnd())) {
                List<Operation> timeEndOperations = new ArrayList<Operation>();
                addOperation(
                        timeEndOperations,
                        PROPERTY_IS_COMP_OPERATION,
                        OperationDefines.PROPERTYISLESSTHANOREQUALTO,
                        getDateAsString(searchCriteria.getMetadataCatalogueSearchCriteria().getTimeEnd()),
                        "RevisionDate");
                addOperation(
                        timeEndOperations,
                        PROPERTY_IS_COMP_OPERATION,
                        OperationDefines.PROPERTYISLESSTHANOREQUALTO,
                        getDateAsString(searchCriteria.getMetadataCatalogueSearchCriteria().getTimeEnd()),
                        "CreationDate");
                addOperation(
                        timeEndOperations,
                        PROPERTY_IS_COMP_OPERATION,
                        OperationDefines.PROPERTYISLESSTHANOREQUALTO,
                        getDateAsString(searchCriteria.getMetadataCatalogueSearchCriteria().getTimeEnd()),
                        "PublicationDate");
                operations.add(new LogicalOperation(OperationDefines.OR, timeEndOperations));
            }

            if (searchCriteria.getMetadataCatalogueSearchCriteria().isMetaInfoTargetHistory()) {
                addOperation(
                        operations,
                        PROPERTY_IS_LIKE_OPERATION,
                        -1,
                        WILDCARD_CHARACTER + searchCriteria.getMetadataCatalogueSearchCriteria().getMetaInfo() + WILDCARD_CHARACTER,
                        "Lineage");
            }

            if (searchCriteria.getMetadataCatalogueSearchCriteria().isMetaInfoTargetDescription()) {
                addOperation(
                        operations,
                        PROPERTY_IS_LIKE_OPERATION,
                        -1,
                        searchCriteria.getMetadataCatalogueSearchCriteria().getMetaInfo(),
                        "Abstract");
            }
        }

        return operations;
    }

    private Node makeQuery(SearchCriteria searchCriteria) throws Exception {
        List<Operation> operations = getOperations(searchCriteria);
        Operation operation;

        if (operations.isEmpty()) {
            return null;
        } else if (operations.size() == 1) {
            operation = operations.get(0);
        } else {
            operation = new LogicalOperation(OperationDefines.AND, operations);
        }

        ComplexFilter filter = new ComplexFilter(operation);

        Map<String, URI> nsmap = new HashMap<String, URI>();
        nsmap.put(GMD_NAMESPACE, new URI("http://www.isotc211.org/2005/gmd"));
        nsmap.put(GCO_NAMESPACE, new URI("http://www.isotc211.org/2005/gco"));
        nsmap.put("csw", new URI("http://www.opengis.net/cat/csw/2.0.2"));

        List<QualifiedName> typeNames = new ArrayList<QualifiedName>();
        typeNames.add(new QualifiedName("gmd:MD_Metadata"));

        List<PropertyPath> elementNamesAsPropertyPaths = new ArrayList<PropertyPath>();

        SortProperty[] sortProperties = SortProperty.create(null, nsmap);

        Query query = new Query("summary", new ArrayList<QualifiedName>(),
                new HashMap<String, QualifiedName>(),
                elementNamesAsPropertyPaths, filter, sortProperties, typeNames,
                new HashMap<String, QualifiedName>());

        GetRecords getRecs = new GetRecords("0", "2.0.2", null, nsmap,
                RESULT_TYPE.RESULTS, "application/xml", "csw:IsoRecord", 1,
                10000, 0, null, query);

        GetRecordsDocument getRecsDoc = XMLFactory.exportWithVersion(getRecs);

        // debug output
        Writer w = new PrintWriter(System.out);
        Properties p = new Properties();
        p.put("indent", "yes");

        // if (log.isDebugEnabled()) {
        //     getRecsDoc.write(w, p);
        // }

        w.flush();

        // POSTing GetRecords request
        URL urlPost = new URL(queryURL);
        URLConnection conn = urlPost.openConnection();
        conn
                .addRequestProperty("Content-Type",
                        "application/xml;charset=UTF-8");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(conn
                .getOutputStream());
        getRecsDoc.write(writer, p);
        writer.flush();

        GetRecordsResultDocument resultsDoc = new Csw202ResultsDoc();
        try {
            resultsDoc.load(conn.getInputStream(), queryURL);
        } finally {
            writer.close();
        }

        //resultsDoc.write(w);

        GetRecordsResult getRecsResult = resultsDoc.parseGetRecordsResponse(getRecs);

        SearchResults results = getRecsResult.getSearchResults();
        return results.getRecords();
    }

    /* Return date as String YYYY-MM-DD */
    private String getDateAsString(String inputDateAsString) {
        if (inputDateAsString == null || "".equals(inputDateAsString)) {
            return "";
        }

        DateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date date;

        try {
            date = inputFormat.parse(inputDateAsString);
        } catch (ParseException e) {
            return "";
        }

        DateFormat returnFormat = new SimpleDateFormat("yyyy-MM-dd");
        return returnFormat.format(date);
    }
}