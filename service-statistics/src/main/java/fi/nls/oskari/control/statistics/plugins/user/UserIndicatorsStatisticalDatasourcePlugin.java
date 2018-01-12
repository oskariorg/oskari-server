package fi.nls.oskari.control.statistics.plugins.user;

import org.oskari.statistics.user.UserIndicatorService;
import org.oskari.statistics.user.UserIndicatorServiceImpl;
import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.indicator.UserIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class UserIndicatorsStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(UserIndicatorsStatisticalDatasourcePlugin.class);
    private static UserIndicatorService userIndicatorService = new UserIndicatorServiceImpl();

    public UserIndicatorsStatisticalDatasourcePlugin() {
    }

    @Override
    public IndicatorSet getIndicatorSet(User user) {
        IndicatorSet set = new IndicatorSet();
        if(user != null) {
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
        List<UserIndicator> userIndicators = userIndicatorService.findAllOfUser(uid);
        List<StatisticalIndicator> indicators = new ArrayList<>();
        for (UserIndicator userIndicator : userIndicators) {
            indicators.add(toUserStatisticalIndicator(userIndicator));
        }
        return indicators;
    }

    private StatisticalIndicator toUserStatisticalIndicator(UserIndicator userIndicator) {
        StatisticalIndicator ind = new StatisticalIndicator();
        ind.setId(String.valueOf(userIndicator.getId()));

        JSONObject title = JSONHelper.createJSONObject(userIndicator.getTitle());
        Iterator<String> langKeys = title.keys();
        while(langKeys.hasNext()) {
            String lang = langKeys.next();
            ind.addName(lang, title.optString(lang));
        }

        JSONObject source = JSONHelper.createJSONObject(userIndicator.getSource());
        langKeys = source.keys();
        while(langKeys.hasNext()) {
            String lang = langKeys.next();
            ind.addSource(lang, source.optString(lang));
        }

        JSONObject desc = JSONHelper.createJSONObject(userIndicator.getDescription());
        langKeys = desc.keys();
        while(langKeys.hasNext()) {
            String lang = langKeys.next();
            ind.addSource(lang, desc.optString(lang));
        }
        ind.setPublic(userIndicator.isPublished());
        ind.addLayer(new StatisticalIndicatorLayer(userIndicator.getMaterial(), ind.getId()));

        // If we want to provide year, need to do it like this. But currently there's always just one choice
        StatisticalIndicatorDataDimension dim = new StatisticalIndicatorDataDimension("year");
        dim.addAllowedValue(String.valueOf(userIndicator.getYear()));
        ind.getDataModel().addDimension(dim);
        return ind;
    }

    @Override
    public boolean canCache() {
        // Because the results are based on the user doing the query, we should not cache here.
        return false;
    }

    /**
     * Override as default impl expects indicators are stored in redis
     * @param user
     * @param indicatorId
     * @return
     */
    @Override
    public StatisticalIndicator getIndicator(User user, String indicatorId) {
        // TODO: optimize to load single indicator or change indicatorset to be stored in redis
        //int id = ConversionHelper.getInt(indicatorId, -1);
        //UserIndicator ui = userIndicatorService.find(id);
        //return toUserStatisticalIndicator(ui);
        List<StatisticalIndicator> list = getIndicators(user);
        for(StatisticalIndicator ind: list) {
            if(ind.getId().equals(indicatorId)) {
                return ind;
            }
        }
        return null;
    }
    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicator indicator,
                                                          StatisticalIndicatorDataModel params,
                                                          StatisticalIndicatorLayer regionset) {
        // Data is a serialized JSON for legacy and backwards compatibility reasons:
        // "data":{"[regionid]" : [value], ...}
        UserIndicator userIndicator = userIndicatorService.find(ConversionHelper.getInt(indicator.getId(), -1));
        Map<String, IndicatorValue> valueMap = new HashMap<>();
        try {
            JSONObject jsonData = new JSONObject(userIndicator.getData());
            Iterator it = jsonData.keys();
            while(it.hasNext()) {
                String key = (String) it.next();
                IndicatorValueFloat indicatorValue = new IndicatorValueFloat(jsonData.getDouble(key));
                valueMap.put(key, indicatorValue);
            }
        } catch (JSONException e) {
            LOG.error(e, "Error transforming data for user indicator");
        }
        return valueMap;
    }
}
