package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;

@Oskari("MockPlugin")
public class MockPluginFactory extends StatisticalDatasourceFactory {

    @Override
    public StatisticalDatasourcePlugin create(StatisticalDatasource source) {
        return new MockPlugin();
    }
}
