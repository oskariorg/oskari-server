package fi.nls.oskari;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.oskari.control.view.modifier.param.ParamControl;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.spring.EnvHelper;
import fi.nls.oskari.spring.extension.OskariParam;
import fi.nls.oskari.util.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fi.nls.oskari.control.ActionConstants.*;

/**
 * Handles the map UI based on requested application and user role default applications
 */
@Controller
public class MapController {

    private final static Logger log = LogFactory.getLogger(MapController.class);

    private final static String PROPERTY_DEVELOPMENT = "development";
    private final static String PROPERTY_VERSION = "oskari.client.version";
    private final static String KEY_PRELOADED = "preloaded";
    private final static String KEY_PATH = "path";

    private final static String KEY_AJAX_URL = "ajaxUrl";
    private final static String KEY_CONTROL_PARAMS = "controlParams";

    private final static String KEY_RESPONSE_HEADER_PREFIX = "oskari.page.header.";

    private final ViewService viewService = new ViewServiceIbatisImpl();
    private boolean isDevelopmentMode = false;
    private String version = null;
    private final Set<String> paramHandlers = new HashSet<String>();

    @Autowired
    private EnvHelper env;

    public MapController() {
        // check if we have development flag -> serve non-minified js
        isDevelopmentMode = ConversionHelper.getBoolean(PropertyUtil.get(PROPERTY_DEVELOPMENT), false);
        // Get version from properties
        version = PropertyUtil.get(PROPERTY_VERSION);
    }

    @RequestMapping("/")
    public String getMap(Model model, @OskariParam ActionParameters params) {
        if(paramHandlers.isEmpty()) {
            // check control params to pass for getappsetup
            // setup on first call to allow more flexibility regarding timing issues
            paramHandlers.addAll(ParamControl.getHandlerKeys());
            log.debug("Checking for params", paramHandlers);
        }
        // for debugging - Note! changes the setting for the whole instance!!! Use with care
        if(params.getUser().isAdmin() && params.getHttpParam("reallyseriouslyyes", false)) {
            isDevelopmentMode = params.getHttpParam("dev", isDevelopmentMode);
        }

        writeCustomHeaders(params.getResponse());
        model.addAttribute("preloaded", !isDevelopmentMode);

        if (isDevelopmentMode) {
            model.addAttribute("oskariApplication", PropertyUtil.get("oskari.development.prefix"));
        } else {
            model.addAttribute("oskariApplication", PropertyUtil.get("oskari.client.version") +
                    PropertyUtil.get("oskari.application"));
        }

        // JSP
        final String viewJSP = setupRenderParameters(params, model);
        if(viewJSP == null) {
            // view not found
            log.debug("View not found, going to error/404");
            return "error/404";
        }

        setupLoginDetails(params, model);

        log.debug("Forward to", viewJSP);

        return viewJSP;
    }

    private void writeCustomHeaders(HttpServletResponse response) {

        final List<String> propertyList = PropertyUtil.getPropertyNamesStartingWith(KEY_RESPONSE_HEADER_PREFIX);
        final int prefixLength = KEY_RESPONSE_HEADER_PREFIX.length();
        for (String key : propertyList) {
            final String value = PropertyUtil.get(key, "");
            final String headerName = key.substring(prefixLength);
            if(!value.isEmpty()) {
                log.debug("Adding header", headerName, "=", value);
                response.addHeader(headerName, value);
            }
        }
    }

    private void setupLoginDetails(final ActionParameters params, Model model) {
        if(env.isHandleLoginForm()) {
            if(!params.getUser().isGuest()) {
                model.addAttribute("_logout_uri", env.getLogoutUrl());
            }
            else {
                // move possible "failed" parameter to attribute as JSP checks attribute
                if ("failed".equals(params.getHttpParam("loginState"))) {
                    model.addAttribute("loginState", "failed");
                }
                // if we are handling login -> setup attributes for login url/fieldnames
                if(env.isSAMLEnabled()) {
                    model.addAttribute("_login_uri_saml", env.getLoginUrlSAML());
                }
                if(env.isDBLoginEnabled()) {
                    model.addAttribute("_login_uri", env.getLoginUrl());
                    model.addAttribute("_login_field_user", env.getParam_username());
                    model.addAttribute("_login_field_pass", env.getParam_password());
                }
            }
        }
    }

    /**
     * Sets up request attributes expected by JSP to link correct Oskari application based on view
     * and construct configuration elements for GetAppSetup action route.
     * @param params
     * @return path for forwarding to correct JSP (based on the view used)
     * @throws javax.servlet.ServletException
     */
    private String setupRenderParameters(final ActionParameters params, Model model) {

        log.debug("getting a view and setting Render parameters");
        HttpServletRequest request = params.getRequest();

        final String uuId = params.getHttpParam(PARAM_UUID);
        long viewId = params.getHttpParam(PARAM_VIEW_ID, -1);
        boolean useDefault = viewId == -1;
        if(uuId == null && useDefault) {
            // get personalized or system default view
            if(params.getHttpParam(PARAM_RESET, false)) {
                viewId = viewService.getSystemDefaultViewId(params.getUser().getRoles());
            } else {
                viewId = viewService.getDefaultViewId(params.getUser());
            }
        }

        log.debug("Loading view with id:", viewId);

        final View view = getView(uuId, viewId, useDefault);
        if (view == null) {
            log.debug("no such view");
            ResponseHelper.writeError(params, "No such view (id:" + viewId + ")");
            return null;
        }

        // Checking referer for published domain
        if (view.getType().equals(ViewTypes.PUBLISHED)) {
            final String referer = RequestHelper.getDomainFromReferer(params.getHttpHeader(IOHelper.HEADER_REFERER));
            final String pubDomain = view.getPubDomain();
            if (ViewHelper.isRefererDomain(referer, pubDomain)) {
                log.info("Granted access to published view in domain:",pubDomain, "for referer", referer);
            } else {
                log.debug("Referer: ", params.getHttpHeader(IOHelper.HEADER_REFERER), " -> ", referer);
                log.warn("Denied access to published view in domain:", pubDomain, "for referer", referer);
                ResponseHelper.writeError(params, "Denied access to published view for domain: " + referer);
                return null;
            }
        }

        log.debug("Serving view with id:", view.getId());
        log.debug("View:", view.getDevelopmentPath(), "/", view.getApplication(), "/", view.getPage());
        model.addAttribute("viewId", view.getId());

        // viewJSP might change if using dev override
        String viewJSP = view.getPage();
        log.debug("Using JSP:", viewJSP, "with view:", view);

        // construct control params
        final JSONObject controlParams = getControlParams(params);

        if(uuId != null){
            JSONHelper.putValue(controlParams, PARAM_UUID, view.getUuid());
        }else{
            JSONHelper.putValue(controlParams, PARAM_VIEW_ID, view.getId());
        }


        JSONHelper.putValue(controlParams, PARAM_SECURE, request.getParameter(PARAM_SECURE));
        JSONHelper.putValue(controlParams, GetAppSetupHandler.PARAM_NO_SAVED_STATE, request.getParameter(GetAppSetupHandler.PARAM_NO_SAVED_STATE));
        model.addAttribute(KEY_CONTROL_PARAMS, controlParams.toString());

        model.addAttribute(KEY_PRELOADED, !isDevelopmentMode);
        if (isDevelopmentMode) {
            model.addAttribute(KEY_PATH, view.getDevelopmentPath() + "/" + view.getApplication());
        } else {
            model.addAttribute(KEY_PATH, "/" + version + "/" + view.getApplication());
        }
        model.addAttribute("application", view.getApplication());
        model.addAttribute("viewName", view.getName());
        model.addAttribute("language", params.getLocale().getLanguage());

        model.addAttribute(KEY_AJAX_URL,
                PropertyUtil.get(params.getLocale(), GetAppSetupHandler.PROPERTY_AJAXURL));
        model.addAttribute("urlPrefix", "");

        // in dev-mode app/page can be overridden
        if (isDevelopmentMode) {
            // check if we want to override the page & app
            final String app = params.getHttpParam("app");
            final String page = params.getHttpParam("page");
            if (page != null && app != null) {
                log.debug("Using dev-override!!! \nUsing JSP:", page, "with application:", app);
                model.addAttribute(KEY_PATH, app);
                model.addAttribute("application", app);
                viewJSP = page;
            }
        }

        // return jsp for the requested view
        return viewJSP;
    }

    private View getView(String uuId, long viewId, boolean isDefault){
        if(uuId != null){
            log.debug("Using Uuid to fetch a view:", uuId);
            return viewService.getViewWithConfByUuId(uuId);
        }

        log.debug("Using id to fetch a view:", viewId);
        View view = viewService.getViewWithConf(viewId);
        if(!isDefault && view.isOnlyForUuId()) {
            log.warn("View can only be loaded by uuid. ViewId:", viewId);
            return null;
        }
        return view;
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

    @RequestMapping("/version")
    public @ResponseBody String index() {
        return version;
    }
}
