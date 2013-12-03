package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;

public class AnalysisStyleDbServiceIbatisImpl extends
		BaseIbatisService<AnalysisStyle> implements AnalysisStyleDbService {

    private static final Logger log = LogFactory.getLogger(AnalysisStyleDbServiceIbatisImpl.class);

	
    @Override
	protected String getNameSpace() {
		return "AnalysisStyle";
	}

	    /*
	     * The purpose of this method is to allow many SqlMapConfig.xml files in a
	     * single portlet
	     */
   
    protected String getSqlMapLocation() {
	        return "META-INF/SqlMapConfig_AnalysisStyle.xml";
	    }

	  
	    /**
         * insert Analysis_style table row
         * 
         * @param analysisStyle
         */

        public long insertAnalysisStyleRow(final AnalysisStyle analysisStyle) {
            
            log.debug("Insert analyse_style row:", analysisStyle);
            final Long id = queryForObject(getNameSpace() + ".insertAnalysisStyleRow", analysisStyle);
            analysisStyle.setId(id);
            log.debug("Got analyse style id:", id);
            return id;
        }
	   
	 

      


}
