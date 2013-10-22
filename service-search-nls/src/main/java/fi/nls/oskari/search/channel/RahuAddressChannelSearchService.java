package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RahuAddressChannelSearchService extends BaseWfsAddressChannelSearchService {

    private Logger log = LogFactory.getLogger(this.getClass());

    private String queryURL = null;

    public void setProperty(String propertyName, String propertyValue) {
        if ("query.url".equals(propertyName)) {
            queryURL = propertyValue;
            log.debug("QueryURL set to " + queryURL);
        } else {
            log.warn("Unknown property for " + ID + " search channel: " + propertyName);
        }
    }
	
	@Override
	protected String getQueryUrl(String filter) {
		return queryURL + filter;
	}

    public static final String ID = "RAHU_ADDRESS_CHANNEL"; 

    public String getId() {
        return ID;
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
			String key = item.getTitle()+item.getVillage();
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
