package fi.nls.oskari.printout.breeding.breeder;

import java.io.IOException;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.grid.OutsideCoverageException;

public interface IChangeSetEntryTransaction {

	public int getPercentComplete();

	public long getTilesLoaded();

	public long getTilesTotal();

	public boolean isCancelled();

	public void process() throws OutsideCoverageException,
			GeoWebCacheException, IOException;

	public void setCancelled(boolean isCancelled);

	public void setTilesLoaded(long tilesLoaded);

	public void setTilesTotal(long tilesTotal);

}