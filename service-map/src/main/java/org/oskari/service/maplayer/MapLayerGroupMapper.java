package org.oskari.service.maplayer;

import fi.nls.oskari.domain.map.MaplayerGroup;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface MapLayerGroupMapper {

    @Insert("INSERT INTO oskari_maplayer_group (parentid, locale, order_number, selectable) "
            + "VALUES (#{group.parentId},#{group.locale},#{group.orderNumber},#{group.selectable})")
    @Options(useGeneratedKeys=true, keyColumn="id", keyProperty="id")
    void insert(@Param("group") MaplayerGroup group);

    @Update("UPDATE oskari_maplayer_group SET " +
            "parentid = #{group.parentId}, " +
            "locale = #{group.locale}, " +
            "order_number = #{group.order_number}, " +
            "selectable = #{group.selectable} " +
            "WHERE id = #{group.id}")
    void update(@Param("group") MaplayerGroup group);

    @Delete("DELETE FROM oskari_maplayer_group WHERE id=#{id}")
    void delete(MaplayerGroup group);

    @Results(id = "MaplayerGroupResult", value = {
            @Result(property="id", column="id", id=true),
            @Result(property="parentId", column="parentid"),
            @Result(property="orderNumber", column="order_number")
    })
    @Select("SELECT id,"
            + "parentid, "
            + "locale, "
            + "order_number, "
            + "selectable "
            + "FROM oskari_maplayer_group")
    List<MaplayerGroup> findAll();

    @ResultMap("MaplayerGroupResult")
    @Select("SELECT id,"
            + "parentid, "
            + "locale, "
            + "order_number, "
            + "selectable "
            + "FROM oskari_maplayer_group "
            + "WHERE id = #{id}")
    MaplayerGroup findById(@Param("id") int id);

    @ResultMap("MaplayerGroupResult")
    @Select("SELECT id, "
            + "parentid, "
            + "locale, "
            + "order_number, "
            + "selectable "
            + "FROM oskari_maplayer_group "
            + "WHERE parentid = #{id}")
    List<MaplayerGroup> findByParentId(@Param("id") int id);

    @Update("UPDATE oskari_maplayer_group SET " +
            "parentid = #{newParent} " +
            "WHERE id = #{groupId}")
    int updateGroupParent(@Param("groupId") int id, @Param("newParent") int parent);

    @Update("UPDATE oskari_maplayer_group SET " +
            "order_number = #{orderNumber} " +
            "WHERE id = #{groupId}")
    int updateOrder(@Param("groupId") int id, @Param("orderNumber") int orderNumber);
}
