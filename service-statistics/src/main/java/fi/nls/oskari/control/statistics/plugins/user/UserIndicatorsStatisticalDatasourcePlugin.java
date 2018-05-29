package fi.nls.oskari.control.statistics.plugins.user;

import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ConversionHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.statistics.user.StatisticalIndicatorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserIndicatorsStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(UserIndicatorsStatisticalDatasourcePlugin.class);
    private StatisticalIndicatorService service;

    public UserIndicatorsStatisticalDatasourcePlugin() {
    }

    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        service = OskariComponentManager.getComponentOfType(StatisticalIndicatorService.class);
    }

    @Override
    public boolean canModify(User user) {
        return true;
    }

    @Override
    public StatisticalIndicator saveIndicator(StatisticalIndicator indicator, User user) {
        return service.saveIndicator(indicator, user.getId());
    }

    @Override
    public void saveIndicatorData(StatisticalIndicator indicator, long regionsetId, Map<String, IndicatorValue> data, User user) {
        int id = ConversionHelper.getInt(indicator.getId(), -1);
        if (id == -1) {
            throw new ServiceRuntimeException("No indicator id to save data to");
        }
        StatisticalIndicator existing = service.findById(id, user.getId());
        if (existing == null) {
            throw new ServiceRuntimeException("Referenced indicator (id:" + id + ") not found. Unable to save data.");
        }
        try {
            int year = Integer.parseInt(indicator.getDataModel().getDimension("year").getValue());
            JSONObject json = new JSONObject();
            for (Map.Entry<String, IndicatorValue> entry : data.entrySet()) {
                entry.getValue().putToJSONObject(json, entry.getKey());
            }
            service.saveIndicatorData(id, regionsetId, year, json.toString());
        } catch (JSONException e) {
            throw new ServiceRuntimeException("Values not valid for JSON.", e);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Unable to save the data.", e);
        }
    }

    @Override
    public IndicatorSet getIndicatorSet(User user) {
        IndicatorSet set = new IndicatorSet();
        if (user != null) {
            set.setIndicators(getIndicators(user));
            set.setComplete(true);
        }
        return set;
    }

    public void update() {
        // NO-OP - getIndicatorSet responds immediately
    }

    private List<StatisticalIndicator> getIndicators(User user) {
        // Getting the general information of all the indicator layers.
        if (user == null) {
            return new ArrayList<>();
        }
        long uid = user.getId();
        return service.findByUser(uid);
    }

    @Override
    public boolean canCache() {
        // Because the results are based on the user doing the query, we should not cache here.
        return false;
    }

    /**
     * Override as default impl expects indicators are stored in redis
     *
     * @param user
     * @param indicatorId
     * @return
     */
    @Override
    public StatisticalIndicator getIndicator(User user, String indicatorId) {
        try {
            return service.findById(ConversionHelper.getInt(indicatorId, -1), user.getId());
        } catch (ServiceRuntimeException ex) {
            LOG.warn("Indicator not found:", ex.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicator indicator,
                                                          StatisticalIndicatorDataModel params,
                                                          StatisticalIndicatorLayer regionset) {
        return service.getData(ConversionHelper.getInt(indicator.getId(), -1),
                regionset.getOskariLayerId(),
                ConversionHelper.getInt(params.getDimension("year").getValue(), -1));
    }
}
