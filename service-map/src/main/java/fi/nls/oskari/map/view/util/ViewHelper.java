package fi.nls.oskari.map.view.util;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ViewHelper {

    private static final Logger log = LogFactory.getLogger(ViewHelper.class);
    private static String[] UNRESTRICTED_USAGE_DOMAINS = PropertyUtil.getCommaSeparatedList("view.published.usage.unrestrictedDomains");

    private ViewHelper() {}

    public static JSONArray getStartupSequence(final View view) throws ViewException {
        final JSONArray startupSequence = new JSONArray();
        final List<Bundle> bundles = view.getBundles();
        log.debug("Got", bundles.size(), "states for view", view.getId());
        for (Bundle s : bundles) {
            final String startup = s.getStartup();
            final String name = s.getName();
            if (startup != null) {
                try {
                    startupSequence.put(new JSONObject(startup));
                } catch (JSONException jsonex) {
                    log.error(jsonex, "Malformed JSON in startup sequence fragment for bundle:", 
                            name, "- JSON:", startup);
                }
            } else {
                throw new ViewException(
                		"Could not get startup sequence fragment for bundle '" + name + "'");
            }
        }
        return startupSequence;
    }

    /**
     * Checks if it's ok to continue loading requested map based on referer/views pubdomain.
     * @param referer from headers
     * @param pubdomain domain the map is published to
     * @return true if referer ends with domains in UNRESTRICTED_USAGE_DOMAINS or the domain defined for the view.
     */
    public static boolean isRefererDomain(final String referer, final String pubdomain) {
        if(referer == null) {
            return false;
        }
        log.debug("Unrestricted domains:", UNRESTRICTED_USAGE_DOMAINS);
        for (String domain : UNRESTRICTED_USAGE_DOMAINS) {
            if(domain.equals("*") || referer.endsWith(domain)) {
                return true;
            }
        }
        return referer.endsWith(pubdomain);
    }

    public static JSONObject getConfiguration(final View view) throws ViewException {
        final JSONObject configuration = new JSONObject();
        final List<Bundle> bundles = view.getBundles();
        for (Bundle s : bundles) {
            final String conf = s.getConfig();
            final String state = s.getState();
            final String name = s.getBundleinstance();
            try {
                // setup bundle node in config
                JSONObject bundle = new JSONObject();
                configuration.put(name, bundle);
                
                // setup conf for bundle
                if (conf != null) {
                    bundle.put("conf", new JSONObject(conf));
                }
                else {
                    log.warn("Could not get configuration fragment for bundle '", name, "'");
                }
                // setup state for bundle
                if (state != null) {
                    bundle.put("state", new JSONObject(state));
                }
                else {
                    log.warn("Could not get state fragment for bundle '", name, "'");
                }
            } catch (Exception ex) {
                log.error("Malformed JSON in configuration fragment for bundle", name, conf);
            }
        }
        return configuration;
    }
}
