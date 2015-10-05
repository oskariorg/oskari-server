package fi.nls.oskari.printout.breeding.maplink;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import fi.nls.oskari.printout.breeding.ChangeSet;
import fi.nls.oskari.printout.breeding.ChangeSetEntry;
import fi.nls.oskari.printout.breeding.WorkingSet;
import fi.nls.oskari.printout.breeding.breeder.ChangeSetEntryTransaction;
import fi.nls.oskari.printout.breeding.breeder.IChangeSetEntryTransaction;
import fi.nls.oskari.printout.breeding.breeder.IWorkingSetTileBreeder;
import fi.nls.oskari.printout.breeding.breeder.WorkingSetTileBreeder;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.filter.request.RequestFilterException;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.grid.OutsideCoverageException;
import org.geowebcache.layer.TileLayer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapLinkWorkingSetTileBreeder extends WorkingSetTileBreeder
		implements IWorkingSetTileBreeder {

	public class MapLinkChangeSetEntryTransaction extends
			ChangeSetEntryTransaction {
		public MapLinkChangeSetEntryTransaction(XMLConfiguration config,
				WorkingSet ws, ChangeSet cs, ChangeSetEntry cse,
				int retryCount, long retryWaitMs, StatusChangeCallBack callback) {
			super(config, ws, cs, cse, retryCount, retryWaitMs, callback);

		}

		
		protected void retrieveTile(WorkingSet ws, ChangeSet cs,
				ChangeSetEntry cse, GridSubset gridSubset, int i,
				BoundingBox gridLocBounds, long[] gridLoc, TileLayer tileLayer)
				throws RequestFilterException, OutsideCoverageException,
				GeoWebCacheException, IOException {

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

			Map<String, String> parameters = new HashMap<String, String>();

			Map<String, String> fullParameters = tileLayer
					.getModifiableParameters(parameters, "UTF-8");

			int th = cs.getTileHeight();
			int tw = cs.getTileWidth();

			tileProcessor.processTile(e, p, tw, th, fullParameters,
					cse.getLayerName(), gridLoc);

		}

	}

	public interface MapLinkTileProcessor {
		public void processTile(Envelope e, Polygon p, int w, int h,
				Map<String, String> parameters, String layerName, long[] gridLoc)
				throws IOException;
	}

	MapLinkTileProcessor tileProcessor;
	private long mapLinkRetryWaitMs = 1000;

	private int mapLinkRetryCount = 4;

	
	public IChangeSetEntryTransaction createChangeSetEntry(WorkingSet ws,
			ChangeSet cs, ChangeSetEntry cse) throws OutsideCoverageException,
			RequestFilterException, GeoWebCacheException, IOException {

		ChangeSetEntryTransaction csex = new MapLinkChangeSetEntryTransaction(
				config, ws, cs, cse, mapLinkRetryCount, mapLinkRetryWaitMs,
				callback);

		return csex;
	}

	public MapLinkTileProcessor getTileProcessor() {
		return tileProcessor;
	}

	
	public void processChangeSetEntry(WorkingSet ws, ChangeSet cs,
			ChangeSetEntry cse) throws OutsideCoverageException,
			RequestFilterException, GeoWebCacheException, IOException {

		ChangeSetEntryTransaction csex = new MapLinkChangeSetEntryTransaction(
				config, ws, cs, cse, mapLinkRetryCount, mapLinkRetryWaitMs,
				callback);

		csex.process();
	}

	public void setTileProcessor(MapLinkTileProcessor tileProcessor) {
		this.tileProcessor = tileProcessor;
	}

}
