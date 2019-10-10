package fi.nls.oskari.control.statistics.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.List;

/**
 * Used to preload and -process statistical indicator data from a datasource
 */
public abstract class DataSourceUpdater implements Runnable {

    private static final Logger LOG = LogFactory.getLogger(DataSourceUpdater.class);

    protected StatisticalDatasourcePlugin plugin;

    public DataSourceUpdater(StatisticalDatasourcePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
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
        DataStatus status = plugin.getStatus();
        status.startUpdate();
        JedisManager.setex(plugin.getStatusKey(), JedisManager.EXPIRY_TIME_DAY * 7, status.toString());
    }

    protected void updateCompleted() {
        storeIndicatorList(getIndicators());
        DataStatus status = plugin.getStatus();
        status.finishUpdate();
        JedisManager.setex(plugin.getStatusKey(), JedisManager.EXPIRY_TIME_DAY * 7, status.toString());
    }

    /**
     * Store the list of indicators to Redis as JSON
     */
    protected void storeIndicatorList(List<StatisticalIndicator> indicators) {
        if (indicators.isEmpty()) {
            return;
        }

        final ObjectMapper listMapper = new ObjectMapper();
        // skip f.ex. description and source when writing list
        listMapper.addMixIn(StatisticalIndicator.class, JacksonIndicatorListMixin.class);
        // write new indicator list
        try {
            String result = listMapper.writeValueAsString(indicators);
            JedisManager.setex(plugin.getIndicatorListKey(), JedisManager.EXPIRY_TIME_DAY * 7, result);
        } catch (JsonProcessingException ex) {
            LOG.error(ex, "Error updating indicator list");
        }
    }

    public abstract boolean isFullUpdate();
    protected abstract void addToWorkQueue(StatisticalIndicator indicator);
    protected abstract List<StatisticalIndicator> getIndicators();

}
