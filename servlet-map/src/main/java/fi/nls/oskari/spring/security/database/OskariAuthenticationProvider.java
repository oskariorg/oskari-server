package fi.nls.oskari.spring.security.database;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.spring.security.OskariUserHelper;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


/**
 * Sample custom authentication provider using Oskari UserService.
 */
public class OskariAuthenticationProvider implements AuthenticationProvider {

    private Logger log = LogFactory.getLogger(OskariAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        try {
            User user = getUser(name, password);
            return new UsernamePasswordAuthenticationToken(name, password, OskariUserHelper.getRoles(user.getRoles()));
        } catch (Exception ex) {
            log.error(ex, "Exception on auth!");
        }
        throw new AuthenticationServiceException("Unable to auth");
    }

    public User getUser(final String username, final String password) throws UsernameNotFoundException {

        try {
            User user = UserService.getInstance().login(username, password);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            return user;

        } catch (Exception ex) {
            log.error(ex, "Exception on auth!");
            throw new UsernameNotFoundException("User not found");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
