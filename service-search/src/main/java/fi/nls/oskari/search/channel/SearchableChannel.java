package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.domain.User;
import org.json.JSONObject;

/**
 * Interface to search service of a single channel.
 */
public interface SearchableChannel {

    enum Capabilities {
        TEXT,
        COORD,
        BOTH;

        public boolean canGeocode() {
            return this.equals(COORD) ||
                    this.equals(BOTH);
        }
        public boolean canTextSearch() {
            return this.equals(TEXT) ||
                    this.equals(BOTH);
        }
    }
    /**
     * Make a search on the channel implementation
     * @param searchCriteria
     * @return
     * @throws IllegalSearchCriteriaException
     */
	ChannelSearchResult doSearch(SearchCriteria searchCriteria) throws IllegalSearchCriteriaException;

    /**
     * Make reverse geocoding using the channel implementation
     * @param searchCriteria
     * @return
     * @throws IllegalSearchCriteriaException
     */
    ChannelSearchResult reverseGeocode(SearchCriteria searchCriteria) throws IllegalSearchCriteriaException;

    /**
     * Returns an id for the search channel
     * @return
     */
    String getId();

    /**
     * Should this channel be included when searching without specifying channels
     * @return
     */
    boolean isDefaultChannel();

    /**
     * Is the user allowed to use this channel.
     * @return
     */
    boolean hasPermission(User user);

    /**
     * JSON presentation of channel localization like
     * {
     *     "en" : {
     *         "name" : "Channel name",
     *         "desc" : "This channel is used for..."
     *     }
     * }
     *
     * Defaults to name as channel ID for the default language
     * @return
     */
    JSONObject getUILabels();

    /**
     * Called when channel is initialized
     */
    void init();

    /**
     * Should return whether channel can be used for searching with text, coordinates (reverse geocode) or both
     * @return
     */
    Capabilities getCapabilities();

    /**
     * Validates the input if its usable for this channel
     * @param criteria
     * @return
     */
    boolean isValidSearchTerm(SearchCriteria criteria);

    /**
     * Setup zoomlevel based on item type etc
     * @param item
     */
    void calculateCommonFields(final SearchResultItem item);
}