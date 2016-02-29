package fi.mml.portti.service.search;

import fi.nls.oskari.search.channel.SearchableChannel;

import java.util.Map;


/**
 * Interface to service that searches all the channels.
 */
public interface SearchService {

	/**
	 * Makes a search with given criteria
	 * 
	 * @param searchCriteria
	 * @return Query
	 */
	public Query doSearch(SearchCriteria searchCriteria);
	public void addChannel(String channelId, SearchableChannel searchableChannel);
    public Map<String, SearchableChannel> getAvailableChannels();
}