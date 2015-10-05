package fi.nls.oskari.printout.breeding.breeder;

import fi.nls.oskari.printout.breeding.ChangeSet;
import fi.nls.oskari.printout.breeding.ChangeSetEntry;
import fi.nls.oskari.printout.breeding.Constants.Status;
import fi.nls.oskari.printout.breeding.WorkingSet;
import fi.nls.oskari.printout.breeding.breeder.IWorkingSetTileBreeder.StatusChangeCallBack;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.filter.request.RequestFilterException;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.grid.OutsideCoverageException;
import org.geowebcache.layer.TileLayer;

import java.io.IOException;

public abstract class ChangeSetEntryTransaction implements
		IChangeSetEntryTransaction {

	protected XMLConfiguration config;
	protected int retryCount;
	protected long retryWaitMs;

	boolean isCancelled = false;

	protected WorkingSet ws;

	protected ChangeSet cs;

	protected ChangeSetEntry cse;
	protected long tilesTotal = 0L;
	protected long tilesProcessed = 0L;

	protected long tilesCallbackInterval = 0L;
	protected int percentCompleteReported = -1;
	private StatusChangeCallBack callback;

	public ChangeSetEntryTransaction(XMLConfiguration config, WorkingSet ws,
			ChangeSet cs, ChangeSetEntry cse, int retryCount, long retryWaitMs,
			StatusChangeCallBack callback) {
		this.ws = ws;
		this.cs = cs;
		this.cse = cse;
		this.tilesTotal = cse.getTileCount();
		this.tilesCallbackInterval = cse.getTileCount() / 20;

		this.retryCount = retryCount;
		this.retryWaitMs = retryWaitMs;
		this.callback = callback;
		this.config = config;

	}
	
	public int getPercentComplete() {
		if (tilesTotal == 0L)
			return 100;

		return new Double(
				100.0 * ((double) tilesProcessed / (double) tilesTotal))
				.intValue();
	}

	
	public long getTilesLoaded() {
		return tilesProcessed;
	}

	
	public long getTilesTotal() {
		return tilesTotal;
	}

	
	public boolean isCancelled() {
		return isCancelled;
	}

	
	public void process() throws OutsideCoverageException,
			GeoWebCacheException, IOException {

		ChangeSetEntry.Status pendingStatus = ChangeSetEntry.Status.BUSY;

		cse.setStatus(pendingStatus);
		if (callback != null) {
			synchronized (callback) {
				callback.noteChange(ws, cs, cse, this);
			}
		}

		if (ws.getStatus().equals(Status.CANCELLED)) {
			setCancelled(true);
			return;
		}

		pendingStatus = ChangeSetEntry.Status.FAILED;
		try {

			TileLayer tileLayer = config.getTileLayer(cs.getLayerName());
			String gridSubsetName = cs.getGridSubsetName();
			GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

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

			canvasSize[0] = (int) Math.round(reqBounds.getWidth()
					/ srcResolution);
			canvasSize[1] = (int) Math.round(reqBounds.getHeight()
					/ srcResolution);

			for (int i = 0; i < 4; i++) {
				canvOfs[i] = (int) Math.round(boundOfs[i] / srcResolution);
			}

			long starty = srcRectangle[1];
			for (long gridy = starty; gridy <= srcRectangle[3]; gridy++) {

				if (isCancelled())
					break;

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
					if (isCancelled())
						break;

					long[] gridLoc = { gridx, gridy, srcIdx + zoomStart };
					BoundingBox gridLocBounds = null;
					gridLocBounds = gridSubset.boundsFromIndex(gridLoc);

					try {

						for (int rc = 0; rc < retryCount; rc++) {

							try {
								retrieveTile(ws, cs, cse, gridSubset, zoomStart
										+ srcIdx, gridLocBounds, gridLoc,
										tileLayer);
								break;
							} catch (IOException ioex) {
								if (callback != null) {
									callback.noteException(ws, cs, cse, this,
											ioex);
								}

							} catch (GeoWebCacheException gex) {
								if (callback != null) {
									callback.noteException(ws, cs, cse, this,
											gex);
								}
							}

							try {
								Thread.sleep(retryWaitMs);
							} catch (InterruptedException e) {
								throw new IOException(
										"retry sleep failed after IOException. Quitting at retry #"
												+ rc + " of " + retryCount);
							}
						}
					} catch (RequestFilterException e) {
						if (callback != null) {
							callback.noteException(ws, cs, cse, this, e);
						}
						continue;
					}

					int pc = getPercentComplete();
					cse.setPercentComplete(pc);

					if (pc != percentCompleteReported) {
						percentCompleteReported = pc;

					}

					if (tilesCallbackInterval == 0
							|| tilesProcessed % tilesCallbackInterval == 0) {

						if (callback != null) {
							synchronized (callback) {
								callback.noteChange(ws, cs, cse, this);
							}

							if (ws.getStatus().equals(Status.CANCELLED)) {
								setCancelled(true);
								break;
							}
						}
					}

				}
			}

			if (isCancelled())
				pendingStatus = ChangeSetEntry.Status.CANCELLED;
			else
				pendingStatus = ChangeSetEntry.Status.FINISHED;

		} finally {
			cse.setStatus(pendingStatus);
			if (callback != null) {
				synchronized (callback) {
					callback.noteChange(ws, cs, cse, this);
				}
			}

		}
	}

	abstract protected void retrieveTile(WorkingSet ws, ChangeSet cs,
			ChangeSetEntry cse, GridSubset gridSubset, int i,
			BoundingBox gridLocBounds, long[] gridLoc, TileLayer tileLayer)
			throws RequestFilterException, OutsideCoverageException,
			GeoWebCacheException, IOException;

	
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	
	public void setTilesLoaded(long tilesLoaded) {
		this.tilesProcessed = tilesLoaded;
	}

	
	public void setTilesTotal(long tilesTotal) {
		this.tilesTotal = tilesTotal;
	}
}