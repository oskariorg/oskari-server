package fi.mml.portti.service.search;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.*;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Oskari
public class SearchServiceImpl extends SearchService implements SearchChannelChangeListener {

    /** logger */
    private static final Logger LOG = LogFactory.getLogger(SearchServiceImpl.class);

    /** Available channels */
    private volatile Map<String, SearchableChannel> availableChannels = null;

    /**
     * Inits channels
     */
    protected void initChannels() {
        final TreeMap<String, SearchableChannel> newChannels = new TreeMap<String, SearchableChannel>();
        LOG.debug("Initializing search channels");
        final Map<String, SearchChannel> annotatedChannels = OskariComponentManager.getComponentsOfType(SearchChannel.class);

        // get comma separated active channel IDs
        String[] activeChannelIDs = PropertyUtil.getCommaSeparatedList("search.channels");

        if (activeChannelIDs.length == 0) {
            LOG.warn("No search channels selected. Using all annotated channels");
            activeChannelIDs = annotatedChannels.keySet().toArray(new String[0]);
        }
        LOG.info("Instantiating search channels:", activeChannelIDs);

        for (String channelID : activeChannelIDs) {
            String cid = channelID.trim();
            SearchableChannel channel = annotatedChannels.get(cid);
            if (channel == null) {
                LOG.warn("Couldn't find annotated search channel for ID:", cid,
                        "- Change the searchchannel to extend fi.nls.oskari.search.channelSearchChannel with",
                        "@Oskari(\"[channel id]\") annotation");
                channel = getLegacyChannel(cid);
            }
            if (channel == null) {
                LOG.warn("Couldn't create search channel for ID:", cid);
                continue;
            }
            newChannels.put(channel.getId(), channel);
        }

        // providers (like WFS search channels)
        Map<String, ChannelProvider> providerList = OskariComponentManager.getComponentsOfType(ChannelProvider.class);
        for(ChannelProvider provider : providerList.values()) {
            for(SearchChannel channel :provider.getChannels()) {
                newChannels.put(channel.getId(), channel);
            }
            // subscribe to channel changes
            provider.addListener(this);
        }
        availableChannels = Collections.synchronizedSortedMap(newChannels);
    }

    @Override
    public void onAdd(SearchChannel channel) {
        availableChannels.put(channel.getId(), channel);
    }

    @Override
    public void onRemove(SearchChannel channel) {
        availableChannels.remove(channel.getId());
    }

    /**
     * Legacy support for registering search channels
     * @param cid
     * @return
     */
    private SearchableChannel getLegacyChannel(final String cid) {

        String className = PropertyUtil.getOptional("search.channel." + cid + ".className");
        if (className == null || className.trim().length() < 1) {
            LOG.error("Class name not found for search channel " + cid);
            return null;
        }
        try {
            // get class for channel
            Class c = Class.forName(className);
            // find a no args constructor for the class
            Constructor[] constructors = c.getConstructors();
            for (Constructor con : constructors) {
                if (con.getGenericParameterTypes().length == 0) {
                    con.setAccessible(true);
                    try {
                        // instantiate and register channel
                        SearchableChannel channel = (SearchableChannel)con.newInstance();
                        channel.init();
                        LOG.info("Instantiated search channel class " + className);
                        if (!cid.equals(channel.getId())) {
                            // This doesn't actually cause any harm ATM, but it might later on.
                            LOG.warn("Non-matching ID for search channel between properties and class: " + cid + " / " + channel.getId());
                        }
                        return channel;
                    } catch (InstantiationException ie) {
                        LOG.error("Couldn't instantiate class " + className + " for channel (InstantationException)");
                    } catch (IllegalAccessException iae) {
                        LOG.error("Couldn't instantiate class " + className + " for channel (IllegalAccessException)");
                    } catch (InvocationTargetException ite) {
                        LOG.error("Couldn't instantiate class " + className + " for channel (InvocationTargetException)");
                    }
                }
                LOG.error("Couldn't find a no-args constructor for search channel class " + className);
            }
        } catch (Exception cnfe) {
            LOG.error("Error constructing (legacy) searchchannel: " + cid + " = " + className);
        }
        return null;
    }

    private void addDefaults(SearchCriteria sc) {
        for(SearchableChannel channel : getAvailableChannels().values()) {
            if(channel.isDefaultChannel()) {
                sc.addChannel(channel.getId());
            }
        }
    }

    public Query doSearch(final SearchCriteria searchCriteria) {

        if (availableChannels == null) {
            initChannels();
        }
        if(searchCriteria.getChannels().isEmpty()) {
            addDefaults(searchCriteria);
        }

        if(searchCriteria.isReverseGeocode()) {
            LOG.debug("Reverse geocode for (lat:", searchCriteria.getLat(), ", lon:", searchCriteria.getLon(), ")");
        } else {
            LOG.debug("Search string is", searchCriteria.getSearchString());
        }

        long fullQueryStartTime = System.currentTimeMillis();

        if(LOG.isDebugEnabled()) {
            printsc(searchCriteria);
            printAvailableChannels();
        }

        final Query query = new Query();
        query.setSearchCriteria(searchCriteria);

        for (String channelId : searchCriteria.getChannels()) {
            if (!availableChannels.containsKey(channelId)) {
                continue;
            }
            long timeStart = System.currentTimeMillis();
            SearchableChannel channel = availableChannels.get(channelId);
            User user = searchCriteria.getUser();
            if(!channel.hasPermission(user)) {
                // Skipping
                LOG.debug("Skipping ", channel.getId(), "- User doesn't have permission to access");
                continue;
            }
            if(!channel.isValidSearchTerm(searchCriteria)) {
                // Skipping
                LOG.debug("Skipping ", channel.getId(), "- criteria not valid");
                continue;
            }
            ChannelSearchResult result = handleChannelSearch(searchCriteria, channel);
            int numResults = -1;
            if(result != null) {
                LOG.debug("Result", result);
                result.setChannelId(channel.getId());
                query.addChannelSearchResult(result);
                numResults = result.getNumberOfResults();
            }
            long timeEnd = System.currentTimeMillis();
            LOG.debug("Search query to", channel.getId(),
                    "took", (timeEnd - timeStart), "ms",
                    "- got", numResults, "results");
        }

        long fullQueryEndTime = System.currentTimeMillis();
        LOG.debug("Search full query took", (fullQueryEndTime - fullQueryStartTime), "ms");

        return query;
    }

    @Override
    public JSONObject doSearchAutocomplete(SearchCriteria searchCriteria) {
        if (availableChannels == null) {
            initChannels();
        }

        long fullQueryStartTime = System.currentTimeMillis();

        List<String> resultList = null;
        for (String channelId : searchCriteria.getChannels()) {
            if (!availableChannels.containsKey(channelId)) {
                continue;
            }

            long timeStart = System.currentTimeMillis();

            SearchableChannel channel = availableChannels.get(channelId);
            if (!(channel instanceof SearchAutocomplete)) {
                continue;
            }

            User user = searchCriteria.getUser();
            if(!channel.hasPermission(user)) {
                LOG.debug("Skipping ", channel.getId(), "- User doesn't have permission to access");
                continue;
            }


            try {
                 resultList = ((SearchAutocomplete) channel).doSearchAutocomplete(searchCriteria.getSearchString());
            } catch (Exception e) {
                LOG.error(e, "Search query to", channel.getId(), "failed! Searchstring was '", searchCriteria.getSearchString(), "'");
                resultList = new ArrayList<String>();
            }

            LOG.debug("Result", resultList);

            long timeEnd = System.currentTimeMillis();
            LOG.debug("Search query to", channel.getId(),
                    "took", (timeEnd - timeStart), "ms");
        }

        long fullQueryEndTime = System.currentTimeMillis();
        LOG.debug("Search full query took", (fullQueryEndTime - fullQueryStartTime), "ms");

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray(resultList);
        JSONHelper.put(jsonObject, "methods", jsonArray);
        return jsonObject;
    }

    /**
     * Handles actual channel search and catches exceptions
     *
     * @param sc
     * @param channel
     * @return
     */
    private ChannelSearchResult handleChannelSearch(
            SearchCriteria sc, SearchableChannel channel)
    {
        try {
            final ChannelSearchResult result;
            if(sc.isReverseGeocode() && channel.getCapabilities().canGeocode()) {
                result = channel.reverseGeocode(sc);
            } else if(channel.getCapabilities().canTextSearch()) {
                result = channel.doSearch(sc);
            } else {
                result = new ChannelSearchResult();
                result.setQueryFailed(true);
            }
            final List<SearchResultItem> items = result.getSearchResultItems();
            // calculate zoom scales etc common fields if we have an annotated (non-legacy) channel
            for(SearchResultItem item : items) {
                channel.calculateCommonFields(item);
            }
            return result;
        } catch (Exception e) {
            LOG.error(e, "Search query to", channel.getId(), "failed! Searchstring was '", sc.getSearchString(), "'");
            final ChannelSearchResult result = new ChannelSearchResult();
            result.setChannelId(channel.getId());
            result.setQueryFailed(true);
            return result;
        }
    }

    /**
     * Adds given channel to service
     *
     * @param channel
     */
    public void addChannel(String channel, SearchableChannel searchableChannel) {
        if (availableChannels == null) {
            initChannels();
        }
        availableChannels.put(channel, searchableChannel);
    }

    public Map<String, SearchableChannel> getAvailableChannels() {
        if (availableChannels == null) {
            initChannels();
        }
        return Collections.unmodifiableMap(availableChannels);
    }

    public boolean isAvailableAutocompleteChannels() {
        for (Map.Entry<String, SearchableChannel> channel : getAvailableChannels().entrySet()) {
            if ((channel.getValue() instanceof SearchAutocomplete)) {
                return true;
            }
        }
        return false;
    }

    private void printsc(SearchCriteria searchCriteria) {
        LOG.debug("printing SearchCriteria");

        try {
            LOG.debug("SearchString: " + searchCriteria.getSearchString());

            if (searchCriteria.getFromDate() == null) {
                LOG.debug("from date = null");
            } else {
                LOG.debug("Datefrom: " + searchCriteria.getFromDate().toString());
            }

            if (searchCriteria.getToDate() == null) {
                LOG.debug("from to = null");
            } else {
                LOG.debug("Dateto: " + searchCriteria.getToDate().toString());
            }

            for (String cha : searchCriteria.getChannels()) {
                LOG.debug("channel for searching: " + cha);
            }

            LOG.debug("printing parameters");
            java.util.Collection<String> set = searchCriteria.getParams().keySet();

            for (java.util.Iterator<String> iterator = set.iterator(); iterator.hasNext(); ) {
                LOG.debug("parm key: " + iterator.next());

            }
        } catch (Exception e) {
            LOG.debug("sc error");
            e.printStackTrace();
        }

        LOG.debug("/printing SearchCriteria");
    }

    private void printAvailableChannels() {
        LOG.debug("printing AvailableChannels");
        try {
            java.util.Collection<String> set = availableChannels.keySet();
            for (java.util.Iterator<String> iterator = set.iterator(); iterator.hasNext(); ) {
                LOG.debug("channel key: " + iterator.next());

            }
        } catch (Exception e) {
            LOG.debug("a error");
        }
        LOG.debug("/printing AvailableChannels");
    }
}
