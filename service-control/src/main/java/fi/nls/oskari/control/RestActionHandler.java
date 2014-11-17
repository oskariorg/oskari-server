package fi.nls.oskari.control;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * RestWrapper for ActionHandler. Concrete subclasses can be annotated with
 * @OskariActionRoute("handlerKey") to register them on runtime.
 */
public abstract class RestActionHandler extends ActionHandler {

    private Logger log = LogFactory.getLogger(RestActionHandler.class);

	/**
	 * Handler method for requests
	 * @param params
	 * @throws fi.nls.oskari.control.ActionException Exception is thrown if the action cannot be handled
	 */
    public void handleAction(ActionParameters params) throws ActionException {
    	preProcess(params);
        final HttpServletRequest req = params.getRequest();
        String method = req.getMethod();
        log.debug("Method:", method);
        final String overrideHeader = req.getHeader("X-HTTP-Method-Override");
        log.debug("Override header:", overrideHeader);
        if(overrideHeader != null) {
            method = overrideHeader;
        }

        if("GET".equals(method)) {
            handleGet(params);
        }
        else if("PUT".equals(method)) {
            handlePut(params);
        }
        else if("POST".equals(method)) {
            handlePost(params);
        }
        else if("DELETE".equals(method)) {
            handleDelete(params);
        }
    }

    public void handleGet(ActionParameters params) throws ActionException {
        throw new ActionDeniedException("Not implemented");
    }

    public void handlePut(ActionParameters params) throws ActionException {
        throw new ActionDeniedException("Not implemented");
    }

    public void handlePost(ActionParameters params) throws ActionException {
        throw new ActionDeniedException("Not implemented");
    }

    public void handleDelete(ActionParameters params) throws ActionException {
        throw new ActionDeniedException("Not implemented");
    }

    /**
     * Hook for common request processing f.ex. check if user has permission to do any of these
     */
    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
    }

    /**
     * Hook for setting up components that the handler needs to handle requests
     */
    public void init() {
        // setup services so we can handle requests
    }
    /**
     * Hook for tearing down/destroying components that the handler needed to handle requests
     */
    public void teardown() {
        // clean up as we are about to be destroyed
    }
}
