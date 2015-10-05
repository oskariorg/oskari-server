package fi.nls.oskari.printout.breeding;

import com.vividsolutions.jts.geom.Polygon;
import org.geowebcache.grid.BoundingBox;

import java.util.Date;


public class ChangeSetEntry implements Constants {
	Long id = null;

	Date timestamp = new Date();
	double resolution;
	int index;
	long[] gridCov;
	long[] closestRectangle;
	long[] srcRectangle;
	BoundingBox srcBounds;
	long tileCount;
	Polygon p;
	private String layerName = null;
	private String gridSubsetName = null;

	Status status = Status.INITIAL;

	int percentComplete = 0;

	public ChangeSetEntry(double d, int srcIdx, long[] gridCov,
			long[] closestRectangle, long[] srcRectangle,
			BoundingBox srcBounds, Polygon p) {
		this.resolution = d;
		this.index = srcIdx;
		this.gridCov = gridCov;
		this.closestRectangle = closestRectangle;
		this.srcRectangle = srcRectangle;
		this.srcBounds = srcBounds;
		this.tileCount = 0;
		this.p = p;

	}

	public void addTileRequest(int i, BoundingBox gridLocBounds, long[] gridLoc) {
		tileCount++;
	}

	public long[] getClosestRectangle() {
		return closestRectangle;
	}
	public long[] getGridCov() {
		return gridCov;
	}

	public String getGridSubsetName() {
		return gridSubsetName;
	}

	public Long getId() {
		return id;
	}

	public int getIndex() {
		return index;
	}

	public String getLayerName() {
		return layerName;
	}

	public Polygon getP() {
		return p;
	}

	public int getPercentComplete() {
		return percentComplete;
	}

	public double getResolution() {
		return resolution;
	}

	public BoundingBox getSrcBounds() {
		return srcBounds;
	}

	public long[] getSrcRectangle() {
		return srcRectangle;
	}

	public Status getStatus() {
		return status;
	}

	public long getTileCount() {
		return tileCount;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setClosestRectangle(long[] closestRectangle) {
		this.closestRectangle = closestRectangle;
	}

	public void setGridCov(long[] gridCov) {
		this.gridCov = gridCov;
	}

	
	public void setGridSubsetName(String gridSubsetName) {
		this.gridSubsetName = gridSubsetName;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public void setP(Polygon p) {
		this.p = p;
	}

	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public void setSrcBounds(BoundingBox srcBounds) {
		this.srcBounds = srcBounds;
	}

	public void setSrcRectangle(long[] srcRectangle) {
		this.srcRectangle = srcRectangle;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setTileCount(long tileCount) {
		this.tileCount = tileCount;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

}
