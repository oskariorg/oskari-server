package fi.nls.oskari.map.data.service;

import fi.nls.oskari.domain.map.PublishedMapUrl;
import fi.nls.oskari.domain.map.PublishedMapUsage;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

public interface PublishedMapRestrictionService extends BaseService<PublishedMapUsage> {

	/**
	 * Method will check if service counts are over usage limits.
	 * 
	 * @param publishedMapIds List of integers that are identifiers of published maps
	 * @return true or false depending if given published maps are reached their service count
	 */
	public boolean isServiceCountExceeded(List<Integer> publishedMapIds);
	
	/**
	 * Method will check if published map with given identifier is forced to be closed.
	 * 
	 * @param publishedMapId published map identifier
	 * @return TRUE if published map with given identifier is forced to be closed; FALSE otherwise
	 */
	public boolean isPublishedMapLocked(int publishedMapId);
	
	public PublishedMapUsage findByPublishedMapId(int publishedMapId);
	
	public List<PublishedMapUrl> findPublishedMapUrlsById(int publishedMapId);
	
	public int insertUsageCountOfTotalLifecycle(PublishedMapUsage publishedMapUsage);
	
	public void deleteUsageCountOfTotalLifecycle(int id);
	
	public Integer findUsageCountOfTotalLifecycle(int publishedMapId);
}
