package org.oskari.statistics.user;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.util.*;

@Oskari
public class StatisticalIndicatorServiceMybatisImpl extends StatisticalIndicatorService {

    private static final Logger LOG = LogFactory.getLogger(StatisticalIndicatorServiceMybatisImpl.class);
    private SqlSessionFactory factory = null;

    public StatisticalIndicatorServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public StatisticalIndicatorServiceMybatisImpl(DataSource ds) {
        if (ds == null) {
            LOG.warn("DataSource was null, all future calls will throw NPEs!");
            factory = null;
        } else {
            factory = MyBatisHelper.initMyBatis(ds, UserIndicatorMapper.class);
        }
    }

    private UserIndicatorMapper getMapper(SqlSession session) {
        return session.getMapper(UserIndicatorMapper.class);
    }

    @Override
    public StatisticalIndicator findById(long id, long userId) {
        try (final SqlSession session = factory.openSession()) {
            UserIndicator ind = getMapper(session).findById(id);
            if(ind == null) {
                return null;
            }
            if(!ind.isPublished() && ind.getUserId() != userId) {
                throw new ServiceRuntimeException("Indicator found, but not public or users");
            }
            return toUserStatisticalIndicator(ind);
        }
    }

    @Override
    public List<StatisticalIndicator> findByUser(long userId) {
        List<StatisticalIndicator> result = new ArrayList<>();
        try (final SqlSession session = factory.openSession()) {
            for (UserIndicator ind : getMapper(session).findByUser(userId)) {
                result.add(toUserStatisticalIndicator(ind));
            }
        }
        return result;
    }

    @Override
    public Map<String, IndicatorValue> getData(long indicator, long regionset, int year) {
        try (final SqlSession session = factory.openSession()) {
            String data = getMapper(session).getData(indicator, regionset, year);
            JSONObject json = new JSONObject(data);
            Map<String, IndicatorValue> valueMap = new HashMap<>();
            Iterator it = json.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                IndicatorValueFloat indicatorValue = new IndicatorValueFloat(json.getDouble(key));
                valueMap.put(key, indicatorValue);
            }
            return valueMap;
        } catch (JSONException ex) {
            throw new ServiceRuntimeException("Data is corrupted in db");
        }
    }

    public boolean delete(long id, long userId) {
        try (final SqlSession session = factory.openSession()) {
            return getMapper(session).delete(id, userId) != 0;
        }
    }

    public void deleteByUser(long userId) {
        try (final SqlSession session = factory.openSession()) {
            getMapper(session).deleteByUser(userId);
        }
    }

    private StatisticalIndicator toUserStatisticalIndicator(UserIndicator userIndicator) {
        StatisticalIndicator ind = new StatisticalIndicator();
        ind.setId(String.valueOf(userIndicator.getId()));

        JSONObject title = JSONHelper.createJSONObject(userIndicator.getTitle());
        Iterator<String> langKeys = title.keys();
        while (langKeys.hasNext()) {
            String lang = langKeys.next();
            ind.addName(lang, title.optString(lang));
        }

        JSONObject source = JSONHelper.createJSONObject(userIndicator.getSource());
        langKeys = source.keys();
        while (langKeys.hasNext()) {
            String lang = langKeys.next();
            ind.addSource(lang, source.optString(lang));
        }

        JSONObject desc = JSONHelper.createJSONObject(userIndicator.getDescription());
        langKeys = desc.keys();
        while (langKeys.hasNext()) {
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
}
