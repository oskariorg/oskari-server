package fi.nls.oskari.control.statistics.plugins;

import java.util.List;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.OskariComponent;

@Oskari("MockPlugin")
public class MockPlugin extends OskariComponent implements StatisticalDatasourcePlugin {

    @Override
    public List<StatisticalIndicator> getIndicators(User user) {
        return null;
    }

    @Override
    public void init() {
    }

    @Override
    public boolean canCache() {
        return false;
    }

}
