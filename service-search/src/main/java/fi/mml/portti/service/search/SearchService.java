package fi.mml.portti.service.search;

import fi.nls.oskari.search.channel.SearchableChannel;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

import java.util.Map;

/**
 * Interface to service that searches all the channels.
 */
public abstract class SearchService extends OskariComponent {

    private int maxCount = -1;
    private int hardLimit = -1;

    @Override
    public void init() {
        super.init();
    }

    /**
     * Makes a search with given criteria
     *
     * @param searchCriteria
     * @return Query
     */
    public abstract Query doSearch(SearchCriteria searchCriteria);
    public abstract JSONObject doSearchAutocomplete(SearchCriteria searchCriteria);
    public abstract void addChannel(String channelId, SearchableChannel searchableChannel);
    public abstract Map<String, SearchableChannel> getAvailableChannels();

    /**
     * Returns a generic maximum results instruction for search functions.
     * SearchChannels/implementations may opt to use this to
     * Can be configured with search.max.results property.
     * Defaults to 100.
     * @return maxCount  - Search results max count
     */
    public int getMaxResultsCount() {
        if (maxCount == -1) {
            maxCount = ConversionHelper.getInt(PropertyUtil.getOptional("search.max.results"), 100);
        }
        if (hardLimit > 0 && maxCount > hardLimit) {
            // sanity-check limit can't be bigger than hard limit if it has been configured
            maxCount = hardLimit;
        }
        return maxCount;
    }

    /**
     * Returns instance limit for maximum results instruction for search functions.
     * SearchChannels/implementations may opt to use this to.
     * Can be configured with search.max.results.hardlimit property.
     * Defaults to 10x max count.
     * @return hard limit for search results. Client can't ask more than this
     */
    public int getMaxResultsHardLimit() {
        if (hardLimit == -1) {
            hardLimit = ConversionHelper.getInt(PropertyUtil.getOptional("search.max.results.hardlimit"), getMaxResultsCount() * 10);
        }
        return hardLimit;
    }
}
