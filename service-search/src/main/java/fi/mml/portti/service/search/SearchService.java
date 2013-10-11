package fi.mml.portti.service.search;

import java.util.Map;

import fi.nls.oskari.search.channel.SearchableChannel;


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