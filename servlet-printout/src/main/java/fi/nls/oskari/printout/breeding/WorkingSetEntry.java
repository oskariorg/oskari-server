package fi.nls.oskari.printout.breeding;

import java.util.Date;

import org.geowebcache.grid.BoundingBox;

class WorkingSetEntry {
	Long id = null;

	Date timestamp = new Date();
	protected Integer level;
	protected Long numberOfTiles = 0L;

	WorkingSetEntry(Integer level, BoundingBox bbox, Long count) {
		this.level = level;
		increment(bbox, count);
	}

	public Long getId() {
		return id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void increment(BoundingBox bbox, Long count) {
		numberOfTiles += count;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	
	public String toString() {

		return "[" + level + "," + numberOfTiles + "]";
	}

}
