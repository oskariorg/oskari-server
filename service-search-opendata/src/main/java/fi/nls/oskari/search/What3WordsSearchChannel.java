package fi.nls.oskari.search;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


@Oskari(What3WordsSearchChannel.ID)
public class What3WordsSearchChannel extends SearchChannel {

    /** logger */
    private static final Logger LOG = LogFactory.getLogger(What3WordsSearchChannel.class);
    public static final String ID = "WHAT3WORDS_CHANNEL";
    public static final String SERVICE_SRS = "EPSG:4326";

    private static final String PROPERTY_SERVICE_URL = "search.channel.WHAT3WORDS_CHANNEL.service.url";
    private static final String PROPERTY_REVERSE_URL = "search.channel.WHAT3WORDS_CHANNEL.reverse.url";
    private static final String PROPERTY_SERVICE_APIKEY = "search.channel.WHAT3WORDS_CHANNEL.service.apikey";

    private String serviceURL = null;
    private String reverseServiceURL = null;
    private boolean forceCoordinateSwitch = false;

    @Override
    public void init() {
        super.init();
        try {
            serviceURL = IOHelper.addUrlParam(
                    PropertyUtil.get(PROPERTY_SERVICE_URL, "https://api.what3words.com/w3w"),
                        "key", PropertyUtil.getNecessary(PROPERTY_SERVICE_APIKEY));
            reverseServiceURL = IOHelper.addUrlParam(
                    PropertyUtil.get(PROPERTY_REVERSE_URL, "https://api.what3words.com/position"),
                    "key", PropertyUtil.getNecessary(PROPERTY_SERVICE_APIKEY));
        } catch (RuntimeException ex) {
            // thrown if apikey is not defined - add user-friendly log message
            LOG.warn("Apikey missing for What3Words.com search - Skipping it. Define", PROPERTY_SERVICE_APIKEY, "to use the channel.");
            throw ex;
        }
        forceCoordinateSwitch = PropertyUtil.getOptional("search.channel.forceXY", forceCoordinateSwitch);
        LOG.debug("ServiceURL set to " + serviceURL);
    }

    public boolean isValidSearchTerm(SearchCriteria criteria) {
        if(criteria.isReverseGeocode()) {
            return true;
        }
        // Just check that we get 3 words when split with dot
        String[] parts = criteria.getSearchString().split("\\.");
        return parts.length == 3;
    }

    public Capabilities getCapabilities() {
        return Capabilities.BOTH;
    }

    /**
     * Returns the channel search results.
     * @param searchCriteria Search criteria.
     * @return Search results.
     */
    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) {
        ChannelSearchResult searchResultList = new ChannelSearchResult();

        if (serviceURL == null) {
            LOG.warn("ServiceURL not configured. Add property with key", PROPERTY_SERVICE_URL);
            return searchResultList;
        }
        String srs = searchCriteria.getSRS();
        if( srs == null ) {
        	srs = "EPSG:3067";
        }

        try {
            // https://api.what3words.com/w3w?key=YOURAPIKEY&string=index.home.raft
            final String url = IOHelper.addUrlParam(serviceURL, "string", searchCriteria.getSearchString());
            String data = IOHelper.getURL(url);
            LOG.debug("Result: " + data);
            SearchResultItem item = parseResult(JSONHelper.createJSONObject(data), srs);

            searchResultList.addItem(item);
        } catch (Exception e) {
            LOG.error(e, "Failed to search locations from register of OpenStreetMap");
        }
        return searchResultList;
    }

    public ChannelSearchResult reverseGeocode(SearchCriteria sc) {
        ChannelSearchResult searchResultList = new ChannelSearchResult();
        try {
            final CoordinateReferenceSystem sourceCrs = CRS.decode(sc.getSRS());
            final CoordinateReferenceSystem targetCrs = CRS.decode(SERVICE_SRS);

            final Point point = ProjectionHelper.transformPoint(
                    sc.getLon(), sc.getLat(),
                    sourceCrs,
                    targetCrs);
            //https://api.what3words.com/position?key=YOURAPIKEY&lang=en&position=51.521251,-0.203586
            final String url = IOHelper.addUrlParam(reverseServiceURL, "position", point.getLat() + "," + point.getLon());
            String data = IOHelper.getURL(url);
            LOG.debug("Result: " + data);
            SearchResultItem item = parseResult(JSONHelper.createJSONObject(data), sc.getSRS());

            searchResultList.addItem(item);
        } catch (Exception e) {
            LOG.error(e, "Failed to search locations from register of OpenStreetMap");
        }
        return searchResultList;
    }

    public SearchResultItem parseResult(JSONObject dataItem, String targetSrs) throws Exception {
        final CoordinateReferenceSystem sourceCrs = CRS.decode(SERVICE_SRS);
        final CoordinateReferenceSystem targetCrs = CRS.decode(targetSrs);
        // geoserver seems to setup the forced XY direction so check if it's in effect
        // http://docs.geotools.org/stable/userguide/library/referencing/order.html
        // TODO: this should be checked in ProjectionHelper
        final boolean reverseCoordinates = "true".equalsIgnoreCase(System.getProperty("org.geotools.referencing.forceXY"));

        final SearchResultItem item = new SearchResultItem();
        item.setType("3 words");
        final JSONArray words = JSONHelper.getJSONArray(dataItem, "words");
        String title = words.join(".").replaceAll("\"", "");
        item.setTitle(title);
        item.setDescription(title);
        final JSONArray position = JSONHelper.getJSONArray(dataItem, "position");
        final String lat = "" + position.getDouble(0);
        final String lon = "" + position.getDouble(1);
        //item.setLocationTypeCode();
        if(reverseCoordinates) {
            item.setLon(lon);
            item.setLat(lat);
        } else {
            item.setLat(lon);
            item.setLon(lat);
        }

        item.setRank(0);

        // convert to map projection
        final Point point = ProjectionHelper.transformPoint(
                ConversionHelper.getDouble(item.getLon(), -1),
                ConversionHelper.getDouble(item.getLat(), -1),
                sourceCrs,
                targetCrs);
        if(point == null) {
            item.setLon("");
            item.setLat("");
            return null;
        }
        // switch order again after making the transform if necessary
        if(!forceCoordinateSwitch && (reverseCoordinates  || ProjectionHelper.isFirstAxisNorth(targetCrs))) {
            point.switchLonLat();
        }
        item.setLon(point.getLon());
        item.setLat(point.getLat());
        return item;
    }

}
