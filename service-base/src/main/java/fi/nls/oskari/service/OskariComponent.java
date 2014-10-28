package fi.nls.oskari.service;

import fi.nls.oskari.annotation.Oskari;

/**
 * OskariComponent is a common base class for annotated Oskari components.
 * Concrete subclasses can be annotated with @Oskari("some value") to register them on runtime.
 */
public abstract class OskariComponent {

    /**
     * Returns @Oskari annotation value if any or defaults to class name
     * @return key for the route
     */
    public String getName () {
        if(getClass().isAnnotationPresent(Oskari.class)) {
            Oskari r = getClass().getAnnotation(Oskari.class);
            if(!r.value().isEmpty()) {
                return r.value();
            }
        }
        return getClass().getSimpleName();
    }
    /**
     * Hook for setting up requirements for component
     */
    public void init() {
        // setup services so we can handle requests
    }
    /**
     * Hook for tearing down/destroying requirements
     */
    public void teardown() {
        // clean up as we are about to be destroyed
    }
}
