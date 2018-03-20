package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RolesMapper {

    @Select("select id, name from oskari_roles")
    List<Role> findAll();

    @Insert("INSERT INTO oskari_roles (name) VALUES (#{name})")
    @Options(useGeneratedKeys=true)
    void insert(Role role);

    @Select("select id, name from oskari_roles where is_guest is true")
    List<Role> findGuestRoles();

    @Insert("INSERT INTO oskari_role_oskari_user (role_id, user_id) " +
            "VALUES (#{roleId}, #{userId})")
    void linkRoleToNewUser(@Param("roleId") long roleId, @Param("userId") long userId);

    @Delete("DELETE FROM oskari_role_oskari_user WHERE user_id = #{userId}")
    void deleteUsersRoles(long userId);

    @Delete("DELETE FROM oskari_roles WHERE id = #{userId}")
    void delete(long userId);

    List<Role> findByUserName(String username);
    List<Role> findByUserId(long userId);
    List<Object> getExternalRolesMapping(String type);
}
