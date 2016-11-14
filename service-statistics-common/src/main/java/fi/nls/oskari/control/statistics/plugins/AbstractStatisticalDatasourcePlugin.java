package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.domain.User;

import java.util.List;

/**
 * Base template for StatisticalDatasourcePlugin for the lazy
 */
public abstract class AbstractStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
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

    /**
     * Hook for setting up components that the handler needs to handle requests
     */
    public void init(StatisticalDatasource source) {}

    /**
     * Generally true, if the data does not change all the time, for example based on the user doing the query.
     */
    public boolean canCache() {
        return true;
    }
}
