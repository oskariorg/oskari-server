package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.service.db.BaseService;

public interface AnalysisDbService extends BaseService<Analysis> {    
        
       
        public long insertAnalysisRow(final Analysis analysis);
        public int updateAnalysisCols(final Analysis analysis);
        public Analysis getAnalysisById(long id);
      
       
        

}
