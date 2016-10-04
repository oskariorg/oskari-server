package fi.nls.oskari.control.users.service;

import fi.nls.oskari.control.users.model.Email;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;


/**
 * Created by SMAKINEN on 3.10.2016.
 */
public interface EmailMapper {
    @Insert("INSERT INTO oskari_users_pending (user_name, email, uuid, expiry_timestamp)" +
            " VALUES (#{screenname}, #{email}, #{uuid}, #{expiryTimestamp})")
    void addEmail(Email email);

    @Select("SELECT id, user_name, email, uuid, expiry_timestamp as expiryTimestamp " +
            "FROM oskari_users_pending  WHERE uuid = #{uuid}")
    Email findByToken(String uuid);

    @Select("SELECT user_name FROM oskari_users WHERE email = #{email}")
    String findUsernameForEmail(String email);

    @Select("SELECT login FROM oskari_jaas_users WHERE login = #user_name#")
    String findUsernameForLogin(String username);

    @Delete("DELETE FROM oskari_users_pending WHERE uuid = #{uuid}")
    void deleteEmailToken(String uuid);

    @Select("SELECT email FROM oskari_users WHERE user_name = #{user_name}")
    String findEmailForUsername(String username);

    @Select("SELECT id FROM oskari_roles WHERE name = #{name}")
    Integer findUserRoleId(String name);
}
