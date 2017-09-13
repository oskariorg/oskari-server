package fi.nls.oskari.control;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Router for Ajax Requests made by the Oskari Map Framework.
 */
public class ActionControl {
    /**
     * Name for the Http parameter that speficies the route key
     */
    public static final String PARAM_ROUTE = "action_route";
    
    private static final Logger LOG = LogFactory.getLogger(ActionControl.class);
	private static final ConcurrentMap<String, ActionHandler> actions = new ConcurrentHashMap<String, ActionHandler>();
    private static final String METRICS_PREFIX = "Oskari.ActionControl";
    static final String PROPERTY_BLACKLIST = "actioncontrol.blacklist";
    static final String PROPERTY_WHITELIST = "actioncontrol.whitelist";

    private static Set<String> BLACKLISTED_ACTIONS = null;
    private static Set<String> WHITELISTED_ACTIONS = null;

    private static final boolean GATHER_METRICS = PropertyUtil.getOptional("actioncontrol.metrics", true);

    private static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    public static MetricRegistry getMetrics() {
        return METRIC_REGISTRY;
    }

    /**
     * Adds an action route handler with given route key
     * @param action route key
     * @param handlerClassName name of the class extending #ActionHandler that will be used to handle the route
     */
    public static void addAction(final String action, final String handlerClassName) {
        try {
            final Class clazz = Class.forName(handlerClassName);
            final ActionHandler handler = (ActionHandler) clazz.newInstance();
            addAction(action, handler);
        } catch (Exception e) {
            LOG.error(e, "Error adding handler for action:", action, " - class:", handlerClassName);
        }
    }
    /**
     * Adds an action route handler with given route key
     * @param action route key
     * @param handler handler for the route
     */
    public static void addAction(final String action, final ActionHandler handler) {
        addAction(action, handler, false);
    }
    /**
     * Adds an action route handler with given route key. Checks for blacklisted action keys. Third parameter
     * (skipBlacklistCheck) can be used to ignore blacklisting. Usable when blacklisting a default actionhandler and
     * replacing it with a custom implementation.
     * @param action route key
     * @param handler handler for the route
     * @param skipAllowedCheck true to ignore checking against blacklisted action keys. False to conform to blacklist checks
     */
    public static void addAction(final String action, final ActionHandler handler, boolean skipAllowedCheck) {
        if(!skipAllowedCheck && !isAllowedKey(action)) {
            LOG.debug("Action disabled by config - Skipping", action, "=", handler.getClass().getCanonicalName());
            return;
        }

        try {
            handler.init();
            actions.put(action, handler);
            LOG.debug("Action added", action, "=", handler.getClass().getCanonicalName());
        }
        catch (Exception ex) {
            LOG.error(ex, "Action init failed! Skipping", action, "=", handler.getClass().getCanonicalName());
        }
    }

    /**
     * Adds all ActionHandlers defined on the properties with the property key as route key
     * @param props
     */
	public static void addActions(final Properties props) {
	    for(Object key : props.keySet()) {
	        final String handlerClassName = (String) props.get(key);
	        addAction((String) key, handlerClassName);
	    }
	}

    /**
     * Uses ServiceLoader to find all ActionHandlers in classpath. Adds them as handlers with the route key
     * returned by getName() method.
     */
    public synchronized static void addDefaultControls() {

        ServiceLoader<ActionHandler> impl = ServiceLoader.load(ActionHandler.class);

        for (ActionHandler loadedImpl : impl) {
            if ( loadedImpl != null ) {
                addAction(loadedImpl.getName(), loadedImpl);
            }
        }
    }

    /**
     * Routes a request to a handler matching the route key
     * @param action route key
     * @param params parameters describing the request
     * @throws ActionException if route is not registered or something goes wrong while handling the request
     */
	public static void routeAction(final String action, final ActionParameters params) throws ActionException {
		if (actions.isEmpty()) {
		    addDefaultControls();
		}
        if (actions.containsKey(action)) {
            Timer.Context actionTimer = null;
            if(GATHER_METRICS) {
                final Meter actionMeter = getMetrics().meter(METRICS_PREFIX);
                actionMeter.mark();
                final com.codahale.metrics.Timer timer = METRIC_REGISTRY.timer(METRICS_PREFIX + "." + action);
                actionTimer = timer.time();
            }

            try {
                actions.get(action).handleAction(params);
            } catch (Exception ex) {
                if(ex instanceof ActionException) {
                    throw (ActionException) ex;
                }
                else {
                    ex.printStackTrace();
                    throw new ActionException("Unhandled exception occured", ex);
                }
            } finally {
                if(actionTimer != null) {
                    actionTimer.stop();
                }
            }
        } else {
            throw new ActionParamsException("ActionRoute not defined: " + action);
        }
	}

    /**
     * Convenient way to check if we have an implementation for the route registered
     * @param action
     * @return true if we have a handler registered for the route
     */
    public static boolean hasAction(final String action) {
        if(action == null) {
            return false;
        }
        if (actions.isEmpty()) {
            addDefaultControls();
        }
        return actions.containsKey(action);
    }

    /**
     * Cleanup method. Calls teardown on all registered handlers.
     */
    public static void teardown() {
        for( ActionHandler h : actions.values()) {
            try {
                h.teardown();
            }
            catch (Exception ex) {
                LOG.error(ex, "Action teardown failed! Skipping", h.getName(), "=", h.getClass().getCanonicalName());
            }
        }
        actions.clear();
        BLACKLISTED_ACTIONS = null;
        WHITELISTED_ACTIONS = null;
    }

    /**
     * Checks key against whitelist/blacklist and returns true if the key is allowed
     * @param key
     * @return
     */
    public static boolean isAllowedKey(String key) {
        if(BLACKLISTED_ACTIONS == null) {
            BLACKLISTED_ACTIONS = new HashSet<>(Arrays.asList(PropertyUtil.getCommaSeparatedList(PROPERTY_BLACKLIST)));
        }
        if(WHITELISTED_ACTIONS == null) {
            WHITELISTED_ACTIONS = new HashSet<>(Arrays.asList(PropertyUtil.getCommaSeparatedList(PROPERTY_WHITELIST)));
        }
        if(!WHITELISTED_ACTIONS.isEmpty()) {
            return WHITELISTED_ACTIONS.contains(key);
        }
        return !BLACKLISTED_ACTIONS.contains(key);
    }

}
