package fi.nls.oskari.map.analysis.service;


import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.service.db.BaseService;

public interface AnalysisStyleDbService extends BaseService<AnalysisStyle> {    
        
       
      
        public long insertAnalysisStyleRow(final AnalysisStyle analysisStyle);
     
        

}
