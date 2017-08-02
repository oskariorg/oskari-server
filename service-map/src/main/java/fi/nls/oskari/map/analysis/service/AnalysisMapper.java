package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.service.ServiceException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AnalysisMapper {

    long insertAnalysisRow(final Analysis analysis);
    int updateAnalysisCols(final Analysis analysis);
    Analysis getAnalysisById(long id);
    List<Analysis> getAnalysisById(List<Long> idList);
    List<Analysis> getAnalysisByUid(String uid);
    List<HashMap<String,Object>> getAnalysisDataByIdUid(Map<String, Object> params);
    void deleteAnalysisById(final long id) throws ServiceException;
}
