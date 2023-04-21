package fi.nls.oskari.user;

import fi.nls.oskari.domain.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UsersMapper {
    @Results(id = "UsersResult", value = {
            @Result(property="id", column="id", id=true),
            @Result(property="firstname", column="first_name"),
            @Result(property="lastname", column="last_name"),
            @Result(property="email", column="email"),
            @Result(property="uuid", column="uuid"),
            @Result(property="screenname", column="user_name"),
            @Result(property="attributes", column="attributes")
    })
    @Select("SELECT id, first_name, last_name, user_name, email, uuid, attributes" +
            " FROM oskari_users" +
            " ORDER BY user_name")
    List<User> findAll();

    @ResultMap("UsersResult")
    @Select("SELECT id, first_name, last_name, user_name, email, uuid, attributes" +
            " FROM oskari_users" +
            " ORDER BY user_name" +
            " LIMIT #{limit} OFFSET #{offset}")
    List<User> findAllPaginated(@Param("limit") int limit, @Param("offset") int offset);

    @ResultMap("UsersResult")
    @Select("SELECT id, first_name, last_name, user_name, email, uuid, attributes" +
            " FROM oskari_users" +
            " WHERE " +
            "   user_name ilike '%' || #{query} || '%'" +
            "   OR first_name ilike '%' || #{query} || '%'" +
            "   OR last_name ilike '%' || #{query} || '%'" +
            "   OR email ilike '%' || #{query} || '%' " +
            " ORDER BY user_name" +
            " LIMIT #{limit} OFFSET #{offset}")
    List<User> findAllPaginatedSearch(@Param("query") String query, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT count(*) FROM oskari_users")
    int findUserCount();

    @Select("SELECT count(*) FROM oskari_users" +
            " WHERE " +
            "   user_name ilike '%' || #{query} || '%'" +
            "   OR first_name ilike '%' || #{query} || '%'" +
            "   OR last_name ilike '%' || #{query} || '%'" +
            "   OR email ilike '%' || #{query} || '%' ")
    int findUserSearchCount(String query);

    @Insert("INSERT INTO oskari_users (" +
            " first_name, " +
            " last_name, " +
            " user_name, " +
            " email, " +
            " uuid, " +
            " attributes " +
            ") " +
            " VALUES (" +
            " #{firstname}," +
            " #{lastname}," +
            " #{screenname}," +
            " #{email}," +
            " #{uuid}," +
            " #{attributes}" +
            ")")
    @Options(useGeneratedKeys=true, keyColumn="id", keyProperty="id")
    Long addUser(User user);

    @Update("UPDATE oskari_users SET " +
            " first_name = #{firstname}, " +
            " last_name = #{lastname}, " +
            " user_name = #{screenname}, " +
            " email = #{email}, " +
            " attributes = #{attributes} " +
            " WHERE id = #{id}")
    void updateUser(User user);

    @ResultMap("UsersResult")
    @Select("SELECT id, first_name, last_name, user_name, email, uuid, attributes" +
            " FROM oskari_users" +
            " WHERE id = #{id}")
    User find(long id);

    @Select("SELECT login" +
            " FROM oskari_users_credentials" +
            " WHERE login = #{username} AND password=#{password}")
    String login(@Param("username") String username, @Param("password") String password);

    @Select("SELECT password" +
            " FROM oskari_users_credentials" +
            " WHERE login = #{username}")
    String getPassword(final String username);

    @ResultMap("UsersResult")
    @Select("SELECT id, first_name, last_name, user_name, email, uuid, attributes" +
            " FROM oskari_users" +
            " WHERE user_name = #{username}")
    User findByUserName(String username);

    @ResultMap("UsersResult")
    @Select("SELECT id, user_name, first_name, last_name, email, uuid, attributes FROM oskari_users" +
            " WHERE LOWER(email) = #{email}")
    User findByEmail(String email);

    @Delete("DELETE FROM oskari_users WHERE id = #{id}")
    void delete(long id);

    @Insert("INSERT INTO oskari_users_credentials"
            + " (login, password)"
            + " VALUES (#{username}, #{password})")
    void addPassword(@Param("username") String username, @Param("password") String password);

    @Update("UPDATE oskari_users_credentials SET" +
            "    password = #{password}" +
            "    WHERE login = #{username}")
    void updatePassword(@Param("username") String username, @Param("password") String password);

    @Delete("DELETE FROM oskari_users_credentials WHERE login = #{username}")
    void deletePassword(String username);
}
