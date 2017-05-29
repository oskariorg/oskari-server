package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Oskari(RahuAddressChannelSearchService.ID)
public class RahuAddressChannelSearchService extends BaseWfsAddressChannelSearchService {

    private Logger log = LogFactory.getLogger(this.getClass());

    public static final String ID = "RAHU_ADDRESS_CHANNEL";
    private static final String PROPERTY_SERVICE_URL = "search.channel.RAHU_ADDRESS_CHANNEL.query.url";

    private String queryURL = null;

    @Override
    public void init() {
        super.init();
        queryURL = PropertyUtil.getOptional(PROPERTY_SERVICE_URL);
        log.debug("ServiceURL set to " + queryURL);
    }

	@Override
	protected String getQueryUrl(String filter, int maxResults) {
        if(queryURL == null) {
            return null;
        }
		return queryURL + filter;
	}

	@Override
	protected ChannelSearchResult filterResultsAfterQuery(
			ChannelSearchResult csr) {
		/* Rahu can return multiple addresses that must be removed.
		 * we are comparing "title" field and skip all titles that
		 * are already in results */
		Map<String, String> alreadyFoundItems = new HashMap<String, String>();
		List<SearchResultItem> results = csr.getSearchResultItems();
		List<SearchResultItem> finalResults = new ArrayList<SearchResultItem>();
		for(SearchResultItem item: results) {
			String key = item.getTitle()+item.getRegion();
			if (!alreadyFoundItems.containsKey(key)) {
				finalResults.add(item);
				alreadyFoundItems.put(key, item.getTitle());
			}
		}
		
		csr.setSearchResultItems(finalResults);
		return csr;
	}

	@Override
	protected String getType() {
		return "Rakennus";
	}

}
