package fi.mml.portti.service.search;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.oskari.search.channel.SearchableChannel;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SearchServiceImpl implements SearchService {

    /** logger */
    private static Logger log = LogFactory.getLogger(SearchServiceImpl.class);

    /** Available channels */
    private volatile Map<String, SearchableChannel> availableChannels = null;

    /**
     * Inits channels
     */
    protected void initChannels() {
        final TreeMap<String, SearchableChannel> newChannels = new TreeMap<String, SearchableChannel>();
        log.debug("Initializing search channels");
        final Map<String, SearchChannel> annotatedChannels = OskariComponentManager.getComponentsOfType(SearchChannel.class);
        // get comma separated active channel IDs
        String[] activeChannelIDs = PropertyUtil.getCommaSeparatedList("search.channels");

        if (activeChannelIDs.length == 0) {
            log.warn("No search channels selected. Using all annotated channels");
            activeChannelIDs = annotatedChannels.keySet().toArray(new String[0]);
        }
        log.info("Instantiating search channels:", activeChannelIDs);

        for (String channelID : activeChannelIDs) {
            String cid = channelID.trim();
            SearchableChannel channel = annotatedChannels.get(cid);
            if (channel == null) {
                log.warn("Couldn't find annotated search channel for ID:", cid,
                        "- Change the searchchannel to extend fi.nls.oskari.search.channelSearchChannel with",
                        "@Oskari(\"[channel id]\") annotation");
                channel = getLegacyChannel(cid);
            }
            if (channel == null) {
                log.warn("Couldn't create search channel for ID:", cid);
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
            log.error("Class name not found for search channel " + cid);
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
                        log.info("Instantiated search channel class " + className);
                        if (!cid.equals(channel.getId())) {
                            // This doesn't actually cause any harm ATM, but it might later on.
                            log.warn("Non-matching ID for search channel between properties and class: " + cid + " / " + channel.getId());
                        }
                        // TODO insert other properties with reflection
                        // get all properties for channel excluding className
                        String regex = "^search\\.channel\\." + cid + "\\.(?!className).*\\b";
                        List<String> propertyKeys = PropertyUtil.getMatchingPropertyNames(regex);
                        for (String propertyKey : propertyKeys) {
                            channel.setProperty(propertyKey.substring(StringUtils.ordinalIndexOf(propertyKey, ".", 3)+1), PropertyUtil.get(propertyKey));
                        }
                        return channel;
                    } catch (InstantiationException ie) {
                        log.error("Couldn't instantiate class " + className + " for channel (InstantationException)");
                    } catch (IllegalAccessException iae) {
                        log.error("Couldn't instantiate class " + className + " for channel (IllegalAccessException)");
                    } catch (InvocationTargetException ite) {
                        log.error("Couldn't instantiate class " + className + " for channel (InvocationTargetException)");
                    }
                }
                log.error("Couldn't find a no-args constructor for search channel class " + className);
            }
        } catch (Exception cnfe) {
            log.error("Error constructing (legacy) searchchannel: " + cid + " = " + className);
        }
        return null;
    }

    public Query doSearch(final SearchCriteria searchCriteria) {

        String searchString = searchCriteria.getSearchString();
        log.debug("Search string is", searchString);
        searchCriteria.setSearchString(searchString);

        if (availableChannels == null) {
            initChannels();
        } else {
            log.debug("Search channels already initialized");
        }

        long fullQueryStartTime = System.currentTimeMillis();

        if(log.isDebugEnabled())
            printsc(searchCriteria);

        if(log.isDebugEnabled())
            printAvailableChannels();

        Query query = new Query();
        query.setSearchCriteria(searchCriteria);

        for (String channel : searchCriteria.getChannels()) {
            if (availableChannels.containsKey(channel)) {
                long timeStart = System.currentTimeMillis();
                SearchableChannel channelImplementation = availableChannels.get(channel);
                log.debug("Channel", channelImplementation);

                ChannelSearchResult result = handleChannelSearch(
                        searchCriteria, channelImplementation);
                log.debug("Result", result);
                result.setChannelId(channelImplementation.getId());

                query.addChannelSearchResult(result);
                long timeEnd = System.currentTimeMillis();
                log.debug("Search query to", channelImplementation.getId(),
                        "took", (timeEnd - timeStart), "ms",
                        "- got", result.getNumberOfResults(), "results" );
            }
        }

        long fullQueryEndTime = System.currentTimeMillis();
        log.debug("Search full query took", (fullQueryEndTime - fullQueryStartTime), "ms" );

        return query;
    }

    /**
     * Handles actual channel search and catches exceptions
     *
     * @param searchCriteria
     * @param actualChannel
     * @return
     */
    private ChannelSearchResult handleChannelSearch(
            SearchCriteria searchCriteria, SearchableChannel actualChannel)
    {
        try {
            ChannelSearchResult result = actualChannel.doSearch(searchCriteria);
            List<SearchResultItem> items = result.getSearchResultItems();
            // calculate zoom scales etc common fields if we have an annotated (non-legacy) channel
            if(actualChannel instanceof SearchChannel) {
                SearchChannel channel = (SearchChannel) actualChannel;
                for(SearchResultItem item : items) {
                    channel.calculateCommonFields(item);
                }
            }
            return result;
        } catch (Exception e) {
            log.error(e, "Search query to", actualChannel.getId(),
                    "failed! Searchstring was '", searchCriteria.getSearchString(), "'" );
            ChannelSearchResult result = new ChannelSearchResult();
            result.setChannelId(actualChannel.getId());
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
        log.debug("printing SearchCriteria");

        try {
            log.debug("SearchString: " + searchCriteria.getSearchString());

            if (searchCriteria.getFromDate() == null) {
                log.debug("from date = null");
            } else {
                log.debug("Datefrom: " + searchCriteria.getFromDate().toString());
            }

            if (searchCriteria.getToDate() == null) {
                log.debug("from to = null");
            } else {
                log.debug("Dateto: " + searchCriteria.getToDate().toString());
            }

            for (String cha : searchCriteria.getChannels()) {
                log.debug("channel for searching: " + cha);
            }

            log.debug("printing parameters");
            java.util.Collection<String> set = searchCriteria.getParams().keySet();

            for (java.util.Iterator<String> iterator = set.iterator(); iterator.hasNext(); ) {
                log.debug("parm key: " + (String) iterator.next());

            }
        } catch (Exception e) {
            log.debug("sc error");
            e.printStackTrace();
        }

        log.debug("/printing SearchCriteria");
    }

    private void printAvailableChannels() {
        log.debug("printing AvailableChannels");
        try {
            java.util.Collection<String> set = availableChannels.keySet();
            for (java.util.Iterator<String> iterator = set.iterator(); iterator.hasNext(); ) {
                log.debug("channel key: " + (String) iterator.next());

            }
        } catch (Exception e) {
            log.debug("a error");
        }
        log.debug("/printing AvailableChannels");
    }
}
