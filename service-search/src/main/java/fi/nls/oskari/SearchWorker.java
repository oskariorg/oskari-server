package fi.nls.oskari;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import fi.nls.oskari.search.channel.SearchableChannel;
import fi.mml.portti.service.search.Query;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.mml.portti.service.search.SearchService;
import fi.mml.portti.service.search.SearchServiceImpl;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.apache.commons.lang.StringEscapeUtils;

public class SearchWorker {

    public static final String KEY_TOTAL_COUNT = "totalCount";
    public static final String KEY_ERROR_TEXT = "errorText";
    public static final String KEY_LOCATIONS = "locations";
    public static final String KEY_HAS_MORE = "hasMore";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_RANK = "rank";
    public static final String KEY_LON = "lon";
    public static final String KEY_LAT = "lat";
    public static final String KEY_VILLAGE = "village";
    public static final String KEY_ZOOMLEVEL = "zoomLevel";

    public static final String ERR_EMPTY = "cannot_be_empty";
    public static final String ERR_TOO_SHORT = "too_short";
    public static final String ERR_TOO_WILD = "too_many_stars";

    public static final String STR_TRUE = "true";
    public static final String STR_NULL = "null";
    /** Our service */
    private static SearchService searchService = new SearchServiceImpl();

    private final static Logger log = LogFactory.getLogger(SearchWorker.class);

    private static String[] defaultChannels = new String[0];
    private static int maxCount = 100;

    public static void init() {
        defaultChannels = PropertyUtil.getCommaSeparatedList("search.channels.default");
        maxCount = ConversionHelper.getInt(PropertyUtil.getOptional("search.max.results"), maxCount);
    }

    public static void addChannel(String channelId, SearchableChannel searchableChannel) {
        searchService.addChannel(channelId, searchableChannel);
    }
    /**
     * Checks if search was legal
     * 
     * @param searchString
     * @return
     */
    public static String checkLegalSearch(String searchString) {

        if (searchString == null || searchString.isEmpty()) {
            return ERR_EMPTY;
        }
        searchString = Jsoup.clean(searchString, Whitelist.none());
        searchString = StringEscapeUtils.unescapeHtml(searchString);
        if (searchString.contains("*") && searchString.length() <= 4) {
            return ERR_TOO_SHORT;
        }
        if (ConversionHelper.count(searchString, "*") > 2) {
            return ERR_TOO_WILD;
        }
        return STR_TRUE;
    }

    /**
     * Does actual search
     * 
     * @param searchString
     * @param locale
     */
    public static JSONObject doSearch(final String searchString,
            final Locale locale) {
        String err = SearchWorker.checkLegalSearch(searchString);
        if (err == null) {
            throw new RuntimeException("Could not check search string");
        }
        if (!err.equals(STR_TRUE)) {
            JSONObject errJson = new JSONObject();
            try {
                errJson.put(KEY_TOTAL_COUNT, -1);
                errJson.put(KEY_ERROR_TEXT, err);
            } catch (JSONException jsonex) {
                throw new RuntimeException("Could not form error JSON");
            }
            return errJson;
        }

        final SearchCriteria sc = new SearchCriteria();
        sc.setSearchString(searchString);

        sc.setLocale(locale.getLanguage());
        if(defaultChannels.length == 0) {
            init();
        }

        for(String channelId : defaultChannels) {
            sc.addChannel(channelId);
        }

        return doSearch(sc);
    }
    
    public static JSONObject doSearch(final SearchCriteria sc) {
        
        Query query = searchService.doSearch(sc);

        List<SearchResultItem> items = new ArrayList<SearchResultItem>();
        for(String channelId : sc.getChannels()) {
            items.addAll(query.findResult(channelId).getSearchResultItems());
        }
        Collections.sort(items);

        JSONObject rootJson = new JSONObject();
        JSONArray itemArray = new JSONArray();

        int itemCount = 0;
        for (SearchResultItem sri : items) {
            if (itemCount >= maxCount) {
                if (items.size() > maxCount) {
                    try {
                        rootJson.put(KEY_HAS_MORE, true);
                    } catch (JSONException jsonex) {
                        throw new RuntimeException("Could not set"
                                + " hasMore in JSON");
                    }
                }
                break;
            }

            JSONObject itemJson = new JSONObject();

            JSONHelper.putValue(itemJson, KEY_ID, itemCount);

            // Name & coordinates
            String name = ConversionHelper.getString(sri.getTitle(), "N/A");
            boolean gotNameAndLocation = JSONHelper.putValue(itemJson, KEY_NAME, Jsoup.clean(name, Whitelist.none())) &&
                    JSONHelper.putValue(itemJson, KEY_LON, sri.getLon()) &&
                    JSONHelper.putValue(itemJson, KEY_LAT, sri.getLat());
            if(!gotNameAndLocation) {
                // didn't get name/location -> skip to next result
                log.warn("Didn't get name or location from search result item:", sri);
                continue;
            }
            // Rank
            JSONHelper.putValue(itemJson, KEY_RANK, sri.getRank());

            // Type
            JSONHelper.putValue(itemJson, KEY_TYPE, sri.getType());

            // Village (?)
            // TODO: Shouldn't this be 'municipality' or sth?
            String village = ConversionHelper.getString(sri.getVillage(), "");
            JSONHelper.putValue(itemJson, KEY_VILLAGE, Jsoup.clean(village, Whitelist.none()));

            // Zoom level
            JSONHelper.putValue(itemJson, KEY_ZOOMLEVEL, sri.getZoomLevel());
            itemArray.put(itemJson);

            // Success
            itemCount++;
        }

        try {
            rootJson.put(KEY_LOCATIONS, itemArray);
        } catch (JSONException jsonex) {
            throw new RuntimeException("Could not set search items in JSON");
        }

        try {
            rootJson.put(KEY_TOTAL_COUNT, itemCount);
        } catch (JSONException jsonex) {
            throw new RuntimeException("Could not set item count in JSON");
        }
        return rootJson;
    }
}
