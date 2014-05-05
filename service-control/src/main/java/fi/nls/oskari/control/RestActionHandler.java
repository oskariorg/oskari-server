package fi.nls.oskari.control;

import javax.servlet.http.HttpServletRequest;

/**
 * RestWrapper for ActionHandler. Concrete subclasses can be annotated with
 * @OskariActionRoute("handlerKey") to register them on runtime.
 */
public abstract class RestActionHandler extends ActionHandler {

	/**
	 * Handler method for requests
	 * @param params
	 * @throws fi.nls.oskari.control.ActionException Exception is thrown if the action cannot be handled
	 */
    public void handleAction(ActionParameters params) throws ActionException {
        preProcess(params);
        final HttpServletRequest req = params.getRequest();
        if("GET".equals(req.getMethod())) {
            handleGet(params);
        }
        else if("PUT".equals(req.getMethod())) {
            handlePut(params);
        }
        else if("POST".equals(req.getMethod())) {
            handlePost(params);
        }
        else if("DELETE".equals(req.getMethod())) {
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
