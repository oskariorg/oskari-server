package fi.nls.oskari.search.channel;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.GeometryHelper;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
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
 *      - blacklist: is a list of response values that will be filtered out
 *
 */
@Oskari(MetadataCatalogueChannelSearchService.ID)
public class MetadataCatalogueChannelSearchService extends SearchChannel {

    private final Logger log = LogFactory.getLogger(this.getClass());

    public static final String ID = "METADATA_CATALOGUE_CHANNEL";
    private static String serverURL = PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.server", "http://geonetwork.nls.fi");
    private static String queryPath = PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.path", "/geonetwork/srv/en/csw");

    private final Map<String, String> imageURLs = new HashMap<String, String>();
    private final Map<String, String> fetchPageURLs = new HashMap<String, String>();

    private final static List<MetadataField> fields = new ArrayList<MetadataField>();

    private MetadataCatalogueResultParser RESULT_PARSER = null;
    private final MetadataCatalogueQueryHelper QUERY_HELPER = new MetadataCatalogueQueryHelper();

    private OskariLayerService mapLayerService = OskariComponentManager.getComponentOfType(OskariLayerService.class);

    private static final String PROPERTY_IMAGE_PREFIX = "search.channel.METADATA_CATALOGUE_CHANNEL.image.url.";
    private static final String PROPERTY_FETCHURL_PREFIX = "search.channel.METADATA_CATALOGUE_CHANNEL.fetchpage.url.";
    private static final String PROPERTY_RESULTPARSER = "search.channel.METADATA_CATALOGUE_CHANNEL.resultparser";

    @Override
    public void init() {
        super.init();
        final List<String> imageKeys = PropertyUtil.getPropertyNamesStartingWith(PROPERTY_IMAGE_PREFIX);
        final int imgPrefixLen = PROPERTY_IMAGE_PREFIX.length();
        for(String key : imageKeys) {
            final String langCode = key.substring(imgPrefixLen);
            imageURLs.put(langCode, PropertyUtil.get(key));
        }
        final List<String> urlKeys = PropertyUtil.getPropertyNamesStartingWith(PROPERTY_FETCHURL_PREFIX);
        final int urlPrefixLen = PROPERTY_FETCHURL_PREFIX.length();
        for(String key : urlKeys) {
            final String langCode = key.substring(urlPrefixLen);
            fetchPageURLs.put(langCode, PropertyUtil.get(key));
        }

        // hook for customized parsing
        final String customResultParser = PropertyUtil.getOptional(PROPERTY_RESULTPARSER);
        if(customResultParser != null) {
            try {
                final Class clazz = Class.forName(customResultParser);
                RESULT_PARSER = (MetadataCatalogueResultParser) clazz.newInstance();
            } catch (Exception e) {
                log.error(e, "Error instantiating custom metadata result parser:", customResultParser);
            }
        }
        if(RESULT_PARSER == null) {
            RESULT_PARSER = new MetadataCatalogueResultParser();
        }
    }

    /**
     * Defaults to false as opposed to true in SearchChannel as this most likely isn't a channel to use for usual searching.
     * @return
     */
    public boolean isDefaultChannel() {
        return PropertyUtil.getOptional("search.channel." + getName() + ".isDefault", false);
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
            field.setDefaultValue(PropertyUtil.getOptional(propPrefix + name + ".value"));
            field.setBlacklist(Arrays.asList(PropertyUtil.getCommaSeparatedList(propPrefix + name + ".blacklist")));
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
            channelSearchResult = parseResults(builder, searchCriteria);
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

    public ChannelSearchResult parseResults(final StAXOMBuilder builder, final SearchCriteria searchCriteria) {
    	
        ChannelSearchResult channelSearchResult = new ChannelSearchResult();
        log.debug("parseResults");
        try {
            final OMElement resultsWrapper = getResultsElement(builder);
            final String locale = searchCriteria.getLocale();
            final String srs = searchCriteria.getSRS();
            // resultsWrapper == null -> no search results
            final Iterator<OMElement> results = resultsWrapper.getChildrenWithLocalName("MD_Metadata");
            final long start = System.currentTimeMillis();
            while(results.hasNext()) {
                final SearchResultItem item = RESULT_PARSER.parseResult(results.next(), locale);
                setupResultItemURLs(item, locale);

                final List<OskariLayer> oskariLayers =  getOskariLayerWithUuid(item);
                for(OskariLayer oskariLayer : oskariLayers){
                    log.debug("METAID: " + oskariLayer.getMetadataId());
                    item.addUuId(oskariLayer.getMetadataId());
                }

                item.addValue("geom", getWKT(item, WKTHelper.PROJ_EPSG_4326, srs));
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

    private String getWKT(final SearchResultItem item, final String sourceSRS, final String targetSRS) {
        // check if we have values
        if((item.getSouthBoundLatitude() == null) ||
            (item.getWestBoundLongitude() == null) ||
            (item.getEastBoundLongitude() == null) ||
            (item.getNorthBoundLatitude() == null)) {
            return null;
        }
        // transform points to map projection and create a WKT bbox
        try {
            double x1 = Double.parseDouble(item.getWestBoundLongitude());
            double y1 = Double.parseDouble(item.getSouthBoundLatitude());
            double x2 = Double.parseDouble(item.getEastBoundLongitude());
            double y2 = Double.parseDouble(item.getNorthBoundLatitude());

            GeometryFactory gf = new GeometryFactory();
            CoordinateSequence cs = GeometryHelper.createLinearRing(gf, x1, y1, x2, y2);
            LineString ls = gf.createLineString(cs);
            CoordinateSequence interpolated = GeometryHelper.interpolateLinear(ls, 1.0, gf);
            Polygon polygon = gf.createPolygon(interpolated);

            CoordinateReferenceSystem from = WKTHelper.getCRS(sourceSRS);
            CoordinateReferenceSystem to = WKTHelper.getCRS(targetSRS);
            MathTransform mt = CRS.findMathTransform(from, to, true);
            Geometry projected = JTS.transform(polygon, mt);

            return WKTHelper.getWKT(projected);
        } catch(Exception e){
            log.error("Unable to transform BBOX WKT:", e.getMessage());
        }
        return null;
    }

    private List<OskariLayer> getOskariLayerWithUuid(SearchResultItem item){
    	
    	log.debug("in getOskariLayerWithUuid");
    	if(mapLayerService == null ||item.getUuId() == null || item.getUuId().isEmpty()){
    		return Collections.emptyList();
    	}
        final List<OskariLayer> list = mapLayerService.findByMetadataId(item.getUuId().get(0));
        if(list == null) {
            return Collections.emptyList();
        }
        return list;
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
                // This only works for GN2 for paikkatietohakemisto.fi
                // GN2-style: http://geonetwork.nls.fi/geonetwork/srv/fi/resources.get.uuid?access=public&uuid=7ac131b9-a307-4aa1-b27a-009e91f6bd45&fname=Pohjak_Ylihrm_s.png
                // GN3-style: http://www.paikkatietohakemisto.fi/geonetwork/srv/api/records/7ac131b9-a307-4aa1-b27a-009e91f6bd45/attachments/Pohjak_Ylihrm_s.png
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
        HttpURLConnection conn = getConnection(queryURL);
        IOHelper.writeHeader(conn, "Content-Type", "application/xml;charset=UTF-8");
        conn.setUseCaches(false);
        IOHelper.writeToConnection(conn, payload);

        final long end =  System.currentTimeMillis();

        final StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(IOHelper.debugResponse(conn.getInputStream()));
        log.debug("Querying metadata service took", (end-start), "ms");
        return stAXOMBuilder;
    }
}