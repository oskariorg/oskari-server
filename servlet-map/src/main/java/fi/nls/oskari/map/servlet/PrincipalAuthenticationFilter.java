package fi.nls.oskari.map.servlet;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.user.IbatisRoleService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

public class PrincipalAuthenticationFilter implements Filter {
    private final static Logger log = LogFactory.getLogger(PrincipalAuthenticationFilter.class);
    private static final String KEY_USER = User.class.getName();
    private String logoutUrl = "logout";
    private String loggedOutPage = "/";
    private boolean addMissingUsers = true;
    private String roleMappingType = null;

    private IbatisRoleService roleService = null;
    private Map<String, Role> EXTERNAL_ROLES_MAPPING = null;
    private UserService userService = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logoutUrl = PropertyUtil.get("auth.logout.url", logoutUrl);
        loggedOutPage = PropertyUtil.get("auth.loggedout.page", PropertyUtil.get("oskari.map.url", loggedOutPage));

        // favor init params, fallback to property
        addMissingUsers = ConversionHelper.getBoolean(filterConfig.getInitParameter("auth.add.missing.users"),
                PropertyUtil.getOptional("auth.add.missing.users", addMissingUsers));
        roleMappingType =ConversionHelper.getString(filterConfig.getInitParameter("auth.external.role.mapping"),
                PropertyUtil.getOptional("auth.external.role.mapping"));

        roleService = new IbatisRoleService();
        try {
            EXTERNAL_ROLES_MAPPING = roleService.getExternalRolesMapping(roleMappingType);
            userService = UserService.getInstance();
        } catch (Exception ex) {
            log.error(ex, "Error getting UserService. Is it configured?");
            addMissingUsers = false;
        }
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
            // NOTE! with dispatch we'll lose everything we've setup in request (login form url etc)
            // Ensure that OskariRequestFilter is run again if forwarding to map url
            final RequestDispatcher dispatch = httpRequest.getRequestDispatcher(loggedOutPage);
            dispatch.forward(request, response);
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
            } catch (Exception e) {
                log.error(e, "Session setup failed");
            }
        }

        chain.doFilter(request, response);
    }

    private void setupSession(final HttpServletRequest httpRequest, final User user) throws Exception {
        final Principal userPrincipal = httpRequest.getUserPrincipal();
        if(userPrincipal != null) {
            final HttpSession session = httpRequest.getSession(false);
            if(session == null || user == null || user.isGuest()) {
                log.debug("Getting user from service with principal name:", userPrincipal.getName());
                User loadedUser = userService.getUser(userPrincipal.getName());
                log.debug("Got user from service:", loadedUser);
                if(addMissingUsers && loadedUser == null) {
                    loadedUser = addUser(httpRequest);
                }
                if(loadedUser != null) {
                    httpRequest.getSession(true).setAttribute(KEY_USER, loadedUser);
                    httpRequest.setAttribute("_logout_uri", logoutUrl);
                }
                else {
                    log.error("Login user check failed! Got user from principal, but can't find it in Oskari db:", userPrincipal.getName());
                }
            }
        }
        // our jaas fail url is by default /?loginState=failed
        // -> pass the failed login flag to request as attribute
        if ("failed".equals(httpRequest.getParameter("loginState"))) {
            httpRequest.setAttribute("loginState", "failed");
        }
    }

    public User addUser(final HttpServletRequest httpRequest) throws Exception {
        final User user = new User();
        user.setScreenname(httpRequest.getUserPrincipal().getName());
        user.setFirstname(null);
        user.setLastname(null);
        user.setUuid(userService.generateUuid(user.getScreenname()));
        for(String extRoleName : EXTERNAL_ROLES_MAPPING.keySet()) {
            if(httpRequest.isUserInRole(extRoleName)) {
                user.addRole(EXTERNAL_ROLES_MAPPING.get(extRoleName));
            }
        }
        return userService.createUser(user);
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
