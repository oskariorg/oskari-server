package fi.nls.oskari.map.view.util;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * @return true if referer doesn't exist, ends with domains in UNRESTRICTED_USAGE_DOMAINS or the domain defined for the view.
     */
    public static boolean isRefererDomain(final String referer, final String pubdomain) {
        if(referer == null || referer.isEmpty()) {
            return true;
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
                    log.info("Could not get configuration fragment for bundle '", name, "'");
                }
                // setup state for bundle
                if (state != null) {
                    bundle.put("state", new JSONObject(state));
                }
                else {
                    log.info("Could not get state fragment for bundle '", name, "'");
                }
            } catch (Exception ex) {
                log.error("Malformed JSON in configuration fragment for bundle", name, conf);
            }
        }
        return configuration;
    }

    public static JSONObject viewToJson(final BundleService bundleService,
            final View view) throws JSONException {
        final JSONObject viewJSON = new JSONObject();
        viewJSON.put("name", view.getName());
        viewJSON.put("type", view.getType());
        viewJSON.put("creator", view.getCreator());
        viewJSON.put("default", view.isDefault());
        viewJSON.put("public", view.isPublic());
        viewJSON.put("onlyUuid", view.isOnlyForUuId());
        viewJSON.put("metadata", view.getMetadata());
        viewJSON.put("application", view.getApplication());
        viewJSON.put("page", view.getPage());
        viewJSON.put("developmentPath", view.getDevelopmentPath());
        viewJSON.put("bundles", createBundles(bundleService, view.getBundles()));
        return viewJSON;
    }

    private static JSONArray createBundles(final BundleService bundleService,
            final List<Bundle> bundles) throws JSONException {
        JSONArray bundlesJSON = new JSONArray();
        if (bundleService != null && bundles != null) {
            for (Bundle bundle : bundles) {
                JSONObject bundleJSON = new JSONObject();
                bundleJSON.put("id", bundle.getName());
                if (bundle.getBundleinstance() != null) {
                    bundleJSON.put("instance", bundle.getBundleinstance());
                }
                if (bundle.getStartup() != null) {
                    bundleJSON.put("startup", new JSONObject(bundle.getStartup()));
                }
                if (bundle.getConfigJSON() != null) {
                    bundleJSON.put("config", bundle.getConfigJSON());
                }
                if (bundle.getStateJSON() != null) {
                    bundleJSON.put("state", bundle.getStateJSON());
                }
                bundlesJSON.put(bundleJSON);
            }
        }
        return bundlesJSON;
    }

    public static View viewFromJson(final BundleService bundleService,
            final JSONObject viewJSON) throws JSONException, IllegalArgumentException {
        final View view = new View();
        view.setName(viewJSON.getString("name"));
        view.setType(viewJSON.getString("type"));
        view.setCreator(viewJSON.optLong("creator", -1L));
        view.setIsDefault(viewJSON.optBoolean("default"));
        view.setIsPublic(viewJSON.optBoolean("public", false));
        view.setOnlyForUuId(viewJSON.optBoolean("onlyUuid", true));
        view.setMetadata(viewJSON.optJSONObject("metadata"));

        if (viewJSON.has("oskari")) {
            // Support "old" format
            final JSONObject oskari = viewJSON.getJSONObject("oskari");
            view.setApplication(oskari.getString("application"));
            view.setPage(oskari.getString("page"));
            view.setDevelopmentPath(oskari.getString("development_prefix"));
        } else {
            view.setApplication(viewJSON.getString("application"));
            view.setPage(viewJSON.getString("page"));
            view.setDevelopmentPath(viewJSON.getString("developmentPath"));
        }

        addBundles(bundleService, view, viewJSON.getJSONArray("bundles"));

        return view;
    }

    private static void addBundles(final BundleService bundleService,
            final View view, final JSONArray bundles)
                    throws JSONException, IllegalArgumentException {
        if (bundleService == null || bundles == null) {
            return;
        }
        for (int i = 0; i < bundles.length(); ++i) {
            final JSONObject bJSON = bundles.getJSONObject(i);
            final String name = bJSON.getString("id");
            final Bundle bundle = bundleService.getBundleTemplateByName(name);
            if (bundle == null) {
                throw new IllegalArgumentException("Bundle not registered - id: " + name);
            }
            if (bJSON.has("instance")) {
                bundle.setBundleinstance(bJSON.getString("instance"));
            }
            if (bJSON.has("startup")) {
                bundle.setStartup(bJSON.getJSONObject("startup").toString());
            }
            if (bJSON.has("config")) {
                bundle.setConfig(bJSON.getJSONObject("config").toString());
            }
            if (bJSON.has("state")) {
                bundle.setState(bJSON.getJSONObject("state").toString());
            }
            view.addBundle(bundle);
        }
    }

    public static List<View> getSystemViews(ViewService viewService)
            throws ServiceException {
        List<Long> viewIds = viewService.getSystemDefaultViewIds();
        List<View> views = new ArrayList<>(viewIds.size());
        for (long viewId : viewIds) {
            views.add(viewService.getViewWithConf(viewId));
        }
        return views;
    }

    public static Set<String> getSystemCRSs(ViewService viewService)
            throws ServiceException {
        List<View> views = getSystemViews(viewService);
        Set<String> crss = new HashSet<>();
        for (View view : views) {
            String srsName = view.getSrsName();
            if (srsName != null) {
                crss.add(srsName);
            }
        }
        return crss;
    }

}
