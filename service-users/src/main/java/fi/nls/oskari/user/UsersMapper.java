package fi.nls.oskari.user;

import fi.nls.oskari.domain.User;

import java.util.Map;

public interface UsersMapper {
    Long addUser(User user);
    void updateUser(User user);
    User find(long id);
    String login(Map<String, String> params);
    String getPassword(final String username);
    User findByUserName(String username);
    User findByEmail(String email);
    void delete(long id);
    void setPassword(Map<String, String> params);
    void updatePassword(Map<String, String> params);
    void deletePassword(String username);
}
