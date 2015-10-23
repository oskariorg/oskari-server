package fi.nls.oskari.service;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.*;

/**
 * Manages any OskariComponents registrated on the classpath.
 */
public class OskariComponentManager {

    private final static Logger log = LogFactory.getLogger(OskariComponentManager.class);
    private static final List<OskariComponent> components = new ArrayList<OskariComponent>();

    /**
     * Registers a OskariComponent with the given key after instantiating a class with the given className.
     * @param className
     */
    public static void addComponent(final String className) {
        try {
            final Class clazz = Class.forName(className);
            final OskariComponent handler = (OskariComponent) clazz.newInstance();
            addComponent(handler);
        } catch (Exception e) {
            log.error(e, "Error adding component with class:", className);
        }
    }
    /**
     * Registers a given OskariComponent with the given key.
     * @param handler
     */
    public static void addComponent(final OskariComponent handler) {
        try {
            handler.init();
            components.add(handler);
            log.debug("OskariComponent added:", handler.getClass().getCanonicalName());
        }
        catch (Exception ex) {
            log.error(ex, "OskariComponent init failed! Skipping", handler.getClass().getCanonicalName());
        }
    }

    /**
     * Registers all OskariComponents in the list
     * @param components
     */
    public static void addComponents(final List<OskariComponent> components) {
        for(OskariComponent c : components) {
            addComponent(c);
        }
    }

    /**
     * Uses ServiceLoader to find all OskariComponents in classpath.
     */
    public static void addDefaultComponents() {

        ServiceLoader<OskariComponent> impl = ServiceLoader.load(OskariComponent.class);
        for (OskariComponent loadedImpl : impl) {
            if ( loadedImpl != null ) {
                addComponent(loadedImpl);
            }
        }
    }
    public static <MOD extends OskariComponent> MOD getComponentOfType(final Class<MOD> clazz) {
        Map<String, MOD> map = getComponentsOfType(clazz);
        // just pick the first one
        // TODO: error handling (nullpointer) and possibly prioritize implementations
        return map.values().iterator().next();
    }

    /**
     * Returns a subset of the registered OskariComponents matching the given class.
     * @param clazz A OskariComponent subclass we are interested in
     * @return unMODifyable map of components mathing the given type
     */
    public static <MOD extends OskariComponent> Map<String, MOD> getComponentsOfType(final Class clazz) {
        if(components.isEmpty()) {
            addDefaultComponents();
        }
        final HashMap<String, MOD> mods = new HashMap<String, MOD>();
        for(OskariComponent comp : components) {
            if(clazz.isInstance(comp)) {
                mods.put(comp.getName(), (MOD)comp);
            }
        }
        return Collections.unmodifiableMap(mods);
    }

    public static void removeComponentsOfType(final Class clazz) {
        final Map<String, OskariComponent> comps = getComponentsOfType(clazz);
        for(OskariComponent c: comps.values()) {
            components.remove(c);
        }
    }

    /**
     * Cleanup method. Calls teardown on all registered components.
     */
    public static void teardown() {
        for( OskariComponent comp : components) {
            try {
                comp.teardown();
            }
            catch (Exception ex) {
                log.error(ex, "OskariComponent teardown failed! Skipping", comp.getClass().getCanonicalName());
            }
        }
    }
}
