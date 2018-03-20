package org.oskari.statistics.user;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.service.OskariComponent;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public abstract class StatisticalIndicatorService extends OskariComponent {

    public abstract StatisticalIndicator findById(long id, long userId);
    public abstract List<StatisticalIndicator> findByUser(long userId);
    public abstract Map<String, IndicatorValue> getData(long indicator, long regionset, int year);
    public abstract void deleteByUser(long userId);
    public abstract boolean delete(long id, long userId);
}
