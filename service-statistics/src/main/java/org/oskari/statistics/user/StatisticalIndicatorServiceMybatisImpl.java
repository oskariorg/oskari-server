package org.oskari.statistics.user;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ConversionHelper;
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
            List<UserIndicatorDataRow> rows = getMapper(session).findById(id);
            if(rows == null || rows.isEmpty()) {
                return null;
            }
            if(!rows.get(0).published && rows.get(0).userId != userId) {
                throw new ServiceRuntimeException("Indicator found, but not public or users");
            }
            Collection<StatisticalIndicator> result = mapToStatisticalIndicator(rows);
            if(result.size() != 1) {
                // Shouldn't happen...
                throw new ServiceRuntimeException("Matched more than one indicator - id: " + id);
            }
            return result.iterator().next();
        }
    }

    @Override
    public List<StatisticalIndicator> findByUser(long userId) {
        List<StatisticalIndicator> result = new ArrayList<>();
        try (final SqlSession session = factory.openSession()) {
            result.addAll(mapToStatisticalIndicator(getMapper(session).findByUser(userId)));
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
            int count = getMapper(session).delete(id, userId);
            session.commit();
            return count != 0;
        }
    }

    public void deleteByUser(long userId) {
        try (final SqlSession session = factory.openSession()) {
            getMapper(session).deleteByUser(userId);
            session.commit();
        }
    }

    private Collection<StatisticalIndicator> mapToStatisticalIndicator(List<UserIndicatorDataRow> rows) {
        Map<Long, StatisticalIndicator> result = new HashMap<>();
        for(UserIndicatorDataRow row: rows) {
            StatisticalIndicator ind = result.get(row.id);
            if(ind == null) {
                ind = createStatisticalIndicator(row);
                result.put(row.id, ind);
            }
            addDimension(ind, row);
        }
        return result.values();
    }

    private StatisticalIndicator createStatisticalIndicator(UserIndicatorDataRow userIndicator) {
        StatisticalIndicator ind = new StatisticalIndicator();
        ind.setId(String.valueOf(userIndicator.id));

        JSONObject title = JSONHelper.createJSONObject(userIndicator.title);
        Iterator<String> langKeys = title.keys();
        while (langKeys.hasNext()) {
            String lang = langKeys.next();
            ind.addName(lang, title.optString(lang));
        }

        JSONObject source = JSONHelper.createJSONObject(userIndicator.source);
        langKeys = source.keys();
        while (langKeys.hasNext()) {
            String lang = langKeys.next();
            ind.addSource(lang, source.optString(lang));
        }

        JSONObject desc = JSONHelper.createJSONObject(userIndicator.description);
        langKeys = desc.keys();
        while (langKeys.hasNext()) {
            String lang = langKeys.next();
            ind.addDescription(lang, desc.optString(lang));
        }
        ind.setPublic(userIndicator.published);
        ind.setCreated(userIndicator.created);

        

        // Initialize the year dimension as the only one and flag it as time variable to be used in time-series ops.
        ind.getDataModel().setTimeVariable("year");
        StatisticalIndicatorDataDimension dim = new StatisticalIndicatorDataDimension("year");
        ind.getDataModel().addDimension(dim);
        return ind;
    }

    private UserIndicatorDataRow getVO(StatisticalIndicator userIndicator) {
        UserIndicatorDataRow row = new UserIndicatorDataRow();
        row.title = new JSONObject(userIndicator.getName()).toString();
        row.description = new JSONObject(userIndicator.getDescription()).toString();
        row.source = new JSONObject(userIndicator.getSource()).toString();
        row.published = userIndicator.isPublic();
        return row;
    }

    private void addDimension(StatisticalIndicator ind, UserIndicatorDataRow row) {
        if(row.regionsetId == 0) {
            // no data for indicator
            return;
        }
        if(ind.getLayer(row.regionsetId) == null) {
            ind.addLayer(new StatisticalIndicatorLayer(row.regionsetId, ind.getId()));
        }
        ind.getDataModel().getDimension("year").addAllowedValue(String.valueOf(row.year));
    }

    public StatisticalIndicator saveIndicator(StatisticalIndicator ind, long userId) {
        int id = ConversionHelper.getInt(ind.getId(), -1);
        UserIndicatorDataRow row = getVO(ind);
        row.userId = userId;
        row.id = id;
        try (final SqlSession session = factory.openSession()) {
            if (id != -1) {
                // update (userId check is in the where clause)
                int updated = getMapper(session).updateIndicator(row);
                if(updated != 1) {
                    throw new ServiceRuntimeException("Indicator '" + id + "' not found for user: " + userId);
                }
            } else {
                // insert
                getMapper(session).addIndicator(row);
            }
            session.commit();
        }
        return findById(row.id, userId);
    }
    public void saveIndicatorData(long indicator, long regionset, int year, String data) {
        try (final SqlSession session = factory.openSession()) {
            // remove possible existing data
            getMapper(session).deleteData(indicator, regionset, year);
            // insert new data
            getMapper(session).addData(indicator, regionset, year, data);
            session.commit();
        }
    }
    public boolean deleteIndicatorData(long indicator, long regionset, int year) {
        try (final SqlSession session = factory.openSession()) {
            int updated = getMapper(session).deleteData(indicator, regionset, year);
            session.commit();
            return updated != 0;
        }
    }
}
