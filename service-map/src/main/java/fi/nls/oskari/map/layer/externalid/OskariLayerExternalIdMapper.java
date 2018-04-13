package fi.nls.oskari.map.layer.externalid;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OskariLayerExternalIdMapper {

    @ConstructorArgs({
        @Arg(column="maplayerid", javaType=Integer.class),
        @Arg(column="externalid", javaType=String.class)
    })
    @Select("SELECT maplayerid, externalid FROM oskari_maplayer_externalid WHERE externalid = #{externalId}")
    OskariLayerExternalId findByExternalId(@Param("externalId") String externalId);

    @Insert("INSERT INTO oskari_maplayer_externalid (maplayerid, externalid) VALUES (#{layerId}, #{externalId})")
    int insert(OskariLayerExternalId link);

    @Delete("DELETE FROM oskari_maplayer_externalid WHERE maplayerid = #{layerId}")
    int delete(@Param("layerId") int layerId);

}
