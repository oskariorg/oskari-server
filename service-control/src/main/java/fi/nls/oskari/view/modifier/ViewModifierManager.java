package fi.nls.oskari.view.modifier;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages any ViewModifiers registrated on the classpath.
 */
public class ViewModifierManager {

    private final static Logger log = LogFactory.getLogger(ViewModifierManager.class);
    private static final ConcurrentMap<String, ViewModifier> actions = new ConcurrentHashMap<String, ViewModifier>();

    /**
     * Registers a ViewModifier with the given key after instantiating a class with the given className.
     * @param action
     * @param handlerClassName
     */
    public static void addModifier(final String action, final String handlerClassName) {
        try {
            final Class clazz = Class.forName(handlerClassName);
            final ViewModifier handler = (ViewModifier) clazz.newInstance();
            addModifier(action, handler);
        } catch (Exception e) {
            log.error(e, "Error adding modifier for key:", action, " - modifier class:", handlerClassName);
        }
    }
    /**
     * Registers a given ViewModifier with the given key.
     * @param action
     * @param handler
     */
    public static void addModifier(final String action, final ViewModifier handler) {
        try {
            handler.init();
            actions.put(action, handler);
            log.debug("ViewModifier added", action,"=", handler.getClass().getCanonicalName());
        }
        catch (Exception ex) {
            log.error(ex, "ViewModifier init failed! Skipping", action,"=", handler.getClass().getCanonicalName());
        }
    }

    /**
     * Adds all ViewModifiers defined on the properties with the property key as modifier key
     * @param props
     */
    public static void addModifiers(final Properties props) {
        for(Object key : props.keySet()) {
            final String handlerClassName = (String) props.get(key);
            addModifier((String) key, handlerClassName);
        }
    }

    /**
     * Uses ServiceLoader to find all ViewModifiers in classpath. Adds them as modifiers with the key
     * returned by getName() method.
     */
    public static void addDefaultControls() {

        ServiceLoader<ViewModifier> impl = ServiceLoader.load(ViewModifier.class);

        for (ViewModifier loadedImpl : impl) {
            if ( loadedImpl != null ) {
                addModifier(loadedImpl.getName(), loadedImpl);
            }
        }
    }

    /**
     * Returns a subset of the registered ViewModifiers matching the given class.
     * @param clazz A ViewModifier subclass we are interested in
     * @return unmodifyable map of view modifiers mathing the given type
     */
    public static <Mod extends ViewModifier> Map<String, Mod> getModifiersOfType(final Class clazz) {
        if(actions.isEmpty()) {
            addDefaultControls();
        }
        final HashMap<String, Mod> mods = new HashMap<String, Mod>();
        for(String key : actions.keySet()) {
            final ViewModifier m = actions.get(key);
            if(clazz.isInstance(m)) {
                mods.put(key, (Mod)m);
            }
        }
        return Collections.unmodifiableMap(mods);
    }

    /**
     * Cleanup method. Calls teardown on all registered modifiers.
     */
    public static void teardown() {
        for( ViewModifier h : actions.values()) {
            try {
                h.teardown();
            }
            catch (Exception ex) {
                log.error(ex, "ViewModifier teardown failed! Skipping", h.getName(),"=", h.getClass().getCanonicalName());
            }
        }
    }
}
