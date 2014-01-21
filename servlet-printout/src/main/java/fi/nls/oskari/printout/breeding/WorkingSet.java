package fi.nls.oskari.printout.breeding;

import java.util.Date;
import java.util.Vector;

import org.geowebcache.grid.BoundingBox;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class WorkingSet implements Constants {

	Long id = null;

	Date timestamp = new Date();
	private Vector<ChangeSet> changeSets = new Vector<ChangeSet>();
	String layer = null;
	String gridSubsetName;
	String identifier;
	private Long totalNumberOfEntries = 0L;
	private Long totalNumberOfTiles = 0L;

	Status status = Status.INITIAL;
	int percentComplete = 0;

	String product;
	String productScale;
	String productResolution;
	String imageFormat;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}

	public Long getTotalNumberOfEntries() {
		return totalNumberOfEntries;
	}

	public void setTotalNumberOfEntries(Long totalNumberOfEntries) {
		this.totalNumberOfEntries = totalNumberOfEntries;
	}

	public Long getTotalNumberOfTiles() {
		return totalNumberOfTiles;
	}

	public void setTotalNumberOfTiles(Long totalNumberOfTiles) {
		this.totalNumberOfTiles = totalNumberOfTiles;
	}

	public void setChangeSets(Vector<ChangeSet> changeSets) {
		this.changeSets = changeSets;
	}

	public WorkingSet(String layer) {
		// TODO Auto-generated constructor stub
		this.layer = layer;
	}

	public Vector<ChangeSet> getChangeSets() {
		return changeSets;
	}

	public ChangeSet addEntry(String layerName, String gridSubsetName,
			Polygon p, BoundingBox reqBounds, Vector<Double> resolutions,
			int zoomStart, int zoomStop, int tileWidth, int tileHeight) {
		totalNumberOfEntries++;

		ChangeSet changeSet = new ChangeSet(layerName, gridSubsetName, p,
				reqBounds, resolutions, zoomStart, zoomStop, tileWidth,
				tileHeight);
		changeSet.setIdentifier(getIdentifier());

		changeSets.add(changeSet);

		return changeSet;

	}

	public MultiPolygon getEntriesAsMultiPolygon(GeometryFactory geomFactory) {

		Vector<Polygon> entryPolygons = new Vector<Polygon>(changeSets.size());

		for (ChangeSet changeSet : changeSets) {
			entryPolygons.add(changeSet.getPolygon());
		}

		MultiPolygon mp = new MultiPolygon(
				entryPolygons.toArray(new Polygon[entryPolygons.size()]),
				geomFactory);
		return mp;

	}

	
	public String toString() {
		StringBuffer buf = new StringBuffer();

		if (getChangeSets().size() > 0) {
			buf.append('[');
			buf.append('\n');

			for (ChangeSet cs : getChangeSets()) {
				if (cs.getChangeSetEntries().size() == 0)
					continue;
				buf.append("- changeSet with "
						+ cs.getChangeSetEntries().size() + " entries\n");
				for (ChangeSetEntry cse : cs.getChangeSetEntries()) {
					buf.append("- changeSet entry with " + cse.getTileCount()
							+ " tiles\n");
				}
			}

			buf.append(']');
			buf.append('\n');
		}

		buf.append("\n}\n");

		return buf.toString();
	}

	public String getGridSubsetName() {
		return gridSubsetName;
	}

	public void setGridSubsetName(String gridSubsetName) {
		this.gridSubsetName = gridSubsetName;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getProductScale() {
		return productScale;
	}

	public void setProductScale(String productScale) {
		this.productScale = productScale;
	}

	public String getProductResolution() {
		return productResolution;
	}

	public void setProductResolution(String productResolution) {
		this.productResolution = productResolution;
	}

	public String getImageFormat() {
		return imageFormat;
	}

	public void setImageFormat(String imageFormat) {
		this.imageFormat = imageFormat;
	}

}
