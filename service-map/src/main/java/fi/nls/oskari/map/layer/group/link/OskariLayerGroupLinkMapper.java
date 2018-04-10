package fi.nls.oskari.map.layer.group.link;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface OskariLayerGroupLinkMapper {

    @Select("SELECT maplayerid AS layerId, groupid AS groupId, order_number AS orderNumber"
            + " FROM oskari_maplayer_group_link"
            + " WHERE maplayerid = #{layerId}"
            + " ORDER BY order_number")
    List<OskariLayerGroupLink> findByLayerId(@Param("layerId") int layerId);

    @Select("SELECT maplayerid AS layerId, groupid AS groupId, order_number AS orderNumber"
            + " FROM oskari_maplayer_group_link"
            + " WHERE groupid = #{groupId}"
            + " ORDER BY order_number")
    List<OskariLayerGroupLink> findByGroupId(@Param("groupId") int groupId);

    @Insert("INSERT INTO DEFAULT_ORDER_NUMBER"
            + " (maplayerid, groupid, order_number) VALUES"
            + " (#{layerId}, #{groupId}, #{orderNumber})")
    void insert(OskariLayerGroupLink link);

    @Delete("DELETE FROM oskari_maplayer_group_link"
            + " WHERE maplayerid = #{layerId}"
            + " AND groupid = #{groupId}")
    void delete(@Param("layerId") int layerId, @Param("groupId") int groupId);

    @Delete("DELETE FROM oskari_maplayer_group_link"
            + " WHERE maplayerid = #{layerId}")
    void deleteByLayerId(@Param("layerId") int layerId);

    @Update("UPDATE oskari_maplayer_group_link"
            + " SET order_number = #{orderNumber}")
    void updateOrderNumber(OskariLayerGroupLink link);

    @Select("SELECT EXISTS (SELECT 1 FROM oskari_maplayer_group_link WHERE groupid = #{groupId})")
    boolean hasLinks(@Param("groupId") int groupId);

}
