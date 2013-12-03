package fi.mml.portti.service.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Search result list of single channel.
 */
public class ChannelSearchResult  implements Serializable {
	
	private static final long serialVersionUID = -6212441908137008495L;
	private String channelId;
	private boolean available;
	private boolean truncated;
	private boolean queryFailed;

	private List<SearchResultItem> searchResultItems = new ArrayList<SearchResultItem>();
	
	public ChannelSearchResult() {
		queryFailed = false;
	}
	
	public boolean isQueryFailed() {
		return queryFailed;
	}

	public void setQueryFailed(boolean queryFailed) {
		this.queryFailed = queryFailed;
	}

	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channel) {
		this.channelId = channel;
	}
	public boolean isAvailable() {
		return available;
	}
	public void setAvailable(boolean available) {
		this.available = available;
	}
	public boolean isTruncated() {
		return truncated;
	}
	public void setTruncated(boolean truncated) {
		this.truncated = truncated;
	}
	public int getNumberOfResults() {
		return searchResultItems.size();
	}
	
	public List<SearchResultItem> getSearchResultItems() {
		return searchResultItems;
	}
	public void setSearchResultItems(List<SearchResultItem> searchResultItems) {
		this.searchResultItems = searchResultItems;
	}
	
	public void addItem(SearchResultItem rsi) {
		searchResultItems.add(rsi);
	}

	public void setException(Exception e) {
		// TODO Auto-generated method stub
		
	}
	
	
}