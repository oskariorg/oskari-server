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
 * Created by SMAKINEN on 13.1.2017.
 */
public class DataSourceUpdater extends Thread {

    private static final Logger LOG = LogFactory.getLogger(StatisticalDatasourcePlugin.class);

    private StatisticalDatasourcePlugin plugin;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public DataSourceUpdater(StatisticalDatasourcePlugin plugin) {
        this.plugin = plugin;
    }

    public void run() {
        updateStarted();
        List<StatisticalIndicator> list = plugin.getIndicators(null);
        for(StatisticalIndicator indicator : list) {
            addToWorkQueue(indicator);
        }
        updateCompleted();
    }

    protected void updateStarted() {
        // remove any previous work
        JedisManager.del(getIndicatorListWorkKey());
        // setup status
        DataStatus status = plugin.getStatus();
        status.setUpdating(true);
        status.setUpdateStarted();
        JedisManager.setex(plugin.getStatusKey(), JedisManager.EXPIRY_TIME_DAY * 7, status.toJSON().toString());
    }
    protected void addToWorkQueue(StatisticalIndicator indicator) {
        try {
            String json = MAPPER.writeValueAsString(indicator);
            JedisManager.pushToList(getIndicatorListWorkKey(), json);
        } catch (JsonProcessingException ex) {
            LOG.error(ex, "Error updating indicator list");
        }
    }
    /**
     * Returns a Redis key that should hold currently processed indicators of this datasource as list.
     * @return
     */
    private String getIndicatorListWorkKey() {
        return plugin.CACHE_PREFIX + "work:" + plugin.getSource().getId() + plugin.CACHE_POSTFIX_LIST;
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

    protected void syncWorkToIndicators() {
        final List<StatisticalIndicator> processIndicators = getWorkQueue();
        // read existing list and merge processed ones to it
        final List<StatisticalIndicator> existingIndicators = plugin.getProcessedIndicators();

        // TODO: Gather results under a temporary key if this is NOT the first run (process indicators have previous data)
        // This was we can update an existing listing with up to date data and not remove indicators that are still under
        // processing

        // merge
        existingIndicators.addAll(processIndicators);

        // write new indicator list
        try {
            String result = MAPPER.writeValueAsString(existingIndicators);
            JedisManager.setex(plugin.getIndicatorListKey(), JedisManager.EXPIRY_TIME_DAY * 7, result);
        } catch (JsonProcessingException ex) {
            LOG.error(ex, "Error updating indicator list");
        }
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
