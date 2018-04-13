package fi.nls.oskari.map.layer.externalid;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OskariLayerExternalIdMapper {

    @Select("SELECT maplayerid FROM oskari_maplayer_externalid WHERE externalid = #{externalId}")
    Integer findByExternalId(@Param("externalId") String externalId);

    @Delete("DELETE FROM oskari_maplayer_externalid WHERE maplayerid = #{layerId}")
    void delete(@Param("layerId") int layerId);

}
