package fi.nls.oskari.control.view.modifier.param;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ViewModifierManager;

/**
 * Manages any ParamHandlers registrated on the classpath.
 */
public class ParamControl {
    
    private final static Logger log = LogFactory.getLogger(ParamControl.class);
	private static final HashMap<String, ParamHandler> actions = new HashMap<String, ParamHandler>();

    /**
     * Registers a ParamHandler with the given key after instantiating a class with the given className.
     * @param action
     * @param handlerClassName
     */
    public static void addHandler(final String action, final String handlerClassName) {
        try {
            final Class clazz = Class.forName(handlerClassName);
            final ParamHandler handler = (ParamHandler) clazz.newInstance();
            addHandler(action, handler);
        } catch (Exception e) {
            log.error(e, "Error adding modifier for param:", action, " - modifier class:", handlerClassName);
        }
    }
    /**
     * Registers a given ParamHandler with the given key.
     * @param action
     * @param handler
     */
    public static void addHandler(final String action, final ParamHandler handler) {
        actions.put(action, handler);
        log.debug("Paramhandler added", action,"=", handler.getClass().getCanonicalName());
    }

    /**
     * Adds all ParamHandler defined on the properties with the property key as modifier key
     * @param props
     */
	public static void addHandlers(final Properties props) {
	    for(Object key : props.keySet()) {
	        final String handlerClassName = (String) props.get(key);
	        addHandler((String) key, handlerClassName);
	    }
	}

    /**
     * Returns a set of registered http parameter names to be handled
     * @return
     */
	public static Set<String> getHandlerKeys() {
	    return actions.keySet();
	}

    /**
     * Uses ViewModifierManager to get all instances of ParamHandler and registers them as parameter handlers.
     * @see ViewModifierManager
     */
    public static void addDefaultControls() {
        final Map<String, ParamHandler> handlers = ViewModifierManager.getModifiersOfType(ParamHandler.class);
        actions.putAll(handlers);
    }

    /**
     * Routes ViewModifiers of type ParamHandler to process the view based on the parameter they were registered to handle.
     * @param paramKey
     * @param params
     * @return
     * @throws ModifierException
     */
	public static boolean handleParam(final String paramKey, final ModifierParams params) throws ModifierException {
		if (actions.isEmpty()) {
		    addDefaultControls();
		}
        if (actions.containsKey(paramKey)) {
            return actions.get(paramKey).handleParam(params);
        }
        return false;
	}
}
