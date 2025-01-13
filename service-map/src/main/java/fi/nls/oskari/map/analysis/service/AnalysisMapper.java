package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.analysis.AnalysisData;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AnalysisMapper {

    Analysis getAnalysisById(long id);
    List<Analysis> getAnalysisByIdList(List<Long> idList);
    List<Analysis> getAnalysisByUid(String uid);
    List<HashMap<String,Object>> getAnalysisDataByIdUid(Map<String, Object> params);
    void deleteAnalysisById(final long id);
    void deleteAnalysisDataById(final long id);
    void updatePublisherName(final Map<String, Object> params);


    @ResultMap("AnalysisDataResult")
    @Select("SELECT " +
            " id, " +
            " analysis_id, " +
            " uuid, " +
            " t1, " +
            " t3, " +
            " t4, " +
            " t5, " +
            " t6, " +
            " t7, " +
            " t8, " +
            " n1, " +
            " n2, " +
            " n3, " +
            " n4, " +
            " n5, " +
            " n6, " +
            " n7, " +
            " n8, " +
            " d1, " +
            " d2, " +
            " d3, " +
            " d4, " +
            " created, " +
            " updated, " +
            " ST_ASTEXT(geometry) as wkt, " +
            " ST_SRID(geometry) as srid " +
            " FROM analysis_data " +
            " WHERE "+
            " analysis_id = #{analysisId} " +
            " AND " +
            " ST_INTERSECTS(" +
            "   ST_MAKEENVELOPE(#{minX}, #{minY}, #{maxX}, #{maxY}, #{srid}), " +
        "       geometry)")
    List<AnalysisData> findAllByBBOX(@Param("analysisId") int analysisId,
                                     @Param("minX") double minX,
                                     @Param("minY") double minY,
                                     @Param("maxX") double maxX,
                                     @Param("maxY") double maxY,
                                     @Param("srid") int srid);
}
