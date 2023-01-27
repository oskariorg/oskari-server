package fi.nls.oskari.search.channel;

import fi.nls.oskari.service.ServiceRuntimeException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.GeometryHelper;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Stream;

import static fi.nls.oskari.csw.service.CSWService.PROP_SERVICE_URL;

/**
 * Search channel for making CSW queries.
 *
 * Configurable by properties:
 * - Server: service.metadata.url (for example "https://www.paikkatietohakemisto.fi/geonetwork/srv/fin/csw")
 * - Query type: search.channel.METADATA_CATALOGUE_CHANNEL.queryType (defaults to "summary")
 * - Query fields: search.channel.METADATA_CATALOGUE_CHANNEL.queryFields (comma-separted list like "Title, Abstract" - defaults to "csw:anyText")
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
    private static String serverURL = PropertyUtil.get(PROP_SERVICE_URL);
    private String queryType;
    private String[] queryFields;

    private final static List<MetadataField> fields = new ArrayList<>();

    private MetadataCatalogueResultParser RESULT_PARSER = null;
    private final MetadataCatalogueQueryHelper QUERY_HELPER = new MetadataCatalogueQueryHelper();

    private OskariLayerService mapLayerService = OskariComponentManager.getComponentOfType(OskariLayerService.class);

    @Override
    public void init() {
        super.init();
        queryType = getProperty("queryType", "summary");
        queryFields = getProperty("queryFields", "csw:anyText").split("\\s*,\\s*");
        RESULT_PARSER = new MetadataCatalogueResultParser();
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

    public static MetadataField getField(String name) {
        for (MetadataField field: getFields()) {
            if (field.getName().equals(name)) {
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

        ChannelSearchResult channelSearchResult;
        try {
            Element root = makeQuery(searchCriteria);
            channelSearchResult = parseResults(root, searchCriteria);
        } catch (Exception x) {
            log.error(x, "Failed to search");
            channelSearchResult = new ChannelSearchResult();
            channelSearchResult.setException(x);
            channelSearchResult.setQueryFailed(true);
        }
        return channelSearchResult;
    }

    private Stream<Element> getResults(Element root) {
        if (!"GetRecordsResponse".equals(XmlHelper.getLocalName(root))) {
            throw new ServiceRuntimeException("Unexpected response. Expected root element 'GetRecordsResponse'");
        }
        Element results = XmlHelper.getFirstChild(root, "SearchResults");
        if (results == null) {
            throw new ServiceRuntimeException(XmlHelper.generateUnexpectedElementMessage(root));
        }
        return XmlHelper.getChildElements(results, "MD_Metadata");
    }

    public ChannelSearchResult parseResults(Element root, SearchCriteria searchCriteria) {

        ChannelSearchResult channelSearchResult = new ChannelSearchResult();
        final String srs = searchCriteria.getSRS();
        try {
            final long start = System.currentTimeMillis();
            getResults(root).forEach(metadata -> {
                try {
                    SearchResultItem item = RESULT_PARSER.parseResult(metadata);
                    channelSearchResult.addItem(item);
                    // add coverage area if we can transform it
                    item.addValue("geom", getWKT(item, WKTHelper.PROJ_EPSG_4326, srs));
                } catch (Exception e) {
                    String msg = "Error parsing metadata search result item or transform coverage area";
                    log.info(msg, ":", e.getMessage());
                    log.debug(e, msg);
                }
            });

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
        double x1 = item.getWestBoundLongitude();
        double y1 = item.getSouthBoundLatitude();
        double x2 = item.getEastBoundLongitude();
        double y2 = item.getNorthBoundLatitude();
        try {

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
        } catch(Exception e) {
            log.error("Unable to transform BBOX WKT:", e.getMessage());
        } catch (OutOfMemoryError oom) {
            log.warn("OutOfMemoryError with bbox:",
                "w:", x1, "s:", y1, "e:", x2, "n:", y2);
        }
        return null;
    }

    private Element makeQuery(SearchCriteria searchCriteria) throws Exception {
        final long start = System.currentTimeMillis();
        final String payload = QUERY_HELPER.getQueryPayload(searchCriteria, queryType, queryFields);
        if (payload == null) {
            // no point in making the query without payload
            return null;
        }

        // POSTing GetRecords request
        HttpURLConnection conn = getConnection(getServerURL());
        conn.setUseCaches(false);
        IOHelper.post(conn, "application/xml;charset=UTF-8", payload);
        log.debug(payload);

        final long end =  System.currentTimeMillis();
        log.debug("Querying metadata service took", (end-start), "ms");
        Element root = XmlHelper.parseXML(IOHelper.debugResponse(conn.getInputStream()));
        log.debug("Parsing metadata service took", (System.currentTimeMillis()-end), "ms");
        return root;
    }
}