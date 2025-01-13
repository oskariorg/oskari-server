package org.oskari.permissions;

import java.util.List;
import java.util.Set;

import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.FetchType;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.PermissionExternalType;
import org.oskari.permissions.model.Resource;

public interface ResourceMapper {

    Resource findById(@Param("id") int id);

    @Select("SELECT EXISTS (SELECT 1 FROM oskari_resource WHERE id = #{id})")
    boolean existsById(@Param("id") int id);

    List<Resource> findAll();

    List<Resource> findByType(@Param("type") String type);

    Resource findByTypeAndMapping(@Param("type") String type, @Param("mapping") String mapping);

    @Select("SELECT EXISTS (SELECT 1 FROM oskari_resource WHERE resource_type = #{type} AND resource_mapping = #{mapping})")
    boolean existsByTypeAndMapping(@Param("type") String type, @Param("mapping") String mapping);

    @Select("select distinct\n" +
            "            r.resource_mapping\n" +
            "        from\n" +
            "            oskari_resource r, oskari_resource_permission p\n" +
            "        where\n" +
            "            r.id=p.resource_id\n" +
            "            and r.resource_type=#{resourceType}\n" +
            "            and p.external_type=#{externalType}\n" +
            "            and p.permission=#{permission}\n" +
            "            and p.external_id IN (#{external_id})")
    Set<String> findMappingsForPermission(@Param("resourceType") String resourceType,
                                          @Param("externalType") PermissionExternalType externalType,
                                          @Param("permission") String permission,
                                          @Param("external_id") String external_id);

    @Insert("INSERT INTO oskari_resource (resource_type, resource_mapping) VALUES (#{type},#{mapping})")
    @Options(useGeneratedKeys=true, keyColumn="id", keyProperty="id")
    void insertResource(Resource resource);

    @Delete("DELETE FROM oskari_resource WHERE id=#{id}")
    void deleteResource(Resource resource);

    @Insert("INSERT INTO oskari_resource_permission (resource_id, permission, external_type, external_id) "
            + "VALUES (#{resourceId},#{permission.type},#{permission.externalType},#{permission.externalId})")
    @Options(useGeneratedKeys=true, keyColumn="id", keyProperty="permission.id")
    void insertPermission(@Param("permission") Permission permission, @Param("resourceId") int resourceId);

    @Delete("DELETE FROM oskari_resource_permission WHERE resource_id=#{id}")
    void deletePermissions(int resourceId);

}
