package fi.nls.oskari.printout.breeding.breeder;

import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.grid.GridSetBroker;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import fi.nls.oskari.printout.breeding.ChangeSet;

public abstract class WorkingSetTileBreeder implements IWorkingSetTileBreeder {

	protected boolean useCache = true;
	protected Geometry maxExtent = null;
	protected long maxCount = -1;

	protected int retryCount = 8;
	protected long retryWaitMs = 60000;

	protected StatusChangeCallBack callback;

	final protected GeometryFactory geomFactory = new GeometryFactory();
	final protected GeometricShapeFactory gsf = new GeometricShapeFactory();

	protected GridSetBroker gridSetBroker;
	protected XMLConfiguration config;

	public StatusChangeCallBack getCallback() {
		return callback;
	}

	public XMLConfiguration getConfig() {
		return config;
	}

	public GridSetBroker getGridSetBroker() {
		return gridSetBroker;
	}

	public long getMaxCount() {
		return maxCount;
	}

	public Geometry getMaxExtent() {
		return maxExtent;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public long getRetryWaitMs() {
		return retryWaitMs;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public void processChangeSet(ChangeSet changeSet) {

	}

	public void setCallback(StatusChangeCallBack callback) {
		this.callback = callback;
	}

	public void setConfig(XMLConfiguration config) {
		this.config = config;
	}

	public void setGridSetBroker(GridSetBroker gridSetBroker) {
		this.gridSetBroker = gridSetBroker;
	}

	public void setMaxCount(long maxCount) {
		this.maxCount = maxCount;
	}

	public void setMaxExtent(Geometry maxExtent) {
		this.maxExtent = maxExtent;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public void setRetryWaitMs(long retryWaitMs) {
		this.retryWaitMs = retryWaitMs;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

}
