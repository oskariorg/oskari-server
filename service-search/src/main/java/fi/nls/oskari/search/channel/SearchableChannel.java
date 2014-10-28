package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;

/**
 * Interface to search service of a single channel.
 */
public interface SearchableChannel {
    /**
     * Make a search on the channel implementation
     * @param searchCriteria
     * @return
     * @throws IllegalSearchCriteriaException
     */
	public ChannelSearchResult doSearch(SearchCriteria searchCriteria) throws IllegalSearchCriteriaException;

    /**
     * Returns an id for the search channel
     * @return
     */
    public String getId();

    /**
     * Hook method to setup properties for the
     * @param propertyName
     * @param propertyValue
     */
    @Deprecated
    public void setProperty(String propertyName, String propertyValue);
}