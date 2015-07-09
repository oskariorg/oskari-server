package fi.nls.oskari.spring.security;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Common helper methods needed for both SAML and DB authentication
 */
public class OskariUserHelper {

    private static Logger log = LogFactory.getLogger(OskariUserHelper.class);

    /**
     * Common code done for SAML and DB authentication on successful login
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws javax.servlet.ServletException
     */
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException,
            javax.servlet.ServletException {
        onAuthenticationSuccess(request, response, authentication.getPrincipal().toString());
    }
    /**
     * Common code done for SAML and DB authentication on successful login
     * @param request
     * @param response
     * @param username
     * @throws IOException
     * @throws javax.servlet.ServletException
     */
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, String username)
            throws IOException,
            javax.servlet.ServletException {
        log.debug("Auth success");
        // setup user object in session for Oskari
        setupSession(request, username);
        log.info("Auth success and session setup complete");
    }

    /**
     * Tries to setup Oskari user information to session based on given username.
     * @param httpRequest
     * @param username
     */
    public void setupSession(final HttpServletRequest httpRequest, final String username)  {
        final User user = getLoggedInUser(httpRequest);
        if(user != null && !user.isGuest()) {
            // user is already logged in
            return;
        }
        log.debug("Getting user from service with principal name:", username);
        try {
            User loadedUser = UserService.getInstance().getUser(username);
            log.debug("Got user from service:", loadedUser);
            if(loadedUser != null) {
                httpRequest.getSession(true).setAttribute(User.class.getName(), loadedUser);
            }
            else {
                log.error("Login user check failed! Got user from principal, but can't find it in Oskari db:", username);
            }
        } catch (Exception e) {
            log.error(e, "Session setup failed");
        }
    }

    /**
     * Returns the user object from session without creating a new session
     * if one is not available. Returns null if user or session does not exist.
     * @param httpRequest
     * @return
     */
    private User getLoggedInUser(final HttpServletRequest httpRequest) {
        final HttpSession session = httpRequest.getSession(false);
        if(session != null) {
            return (User) session.getAttribute(User.class.getName());
        }
        return null;
    }

    /**
     * Wraps Oskari roles as a List of Spring security GrantedAuthorities
     * @param roles
     * @return
     */
    public static List<GrantedAuthority> getRoles(Set<Role> roles) {
        final List<GrantedAuthority> grantedAuths = new ArrayList<>();
        if(roles == null) {
            return grantedAuths;
        }
        for(Role role : roles) {
            grantedAuths.add(new SimpleGrantedAuthority(role.getName()));
        }
        return grantedAuths;
    }
}
