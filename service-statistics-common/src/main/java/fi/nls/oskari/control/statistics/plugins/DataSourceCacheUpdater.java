package fi.nls.oskari.control.statistics.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * Used to preload and -process statistical indicator data from a datasource
 * 
 * This version of DataSourceUpdater should be used when already cached version of the indicator list exists.
 * Uses Redis for storing the WorkQueue
 * 
 * @see fi.nls.oskari.control.statistics.plugins.DataSourceUpdater
 * @see fi.nls.oskari.control.statistics.plugins.DataSourceCachePopulator
 */
public final class DataSourceCacheUpdater extends DataSourceUpdater {

    private static final Logger LOG = LogFactory.getLogger(DataSourceCacheUpdater.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public DataSourceCacheUpdater(StatisticalDatasourcePlugin plugin) {
        super(plugin);
    }

    protected void addToWorkQueue(StatisticalIndicator indicator) {
        // maybe add a metric how many indicators are processed/timeunit at some point
        try {
            String json = MAPPER.writeValueAsString(indicator);
            JedisManager.pushToList(getIndicatorListWorkKey(), json);
        } catch (JsonProcessingException ex) {
            LOG.error(ex, "Error updating indicator list");
        }
    }

    @Override
    protected void updateStarted() {
        super.updateStarted();
        // remove any previous work fron Redis
        JedisManager.del(getIndicatorListWorkKey());
    }

    @Override
    protected List<StatisticalIndicator> getIndicators() {
        final String workCacheKey = getIndicatorListWorkKey();
        final List<StatisticalIndicator> processIndicators = new ArrayList<>();

        // read work queue to Java objects
        String json = JedisManager.popList(workCacheKey, true);
        while(json != null) {
            try {
                StatisticalIndicator indicator = MAPPER.readValue(json, StatisticalIndicator.class);
                processIndicators.add(indicator);
            } catch (IOException ex) {
                LOG.error(ex, "Couldn't read indicator data from work queue:", json);
            }
            json = JedisManager.popList(workCacheKey, true);
        }
        return processIndicators;
    }

    /**
     * Returns a Redis key that should hold currently processed indicators of this datasource as list.
     */
    private String getIndicatorListWorkKey() {
        return StatisticalDatasourcePlugin.CACHE_PREFIX + plugin.getSource().getId() +  ":worklist";
    }

    @Override
    public boolean isFullUpdate() {
        return false;
    }

}
