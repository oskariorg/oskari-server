package fi.nls.oskari.domain.map;

public class PublishedMapUsage {

	private int publishedMapId;
	
	private int usageCount;
	
	private int usageCountOfTotalLifecycle;
	
	private boolean forceLock;

	public int getPublishedMapId() {
		return publishedMapId;
	}

	public void setPublishedMapId(int publishedMapId) {
		this.publishedMapId = publishedMapId;
	}

	public int getUsageCount() {
		return usageCount;
	}

	public void setUsageCount(int usageCount) {
		this.usageCount = usageCount;
	}
	
	public int getUsageCountOfTotalLifecycle() {
		return usageCountOfTotalLifecycle;
	}
	
	public void setUsageCountOfTotalLifecycle(int usageCountOfTotalLifecycle) {
		this.usageCountOfTotalLifecycle = usageCountOfTotalLifecycle;
	}
	
	public boolean isForceLock() {
		return forceLock;
	}

	public void setForceLock(boolean forceLock) {
		this.forceLock = forceLock;
	}
	
	
}
