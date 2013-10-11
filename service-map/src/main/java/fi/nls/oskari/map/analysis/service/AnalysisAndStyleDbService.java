package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.AnalysisAndStyle;
import fi.nls.oskari.service.db.BaseService;

public interface AnalysisAndStyleDbService extends BaseService<AnalysisAndStyle> {    
        public AnalysisAndStyle getAnalysisAndStyleById(int id);
       
}
