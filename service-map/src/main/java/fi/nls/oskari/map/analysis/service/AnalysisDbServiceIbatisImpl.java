package fi.nls.oskari.map.analysis.service;

import com.ibatis.sqlmap.client.SqlMapSession;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.sql.SQLException;
import java.util.List;

public class AnalysisDbServiceIbatisImpl extends
        BaseIbatisService<Analysis> implements AnalysisDbService {

    private static final Logger log = LogFactory.getLogger(AnalysisDbServiceIbatisImpl.class);


    @Override
    protected String getNameSpace() {
        return "Analysis";
    }

    /*
     * The purpose of this method is to allow many SqlMapConfig.xml files in a
     * single portlet
     */
    protected String getSqlMapLocation() {
        return "META-INF/SqlMapConfig_Analysis.xml";
    }

    /**
     * insert Analysis table row
     *
     * @param analysis
     */

    public long insertAnalysisRow(final Analysis analysis) {

        log.debug("Insert analyse row:", analysis);
        final Long id = queryForObject(getNameSpace() + ".insertAnalysis", analysis);
        analysis.setId(id);
        log.debug("Got analyse id:", id);
        return id;
    }

    /**
     * update Analysis table row field mapping
     *
     * @param analysis
     */
    public int updateAnalysisCols(final Analysis analysis) {


        try {
            return getSqlMapClient().update(
                    getNameSpace() + ".updateAnalysisCols", analysis);
        } catch (SQLException e) {
            log.error(e, "Failed to update analysis col mapping", analysis);
        }
        return 0;
    }

    /**
     * Get Analysis row  by id
     *
     * @param id
     * @return analysis object
     */
    public Analysis getAnalysisById(long id) {
        return queryForObject(getNameSpace() + ".findAnalysis", id);
    }

    /**
     * Get Analysis rows of one user by uuid
     *
     * @param uid user uuid
     * @return List of analysis objects
     */
    public List<Analysis> getAnalysisByUid(String uid) {
        return queryForList(getNameSpace() + ".findAnalysisByUid", uid);
    }

    public void deleteAnalysisById(final long id) throws ServiceException {
        final Analysis analysis = getAnalysisById(id);
        deleteAnalysis(analysis);
    }

    public void deleteAnalysis(final Analysis analysis) throws ServiceException {
        if(analysis == null) {
            throw new ServiceException("Tried to delete analysis with <null> param");
        }
        final SqlMapSession session = openSession();
        try {
            session.startTransaction();
            session.delete(getNameSpace() + ".delete-analysis-data", analysis.getId());
            session.delete(getNameSpace() + ".delete-analysis", analysis.getId());
            // style is for now 1:1 to analysis so we can delete it here
            session.delete(getNameSpace() + ".delete-analysis-style", analysis.getStyle_id());
            session.commitTransaction();
        } catch (Exception e) {
            throw new ServiceException("Error deleting analysis data with id:" + analysis.getId(), e);
        } finally {
            endSession(session);
        }
    }


}
