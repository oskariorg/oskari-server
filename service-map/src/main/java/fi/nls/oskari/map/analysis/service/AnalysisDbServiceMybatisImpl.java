package fi.nls.oskari.map.analysis.service;

import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
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

    private static final Logger log = LogFactory.getLogger(AnalysisDbServiceMybatisImpl.class);

    private PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    private SqlSessionFactory factory = null;

    public AnalysisDbServiceMybatisImpl() {

        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("analysis"));
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
        long analysisId = 0;
        try {
            log.debug("Insert analyse row:", analysis);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            mapper.insertAnalysisRow(analysis);
            //TODO get keyword id
            //analysisId =  insertAnalysisRow(analysis);
            //analysis.setId(analysisId);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to add analysis: ", analysis);
        } finally {
            session.close();
        }
        log.debug("Got analyse id:", analysisId);
        return analysisId;
    }

    /**
     * update Analysis table row field mapping
     *
     * @param analysis
     */
    public int updateAnalysisCols(final Analysis analysis) {
        final SqlSession session = factory.openSession();
        int analysisId = 0;
        try {
            log.debug("Updating analysis columns:", analysis);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            analysisId = mapper.updateAnalysisCols(analysis); //TODO return id
        } catch (Exception e) {
            log.warn(e, "Exception when trying to update analysis columns mapping: ", analysis);
        } finally {
            session.close();
        }
        return analysisId;
    }

    /**
     * Get Analysis row  by id
     *
     * @param id
     * @return analysis object
     */
    public Analysis getAnalysisById(long id) {
        final SqlSession session = factory.openSession();
        Analysis analysis = null;
        try {
            log.debug("Finding analysis matching id: ", id);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            analysis =  mapper.getAnalysisById(id);
            log.debug("Found analysis: ", analysis);
        } catch (Exception e) {
            log.warn(e, "Exception when trying get analysis by id: ", id);
        } finally {
            session.close();
        }
        return analysis;
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
            analysisList =  mapper.getAnalysisById(idList);
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
        final SqlSession session = factory.openSession();
        List<HashMap<String,Object>> analysisdataList = null;
        try {
            log.debug("Finding analysis data matching id and uid", id, uuid);

            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", id);
            params.put("uuid", uuid);
            params.put("select_items", select_items);
            analysisdataList =  mapper.getAnalysisDataByIdUid(params);
            if(analysisdataList == null) {
                analysisdataList = Collections.emptyList();
            }
            log.debug("Found analysis data: ", analysisdataList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying get analysis data by Id and Uid: ", id, uuid);
        } finally {
            session.close();
        }
        return analysisdataList;
    }

    public void deleteAnalysisById(final long id) throws ServiceException {
        final Analysis analysis = getAnalysisById(id);
        deleteAnalysis(analysis);
    }


    public void deleteAnalysis(final Analysis analysis) throws ServiceException {
        /*if(analysis == null) {
            throw new ServiceException("Tried to delete analysis with <null> param");
        }
        final SqlMapSession session = openSession();
        try {
            session.startTransaction();
            // remove resource & permissions
            final Resource res = permissionsService.getResource(AnalysisLayer.TYPE, "analysis+" + analysis.getId());
            permissionsService.deleteResource(res);

            // remove analysis
            session.delete(getNameSpace() + ".delete-analysis-data", analysis.getId());
            session.delete(getNameSpace() + ".delete-analysis", analysis.getId());
            // style is for now 1:1 to analysis so we can delete it here
            session.delete(getNameSpace() + ".delete-analysis-style", analysis.getStyle_id());
            session.commitTransaction();
        } catch (Exception e) {
            throw new ServiceException("Error deleting analysis data with id:" + analysis.getId(), e);
        } finally {
            endSession(session);
        }*/
    }

    public void mergeAnalysis(final Analysis analysis, final List<Long> ids) throws ServiceException {
        /*if (ids == null) {
            throw new ServiceException("Tried to merge analysis with <null> param");
        }

        if (ids.size() > 1) {
            final SqlMapSession session = openSession();
            try {
                session.startTransaction();
                // replace data of old analysises to new analysis
                for (long id : ids) {
                    analysis.setOld_id(id);
                    session.update(getNameSpace() + ".merge-analysis-data", analysis);
                }
                for (long id : ids) {
                    Analysis analysis_old = queryForObject(getNameSpace() + ".findAnalysis", id);
                    session.delete(getNameSpace() + ".delete-analysis", id);
                    // style is for now 1:1 to analysis so we can delete it here
                    session.delete(getNameSpace() + ".delete-analysis-style", analysis_old.getStyle_id());
                }
                session.commitTransaction();
            } catch (Exception e) {
                throw new ServiceException("Error merging analysis data with id:" + ids.get(0), e);
            } finally {
                endSession(session);
            }
        }*/
    }

    /**
     * Updates a analysis publisher screenName
     *
     * @param id
     * @param uuid
     * @param name
     */
    public int updatePublisherName(final long id, final String uuid, final String name) {
/*
        final Map<String, Object> data = new HashMap<String,Object>();
        data.put("publisher_name", name);
        data.put("uuid", uuid);
        data.put("id", id);
        try {
            return getSqlMapClient().update(
                    getNameSpace() + ".updatePublisherName", data);
        } catch (SQLException e) {
            log.error(e, "Failed to update publisher name", data);
        }*/
        return 0;
    }
}
