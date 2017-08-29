package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.view.modifier.ViewModifierManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages any ParamHandlers registrated on the classpath.
 */
public class ParamControl {
    
    private final static Logger log = LogFactory.getLogger(ParamControl.class);
	private static final ConcurrentMap<String, ParamHandler> actions = new ConcurrentHashMap<String, ParamHandler>();
    private static final ConcurrentMap<String, List<ParamHandler>> preprocessors = new ConcurrentHashMap<String, List<ParamHandler>>();

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
     * Whenever a handler is added, checks a property with the key "view.modifier.param." + action + ".prepocessors"
     * for a comma-separated list of classnames to use as preprocessors.
     * @param action
     * @param handler
     */
    public static void addHandler(final String action, final ParamHandler handler) {
        actions.put(action, handler);
        log.debug("Paramhandler added", action,"=", handler.getClass().getCanonicalName());
        final String[] preprocHandlers = PropertyUtil.getCommaSeparatedList("view.modifier.param." + action + ".prepocessors");
        final List<ParamHandler> list = new ArrayList<ParamHandler>(preprocHandlers.length);
        for(String prehandler: preprocHandlers) {
            try {
                final Class clazz = Class.forName(prehandler);
                final ParamHandler preprocessor = (ParamHandler) clazz.newInstance();
                log.debug("Preprocessor added for", action,"=", clazz.getCanonicalName());
                list.add(preprocessor);
            } catch (Exception e) {
                log.warn(e, "Error instantiating preprocessor for param:", action, " preprocessor:", prehandler);
            }
        }
        preprocessors.put(action, list);
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
        if (actions.isEmpty()) {
            addDefaultControls();
        }
        // sort keys by priority
        Object[] handlers = actions.values().toArray();
        Arrays.sort(handlers);
        Set<String> keys = new LinkedHashSet<>();
        for(Object h : handlers) {
            keys.add(((ParamHandler) h).getName());
        }
	    return keys;
	}

    /**
     * Uses ViewModifierManager to get all instances of ParamHandler and registers them as parameter handlers.
     * @see ViewModifierManager
     */
    public synchronized static void addDefaultControls() {
        if (!actions.isEmpty()) {
            return;
        }
        final Map<String, ParamHandler> handlers = ViewModifierManager.getModifiersOfType(ParamHandler.class);
        for(String key : handlers.keySet()) {
            addHandler(key, handlers.get(key));
        }
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
            // loop through preprocessors first, they might change the param value f.ex.
            // addHandler should always add a preprocessor list for any action it adds
            // so this should be safe
            for(ParamHandler preprocessor : preprocessors.get(paramKey)) {
                preprocessor.handleParam(params);
            }
            return actions.get(paramKey).handleParam(params);
        }
        return false;
	}
}
