package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.Analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AnalysisMapper {

    void insertAnalysisRow(final Analysis analysis);
    void updateAnalysisCols(final Analysis analysis);
    Analysis getAnalysisById(long id);
    List<Analysis> getAnalysisByIdList(List<Long> idList);
    List<Analysis> getAnalysisByUid(String uid);
    List<HashMap<String,Object>> getAnalysisDataByIdUid(Map<String, Object> params);
    void deleteAnalysisById(final long id);
    void deleteAnalysisDataById(final long id);
    void deleteAnalysisStyleById(final long id);
    void updatePublisherName(final Map<String, Object> params);
    void mergeAnalysisData(final Analysis analysis);
}
