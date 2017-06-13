package fi.nls.oskari.control.users.service;

import fi.nls.oskari.control.users.model.EmailToken;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


/**
 * Created by SMAKINEN on 3.10.2016.
 */
public interface EmailMapper {
    @Insert("INSERT INTO oskari_users_pending (user_name, email, uuid, expiry_timestamp)" +
            " VALUES (#{screenname}, #{email}, #{uuid}, #{expiryTimestamp})")
    void addEmail(EmailToken emailToken);

    @Update("UPDATE oskari_users_pending SET expiry_timestamp=#{expiryTimestamp}, uuid=#{uuid} where id=#{id}")
    void updateEmail(EmailToken emailToken);

    @Select("SELECT id, user_name, email, uuid, expiry_timestamp as expiryTimestamp " +
            "FROM oskari_users_pending  WHERE uuid = #{uuid}")
    EmailToken findByToken(String uuid);

    @Select("SELECT id, user_name, email, uuid, expiry_timestamp as expiryTimestamp " +
            "FROM oskari_users_pending  WHERE email = #{email}")
    EmailToken findTokenByEmail(String email);

    @Select("SELECT user_name FROM oskari_users WHERE LOWER(email) = #{email}")
    String findUsernameForEmail(String email);

    @Select("SELECT login FROM oskari_jaas_users WHERE LOWER(login) = #{user_name}")
    String findUsernameForLogin(String username);

    @Delete("DELETE FROM oskari_users_pending WHERE uuid = #{uuid}")
    void deleteEmailToken(String uuid);

    @Select("SELECT id FROM oskari_users WHERE LOWER(user_name) = LOWER(#{user_name})")
    Long isUsernameReserved(String username);
}
