package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.domain.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Base template for StatisticalDatasourcePlugin for the lazy
 */
public abstract class AbstractStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private StatisticalDatasource source = null;

    /**
     * Returns currently available dataset for this datasource. Should use preprocessed and cached data with scheduled update
     * triggered with the update() method.
     */
    public IndicatorSet getIndicatorSet(User user) {
        DataStatus status = getStatus();
        IndicatorSet set = new IndicatorSet();
        set.setComplete(!status.isUpdating());
/*
        // TODO: cache usage
        String indicatorJSON = JedisManager.get(getIndicatorListKey());
        if(indicatorJSON == null) {
            // trigger update?
            return set;
        }
        */
        set.setIndicators(getIndicators(user));
        return set;
    }

    /**
     * Trigger update on the data. Should refresh cached data for getIndicatorSet and track progress.
     */
    public void update() {
        // TODO: this should be abstract and replace the getIndicators(User user) method.
    }

    /**
     * Returns a Redis key that should hold client ready indicators as JSON
     * @return
     */
    String getIndicatorListKey() {
        return "oskari:stats:" + getSource().getId() + ":indicators";
    }

    /**
     * Returns a Redis key that should hold currently processed indicators of this datasource as list.
     * @return
     */
    String getIndicatorListWorkKey() {
        return "oskari:stats:work:" + getSource().getId() + ":indicators";
    }
    /**
     * Returns a Redis key that should status information as JSON for this datasource:
     * { complete : [true|false], updateStart : [timestamp], lastUpdate : [timestamp] }
     * @return
     */
    String getStatusKey() {
        return "oskari:stats:" + getSource().getId() + ":progress";
    }

    public DataStatus getStatus() {
        String status = JedisManager.get(getStatusKey());
        return new DataStatus(status);
    }

    /**
     * Returns a list of statistical data indicators, each with several granularity layers.
     * @param user 
     * @return
     */
    public abstract List<? extends StatisticalIndicator> getIndicators(User user);
    public List<? extends StatisticalIndicator> getIndicators(User user, boolean noMetadata) {
        return getIndicators(user);
    }

    public StatisticalIndicator getIndicator(User user, String indicatorId) {
        for (StatisticalIndicator indicator : getIndicators(user)) {
            if (indicator.getId().equals(indicatorId)) {
                return indicator;
            }
        }
        return null;
    }

    public StatisticalDatasource getSource() {
        return source;
    }

    /**
     * Hook for setting up components that the handler needs to handle requests
     */
    public void init(StatisticalDatasource source) {
        this.source = source;
    }

    /**
     * Generally true, if the data does not change all the time, for example based on the user doing the query.
     */
    public boolean canCache() {
        return true;
    }
}
