package fi.nls.oskari.map.data.service;

import com.ibatis.sqlmap.client.SqlMapClient;
import fi.nls.oskari.domain.map.PublishedMapUrl;
import fi.nls.oskari.domain.map.PublishedMapUsage;
import fi.nls.oskari.service.db.BaseIbatisService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.sql.SQLException;
import java.util.List;

public class PublishedMapRestrictionServiceImpl extends BaseIbatisService<PublishedMapUsage> implements PublishedMapRestrictionService {

    private long usageLimit = -1;

    public PublishedMapRestrictionServiceImpl() {
        usageLimit = ConversionHelper.getLong(PropertyUtil.get("view.published.usage.limit"), usageLimit);
    }

	@Override
	protected String getNameSpace() {
		return "PublishedMapUsage";
	}

	public boolean isServiceCountExceeded(final List<Integer> publishedMapIds) {
        int counter = 0;
        // TODO: refactor to make single select instead of looping!
		for (int publishedMapId : publishedMapIds) {
			Object usageCount = queryForObject(
					getNameSpace() + ".selectUsageCount", new Integer(publishedMapId));
			
			if (usageCount != null) {
				counter += (Integer) usageCount;
			}
		}

		return usageLimit != -1 && counter > usageLimit;
	}

	public boolean isPublishedMapLocked(int publishedMapId) {
		Object publishedMapLocked = queryForObject(
				getNameSpace() + ".selectForceLock", new Integer(publishedMapId));
		
		if (publishedMapLocked == null) {
			return false;
		}
		
		return (Boolean) publishedMapLocked;
	}

	public PublishedMapUsage findByPublishedMapId(int publishedMapId) {
		PublishedMapUsage publishedMapUsage = queryForObject(
				getNameSpace() + ".findByPublishedMapId", new Integer(publishedMapId));
		return publishedMapUsage;
	}

	public List<PublishedMapUrl> findPublishedMapUrlsById(int publishedMapId) {
		List<PublishedMapUrl> publishedMapUrls = queryForList(
				getNameSpace() + ".findPublishedMapUrlsById", new Integer(publishedMapId));
		return publishedMapUrls;
	}
	
	public void deleteUsageCountOfTotalLifecycle(int id) {
		delete(getNameSpace() + ".deleteTotalCount", id);
	}

	public int insertUsageCountOfTotalLifecycle(
			PublishedMapUsage publishedMapUsage) {
		
		SqlMapClient client = null;
		try {
			client = getSqlMapClient();
			client.startTransaction();
			client.insert(getNameSpace() + ".insertTotalCount", publishedMapUsage);			
			Integer id = (Integer) client.queryForObject(getNameSpace() + ".maxIdStatistics"); 
			client.commitTransaction();
			return id;
		} catch (Exception e) {
			throw new RuntimeException("Failed to insert", e);
		} finally {
			if (client != null) {
				try {
					client.endTransaction();
				} catch (SQLException e) {
					// forget
				}
			}
		} 
	}

	public Integer findUsageCountOfTotalLifecycle(int publishedMapId) {
		Integer usageCountOfTotalLifecycle = queryForObject(
				getNameSpace() + ".findTotalCountById", new Integer(publishedMapId));
		return usageCountOfTotalLifecycle;
	}
}
