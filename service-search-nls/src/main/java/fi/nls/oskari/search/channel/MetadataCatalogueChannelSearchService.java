package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Search channel for making CSW queries.
 *
 * Configurable by properties:
 * - Server: search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.server (defaults to "http://geonetwork.nls.fi")
 * - QueryPath: search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.path (defaults to "/geonetwork/srv/en/csw")
 * - localized urls for images: search.channel.METADATA_CATALOGUE_CHANNEL.image.url.[lang code] (as contentURL in conjunction with resourceId)
 * - localized urls for service: search.channel.METADATA_CATALOGUE_CHANNEL.fetchpage.url.[lang code] (as actionURL in conjunction with resourceId)
 * - advanced filter fields (dropdowns) that are available on form based on the service:
 *      search.channel.METADATA_CATALOGUE_CHANNEL.fields (Note! Uses GetDomain Operation on CSW to populate values for fields)
 * - per field processing definitions (each property prefixed with "search.channel.METADATA_CATALOGUE_CHANNEL.field.[field name]."):
 *      - isMulti: true to allow multiple values, defaults to false
 *      - dependencies: comma-separated list of dependent value pairs, for example "type.dependencies=service|serviceType" means that if type has
 *          value 'service' add to the same filter operation any serviceType parameters as single AND-operation
 *      - filter: maps the field to a property when creating query filter for example type.filter=gmd:hierarchyLevel maps the type value to gmd:hierarchyLevel when creating the query (defaults to field name)
 *      - shownIf: information for UI of a dependent field for example serviceType.shownIf=[{"type":"service"}] means that serviceType field should only be activate in UI if field "type" has value "service"
 *          showIf is closely related to dependencies field
 *      - filterOp: used for creating query and mapped in code to Deegree filter operations (defaults to LIKE operations)
 *      - mustMatch: true means the field will be treated as AND filter instead of OR when creating query filter (defaults to false)
 *
 */
public class MetadataCatalogueChannelSearchService implements SearchableChannel {

    private final Logger log = LogFactory.getLogger(this.getClass());

    public static final String ID = "METADATA_CATALOGUE_CHANNEL";
    private static String serverURL = PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.server", "http://geonetwork.nls.fi");
    private static String queryPath = PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.path", "/geonetwork/srv/en/csw");

    private final Map<String, String> imageURLs = new HashMap<String, String>();
    private final Map<String, String> fetchPageURLs = new HashMap<String, String>();

    private final static List<MetadataField> fields = new ArrayList<MetadataField>();

    private final MetadataCatalogueResultParser RESULT_PARSER = new MetadataCatalogueResultParser();
    private final MetadataCatalogueQueryHelper QUERY_HELPER = new MetadataCatalogueQueryHelper();

    public String getId() {
        return ID;
    }

    public static String getServerURL() {
        return serverURL;
    }

    public static String getServerPath() {
        return queryPath;
    }

    public static List<MetadataField> getFields() {
        if(!fields.isEmpty()) {
            return fields;
        }

        final String[] propFields = PropertyUtil.getCommaSeparatedList("search.channel.METADATA_CATALOGUE_CHANNEL.fields");

        final String propPrefix =  "search.channel.METADATA_CATALOGUE_CHANNEL.field.";
        for(String name : propFields) {
            final MetadataField field = new MetadataField(name, PropertyUtil.getOptional(propPrefix + name + ".isMulti", false));
            field.setFilter(PropertyUtil.getOptional(propPrefix + name + ".filter"));
            field.setShownIf(PropertyUtil.getOptional(propPrefix + name + ".shownIf"));
            field.setFilterOp(PropertyUtil.getOptional(propPrefix + name + ".filterOp"));
            field.setMustMatch(PropertyUtil.getOptional(propPrefix + name + ".mustMatch", false));
            field.setDependencies(PropertyUtil.getMap(propPrefix + name + ".dependencies"));
            fields.add(field);
        }
        return fields;
    }

    /**
     * Reset #getServerURL, #getServerPath and #getFields. Fields will be reconstructed based on properties on
     * next #getFields() call.
     */
    public static void resetProperties() {
        serverURL = PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.server", "http://geonetwork.nls.fi");
        queryPath = PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.path", "/geonetwork/srv/en/csw");
        fields.clear();
    }

    public static MetadataField getField(String name) {
        for(MetadataField field: getFields()) {
            if(field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public void setProperty(String propertyName, String propertyValue) {

        if (null != propertyName) {
            if (propertyName.indexOf("image.url.") == 0) {
                // after first 10 chars there is a language code
                imageURLs.put(propertyName.substring(10), propertyValue);
            } else if (propertyName.indexOf("fetchpage.url.") == 0) {
                // after first 14 chars there is a language code
                fetchPageURLs.put(propertyName.substring(14), propertyValue);
            }
        }
    }

    public ChannelSearchResult doSearch(SearchCriteria searchCriteria)
            throws IllegalSearchCriteriaException {
        ChannelSearchResult searchResultList = readQueryData(searchCriteria);
        searchResultList.setChannelId(getId());

        return searchResultList;
    }

    private ChannelSearchResult readQueryData(SearchCriteria searchCriteria) {

        ChannelSearchResult channelSearchResult = null;
        StAXOMBuilder builder = null;
        try {
            builder = makeQuery(searchCriteria);
            channelSearchResult = parseResults(builder, searchCriteria.getLocale());
        } catch (Exception x) {
            log.error(x, "Failed to search");
            channelSearchResult = new ChannelSearchResult();
            channelSearchResult.setException(x);
            channelSearchResult.setQueryFailed(true);
        }
        finally {
            try {
                builder.close();
            } catch (Exception ignored) {}
        }
        return channelSearchResult;
    }

    public ChannelSearchResult parseResults(final StAXOMBuilder builder, final String locale) {
        ChannelSearchResult channelSearchResult = new ChannelSearchResult();
        try {
            final OMElement resultsWrapper = getResultsElement(builder);
            // resultsWrapper == null -> no search results
            final Iterator<OMElement> results = resultsWrapper.getChildrenWithLocalName("MD_Metadata");
            final long start = System.currentTimeMillis();
            while(results.hasNext()) {
                final SearchResultItem item = RESULT_PARSER.parseResult(results.next(), locale);
                setupResultItemURLs(item, locale);
                channelSearchResult.addItem(item);
            }
            final long end =  System.currentTimeMillis();
            log.debug("Parsing metadata results took", (end-start), "ms");
            channelSearchResult.setQueryFailed(false);
        } catch (Exception x) {
            log.error(x, "Failed to search");
            channelSearchResult.setException(x);
            channelSearchResult.setQueryFailed(true);
        }
        return channelSearchResult;
    }

    private void setupResultItemURLs(final SearchResultItem item, final String locale) {
        final String uuid = item.getResourceId();

        if (uuid != null) {
            // uuid = getLocalizedString(xpath, uuidNode, locales);
            item.setActionURL(fetchPageURLs.get(locale) + uuid);

            final boolean replaceImageURL = item.getContentURL() != null &&
                    !item.getContentURL().isEmpty() &&
                    !item.getContentURL().startsWith("http://") ;

            if (replaceImageURL) {
                item.setContentURL(imageURLs.get(locale) + "uuid=" + uuid + "&fname=" + item.getContentURL());
            }
        }
        item.setResourceNameSpace(getServerURL());
    }

    private OMElement getResultsElement(final StAXOMBuilder builder) {
        final Iterator<OMElement> resultIt = builder.getDocumentElement().getChildrenWithLocalName("SearchResults");
        if(resultIt.hasNext()) {
            return resultIt.next();
        }
        return null;
    }

    private StAXOMBuilder makeQuery(SearchCriteria searchCriteria) throws Exception {
        final long start = System.currentTimeMillis();
        final String payload = QUERY_HELPER.getQueryPayload(searchCriteria);
        if(payload == null) {
            // no point in making the query without payload
            return null;
        }

        // POSTing GetRecords request
        final String queryURL = serverURL + queryPath;
        HttpURLConnection conn = IOHelper.getConnection(queryURL);
        IOHelper.writeHeader(conn, "Content-Type", "application/xml;charset=UTF-8");
        conn.setUseCaches(false);
        IOHelper.writeToConnection(conn, payload);

        final long end =  System.currentTimeMillis();

        final StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(conn.getInputStream());
        log.debug("Querying metadata service took", (end-start), "ms");
        return stAXOMBuilder;
    }
}