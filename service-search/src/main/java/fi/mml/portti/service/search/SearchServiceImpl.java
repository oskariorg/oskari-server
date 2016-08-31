package fi.mml.portti.service.search;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.oskari.search.channel.SearchableChannel;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Oskari
public class SearchServiceImpl extends SearchService {

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
        availableChannels = Collections.synchronizedSortedMap(newChannels);
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

    public Query doSearch(final SearchCriteria searchCriteria) {

        if (availableChannels == null) {
            initChannels();
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
        // TODO: return immutable map
        return availableChannels;
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
                LOG.debug("parm key: " + (String) iterator.next());

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
                LOG.debug("channel key: " + (String) iterator.next());

            }
        } catch (Exception e) {
            LOG.debug("a error");
        }
        LOG.debug("/printing AvailableChannels");
    }
}
