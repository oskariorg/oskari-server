package fi.mml.portti.service.search;

import fi.nls.oskari.search.channel.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SearchServiceImpl implements SearchService {

    /** logger */
    private static Logger log = LogFactory.getLogger(SearchServiceImpl.class);

    /** Available channels */
    private Map<String, SearchableChannel> availableChannels = null;

    /**
     * Inits channels
     */
    protected void initChannels() {
        log.debug("Initializing search channels");
        availableChannels = new TreeMap<String, SearchableChannel>();
        // get comma separated active channel IDs
        final String[] activeChannelIDs = PropertyUtil.getCommaSeparatedList("search.channels");

        if (activeChannelIDs.length == 0) {
            log.warn("No search channels selected.");
            return;
        } else {
            log.info("Instantiating search channels:", activeChannelIDs);
        }

        // get class names for active channels, assume that channel IDs don't contain special characters
        //String regex = "^search\\.channel\\.(" + StringUtils.join(activeChannelIDs, ",") + ")\\.className\\b";
        //List <String> channelKeys = PropertyUtil.getMatchingPropertyNames(regex);
        for(String channelID : activeChannelIDs) {
            String cid = channelID.trim();
            String className = PropertyUtil.get("search.channel." + cid + ".className", null);
            if (null == className || className.length() < 1) {
                log.error("Class name not found for search channel " + cid);
                continue;
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
                            addChannel(channel.getId(), channel);
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
                            break;
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
            } catch (ClassNotFoundException cnfe) {
                log.error("Invalid className for channel: " + cid + " = " + className);
            }
        }
    }

    public Query doSearch(final SearchCriteria searchCriteria) {

        String searchString = searchCriteria.getSearchString();
        log.debug("Search string is", searchString);
        searchCriteria.setSearchString(searchString);

        synchronized (this) {
            if (availableChannels == null) {
                initChannels();
            } else {
                log.debug("Search channels already initialized");
            }
        }

        long fullQueryStartTime = System.currentTimeMillis();

        Query query = new Query();
        query.setSearchCriteria(searchCriteria);
        //availableChannels.keySet()
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
            SearchCriteria searchCriteria, SearchableChannel actualChannel) {
        try {
            ChannelSearchResult result = actualChannel.doSearch(searchCriteria);
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
        synchronized (this) {
            if (availableChannels == null) {
                if (availableChannels == null) {
                    initChannels();
                }
            }
        }
        availableChannels.put(channel, searchableChannel);
    }

    public Map<String, SearchableChannel> getAvailableChannels() {
        synchronized (this) {
            if (availableChannels == null) {
                if (availableChannels == null) {
                    initChannels();
                }
            }
        }
        // TODO: return immutable map
        return availableChannels;
    }
}
