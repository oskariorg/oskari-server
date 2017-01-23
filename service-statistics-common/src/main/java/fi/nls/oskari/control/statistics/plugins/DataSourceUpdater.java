package fi.nls.oskari.control.statistics.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to preload and -process statistical indicator data from a datasource
 */
public class DataSourceUpdater implements Runnable {

    private static final Logger LOG = LogFactory.getLogger(StatisticalDatasourcePlugin.class);

    private StatisticalDatasourcePlugin plugin;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private boolean wasCacheEmptyOnStart = false;
    private long lastSync = -1;
    private long indicatorsProcessedSinceLastSync = 0;
    // 30 seconds between syncs
    private long syncThreshold = 20 * 1000;

    public DataSourceUpdater(StatisticalDatasourcePlugin plugin) {
        this.plugin = plugin;
    }

    public void run() {
        updateStarted();
        try {
            plugin.update();
        } catch (Exception ex) {
            LOG.error(ex, "Error updating datasource indicators! Datasource id: ", plugin.getSource().getId());
        }
        // TODO: Should we modify status on problematic update? Should atleast update that it's not running anymore.
        updateCompleted();
    }

    protected void updateStarted() {
        // remove any previous work
        JedisManager.del(getIndicatorListWorkKey());
        wasCacheEmptyOnStart = plugin.isCacheEmpty();
        lastSync = System.currentTimeMillis();
        indicatorsProcessedSinceLastSync = 0;
        // setup status
        DataStatus status = plugin.getStatus();
        status.setUpdating(true);
        status.setUpdateStarted();
        JedisManager.setex(plugin.getStatusKey(), JedisManager.EXPIRY_TIME_DAY * 7, status.toJSON().toString());
    }
    protected void addToWorkQueue(StatisticalIndicator indicator) {
        // maybe add a metric how many indicators are processed/timeunit at some point
        try {
            String json = MAPPER.writeValueAsString(indicator);
            JedisManager.pushToList(getIndicatorListWorkKey(), json);
            indicatorsProcessedSinceLastSync++;
        } catch (JsonProcessingException ex) {
            LOG.error(ex, "Error updating indicator list");
        }
        // we are populating empty cache AND if time between sync > threshold -> syncWorkToIndicators()
        // wasCacheEmptyOnStart is important as it switches between full rewrite AND gradual update
        if(wasCacheEmptyOnStart && (indicatorsProcessedSinceLastSync > 30 || System.currentTimeMillis() - lastSync > syncThreshold)) {
            syncWorkToIndicators();
        }
    }
    /**
     * Returns a Redis key that should hold currently processed indicators of this datasource as list.
     * @return
     */
    private String getIndicatorListWorkKey() {
        return plugin.CACHE_PREFIX + "worklist:" + plugin.getSource().getId();
    }

    List<StatisticalIndicator> getWorkQueue() {
        final String workCacheKey = getIndicatorListWorkKey();
        final List<StatisticalIndicator> processIndicators = new ArrayList<>();

        // read work queue to Java classes
        String json = JedisManager.popList(workCacheKey);
        while(json != null) {
            try {
                StatisticalIndicator indicator = MAPPER.readValue(json, StatisticalIndicator.class);
                processIndicators.add(indicator);
            } catch (IOException ex) {
                LOG.error(ex, "Couldn't read indicator data from work queue:", json);
            }
            json = JedisManager.popList(workCacheKey);
        }
        return processIndicators;
    }

    /**
     * Should be called only after the whole listing has been processed IF cache already has indicator data (rewrites the previous list).
     * Should be called multiple times if cache was EMPTY when the update started (gradually adds to the existing listing).
     */
    protected void syncWorkToIndicators() {
        lastSync = System.currentTimeMillis();
        indicatorsProcessedSinceLastSync = 0;
        final List<StatisticalIndicator> processIndicators = getWorkQueue();
        // read existing list and merge processed ones to it
        final List<StatisticalIndicator> existingIndicators = getProcessedIndicators();

        // merge
        existingIndicators.addAll(processIndicators);

        final ObjectMapper listMapper = new ObjectMapper();
        // skip f.ex. description and source when writing list
        listMapper.addMixIn(StatisticalIndicator.class, JacksonIndicatorListMixin.class);
        // write new indicator list
        try {
            String result = listMapper.writeValueAsString(existingIndicators);
            JedisManager.setex(plugin.getIndicatorListKey(), JedisManager.EXPIRY_TIME_DAY * 7, result);
        } catch (JsonProcessingException ex) {
            LOG.error(ex, "Error updating indicator list");
        }
    }
    private List<StatisticalIndicator> getProcessedIndicators() {
        if(wasCacheEmptyOnStart) {
            // continue to add to the existing ones
            return plugin.getProcessedIndicators();
        }
        // write to new list if we are updating a cached list
        return new ArrayList<>();
    }

    protected void updateCompleted() {
        // sync the remaining work list to actual indicators listing
        syncWorkToIndicators();
        // setup status
        DataStatus status = plugin.getStatus();
        status.setUpdating(false);
        status.setLastUpdate();
        JedisManager.setex(plugin.getStatusKey(), JedisManager.EXPIRY_TIME_DAY * 7, status.toJSON().toString());
    }
}
