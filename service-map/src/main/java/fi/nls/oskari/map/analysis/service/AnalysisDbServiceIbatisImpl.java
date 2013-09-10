package fi.nls.oskari.map.analysis.service;

import java.sql.SQLException;
import java.util.List;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;

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
         * @param user uuid
         * @return List of analysis objects
         */
        public List<Analysis> getAnalysisByUid(String uid) {
            return queryForList(getNameSpace() + ".findAnalysisByUid", uid);
        }


}
