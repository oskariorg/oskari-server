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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Support for what3words API v2
 */
@Oskari(What3WordsSearchChannel.ID)
public class What3WordsSearchChannel extends SearchChannel {

    /** logger */
    private static final Logger LOG = LogFactory.getLogger(What3WordsSearchChannel.class);
    public static final String ID = "WHAT3WORDS_CHANNEL";
    public static final String SERVICE_SRS = "EPSG:4326";

    private static final String PROPERTY_SERVICE_URL = "search.channel.WHAT3WORDS_CHANNEL.service.url";
    private static final String PROPERTY_REVERSE_URL = "search.channel.WHAT3WORDS_CHANNEL.reverse.url";
    private static final String PROPERTY_SERVICE_APIKEY = "search.channel.WHAT3WORDS_CHANNEL.service.apikey";
    private static final String PROPERTY_FORCED_LANG = "search.channel.WHAT3WORDS_CHANNEL.lang";

    private String serviceURL = null;
    private String reverseServiceURL = null;
    private String forcedLanguage = null;
    private Set<String> availableLangs = null;

    @Override
    public void init() {
        super.init();
        try {
            serviceURL = IOHelper.addUrlParam(
                    PropertyUtil.get(PROPERTY_SERVICE_URL, "https://api.what3words.com/v2/forward?display=minimal"),
                        "key", PropertyUtil.getNecessary(PROPERTY_SERVICE_APIKEY));
            reverseServiceURL = IOHelper.addUrlParam(
                    PropertyUtil.get(PROPERTY_REVERSE_URL, "https://api.what3words.com/v2/reverse?display=minimal"),
                    "key", PropertyUtil.getNecessary(PROPERTY_SERVICE_APIKEY));
        } catch (RuntimeException ex) {
            // thrown if apikey is not defined - add user-friendly log message
            LOG.warn("Apikey missing for What3Words.com search - Skipping it. Define", PROPERTY_SERVICE_APIKEY, "to use the channel.");
            throw ex;
        }
        availableLangs = getAvailableLanguages();
        forcedLanguage = PropertyUtil.getOptional(PROPERTY_FORCED_LANG);
        LOG.debug("ServiceURL set to " + serviceURL);
    }

    private Set<String> getAvailableLanguages() {
        String langUrl = IOHelper.addUrlParam(
                PropertyUtil.get(PROPERTY_SERVICE_URL, "https://api.what3words.com/v2/languages?format=json"),
                "key", PropertyUtil.getNecessary(PROPERTY_SERVICE_APIKEY));
        final Set<String> value = new HashSet<>();
        try {
            JSONObject response = JSONHelper.createJSONObject(IOHelper.getURL(langUrl));
            JSONArray languages = JSONHelper.getJSONArray(response, "languages");
            for(int i = 0; i < languages.length(); ++i) {
                JSONObject lang = languages.optJSONObject(i);
                if(lang == null) {
                    continue;
                }
                value.add(lang.optString("code"));
            }
        } catch (Exception e) {
            LOG.warn(e, "Error getting supported languages from what3words");
            value.add("en");
        }
        return value;
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
            // v1: https://api.what3words.com/w3w?key=YOURAPIKEY&string=index.home.raft
            // v2: https://api.what3words.com/v2/forward?key=YOURAPIKEY&addr=index.home.raft
            final Map<String, String> params = new HashMap<>();
            params.put("addr", searchCriteria.getSearchString());
            // setup language
            final String lang = getLang(searchCriteria.getLocale());
            params.put("lang", lang);
            final String url = IOHelper.constructUrl(serviceURL, params);
            String data = IOHelper.getURL(url);
            LOG.debug("Result: " + data);
            SearchResultItem item = parseResult(JSONHelper.createJSONObject(data), srs);
            // set language of result based on what was asked
            item.setLang(lang);

            searchResultList.addItem(item);
        } catch (Exception e) {
            LOG.error(e, "Failed to search locations from register of What3Words");
        }
        return searchResultList;
    }

    public Point getServiceCoordinates(double lon, double lat, String srs) throws Exception {
        final CoordinateReferenceSystem sourceCrs = CRS.decode(srs);
        final CoordinateReferenceSystem targetCrs = CRS.decode(SERVICE_SRS);

        Point point = new Point(lon, lat);
        final Point transformed = ProjectionHelper.transformPoint(point, sourceCrs, targetCrs);

        return transformed;
    }

    public ChannelSearchResult reverseGeocode(SearchCriteria sc) {
        ChannelSearchResult searchResultList = new ChannelSearchResult();
        try {
            LOG.debug("Transforming coordinates");
            final Point point = getServiceCoordinates(sc.getLon(), sc.getLat(), sc.getSRS());
            // v1: https://api.what3words.com/position?key=YOURAPIKEY&lang=en&position=51.521251,-0.203586
            // v2: https://api.what3words.com/v2/reverse?key=YOURAPIKEY&lang=en&coords=51.521251,-0.203586

            final Map<String, String> params = new HashMap<>();
            params.put("coords", point.getLat() + "," + point.getLon());
            // setup language
            final String lang = getLang(sc.getLocale());
            params.put("lang", lang);
            final String url = IOHelper.constructUrl(reverseServiceURL, params);

            String data = IOHelper.getURL(url);
            LOG.debug("Result: " + data);
            SearchResultItem item = parseResult(JSONHelper.createJSONObject(data), sc.getSRS());
            // set language of result based on what was asked
            item.setLang(lang);

            searchResultList.addItem(item);
        } catch (Exception e) {
            LOG.error(e, "Failed to search locations from register of What3Words");
        }
        return searchResultList;
    }

    private String getLang(String requested) {

        // setup language
        if(forcedLanguage != null) {
           return forcedLanguage;
        } else if(availableLangs.contains(requested)){
            return requested;
        }
        return "en";
    }

    public SearchResultItem parseResult(JSONObject dataItem, String targetSrs) throws Exception {
        final CoordinateReferenceSystem sourceCrs = CRS.decode(SERVICE_SRS);
        final CoordinateReferenceSystem targetCrs = CRS.decode(targetSrs);
        // geoserver seems to setup the forced XY direction so check if it's in effect
        // http://docs.geotools.org/stable/userguide/library/referencing/order.html

        final SearchResultItem item = new SearchResultItem();
        item.setType("what3words");
        String title = dataItem.optString("words");
        item.setTitle(title);
        item.setDescription(title);
        final JSONObject position = JSONHelper.getJSONObject(dataItem, "geometry");
        final String lat = "" + position.optDouble("lat");
        final String lon = "" + position.optDouble("lng");

        // convert to map projection
        final Point point = ProjectionHelper.transformPoint(
                ConversionHelper.getDouble(lon, -1),
                ConversionHelper.getDouble(lat, -1),
                sourceCrs,
                targetCrs);
        if(point == null) {
            item.setLon("");
            item.setLat("");
            return null;
        }

        item.setLon(point.getLon());
        item.setLat(point.getLat());
        return item;
    }

    public int getRank(String type) {
        int rank = super.getRank(type);
        if(rank != -1) {
            return rank;
        }
        // default if not configured
        return 100;
    }

}
