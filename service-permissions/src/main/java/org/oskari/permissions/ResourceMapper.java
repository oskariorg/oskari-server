package org.oskari.permissions;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import fi.nls.oskari.permission.domain.Permission;
import fi.nls.oskari.permission.domain.Resource;

public interface ResourceMapper {

    @Results(id = "ResourceResult", value = {
            @Result(property="id", column="id", id=true),
            @Result(property="type", column="resource_type"),
            @Result(property="mapping", column="resource_mapping"),
            @Result(property="permissions", column="oskari_resource_id",
            javaType=List.class, many=@Many(select="findPermissionsByResourceId"))
    })
    @Select("SELECT id,"
            + "resource_type,"
            + "resource_mapping "
            + "FROM oskari_resource "
            + "WHERE id = #{id}")
    Resource findById(@Param("id") int id);

    @ResultMap("ResourceResult")
    @Select("SELECT id,"
            + "resource_type,"
            + "resource_mapping "
            + "FROM oskari_resource "
            + "WHERE resource_type = #{type} "
            + "AND resource_mapping = #{mapping}")
    Resource findByTypeAndMapping(@Param("type") Resource.Type type, @Param("mapping") int mapping);

    @Select("SELECT id,"
            + "external_type,"
            + "permission,"
            + "external_id "
            + "FROM oskari_permission "
            + "WHERE oskari_resource_id = #{resourceId}")
    List<Permission> findPermissionsByResourceId(@Param("resourceId") int resourceId);

    @Insert("INSERT INTO oskari_resource (resource_type, resource_mapping) VALUES (#{type},#{mapping})")
    @Options(useGeneratedKeys=true)
    void insertResource(Resource resource);

    @Delete("DELETE FROM oskari_resource WHERE id=#{id}")
    void deleteResource(Resource resource);

    @Insert("INSERT INTO oskari_permission (oskari_resource_id, external_type, permission, external_id) "
            + "VALUES (#{resourceId},#{permission.type},#{permission.externalType},#{permission.externalId})")
    @Options(useGeneratedKeys=true, keyColumn="id", keyProperty="permission.id")
    void insertPermission(@Param("permission") Permission permission, @Param("resourceId") int resourceId);

    @Delete("DELETE FROM oskari_permission WHERE oskari_resource_id=#{id}")
    void deletePermissions(Resource resource);

}
