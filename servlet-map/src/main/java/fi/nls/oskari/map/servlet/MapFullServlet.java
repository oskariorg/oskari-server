package fi.nls.oskari.map.servlet;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.servlet.db.DBHandler;
import fi.nls.oskari.permission.UserService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

public class MapFullServlet extends HttpServlet {

    private final static Locale LOCALE_FINNISH = new Locale("fi", "FI");
    private final static Locale LOCALE_SWEDISH = new Locale("sv", "SE");
    private final static Locale LOCALE_ENGLISH = new Locale("en", "US");

    private final static int GUEST_ROLE = 10110;

    private static final String KEY_REDIS_HOSTNAME = "redis.hostname";
    private static final String KEY_REDIS_PORT = "redis.port";
    private static final String KEY_REDIS_POOL_SIZE = "redis.pool.size";

    private static final long serialVersionUID = 1L;

    private final static Logger log = LogFactory.getLogger(MapFullServlet.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public MapFullServlet() {

    }

    @Override
    public void init() {

        DBHandler.createContentIfNotCreated();

        try {
            Properties prop = new Properties();
            InputStream in = MapFullServlet.class
                    .getResourceAsStream("oskari.properties");
            prop.load(in);
            in.close();
            PropertyUtil.addProperties(prop); // TODO: kts. actioncontrols
        } catch (DuplicateException e) {
            log.error(e, "Found duplicate propertykeys on init!");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // init jedis
        JedisManager.connect(ConversionHelper.getInt(PropertyUtil
                .get(KEY_REDIS_POOL_SIZE), 30), PropertyUtil
                .get(KEY_REDIS_HOSTNAME), ConversionHelper.getInt(PropertyUtil
                .get(KEY_REDIS_PORT), 6379));

        // Action route initialization:
        ActionControl.addDefaultControls();

        // ajaxUrl =
        // "/web/fi/oskari?p_p_id=OskariMap_WAR_oskarimapportlet&p_p_lifecycle=2&";
        // from properties

        try {
            PropertyUtil.addProperty(GetAppSetupHandler.PROPERTY_AJAXURL,
                    "/ajax/?", LOCALE_FINNISH);

            PropertyUtil.addProperty(GetAppSetupHandler.PROPERTY_AJAXURL,
                    "/ajax/?", LOCALE_SWEDISH);

            PropertyUtil.addProperty(GetAppSetupHandler.PROPERTY_AJAXURL,
                    "/ajax/?", LOCALE_ENGLISH);
        } catch (DuplicateException ignored) {
            // should never happen
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        final ActionParameters params = this.getActionParameters(request,
                response);

        if (request.getParameter("action_route") != null) {

            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            if (user == null) {
                user = new GuestUser();
                // user.setId(GUEST_ROLE);
                user.addRole(GUEST_ROLE, "Guest");

            }
            params.setUser(user);

            final String route = params.getRequest().getParameter("action_route");
            try {
                ActionControl.routeAction(route, params);
                // TODO:  HANDLE THE EXCEPTION, LOG USER AGENT ETC. on exceptions
            } catch (ActionParamsException e) {
                // For cases where we dont want a stack trace
                log.error("Couldn't handle action:", route, ". Message: ", e.getMessage(), ". Parameters: ", params.getRequest().getParameterMap());
                ResponseHelper.writeError(params, e.getMessage(), HttpServletResponse.SC_NOT_IMPLEMENTED, e.getOptions());
            } catch (ActionDeniedException e) {
                // User tried to execute action he/she is not authorized to execute
                log.error(e, "Action was denied:", route, ". User: ", params.getUser(), ". Parameters: ", params.getRequest().getParameterMap());
                ResponseHelper.writeError(params, e.getMessage(), HttpServletResponse.SC_FORBIDDEN, e.getOptions());
            } catch (ActionException e) {
                // Internal failure -> print stack trace
                log.error(e, "Couldn't handle action:", route, ". Parameters: ", params.getRequest().getParameterMap());
                ResponseHelper.writeError(params, e.getMessage());
            }
        } else if (request.getParameter("action") != null) {
            final String route = params.getRequest().getParameter("action");
            if (route.equals("login"))
                try {
                    handleLogin(request, params);
                } catch (ServiceException e) {
                    log.error("Couldn't handle login: ", e);
                }
            else if (route.equals("logout"))
                handleLogout(request, params);
            else {
                log.error("Unknown action:", params.getRequest()
                        .getParameterMap());
                throw new RuntimeException("Unknown action");
            }
        } else {
            log.error("Unknown ajax request for map:", params.getRequest()
                    .getParameterMap());
            throw new RuntimeException("Unknown ajax request for map");
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        doGet(request, response);
    }

    public ActionParameters getActionParameters(
            final HttpServletRequest request, final HttpServletResponse response) {

        final ActionParameters params = new ActionParameters();
        params.setRequest(request);
        params.setResponse(response);
        params.setLocale(LOCALE_FINNISH);
        GuestUser guest = new GuestUser();
        guest.addRole(GUEST_ROLE, "Guest");
        params.setUser(guest);

        return params;
    }

    public void handleLogin(HttpServletRequest request, ActionParameters params)
            throws ServiceException {

        final String username = params.getHttpParam("username", "");
        final String password = params.getHttpParam("password", "");
        try {
            UserService service = UserService.getInstance(); // interface for
                                                             // this service
            User user = service.login(username, password);
            HttpSession session = request.getSession();
            
            if (user != null) {
                /*
                    user = new GuestUser();
                    // user.setId(GUEST_ROLE);
                    user.addRole(GUEST_ROLE, "Guest");
                    */

                session.removeAttribute("loginState");
                session.setAttribute("user", user);
            } else {
                session.setAttribute("loginState", "failed");
            }
            params.getResponse().sendRedirect("/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLogout(HttpServletRequest request, ActionParameters params) {
        try {
            HttpSession session = request.getSession();
            session.invalidate();
            params.getResponse().sendRedirect("/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
