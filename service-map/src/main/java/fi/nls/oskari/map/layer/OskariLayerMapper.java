package fi.nls.oskari.map.layer;

import fi.nls.oskari.domain.map.OskariLayer;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Select;

public interface OskariLayerMapper {

    List<Map<String,Object>> findByUrlAndName(final Map<String, String> params);
    List<Map<String,Object>> findByIdList(final Map<String, Object> params);
    List<Map<String,Object>> find(int id);
    List<Map<String,Object>> findByUuid(String uuid);
    List<Map<String,Object>> findByParentId(int parentId);
    List<Map<String,Object>> findAll();
    List<Map<String,Object>> findAllWithPositiveUpdateRateSec();
    @Select ("select name from oskari_maplayer") List<String> findAllNames();
    int update(final OskariLayer layer);
    void insert(final OskariLayer layer);
    int delete(final int layerId);

}
