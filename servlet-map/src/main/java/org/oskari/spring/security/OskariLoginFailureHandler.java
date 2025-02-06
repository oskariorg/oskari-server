package org.oskari.spring.security;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.oskari.log.AuditLog;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Simple extension for getting the login failure exception logged
 */
public class OskariLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private Logger log = LogFactory.getLogger(OskariLoginFailureHandler.class);

    public OskariLoginFailureHandler() {
        super();
    }

    public OskariLoginFailureHandler(String url) {
        super(url);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.debug(exception, "Login failed!");
        log.warn("Login failed:", exception.getMessage(), getCauseMsg(exception));
        super.onAuthenticationFailure(request, response, exception);
        AuditLog.guest(ActionParameters.getClientIp(request))
                .withMsg("Login")
                .withParam("ex", exception.getMessage() + " " + getCauseMsg(exception))
                .errored(AuditLog.ResourceType.USER);
    }
    private String getCauseMsg(Exception e) {
        if (e.getCause() == null) {
            return "";
        }
        return "- Cause: " + e.getCause().getClass() + " with msg: " + e.getCause().getMessage();
    }
}
