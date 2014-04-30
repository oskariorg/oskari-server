package fi.nls.oskari.map.servlet;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;

public class JaasAuthenticationFilter implements Filter {
    private final static Logger log = LogFactory.getLogger(JaasAuthenticationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        try {
            setupSession(httpRequest, httpRequest.getUserPrincipal());
        } catch (ServiceException e) {
            log.error(e, "Session setup failed");
        }

        chain.doFilter(request, response);
    }

    private void setupSession(HttpServletRequest httpRequest, Principal userPrincipal) throws ServiceException {
        if(userPrincipal != null) {
            final String KEY_ATTR = User.class.getName();
            HttpSession session = httpRequest.getSession(false);
            if(session == null || session.getAttribute(KEY_ATTR) == null) {
                log.debug("Setting user", userPrincipal.getName(), "in session with key", KEY_ATTR);
                User user = UserService.getInstance().getUser(userPrincipal.getName());
                log.debug("Got user", user, "from", UserService.getInstance().getClass().getName());
                httpRequest.getSession().setAttribute(KEY_ATTR, user);
            }
        }
    }

    @Override
    public void destroy() {}
}
