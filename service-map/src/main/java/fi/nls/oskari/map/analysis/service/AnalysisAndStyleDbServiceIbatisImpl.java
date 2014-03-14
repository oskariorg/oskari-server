package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.AnalysisAndStyle;
import fi.nls.oskari.service.db.BaseIbatisService;

public class AnalysisAndStyleDbServiceIbatisImpl extends
		BaseIbatisService<AnalysisAndStyle> implements AnalysisAndStyleDbService {

 

	
    @Override
	protected String getNameSpace() {
		return "AnalysisAndStyle";
	}

	    /*
	     * The purpose of this method is to allow many SqlMapConfig.xml files in a
	     * single portlet
	     */
	    protected String getSqlMapLocation() {
	        return "META-INF/SqlMapConfig_Analysis.xml";
	    }

	  
	   
	 
	    public AnalysisAndStyle getAnalysisAndStyleById(int id) {
	        return queryForObject(getNameSpace() + ".findAnalysisAndStyle", id);
	    }
	    
	  

      


}
