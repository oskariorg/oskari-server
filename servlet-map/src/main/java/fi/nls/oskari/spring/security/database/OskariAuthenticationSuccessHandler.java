package fi.nls.oskari.spring.security.database;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.security.OskariUserHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Hooks in OskariUserHelper.onAuthenticationSuccess(). Extends different Spring class than the similar class
 * in SAML package.
 */
public class OskariAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private Logger log = LogFactory.getLogger(OskariAuthenticationSuccessHandler.class);
    private OskariUserHelper helper = new OskariUserHelper();

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, javax.servlet.ServletException {
        log.debug("Database based login successful:", authentication.getPrincipal());
        super.onAuthenticationSuccess(request, response, authentication);
        helper.onAuthenticationSuccess(request, response, authentication);
    }
}
