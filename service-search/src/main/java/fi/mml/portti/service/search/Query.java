package fi.mml.portti.service.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Query that contains both search criteria and search results.
 */
public class Query implements Serializable {
	private static final long serialVersionUID = 5548549778564190366L;
	private SearchCriteria searchCriteria;
	private List<ChannelSearchResult> channelSearchResults;
	
	public Query() {
		channelSearchResults = new ArrayList<ChannelSearchResult>();
	}
	
	public SearchCriteria getSearchCriteria() {
		return searchCriteria;
	}
	public void setSearchCriteria(SearchCriteria searchCriteria) {
		this.searchCriteria = searchCriteria;
	}
	public void addChannelSearchResult(ChannelSearchResult result) {
		channelSearchResults.add(result);		
	}
	public List<ChannelSearchResult> getResults() {
	    // check the order here
	    return channelSearchResults;
	}

	public ChannelSearchResult findResult(String channel) {
		for(ChannelSearchResult csr: channelSearchResults) {
			if (csr.getChannelId().equals(channel)) {
				return csr;
			}
		}
		// else return empty result
		return new ChannelSearchResult();
	}
}
