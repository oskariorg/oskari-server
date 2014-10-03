package fi.nls.oskari.map.servlet;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.oskari.control.view.modifier.param.ParamControl;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Example implementation for oskari-server endpoint.
 * Assumes PropertyUtil has been populated in ContextInitializer!!
 * Assumes that locale and logged in user has been set with ServletFilters.
 *
 * @see OskariContextInitializer
 * @see OskariRequestFilter
 * @see PrincipalAuthenticationFilter
 */
public class MapFullServlet extends HttpServlet {

    private static final String KEY_REDIS_HOSTNAME = "redis.hostname";
    private static final String KEY_REDIS_PORT = "redis.port";
    private static final String KEY_REDIS_POOL_SIZE = "redis.pool.size";

    private final static String PROPERTY_DEVELOPMENT = "development";
    private final static String PROPERTY_VERSION = "oskari.client.version";
    private final static String KEY_PRELOADED = "preloaded";
    private final static String KEY_PATH = "path";
    private final static String KEY_VERSION = "version";

    private final static String KEY_AJAX_URL = "ajaxUrl";
    private final static String KEY_CONTROL_PARAMS = "controlParams";

    private final ViewService viewService = new ViewServiceIbatisImpl();
    private boolean isDevelopmentMode = false;
    private String version = null;
    private final Set<String> paramHandlers = new HashSet<String>();

    private static final long serialVersionUID = 1L;

    private final static Logger log = LogFactory.getLogger(MapFullServlet.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public MapFullServlet() {

    }

    @Override
    public void init() {

        // init jedis
        JedisManager.connect(ConversionHelper.getInt(PropertyUtil
                .get(KEY_REDIS_POOL_SIZE), 30), PropertyUtil
                .get(KEY_REDIS_HOSTNAME, "localhost"), ConversionHelper.getInt(PropertyUtil
                .get(KEY_REDIS_PORT), 6379));

        // Action route initialization
        ActionControl.addDefaultControls();
        // check control params to pass for getappsetup
        paramHandlers.addAll(ParamControl.getHandlerKeys());
        log.debug("Checking for params", paramHandlers);

        // check if we have development flag -> serve non-minified js
        isDevelopmentMode = "true".equals(PropertyUtil.get(PROPERTY_DEVELOPMENT));
        // Get version from init params or properties, prefer version from properties and default to init param
        version = PropertyUtil.get(PROPERTY_VERSION, getServletConfig().getInitParameter(KEY_VERSION));
    }

    /**
     * Handles ajax requests if request has parameter "action_route" or
     * renders a map view (also handles login/logout if "action" parameter
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException {
        final ActionParameters params = this.getActionParameters(request,
                response);

        if (request.getParameter("action_route") != null) {
            // calling an ajax route
            handleActionRoute(params);
        } else {
            // JSP
            try {
                final String viewJSP = setupRenderParameters(params);
                if(viewJSP == null) {
                    // view not found
                    return;
                }
                log.debug("Forward to", viewJSP);
                request.getRequestDispatcher(viewJSP).forward(request, response);
            }
            catch (IOException ignored) {}
        }
    }

    /**
     * Handles action routes mapping through ActionControl.routeAction and handles errors for routes.
     * @param params
     */
    private void handleActionRoute(final ActionParameters params) {

        final String route = params.getHttpParam("action_route");
        if(!ActionControl.hasAction(route)) {
            ResponseHelper.writeError(params, "No such route registered: " + route, HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }
        try {
            ActionControl.routeAction(route, params);
            // TODO:  HANDLE THE EXCEPTION, LOG USER AGENT ETC. on exceptions
        } catch (ActionParamsException e) {
            // For cases where we dont want a stack trace
            log.error("Couldn't handle action:", route, ". Message: ", e.getMessage(), ". Parameters: ", params.getRequest().getParameterMap());
            ResponseHelper.writeError(params, e.getMessage(), HttpServletResponse.SC_NOT_IMPLEMENTED, e.getOptions());
        } catch (ActionDeniedException e) {
            // User tried to execute action he/she is not authorized to execute or session had expired
            if(params.getUser().isGuest()) {
                log.error("Session expired - Action was denied:", route, ". Parameters: ", params.getRequest().getParameterMap());
            }
            else {
                log.error("Action was denied:", route, ", Error msg:", e.getMessage(), ". User: ", params.getUser(), ". Parameters: ", params.getRequest().getParameterMap());
            }
            ResponseHelper.writeError(params, e.getMessage(), HttpServletResponse.SC_FORBIDDEN, e.getOptions());
        } catch (ActionException e) {
            // Internal failure -> print stack trace
            log.error(e, "Couldn't handle action:", route, ". Parameters: ", params.getRequest().getParameterMap());
            ResponseHelper.writeError(params, e.getMessage());
        }
    }

    /**
     * Sets up request attributes expected by JSP to link correct Oskari application based on view
     * and construct configuration elements for GetAppSetup action route.
     * @param params
     * @return path for forwarding to correct JSP (based on the view used)
     * @throws ServletException
     */
    private String setupRenderParameters(final ActionParameters params) throws ServletException {

        try {
            HttpServletRequest request = params.getRequest();
            
            final long viewId = ConversionHelper.getLong(params.getHttpParam("viewId"),
                    viewService.getDefaultViewId(params.getUser()));
            
            final String uuid = ConversionHelper.getString(params.getHttpParam("uuId"), null);

            final View view = getView(uuid, viewId);
            if (view == null) {
                ResponseHelper.writeError(params, "No such view (id:" + viewId + ")");
                return null;
            }
            log.debug("Serving view with id:", view.getId());
            log.debug("View:", view.getDevelopmentPath(), "/", view.getApplication(), "/", view.getPage());
            request.setAttribute("viewId", view.getId());

            // viewJSP might change if using dev override
            String viewJSP = view.getPage();
            log.debug("Using JSP:", viewJSP, "with view:", view);

            // construct control params
            final JSONObject controlParams = getControlParams(params);
            
            JSONHelper.putValue(controlParams, "viewId", view.getId());
            JSONHelper.putValue(controlParams, "ssl", request.getParameter("ssl"));
            request.setAttribute(KEY_CONTROL_PARAMS, controlParams.toString());

            request.setAttribute(KEY_PRELOADED, !isDevelopmentMode);
            if (isDevelopmentMode) {
                request.setAttribute(KEY_PATH, view.getDevelopmentPath() + "/" + view.getApplication());
            } else {
                request.setAttribute(KEY_PATH, "/" + version + "/" + view.getApplication());
            }
            request.setAttribute("application", view.getApplication());
            request.setAttribute("viewName", view.getName());
            request.setAttribute("language", params.getLocale().getLanguage());

            request.setAttribute(KEY_AJAX_URL,
                    PropertyUtil.get(params.getLocale(), GetAppSetupHandler.PROPERTY_AJAXURL));
            request.setAttribute("urlPrefix", "");

            // in dev-mode app/page can be overridden
            if (isDevelopmentMode) {
                // check if we want to override the page & app
                final String app = params.getHttpParam("app");
                final String page = params.getHttpParam("page");
                if (page != null && app != null) {
                    log.debug("Using dev-override!!! \nUsing JSP:", page, "with application:", app);
                    request.setAttribute(KEY_PATH, app);
                    request.setAttribute("application", app);
                    viewJSP = page;
                }
            }

            // return jsp for the requested view
            return "/" + viewJSP + ".jsp";
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
    
    
    private View getView(String uuId, long viewId){
    	if(uuId != null){
    		return viewService.getViewWithConfByUuId(uuId);
    	}else{
    		return viewService.getViewWithConf(viewId);
    	}
    }

    /**
     * Checks all viewmodifiers registered in the system that are handling parameters
     * and constructs a controlParams JSON to be passed on to GetAppSetup action route.
     * @param params
     * @return
     */
    private JSONObject getControlParams(final ActionParameters params) {
        final JSONObject p = new JSONObject();
        for (String key : paramHandlers) {
            JSONHelper.putValue(p, key, params.getHttpParam(key, null));
        }
        return p;
    }

    /**
     * Passes requests to doGet().
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException {
        doGet(request, response);
    }

    /**
     * Passes requests to doGet().
     */
    protected void doPut(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException {
        doGet(request, response);
    }

    /**
     * Passes requests to doGet().
     */
    protected void doDelete(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException {
        doGet(request, response);
    }

    /**
     * Wraps request to ActionParameters object that is used by action routes.
     * Populates user information etc to the request.
     * @param request
     * @param response
     * @return
     */
    private ActionParameters getActionParameters(
            final HttpServletRequest request, final HttpServletResponse response) {

        final ActionParameters params = new ActionParameters();
        params.setRequest(request);
        params.setResponse(response);

        // Request locale setup in OskariRequestFilter
        params.setLocale(request.getLocale());
        // User setup in PrincipalAuthenticationFilter
        HttpSession session = request.getSession();
        params.setUser((User) session.getAttribute(User.class.getName()));
        return params;
    }

    @Override
    public void destroy() {
        ActionControl.teardown();
        super.destroy();
    }
}
