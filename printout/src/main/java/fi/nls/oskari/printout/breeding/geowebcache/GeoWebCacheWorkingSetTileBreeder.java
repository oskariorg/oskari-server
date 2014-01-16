package fi.nls.oskari.printout.breeding.geowebcache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.filter.request.RequestFilterException;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.grid.OutsideCoverageException;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.mime.ImageMime;
import org.geowebcache.storage.StorageBroker;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

import fi.nls.oskari.printout.breeding.ChangeSet;
import fi.nls.oskari.printout.breeding.ChangeSetEntry;
import fi.nls.oskari.printout.breeding.WorkingSet;
import fi.nls.oskari.printout.breeding.breeder.ChangeSetEntryTransaction;
import fi.nls.oskari.printout.breeding.breeder.WorkingSetTileBreeder;

public class GeoWebCacheWorkingSetTileBreeder extends WorkingSetTileBreeder {
	public class GeoWebCacheChangeSetEntryTransaction extends
			ChangeSetEntryTransaction {
		public GeoWebCacheChangeSetEntryTransaction(XMLConfiguration config,
				WorkingSet ws, ChangeSet cs, ChangeSetEntry cse,
				int retryCount, long retryWaitMs, StatusChangeCallBack callback) {
			super(config, ws, cs, cse, retryCount, retryWaitMs, callback);

		}

		protected void retrieveTile(WorkingSet ws, ChangeSet cs,
				ChangeSetEntry cse, GridSubset gridSubset, int i,
				BoundingBox gridLocBounds, long[] gridLoc, TileLayer tileLayer)
				throws RequestFilterException, OutsideCoverageException,
				GeoWebCacheException, IOException {

			if (maxCount > 0 && tilesProcessed > maxCount) {
				return;
			}

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

			tilesProcessed++;

			Map<String, String> parameters = new HashMap<String, String>();

			Map<String, String> fullParameters = tileLayer
					.getModifiableParameters(parameters, "UTF-8");

			ConveyorTile tile = new ConveyorTile(storageBroker,
					tileLayer.getName(), gridSubset.getName(), gridLoc,
					ImageMime.png, fullParameters, null, null);

			tileLayer.applyRequestFilters(tile);

			try {

				tileLayer.seedTile(tile, useCache);
			} catch (GeoWebCacheException gex) {

				if (tile.getStatus() == 204) {

					return;
				}

				throw gex;
			}

		}

	}

	private long retryWaitMs = 1000;

	private int retryCount = 4;

	protected StorageBroker storageBroker;

	public ChangeSetEntryTransaction createChangeSetEntry(WorkingSet ws,
			ChangeSet cs, ChangeSetEntry cse) throws OutsideCoverageException,
			RequestFilterException, GeoWebCacheException, IOException {

		ChangeSetEntryTransaction csex = new GeoWebCacheChangeSetEntryTransaction(
				config, ws, cs, cse, retryCount, retryWaitMs, callback);

		return csex;
	}

	public StorageBroker getStorageBroker() {
		return storageBroker;
	}

	public void processChangeSetEntry(WorkingSet ws, ChangeSet cs,
			ChangeSetEntry cse) throws OutsideCoverageException,
			RequestFilterException, GeoWebCacheException, IOException {

		GeoWebCacheChangeSetEntryTransaction csex = new GeoWebCacheChangeSetEntryTransaction(
				config, ws, cs, cse, retryCount, retryWaitMs, callback);

		csex.process();
	}

	public void setStorageBroker(StorageBroker storageBroker) {
		this.storageBroker = storageBroker;
	}
}
