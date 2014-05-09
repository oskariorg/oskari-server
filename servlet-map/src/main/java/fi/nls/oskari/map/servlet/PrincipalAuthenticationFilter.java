package fi.nls.oskari.map.servlet;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.PropertyUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;

public class PrincipalAuthenticationFilter implements Filter {
    private final static Logger log = LogFactory.getLogger(PrincipalAuthenticationFilter.class);

    private static final String KEY_USER = User.class.getName();
    private String logoutUrl = "logout";
    private String loggedOutPage = "/";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.logoutUrl = PropertyUtil.get("auth.logout.url", logoutUrl);
        this.loggedOutPage = PropertyUtil.get("auth.loggedout.page", PropertyUtil.get("oskari.map.url", loggedOutPage));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest)request;
        final User user = getLoggedInUser(httpRequest);
        final String servletPath = httpRequest.getServletPath();
        // logout
        if (servletPath != null && servletPath.indexOf(logoutUrl) != -1) {
            log.debug("Logout for user:", user);
            logout(httpRequest);
            // redirect to auth.loggedout.page with fallbacks to oskari.map.url or / if not defined
            request.removeAttribute("_logout_uri");
            // with dispatch we'll lose everything we've setup in request (login form url etc)
            // TODO: make it configurable
            final RequestDispatcher dispatch = httpRequest.getRequestDispatcher(loggedOutPage);
            dispatch.forward(request, response);

            //((HttpServletResponse)response).sendRedirect(loggedOutPage);
            return;
        }
        // user already logged in - provide logout url in request attribute
        if(user != null && !user.isGuest()) {
            request.setAttribute("_logout_uri", logoutUrl);
        }
        // handle login/session setup
        else {
            try {
                setupSession(httpRequest, user);
            } catch (ServiceException e) {
                log.error(e, "Session setup failed");
            }
        }

        chain.doFilter(request, response);
    }

    private void setupSession(final HttpServletRequest httpRequest, final User user) throws ServiceException {
        final Principal userPrincipal = httpRequest.getUserPrincipal();
        if(userPrincipal != null) {
            final HttpSession session = httpRequest.getSession(false);
            if(session == null || user == null || user.isGuest()) {
                log.debug("Getting user from service with principal name:", userPrincipal.getName());
                final User loadedUser = UserService.getInstance().getUser(userPrincipal.getName());
                log.debug("Got user from service:", loadedUser);
                httpRequest.getSession(true).setAttribute(KEY_USER, loadedUser);
                httpRequest.setAttribute("_logout_uri", logoutUrl);
            }
        }
        // our jaas fail url is by default /?loginState=failed
        // -> pass the failed login flag to request as attribute
        if ("failed".equals(httpRequest.getParameter("loginState"))) {
            httpRequest.setAttribute("loginState", "failed");
        }
    }

    private User getLoggedInUser(final HttpServletRequest httpRequest) {
        final HttpSession session = httpRequest.getSession(false);
        if(session != null) {
            return (User) session.getAttribute(KEY_USER);
        }
        return null;
    }

    private void logout(final HttpServletRequest httpRequest) {
        final HttpSession session = httpRequest.getSession(false);
        if(session != null) {
            session.invalidate();
        }
    }

    @Override
    public void destroy() {}
}
