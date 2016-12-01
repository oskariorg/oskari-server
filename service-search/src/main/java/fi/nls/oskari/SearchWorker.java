package fi.nls.oskari;

import fi.mml.portti.service.search.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.SearchableChannel;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SearchWorker {

    public static final String KEY_TOTAL_COUNT = "totalCount";
    public static final String KEY_ERROR_TEXT = "errorText";
    public static final String KEY_LOCATIONS = "locations";
    public static final String KEY_METHODS = "methods";
    public static final String KEY_HAS_MORE = "hasMore";


    public static final String ERR_EMPTY = "cannot_be_empty";
    public static final String ERR_TOO_SHORT = "too_short";
    public static final String ERR_TOO_WILD = "too_many_stars";

    public static final String STR_TRUE = "true";
    public static final String STR_NULL = "null";
    /** Our service */
    private static SearchService searchService = new SearchServiceImpl();

    private final static Logger log = LogFactory.getLogger(SearchWorker.class);

    private static String[] defaultChannels = new String[0];

    public static void init() {
        defaultChannels = PropertyUtil.getCommaSeparatedList("search.channels.default");
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
        JSONArray methodArray = new JSONArray();
        for(String channelId : sc.getChannels()) {
            //log.debug("channelId = " + channelId);
            items.addAll(query.findResult(channelId).getSearchResultItems());
            methodArray.put(JSONHelper.createJSONObject(channelId,query.findResult(channelId).getSearchMethod()));
        }
        Collections.sort(items);

        JSONObject rootJson = new JSONObject();
        JSONArray itemArray = new JSONArray();

        int maxResults = searchService.getMaxResultsCount();
        int itemCount = 0;
        for (SearchResultItem sri : items) {
            if (itemCount >= maxResults) {
                if (items.size() > maxResults) {
                    try {
                        rootJson.put(KEY_HAS_MORE, true);
                    } catch (JSONException jsonex) {
                        throw new RuntimeException("Could not set"
                                + " hasMore in JSON");
                    }
                }
                break;
            }
            if(!sri.hasNameAndLocation()) {
                // didn't get name/location -> skip to next result
                log.warn("Didn't get name or location from search result item:", sri);
                continue;
            }

            itemArray.put(sri.toJSON(itemCount));

            // Success
            itemCount++;
        }

        try {
            rootJson.put(KEY_LOCATIONS, itemArray);
        } catch (JSONException jsonex) {
            throw new RuntimeException("Could not set search items in JSON");
        }

        try {
            rootJson.put(KEY_METHODS, methodArray);
        } catch (JSONException jsonex) {
            throw new RuntimeException("Could not set search method items in JSON");
        }

        try {
            rootJson.put(KEY_TOTAL_COUNT, itemCount);
        } catch (JSONException jsonex) {
            throw new RuntimeException("Could not set item count in JSON");
        }
        return rootJson;
    }
}
