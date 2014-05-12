package fi.nls.oskari.map.servlet;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.PropertyUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ServletFilter for Oskari-map servlet. Handles httpRequest state before servlet processes it:
 * - locale (property oskari.request.handleLocale=[true, false] defaults to true)
 *      - sets the request.getLocale() to have only valid values for Oskari (oskari.locales property)
 *      - user language can be changed by adding parameter "lang=[2 letter language ISO code]"
 * - login form (property oskari.request.handleLoginForm=[true, false] defaults to true)
 *      - adds attributes to request which JSP will use to create login form
 * - principal (property oskari.request.handlePrincipal=[true, false] defaults to true)
 *      - sets request.getPrincipal() to have name based on configured login form
 *      - this should be disabled by setting the property to false if JAAS or other custom authentication is used
 *      - configurable by properties (defaults values): auth.login.url (j_security_check), auth.login.field.user (j_username), auth.login.field.pass (j_password)
 */
public class OskariRequestFilter implements Filter {
    private final static Logger log = LogFactory.getLogger(OskariRequestFilter.class);

    private boolean handleLoginForm = true;
    private boolean handlePrincipal = true;
    private boolean handleLocale = true;

    private String loginUrl = "j_security_check";
    private String param_username = "j_username";
    private String param_password = "j_password";

    private WebLocaleResolver localeResolver;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        handlePrincipal = PropertyUtil.getOptional("oskari.request.handlePrincipal", handlePrincipal);
        handleLocale = PropertyUtil.getOptional("oskari.request.handleLocale", handleLocale);
        if(handleLocale) {
            localeResolver = new WebLocaleResolver();
        }

        handleLoginForm = PropertyUtil.getOptional("oskari.request.handleLoginForm", handleLoginForm);
        loginUrl = PropertyUtil.get("auth.login.url", loginUrl);
        param_username = PropertyUtil.get("auth.login.field.user", param_username);
        param_password = PropertyUtil.get("auth.login.field.pass", param_password);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final OskariRequest req = new OskariRequest((HttpServletRequest) request);
        // setup character encoding
        req.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if(handleLocale) {
            handleLocale(req, (HttpServletResponse) response);
        }
        if(handleLoginForm) {
            // if we are handling login form -> setup attributes for login url/fieldnames
            handleLoginForm(req);
        }
        if(handlePrincipal) {
            final String servletPath = req.getServletPath();
            if(servletPath != null && servletPath.indexOf(loginUrl)  != -1) {
                log.debug("Called login url:", loginUrl);
                handlePrincipal(req);
            }
        }

        // passing OskariRequest as req to chain
        chain.doFilter(request, response);
    }

    /**
     * Override to setup request for custom login form.
     * Can be disabled with property oskari.request.handleLoginForm=[true, false] defaults to true
     * @param req
     */
    public void handleLoginForm(OskariRequest req) {
        log.debug("Handling login form with url:", loginUrl);
        req.setAttribute("_login_uri", loginUrl);
        req.setAttribute("_login_field_user", param_username);
        req.setAttribute("_login_field_pass", param_password);
    }

    /**
     * Override to setup custom locale handling for request
     * Can be disabled with property oskari.request.handleLocale=[true, false] defaults to true
     * @param req
     */
    public void handleLocale(OskariRequest req, HttpServletResponse resp) {
        req.setLocale(localeResolver.resolveLocale(req, resp));
        log.debug("Locale set as:", req.getLocale());
        req.setAttribute("oskari_supported_languages", PropertyUtil.getSupportedLanguages());
    }

    /**
     * Override to setup custom principal handling for request.
     * Can be disabled with property oskari.request.handlePrincipal=[true, false] defaults to true
     * @param req
     */
    public void handlePrincipal(final OskariRequest req) {
        final String username = req.getParameter(param_username);
        log.debug("Handling Principal for user:", username);
        final String pass = req.getParameter(param_password);
        User usr = null;
        try {
            usr = UserService.getInstance().login(username, pass);
        } catch (Exception ex) {
            log.error(ex, "Error logging in user:", username);
        }
        if(usr != null && !usr.isGuest()) {
            req.setUserPrincipal(usr.getScreenname());
        }
        else {
            req.setAttribute("loginState", "failed");
        }
    }

    @Override
    public void destroy() {}
}
