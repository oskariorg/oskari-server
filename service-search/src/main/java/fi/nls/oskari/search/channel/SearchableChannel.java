package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;

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
	ChannelSearchResult doSearch(SearchCriteria searchCriteria) throws IllegalSearchCriteriaException;

    /**
     * Returns an id for the search channel
     * @return
     */
    String getId();

    /**
     * Called when channel is initialized
     */
    void init();

    /**
     * Should return true if channel implements reverse geocoding
     * @return
     */
    boolean hasReverseGeocode();

    /**
     * Validates the input if its usable for this channel
     * @param criteria
     * @return
     */
    boolean isValidSearchTerm(SearchCriteria criteria);

    /**
     * Make reverse geocoding using the channel implementation
     * @param lon
     * @param lat
     * @param srs
     * @return
     * @throws IllegalSearchCriteriaException
     */
    ChannelSearchResult doSearch(double lon, double lat, final String srs) throws IllegalSearchCriteriaException;

    /**
     * Setup zoomlevel based on item type etc
     * @param item
     */
    void calculateCommonFields(final SearchResultItem item);
}