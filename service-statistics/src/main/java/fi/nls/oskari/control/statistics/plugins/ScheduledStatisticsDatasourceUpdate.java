package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.worker.ScheduledJob;

import java.util.Map;

/**
 * Triggers update on plugins that support it
 */
@Oskari("StatisticsDatasources")
public class ScheduledStatisticsDatasourceUpdate extends ScheduledJob {
    public void execute(Map<String, Object> params) {
        StatisticalDatasourcePluginManager pluginManager = StatisticalDatasourcePluginManager.getInstance();
        for(StatisticalDatasourcePlugin plugin : pluginManager.getPlugins().values()) {
            // triggers update on plugins that support it
            plugin.getIndicatorSet(null);
        }
    }

    @Override
    public String getCronLine() {
        String line = super.getCronLine();
        if(line != null) {
            // use property if specified
            return line;
        }
        // default if not specified (at 4 AM each night)
        return "0 0 4 * * ?";
    }
}
