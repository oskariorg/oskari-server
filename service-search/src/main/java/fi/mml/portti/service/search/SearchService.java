package fi.mml.portti.service.search;

import fi.nls.oskari.search.channel.SearchableChannel;
import fi.nls.oskari.service.OskariComponent;

import java.util.Map;


/**
 * Interface to service that searches all the channels.
 */
public abstract class SearchService extends OskariComponent {

	/**
	 * Makes a search with given criteria
	 * 
	 * @param searchCriteria
	 * @return Query
	 */
	public abstract Query doSearch(SearchCriteria searchCriteria);
	public abstract void addChannel(String channelId, SearchableChannel searchableChannel);
    public abstract Map<String, SearchableChannel> getAvailableChannels();
}