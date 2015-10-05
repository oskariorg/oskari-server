package fi.nls.oskari.printout.breeding;

import com.vividsolutions.jts.geom.Polygon;
import org.geowebcache.grid.BoundingBox;

import java.util.Date;
import java.util.Vector;


public class ChangeSet implements Constants {
	Long id = null;

	Date timestamp = new Date();

	private String identifier = null;

	private String layerName = null;

	private String gridSubsetName = null;

	private Polygon p = null;

	private BoundingBox reqBounds = null;

	private Vector<Double> resolutions = null;
	private Vector<ChangeSetEntry> changeSetEntries = new Vector<ChangeSetEntry>();
	int zoomStart;
	int zoomStop;
	int tileWidth;
	int tileHeight;
	Status status = Status.INITIAL;
	int percentComplete = 0;
	public ChangeSet(String layerName, String gridSubsetName, Polygon p,
			BoundingBox reqBounds, Vector<Double> resolutions, int zoomStart,
			int zoomStop, int tileWidth, int tileHeight) {
		this.layerName = layerName;
		this.gridSubsetName = gridSubsetName;
		this.p = p;
		this.reqBounds = reqBounds;
		this.resolutions = resolutions;
		this.zoomStart = zoomStart;
		this.zoomStop = zoomStop;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}
	public ChangeSetEntry addEntry(double d, int srcIdx, long[] gridCov,
			long[] closestRectangle, long[] srcRectangle,
			BoundingBox srcBounds, Polygon p) {

		ChangeSetEntry changeSetEntry = new ChangeSetEntry(d, srcIdx, gridCov,
				closestRectangle, srcRectangle, srcBounds, p);
		changeSetEntries.add(changeSetEntry);

		return changeSetEntry;

	}
	public Vector<ChangeSetEntry> getChangeSetEntries() {
		return changeSetEntries;
	}
	public String getGridSubsetName() {
		return gridSubsetName;
	}

	public Long getId() {
		return id;
	}
	public String getIdentifier() {
		return identifier;
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

	public Polygon getPolygon() {
		return p;
	}

	public BoundingBox getReqBounds() {
		return reqBounds;
	}

	public Vector<Double> getResolutions() {
		return resolutions;
	}

	public Status getStatus() {
		return status;
	}

	public int getTileHeight() {
		return tileHeight;
	}

	public int getTileWidth() {
		return tileWidth;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public int getZoomStart() {
		return zoomStart;
	}

	public int getZoomStop() {
		return zoomStop;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setP(Polygon p) {
		this.p = p;
	}

	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setZoomStart(int zoomStart) {
		this.zoomStart = zoomStart;
	}

	public void setZoomStop(int zoomStop) {
		this.zoomStop = zoomStop;
	}

}