package fi.nls.oskari.printout.breeding;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.Grid;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class WorkingSetProcessor {

	long tileCount = 0;
	long subImages = 0;

	final protected GeometryFactory geomFactory = new GeometryFactory();

	private GridSetBroker gridSetBroker;

	private XMLConfiguration config;

	Geometry maxExtent = null;

	public long[] closestRectangle(GridSet gridSet, int level,
			BoundingBox rectangeBounds) {

		@SuppressWarnings("deprecation")
		Grid grid = gridSet.getGridLevels()[level];

		double width = grid.getResolution() * gridSet.getTileWidth();
		double height = grid.getResolution() * gridSet.getTileHeight();

		long minX = (long) Math.floor((rectangeBounds.getMinX() - gridSet
				.tileOrigin()[0]) / width);
		long minY = (long) Math.floor((rectangeBounds.getMinY() - gridSet
				.tileOrigin()[1]) / height);
		long maxX = (long) Math.ceil(((rectangeBounds.getMaxX() - gridSet
				.tileOrigin()[0]) / width));
		long maxY = (long) Math.ceil(((rectangeBounds.getMaxY() - gridSet
				.tileOrigin()[1]) / height));

		if (gridSet.isTopLeftAligned()) {
			minY = minY + grid.getNumTilesHigh();
			maxY = maxY + grid.getNumTilesHigh();
		}

		long[] ret = { minX, minY, maxX - 1, maxY - 1, level };

		return ret;
	}

	public ChangeSet createChangeSet(WorkingSet ws, Polygon polygon,
			TileLayer tileLayer, String gridSubsetName, String entryId)
			throws GeoWebCacheException {

		Envelope envelope = polygon.getEnvelopeInternal();

		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		double[] resArray = gridSubset.getResolutions();
		int zoomStart = gridSubset.getZoomStart();
		int zoomStop = gridSubset.getZoomStop();

		Vector<Double> resolutions = new Vector<Double>();
		int n = 0;
		for (double d : resArray) {

			if (!isInRange(n, zoomStart, zoomStop)) {
				n++;
				continue;
			}

			resolutions.add(d);
			n++;
		}

		BoundingBox reqBounds = new BoundingBox(envelope.getMinX(),
				envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());

		ChangeSet changeSet = ws.addEntry(tileLayer.getName(), gridSubsetName,
				polygon, reqBounds, resolutions, zoomStart, zoomStop,
				gridSubset.getTileWidth(), gridSubset.getTileHeight());

		changeSet.setIdentifier(entryId);

		return changeSet;

	}

	public void createChangeSets(WorkingSet ws, String layer)
			throws IOException {

		String gridSubsetName = ws.getGridSubsetName();

		TileLayer tileLayer = config.getTileLayer(layer);

		Vector<ChangeSet> changeSets = ws.getChangeSets();

		for (ChangeSet changeSet : changeSets) {

			processChangeSet(ws, changeSet, tileLayer, gridSubsetName);

		}

	}

	public WorkingSet createWorkingSet(WorkingSet ws, String layer,
			String gridSubsetName, Geometry p, String identifier)
			throws IOException, GeoWebCacheException, XMLStreamException,
			FactoryConfigurationError {

		ws.setLayer(layer);
		ws.setGridSubsetName(gridSubsetName);
		ws.setTimestamp(new Date());

		TileLayer tileLayer = config.getTileLayer(layer);

		Polygon polygon = (Polygon) p;

		createChangeSet(ws, polygon, tileLayer, gridSubsetName, identifier);

		return ws;
	}

	public XMLConfiguration getConfig() {
		return config;
	}

	public GridSetBroker getGridSetBroker() {
		return gridSetBroker;
	}

	long[] getIntersection(long[] coverage, long[] rectangle) {
		long[] ret = {
				Math.min(Math.max(coverage[0], rectangle[0]), coverage[2]),
				Math.min(Math.max(coverage[1], rectangle[1]), coverage[3]),
				Math.min(Math.max(coverage[0], rectangle[2]), coverage[2]),
				Math.min(Math.max(coverage[1], rectangle[3]), coverage[3]),
				rectangle[4] };

		return ret;
	}

	public Geometry getMaxExtent() {
		return maxExtent;
	}

	public boolean isInRange(int n, int zoomStart, int zoomStop) {
		return (n >= zoomStart && n <= zoomStop);
	}

	public void processChangeSet(WorkingSet ws, ChangeSet changeSet,
			TileLayer tileLayer, String gridSubsetName) throws IOException {

		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);
		int zoomStart = changeSet.getZoomStart();
		BoundingBox reqBounds = changeSet.getReqBounds();

		Vector<Double> resolutions = changeSet.getResolutions();

		int srcIdx = 0;
		for (double srcResolution : resolutions) {

			long[] closestRectangle = closestRectangle(gridSubset.getGridSet(),
					srcIdx + zoomStart, reqBounds);
			long[] gridCov = gridSubset.getCoverage(srcIdx + zoomStart);
			long[] srcRectangle = getIntersection(gridCov, closestRectangle);

			BoundingBox srcBounds = gridSubset
					.boundsFromRectangle(srcRectangle);

			Envelope e = new Envelope(srcBounds.getMinX(), srcBounds.getMaxX(),
					srcBounds.getMinY(), srcBounds.getMaxY());
			Polygon p = geomFactory.createPolygon(
					geomFactory.createLinearRing(new Coordinate[] {
							new Coordinate(e.getMinX(), e.getMinY()),
							new Coordinate(e.getMaxX(), e.getMinY()),
							new Coordinate(e.getMaxX(), e.getMaxY()),
							new Coordinate(e.getMinX(), e.getMaxY()),
							new Coordinate(e.getMinX(), e.getMinY()) }), null);

			ChangeSetEntry changeSetEntry = changeSet.addEntry(srcResolution,
					srcIdx, gridCov, closestRectangle, srcRectangle, srcBounds,
					p);

			changeSetEntry.setLayerName(changeSet.getLayerName());
			changeSetEntry.setGridSubsetName(gridSubsetName);

			processChangeSetEntry(ws, changeSet, changeSetEntry, gridSubset);

			srcIdx++;
		}

		ws.setTotalNumberOfTiles(tileCount);

	}

	public void processChangeSetEntry(WorkingSet ws, ChangeSet cs,
			ChangeSetEntry cse, GridSubset gridSubset) throws IOException {

		double srcResolution = cse.getResolution();
		BoundingBox srcBounds = cse.getSrcBounds();
		long[] srcRectangle = cse.getSrcRectangle();
		BoundingBox reqBounds = cs.getReqBounds();
		int zoomStart = cs.getZoomStart();
		int srcIdx = cse.getIndex();
		int[] canvasSize = new int[2];
		int[] canvOfs = new int[4];

		double[] boundOfs = new double[4];

		boundOfs[0] = srcBounds.getMinX() - reqBounds.getMinX();
		boundOfs[1] = srcBounds.getMinY() - reqBounds.getMinY();
		boundOfs[2] = reqBounds.getMaxX() - srcBounds.getMaxX();
		boundOfs[3] = reqBounds.getMaxY() - srcBounds.getMaxY();

		canvasSize[0] = (int) Math.round(reqBounds.getWidth() / srcResolution);
		canvasSize[1] = (int) Math.round(reqBounds.getHeight() / srcResolution);

		for (int i = 0; i < 4; i++) {
			canvOfs[i] = (int) Math.round(boundOfs[i] / srcResolution);
		}

		long starty = srcRectangle[1];
		for (long gridy = starty; gridy <= srcRectangle[3]; gridy++) {

			int tileHeight = gridSubset.getTileHeight();

			if (!(canvOfs[3] > 0)) {
				if (gridy == srcRectangle[3]) {
					tileHeight = tileHeight + canvOfs[3];
				}
			}

			if (gridy == srcRectangle[1] && canvOfs[1] < 0) {
				tileHeight += canvOfs[1];
			}

			long startx = srcRectangle[0];
			for (long gridx = startx; gridx <= srcRectangle[2]; gridx++) {

				long[] gridLoc = { gridx, gridy, srcIdx + zoomStart };

				BoundingBox gridLocBounds = null;

				gridLocBounds = gridSubset.boundsFromIndex(gridLoc);

				processTile(ws, cs, cse, gridSubset, zoomStart + srcIdx,
						gridLocBounds, gridLoc);

			}
		}

	}

	public void processTile(WorkingSet ws, ChangeSet cs, ChangeSetEntry cse,
			GridSubset gridSubset, int i, BoundingBox gridLocBounds,
			long[] gridLoc) throws IOException {

		Envelope e = new Envelope(gridLocBounds.getMinX(),
				gridLocBounds.getMaxX(), gridLocBounds.getMinY(),
				gridLocBounds.getMaxY());

		Polygon p = geomFactory.createPolygon(
				geomFactory.createLinearRing(new Coordinate[] {
						new Coordinate(e.getMinX(), e.getMinY()),
						new Coordinate(e.getMaxX(), e.getMinY()),
						new Coordinate(e.getMaxX(), e.getMaxY()),
						new Coordinate(e.getMinX(), e.getMaxY()),
						new Coordinate(e.getMinX(), e.getMinY()) }), null);

		if (cs.getPolygon() != null && !p.intersects(cs.getPolygon())) {
			return;
		}

		if (maxExtent != null && !p.intersects(maxExtent)) {

			return;
		}

		tileCount++;

		cse.addTileRequest(i, gridLocBounds, gridLoc);

	}

	public void setConfig(XMLConfiguration config) {
		this.config = config;
	}

	public void setGridSetBroker(GridSetBroker gridSetBroker) {
		this.gridSetBroker = gridSetBroker;
	}

	public void setMaxExtent(Geometry maxExtent) {
		this.maxExtent = maxExtent;
	}

}
