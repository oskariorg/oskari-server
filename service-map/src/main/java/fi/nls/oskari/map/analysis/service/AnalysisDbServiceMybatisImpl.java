package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisDbServiceMybatisImpl implements AnalysisDbService {

    protected static final String DATASOURCE_ANALYSIS = "analysis";

    private static final Logger log = LogFactory.getLogger(AnalysisDbServiceMybatisImpl.class);

    private SqlSessionFactory factory = null;

    public AnalysisDbServiceMybatisImpl() {

        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName(DATASOURCE_ANALYSIS));
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        }
        else {
            log.error("Couldn't get datasource for analysisservice");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(Analysis.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(AnalysisMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }


    /**
     * insert Analysis table row
     *
     * @param analysis
     */

    public long insertAnalysisRow(final Analysis analysis) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Insert analyse row:", analysis);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            mapper.insertAnalysisRow(analysis);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to add analysis: ", analysis);
        } finally {
            session.close();
        }
        log.debug("Got analyse id:", analysis.getId());
        return analysis.getId();
    }

    /**
     * update Analysis table row field mapping
     *
     * @param analysis
     */
    public long updateAnalysisCols(final Analysis analysis) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Updating analysis columns:", analysis);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            mapper.updateAnalysisCols(analysis);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to update analysis columns mapping: ", analysis);
        } finally {
            session.close();
        }
        return analysis.getId();
    }

    /**
     * Get Analysis row  by id
     *
     * @param id
     * @return analysis object
     */
    public Analysis getAnalysisById(long id) {
        try (SqlSession session = factory.openSession()) {
            log.debug("Finding analysis by id:", id);
            AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            Analysis analysis =  mapper.getAnalysisById(id);
            if (analysis == null) {
                log.debug("Could not find analysis by id:", id);
                return null;
            }
            log.debug("Found analysis: ", analysis);
            return analysis;
        }
    }

    public List<Analysis> getAnalysisById(List<Long> idList) {
        if(idList == null) {
            return Collections.emptyList();
        }

        final SqlSession session = factory.openSession();
        List<Analysis> analysisList = null;
        try {
            log.debug("Finding analysis matching: ", idList);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            analysisList =  mapper.getAnalysisByIdList(idList);
            if(analysisList == null) {
                analysisList = Collections.emptyList();
            }
            log.debug("Found analysis: ", analysisList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying get analysis by id: ", idList);
        } finally {
            session.close();
        }
        return analysisList;
    }

    /**
     * Get Analysis rows of one user by uuid
     *
     * @param uid user uuid
     * @return List of analysis objects
     */
    public List<Analysis> getAnalysisByUid(String uid) {
        if(uid == null) {
            return Collections.emptyList();
        }

        final SqlSession session = factory.openSession();
        List<Analysis> analysisList = null;
        try {
            log.debug("Finding analysis matching uid: ", uid);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            analysisList =  mapper.getAnalysisByUid(uid);
            if(analysisList == null) {
                analysisList = Collections.emptyList();
            }
            log.debug("Found analysis: ", analysisList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying get analysis by Uid: ", uid);
        } finally {
            session.close();
        }
        return analysisList;
    }

    @Override
    public void deleteAnalysisByUid(final String uid) throws ServiceException {
        final List<Analysis> userLayers = getAnalysisByUid(uid);
        for (Analysis userLayer: userLayers) {
            deleteAnalysis(userLayer);
        }
    }
    /**
     * Get Analysis data  by uuid and analysis id
     *
     * @param id analysis id
     * @param uuid user uuid
     * @param select_items select body string in select statement
     * @return List of analysis data rows
     */
    public List<HashMap<String,Object>> getAnalysisDataByIdUid(long id, String uuid, String select_items) {
        try (SqlSession session = factory.openSession()) {
            log.debug("Finding analysis data for id:", id, " uid:", uuid);
            AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", id);
            params.put("uuid", uuid);
            params.put("select_items", select_items);
            List<HashMap<String,Object>> analysisdataList = mapper.getAnalysisDataByIdUid(params);
            if (analysisdataList == null) {
                return Collections.emptyList();
            }
            log.debug("Found analysis data: ", analysisdataList);
            return analysisdataList;
        }
    }

    public void deleteAnalysisById(final long id) throws ServiceException {
        deleteAnalysis(getAnalysisById(id));
    }


    public void deleteAnalysis(final Analysis analysis) throws ServiceException {
        if(analysis == null) {
            throw new ServiceException("Tried to delete analysis with <null> param");
        }
        final SqlSession session = factory.openSession();
        List<Analysis> analysisList = null;
        try {
            log.debug("Deleting analysis: ", analysis);
            //TODO final Resource res = permissionsService.getResource(AnalysisLayer.TYPE, "analysis+" + analysis.getId());
            //permissionsService.deleteResource(res);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            mapper.deleteAnalysisById(analysis.getId());
            mapper.deleteAnalysisDataById(analysis.getId());
            mapper.deleteAnalysisStyleById(analysis.getStyle_id());
            session.commit();
        } catch (Exception e) {
            session.rollback();
            log.warn(e, "Exception when trying delete analysis by id: ", analysis);
        } finally {
            session.close();
        }
    }

    public void mergeAnalysis(final Analysis analysis, final List<Long> ids) throws ServiceException {
        if(analysis == null) {
            throw new ServiceException("Tried to merge analysis with <null> param");
        }
        final SqlSession session = factory.openSession();
        List<Analysis> analysisList = null;
        if (ids.size() > 1) {
            try {
                log.debug("Merging analysis: ", analysis);
                //TODO final Resource res = permissionsService.getResource(AnalysisLayer.TYPE, "analysis+" + analysis.getId());
                //permissionsService.deleteResource(res);
                final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
                for (long id : ids) {
                    analysis.setOld_id(id);
                    mapper.mergeAnalysisData(analysis);
                }
                for (long id : ids) {
                    Analysis analysis_old = mapper.getAnalysisById(id);
                    mapper.deleteAnalysisById(id);
                    mapper.deleteAnalysisStyleById(analysis_old.getStyle_id());
                }
                session.commit();
            } catch (Exception e) {
                log.warn(e, "Error merging analysis data with ids: ", ids);
            } finally {
                session.close();
            }
        }
    }

    /**
     * Updates a analysis publisher screenName
     *
     * @param id
     * @param uuid
     * @param name
     */
    public long updatePublisherName(final long id, final String uuid, final String name) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Updating publisher name with id: ", id);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            final Map<String, Object> params = new HashMap<>();
            params.put("publisher_name", name);
            params.put("uuid", uuid);
            params.put("id", id);
            mapper.updatePublisherName(params);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Failed to update publisher name");
        } finally {
            session.close();
        }
        return id;
    }
}
