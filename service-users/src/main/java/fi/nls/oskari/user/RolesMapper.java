package fi.nls.oskari.user;

import org.oskari.user.Role;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface RolesMapper {

    @Select("select id, name from oskari_roles")
    List<Role> findAll();

    @Insert("INSERT INTO oskari_roles (name) VALUES (#{name})")
    @Options(useGeneratedKeys=true)
    void insert(Role role);

    @Select("select id, name from oskari_roles where is_guest is true")
    List<Role> findGuestRoles();

    @Insert("INSERT INTO oskari_users_roles (role_id, user_id) " +
            "VALUES (#{roleId}, #{userId})")
    void linkRoleToNewUser(@Param("roleId") long roleId, @Param("userId") long userId);

    @Update("UPDATE oskari_roles SET name = #{name} WHERE id = #{id}")
    void update(@Param("id") long id, @Param("name") String name);

    @Delete("DELETE FROM oskari_users_roles WHERE user_id = #{userId}")
    void deleteUsersRoles(long userId);

    @Delete("DELETE FROM oskari_roles WHERE id = #{userId}")
    void delete(long userId);

    List<Role> findByUserName(String username);
    List<Role> findByUserId(long userId);
    List<Object> getExternalRolesMapping(String type);
}
