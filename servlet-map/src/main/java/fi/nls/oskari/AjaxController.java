package fi.nls.oskari;

import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.extension.OskariParam;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.log.AuditLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * Handles ajax routing to Oskari Action handlers
 */
@Controller
public class AjaxController {

    private final static Logger log = LogFactory.getLogger(AjaxController.class);

    @RequestMapping("/action")
    @ResponseBody
    public void handleAction(@OskariParam ActionParameters params, @RequestParam("action_route") String route) {
        // ActionHandlers write the response internally.
        // ResponseBody with void return type is needed so Spring doesn't try to show a view/JSP.

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
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParams(params.getRequest().getParameterMap())
                    .withMsg(e.getMessage())
                    .usedInvalidParams(AuditLog.ResourceType.GENERIC);
            ResponseHelper.writeError(params, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST, e.getOptions());
        } catch (ActionDeniedException e) {
            // User tried to execute action he/she is not authorized to execute or session had expired
            if(params.getUser().isGuest()) {
                log.error("Action was denied:", route, ", Error msg:", e.getMessage(), ". Parameters: ", params.getRequest().getParameterMap());
            }
            else {
                log.error("Action was denied:", route, ", Error msg:", e.getMessage(), ". User: ", params.getUser(), ". Parameters: ", params.getRequest().getParameterMap());
            }
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParams(params.getRequest().getParameterMap())
                    .withMsg(e.getMessage())
                    .wasDenied(AuditLog.ResourceType.GENERIC);
            ResponseHelper.writeError(params, e.getMessage(), HttpServletResponse.SC_FORBIDDEN, e.getOptions());
        } catch (ActionCommonException e) {
            Throwable error = e;
            if (e.getCause() != null) {
                error = e.getCause();
            }
            if (log.isDebugEnabled()) {
                log.debug(error, "Couldn't handle action:", route, "Message: ", e.getMessage(), ". Parameters: ", params.getRequest().getParameterMap());
            } else {
                log.error("Couldn't handle action:", route, ". Message: ", e.getMessage(), ". Parameters: ", params.getRequest().getParameterMap());
            }
            // Not AuditLogging here since this is for common errors like map layer proxying
            ResponseHelper.writeError(params, e.getMessage());
        } catch (ActionException e) {
            // Internal failure -> print stack trace
            Throwable error = e;
            if(e.getCause() != null) {
                error = e.getCause();
            }
            log.error(error, "Couldn't handle action:", route, "Message: ", e.getMessage(), ". Parameters: ", params.getRequest().getParameterMap());
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParams(params.getRequest().getParameterMap())
                    .withMsg(error.getMessage())
                    .errored(AuditLog.ResourceType.GENERIC);
            ResponseHelper.writeError(params, e.getMessage());
        }
    }

}
