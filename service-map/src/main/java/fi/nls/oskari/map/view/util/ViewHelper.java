package fi.nls.oskari.map.view.util;

import java.util.List;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewException;

public class ViewHelper {

    private static final Logger log = LogFactory.getLogger(ViewHelper.class);
    
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
