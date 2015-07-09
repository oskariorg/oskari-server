package fi.nls.oskari.spring.security.saml;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.security.OskariUserHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Hooks in OskariUserHelper.onAuthenticationSuccess(). Extends different Spring class than the similar class
 * in DB package.
 */
public class OskariAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private Logger log = LogFactory.getLogger(OskariAuthenticationSuccessHandler.class);
    private OskariUserHelper helper = new OskariUserHelper();

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, javax.servlet.ServletException {
        log.debug("SAML based login successful:", authentication.getPrincipal(), "- User details:", authentication.getDetails());
        super.onAuthenticationSuccess(request, response, authentication);
        if(authentication.getDetails() instanceof User) {
            helper.onAuthenticationSuccess(request,response,((User) authentication.getDetails()).getUsername());
        }
        else {
            helper.onAuthenticationSuccess(request,response,authentication);
        }
    }
}
