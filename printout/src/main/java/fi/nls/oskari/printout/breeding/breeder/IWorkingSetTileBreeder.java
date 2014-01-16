package fi.nls.oskari.printout.breeding.breeder;

import java.io.IOException;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.filter.request.RequestFilterException;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.grid.OutsideCoverageException;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.printout.breeding.ChangeSet;
import fi.nls.oskari.printout.breeding.ChangeSetEntry;
import fi.nls.oskari.printout.breeding.WorkingSet;

public interface IWorkingSetTileBreeder {
	public interface StatusChangeCallBack {

		public void noteChange(WorkingSet workingSet, ChangeSet changeSet,
				ChangeSetEntry changeSetEntry,
				IChangeSetEntryTransaction transaction);

		public void noteChange(WorkingSet workingSet, ChangeSet changeSet,
				IChangeSetEntryTransaction transaction);

		public void noteChange(WorkingSet workingSet,
				IChangeSetEntryTransaction transaction);

		public void noteException(WorkingSet workingSet, ChangeSet changeSet,
				ChangeSetEntry changeSetEntry,
				IChangeSetEntryTransaction transaction, Exception x);
	}

	public IChangeSetEntryTransaction createChangeSetEntry(WorkingSet ws,
			ChangeSet cs, ChangeSetEntry cse) throws OutsideCoverageException,
			RequestFilterException, GeoWebCacheException, IOException;

	public StatusChangeCallBack getCallback();

	public XMLConfiguration getConfig();

	public GridSetBroker getGridSetBroker();

	public long getMaxCount();

	public Geometry getMaxExtent();

	public int getRetryCount();

	public long getRetryWaitMs();

	public boolean isUseCache();

	public void processChangeSet(ChangeSet changeSet);

	public void processChangeSetEntry(WorkingSet ws, ChangeSet cs,
			ChangeSetEntry cse) throws OutsideCoverageException,
			RequestFilterException, GeoWebCacheException, IOException;

	public void setCallback(StatusChangeCallBack callback);

	public void setConfig(XMLConfiguration config);

	public void setGridSetBroker(GridSetBroker gridSetBroker);

	public void setMaxCount(long maxCount);

	public void setMaxExtent(Geometry maxExtent);

	public void setRetryCount(int retryCount);

	public void setRetryWaitMs(long retryWaitMs);

	public void setUseCache(boolean useCache);

}