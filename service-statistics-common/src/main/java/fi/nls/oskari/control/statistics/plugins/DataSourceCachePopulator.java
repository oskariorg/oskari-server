package fi.nls.oskari.control.statistics.plugins;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;

/**
 * Used to preload and -process statistical indicator data from a datasource
 * 
 * This version of DataSourceUpdater should be used when there cache is empty
 * Uses ArrayList for storing the WorkQueue, gradually 'commits' the results to Redis
 * 
 * @see fi.nls.oskari.control.statistics.plugins.DataSourceUpdater
 * @see fi.nls.oskari.control.statistics.plugins.DataSourceCacheUpdater
 */
public final class DataSourceCachePopulator extends DataSourceUpdater {

    private static final TemporalAmount SYNC_INTERVAL = Duration.ofSeconds(20);

    private List<StatisticalIndicator> workQueue;
    private Instant lastSync;

    public DataSourceCachePopulator(StatisticalDatasourcePlugin plugin) {
        super(plugin);
        this.workQueue = new ArrayList<>();
    }

    @Override
    protected void updateStarted() {
        super.updateStarted();
        lastSync = Instant.now();
    }

    @Override
    protected void addToWorkQueue(StatisticalIndicator indicator) {
        workQueue.add(indicator);
        if (workQueue.size() > 30 || Instant.now().isAfter(lastSync.plus(SYNC_INTERVAL))) {
            storeIndicatorList(getIndicators());
        }
    }

    @Override
    protected List<StatisticalIndicator> getIndicators() {
        List<StatisticalIndicator> indicators = new ArrayList<>();
        indicators.addAll(plugin.getProcessedIndicators());
        indicators.addAll(workQueue);
        return indicators;
    }

    @Override
    protected void storeIndicatorList(List<StatisticalIndicator> indicators) {
        super.storeIndicatorList(indicators);
        workQueue.clear();
        lastSync = Instant.now();
    }

    @Override
    public boolean isFullUpdate() {
        return true;
    }

}
