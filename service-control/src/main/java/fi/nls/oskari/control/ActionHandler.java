package fi.nls.oskari.control;

import fi.nls.oskari.annotation.OskariActionRoute;

/**
 * ActionHandler is a common interface for handling requests. Concrete subclasses can be annotated with
 * @OskariActionRoute("handlerKey") to register them on runtime.
 */
public abstract class ActionHandler {

    /**
     * Returns @OskariActionRoute annotation value if any or defaults to class name
     * @return key for the route
     */
    public String getName () {
        if(getClass().isAnnotationPresent(OskariActionRoute.class)) {
            OskariActionRoute r = getClass().getAnnotation(OskariActionRoute.class);
            if(!r.value().isEmpty()) {
                return r.value();
            }
        }
        return getClass().getSimpleName();
    }
	/**
	 * Handler method for requests
	 * @param params
	 * @throws ActionException Exception is thrown if the action cannot be handled
	 */
    public abstract void handleAction(ActionParameters params) throws ActionException;

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
